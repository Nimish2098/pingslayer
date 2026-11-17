package com.nimish.pingslayer.endpoint;

import com.nimish.pingslayer.model.CheckResult;
import com.nimish.pingslayer.model.MonitoredEndpoint;
import com.nimish.pingslayer.service.HealthCheckService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
class HealthCheckServiceTest {
  MockWebServer server;
  HealthCheckService service;

  @BeforeEach void setup() throws IOException {
    server = new MockWebServer(); server.start();
    service = new HealthCheckService(WebClient.builder());
  }
  @AfterEach void teardown() throws IOException { server.shutdown(); }

  private MonitoredEndpoint ep(int timeoutSec) {
    MonitoredEndpoint e = new MonitoredEndpoint();
    e.setId(1L); e.setMethod("GET"); e.setTimeoutSeconds(timeoutSec);
    e.setUrl(server.url("/health").toString());
    return e;
  }

  @Test void latency_reflectsServerDelay() {
    server.enqueue(new MockResponse().setResponseCode(200).setHeadersDelay(400, TimeUnit.MILLISECONDS));
    StepVerifier.create(service.check(ep(5)))
                .assertNext(result -> {
                  assertTrue(result.isSuccess());
                  assertTrue(result.getResponseTimeMs() >= 400, "latency was " + result.getResponseTimeMs());
                  assertTrue(result.getResponseTimeMs() < 2000, "latency was " + result.getResponseTimeMs());
                }).verifyComplete();
  }

  @Test void fastResponse_hasLowLatency() {
    server.enqueue(new MockResponse().setResponseCode(200));
    StepVerifier.create(service.check(ep(5)))
                .assertNext(result -> assertTrue(result.getResponseTimeMs() < 1000)).verifyComplete();
  }

  @Test void status500_isDown() {
    server.enqueue(new MockResponse().setResponseCode(500));
    StepVerifier.create(service.check(ep(5)))
                .assertNext(result -> {
                  assertFalse(result.isSuccess());
                  assertEquals(500, result.getStatusCode());
                })
                .verifyComplete();
  }

  @Test void status404_isDown() {
    server.enqueue(new MockResponse().setResponseCode(404));
    StepVerifier.create(service.check(ep(5)))
                .assertNext(result -> {
                  assertFalse(result.isSuccess());
                  assertEquals(404, result.getStatusCode());
                }).verifyComplete();
  }

  @Test void status302_isUp() {
    server.enqueue(new MockResponse().setResponseCode(302));
    StepVerifier.create(service.check(ep(5)))
                .assertNext(CheckResult::isSuccess)
                .verifyComplete();
  }

  @Test void timeout_isDown_withNullStatusCode() {
    server.enqueue(new MockResponse().setResponseCode(200).setHeadersDelay(3, TimeUnit.SECONDS));
    StepVerifier.create(service.check(ep(1)))
                .assertNext(result -> {
                  assertFalse(result.isSuccess());
                  assertNull(result.getStatusCode());
                  assertNotNull(result.getError());
                  assertTrue(result.getResponseTimeMs() >= 1000);
                }).verifyComplete();
  }

  @Test void connectionRefused_isDown() throws IOException {
    MonitoredEndpoint e = ep(2);
    server.shutdown();
    StepVerifier.create(service.check(e))
                .assertNext(result -> {
                  assertFalse(result.isSuccess());
                  assertNull(result.getStatusCode());
                  assertNotNull(result.getError());
                })
                .verifyComplete();
  }
}
