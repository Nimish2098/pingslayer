package com.nimish.pingslayer.endpoint;

import com.nimish.pingslayer.model.EndpointStatus;
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
                .assertNext(h -> {
                  assertEquals(EndpointStatus.UP, h.getStatus());
                  assertTrue(h.getLatencyMs() >= 400, "latency was " + h.getLatencyMs());
                  assertTrue(h.getLatencyMs() < 2000, "latency was " + h.getLatencyMs()); // generous, avoids CI flakiness
                }).verifyComplete();
  }

  @Test void fastResponse_hasLowLatency() {
    server.enqueue(new MockResponse().setResponseCode(200));
    StepVerifier.create(service.check(ep(5)))
                .assertNext(h -> assertTrue(h.getLatencyMs() < 1000)).verifyComplete();
  }

  @Test void status500_isDown() {
    server.enqueue(new MockResponse().setResponseCode(500));
    StepVerifier.create(service.check(ep(5)))
                .assertNext(h -> { assertEquals(EndpointStatus.DOWN, h.getStatus()); assertEquals(500, h.getStatusCode()); })
                .verifyComplete();
  }

  @Test void status404_isDown() {
    server.enqueue(new MockResponse().setResponseCode(404));
    StepVerifier.create(service.check(ep(5)))
                .assertNext(h -> assertEquals(EndpointStatus.DOWN, h.getStatus())).verifyComplete();
  }

  @Test void timeout_isDown_withNullStatusCode() {
    server.enqueue(new MockResponse().setResponseCode(200).setHeadersDelay(3, TimeUnit.SECONDS));
    StepVerifier.create(service.check(ep(1)))
                .assertNext(h -> {
                  assertEquals(EndpointStatus.DOWN, h.getStatus());
                  assertNull(h.getStatusCode());
                  assertNotNull(h.getErrorMessage());
                  assertTrue(h.getLatencyMs() >= 1000);   // measured up to the timeout
                }).verifyComplete();
  }

  @Test void connectionRefused_isDown() throws IOException {
    MonitoredEndpoint e = ep(2);
    server.shutdown();
    StepVerifier.create(service.check(e))
                .assertNext(h -> { assertEquals(EndpointStatus.DOWN, h.getStatus()); assertNotNull(h.getErrorMessage()); })
                .verifyComplete();
  }
}
