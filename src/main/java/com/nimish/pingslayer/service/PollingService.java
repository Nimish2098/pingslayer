package com.nimish.pingslayer.service;

import com.nimish.pingslayer.model.EndpointStatus;
import com.nimish.pingslayer.model.HealthLog;
import com.nimish.pingslayer.model.MonitoredEndpoint;
import com.nimish.pingslayer.repository.EndpointRepository;
import com.nimish.pingslayer.repository.HealthLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollingService {
  private static final int MAX_CONCURRENCY = 200;

  private final EndpointRepository endpoints;
  private final HealthLogRepository healthLogs;
  private final HealthCheckService checker;

  private final Scheduler virtualScheduler =
      Schedulers.fromExecutorService(Executors.newVirtualThreadPerTaskExecutor());

  /** Polls every active endpoint whose interval has elapsed. Returns how many were polled. */
  public int pollDueEndpoints() {
    Instant now = Instant.now();
    List<MonitoredEndpoint> due = endpoints.findByActiveTrue().stream()
                                           .filter(e -> e.getLastCheckedAt() == null
                                                        || !e.getLastCheckedAt().plusSeconds(e.getIntervalSeconds()).isAfter(now))
                                           .toList();

    Flux.fromIterable(due)
        .flatMap(this::pollOne, MAX_CONCURRENCY)
        .blockLast();
    return due.size();
  }

  Mono<HealthLog> pollOne(MonitoredEndpoint ep) {
    return checker.check(ep)
                  .publishOn(virtualScheduler)             // persistence on a virtual thread
                  .doOnNext(h -> {
                    healthLogs.save(h);
                    endpoints.updateStatus(ep.getId(), h.getStatus(), h.getCheckedAt());
                    if (h.getStatus() == EndpointStatus.DOWN) {
                      log.warn("DOWN {} ({}): {}", ep.getName(), h.getStatusCode(), h.getErrorMessage());
                    }
                  })
                  .onErrorResume(e -> {                    // a DB hiccup must not kill the whole batch
                    log.error("Failed to persist check for {}", ep.getId(), e);
                    return Mono.empty();
                  });
  }
}
