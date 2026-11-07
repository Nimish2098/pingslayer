package com.nimish.pingslayer.config;

import com.nimish.pingslayer.model.MonitoredEndpoint;
import com.nimish.pingslayer.repository.CheckResultRepository;
import com.nimish.pingslayer.repository.EndpointRepository;
import com.nimish.pingslayer.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CheckScheduler {
  private final EndpointRepository endpoints;
  private final CheckResultRepository results;
  private final HealthCheckService checker;

  @Scheduled(fixedDelay = 5000) // fixedDelay => runs never overlap
  public void tick() {
    Instant now = Instant.now();
    List<MonitoredEndpoint> due = endpoints.findByActiveTrue().stream()
                                           .filter(e -> e.getLastCheckedAt() == null
                                                        || e.getLastCheckedAt().plusSeconds(e.getIntervalSeconds()).isBefore(now))
                                           .toList();

    Flux.fromIterable(due)
        .flatMap(ep -> checker.check(ep)
                              .publishOn(Schedulers.boundedElastic()) // JPA is blocking, keep it off Netty threads
                              .doOnNext(r -> {
                                results.save(r);
                                ep.setLastCheckedAt(now);
                                endpoints.save(ep);
                              }), 20) // max 20 concurrent checks
        .blockLast();
  }
}