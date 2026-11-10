package com.nimish.pingslayer.endpoint;

import com.nimish.pingslayer.component.CheckScheduler;
import com.nimish.pingslayer.model.MonitoredEndpoint;
import com.nimish.pingslayer.repository.CheckResultRepository;
import com.nimish.pingslayer.repository.EndpointRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class CheckSchedulerIT {
  @Autowired
  CheckScheduler scheduler;
  @Autowired
  EndpointRepository endpoints;
  @Autowired
  CheckResultRepository results;

  @Test
  void tick_checksDueEndpoint_once() throws Exception {
    try (MockWebServer server = new MockWebServer()) {
      server.enqueue(new MockResponse().setResponseCode(200));
      server.start();

      MonitoredEndpoint ep = new MonitoredEndpoint();
      ep.setName("it"); ep.setUrl(server.url("/").toString());
      ep.setIntervalSeconds(60);
      ep = endpoints.save(ep);

      scheduler.tick();
      scheduler.tick(); // not due yet

      assertEquals(1, results.findTop50ByEndpointIdOrderByCheckedAtDesc(ep.getId()).size());
      assertEquals(1, server.getRequestCount());
    }
  }
}