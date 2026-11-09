package com.nimish.pingslayer.service;

import com.nimish.pingslayer.model.EndpointStatus;
import com.nimish.pingslayer.model.HealthLog;
import com.nimish.pingslayer.model.MonitoredEndpoint;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;

@Service
public class HealthCheckService {
  private final WebClient webClient;

  public HealthCheckService(WebClient.Builder builder) { this.webClient = builder.build(); }

  public Mono<HealthLog> check(MonitoredEndpoint ep) {
    return Mono.defer(() -> {                       // start the clock at subscription
      long start = System.nanoTime();
      return webClient.method(HttpMethod.valueOf(ep.getMethod()))
                      .uri(URI.create(ep.getUrl()))
                      .exchangeToMono(resp -> resp.releaseBody().thenReturn(resp.statusCode().value()))
                      .timeout(Duration.ofSeconds(ep.getTimeoutSeconds()))
                      .map(code -> log(ep, code, start, null))
                      .onErrorResume(e -> Mono.just(log(ep, null, start,
                                                        e.getClass().getSimpleName() + ": " + e.getMessage())));
    });
  }

  private HealthLog log(MonitoredEndpoint ep, Integer code, long startNanos, String error) {
    HealthLog h = new HealthLog();
    h.setEndpointId(ep.getId());
    h.setStatusCode(code);
    h.setLatencyMs((System.nanoTime() - startNanos) / 1_000_000);
    h.setStatus(code != null && code >= 200 && code < 400 ? EndpointStatus.UP : EndpointStatus.DOWN);
    h.setErrorMessage(error);
    h.setCheckedAt(Instant.now());
    return h;
  }
}