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

  public HealthCheckService(WebClient.Builder builder) { this.webClient = builder.build(); }

  public Mono<CheckResult> check(MonitoredEndpoint ep) {
    return Mono.defer(() -> {                       // start the clock at subscription
      long start = System.nanoTime();
      return webClient.method(HttpMethod.valueOf(ep.getMethod()))
                      .uri(URI.create(ep.getUrl()))
                      .exchangeToMono(resp -> resp.releaseBody().thenReturn(resp.statusCode().value()))
                      .timeout(Duration.ofSeconds(ep.getTimeoutSeconds()))
                      .map(code -> buildResult(ep, code, start, null))
                      .onErrorResume(e -> Mono.just(buildResult(ep, null, start,
                                                                e.getClass().getSimpleName() + ": " + e.getMessage())));
    });
  }

  private CheckResult buildResult(MonitoredEndpoint ep, Integer code, long startNanos, String error) {
    CheckResult result = new CheckResult();
    result.setEndpointId(ep.getId());
    result.setStatusCode(code);
    result.setResponseTimeMs((System.nanoTime() - startNanos) / 1_000_000);
    result.setSuccess(code != null && code >= 200 && code < 400);
    result.setError(error);
    result.setCheckedAt(Instant.now());
    return result;
  }
}