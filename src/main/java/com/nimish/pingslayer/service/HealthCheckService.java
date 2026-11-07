package com.nimish.pingslayer.service;

import com.nimish.pingslayer.model.CheckResult;
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

  public HealthCheckService(WebClient.Builder builder) {
    this.webClient = builder.build();
  }

  public Mono<CheckResult> check(MonitoredEndpoint ep) {
    long start = System.nanoTime();
    return webClient.method(HttpMethod.valueOf(ep.getMethod()))
                    .uri(URI.create(ep.getUrl()))
                    .exchangeToMono(resp -> resp.releaseBody().thenReturn(resp.statusCode().value()))
                    .timeout(Duration.ofSeconds(ep.getTimeoutSeconds()))
                    .map(status -> result(ep, status, start, null))
                    .onErrorResume(e -> Mono.just(
                        result(ep, null, start, e.getClass().getSimpleName() + ": " + e.getMessage())));
  }

  private CheckResult result(MonitoredEndpoint ep, Integer status, long startNanos, String error) {
    CheckResult r = new CheckResult();
    r.setEndpointId(ep.getId());
    r.setStatusCode(status);
    r.setResponseTimeMs((System.nanoTime() - startNanos) / 1_000_000);
    r.setSuccess(status != null && status >= 200 && status < 400);
    r.setError(error);
    r.setCheckedAt(Instant.now());
    return r;
  }
}