package com.nimish.pingslayer.service;

import com.nimish.pingslayer.model.CheckResult;
import com.nimish.pingslayer.model.EndpointStatus;
import com.nimish.pingslayer.model.MonitoredEndpoint;
import com.nimish.pingslayer.repository.CheckResultRepository;
import com.nimish.pingslayer.repository.EndpointRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PollingServiceTest {
  @Mock
  EndpointRepository endpoints;
  @Mock
  CheckResultRepository checkResults;
  private final Map<Long, CheckResult> checkResponses = new HashMap<>();
  private PollingService pollingService;

  @BeforeEach
  void setUp() {
    HealthCheckService checker = new HealthCheckService(WebClient.builder()) {
      @Override
      public Mono<CheckResult> check(MonitoredEndpoint endpoint) {
        return Mono.just(checkResponses.get(endpoint.getId()));
      }
    };
    pollingService = new PollingService(endpoints, checkResults, checker);
  }

  @Test
  void pollDueEndpoints_checksOnlyDueEndpointsAndPersistsUpStatus() {
    MonitoredEndpoint due = endpoint(1L);
    MonitoredEndpoint notDue = endpoint(2L);
    notDue.setLastCheckedAt(Instant.now().plusSeconds(120));
    CheckResult result = result(1L, true);

    checkResponses.put(1L, result);
    when(endpoints.findByActiveTrue()).thenReturn(List.of(due, notDue));

    assertEquals(1, pollingService.pollDueEndpoints());

    verify(checkResults).save(result);
    verify(endpoints).updateStatus(1L, EndpointStatus.UP, result.getCheckedAt());
  }

  @Test
  void pollDueEndpoints_persistsDownStatusForUnsuccessfulCheck() {
    MonitoredEndpoint due = endpoint(1L);
    CheckResult result = result(1L, false);

    checkResponses.put(1L, result);
    when(endpoints.findByActiveTrue()).thenReturn(List.of(due));

    assertEquals(1, pollingService.pollDueEndpoints());

    verify(checkResults).save(result);
    verify(endpoints).updateStatus(1L, EndpointStatus.DOWN, result.getCheckedAt());
  }

  @Test
  void pollDueEndpoints_continuesAfterOneResultFailsToPersist() {
    MonitoredEndpoint first = endpoint(1L);
    MonitoredEndpoint second = endpoint(2L);
    CheckResult firstResult = result(1L, true);
    CheckResult secondResult = result(2L, true);

    checkResponses.put(1L, firstResult);
    checkResponses.put(2L, secondResult);
    when(endpoints.findByActiveTrue()).thenReturn(List.of(first, second));
    doAnswer(invocation -> {
      CheckResult result = invocation.getArgument(0);
      if (result.getEndpointId().equals(1L)) {
        throw new IllegalStateException("database unavailable");
      }
      return result;
    }).when(checkResults).save(any(CheckResult.class));

    assertEquals(2, pollingService.pollDueEndpoints());

    verify(endpoints, never()).updateStatus(1L, EndpointStatus.UP, firstResult.getCheckedAt());
    verify(endpoints).updateStatus(2L, EndpointStatus.UP, secondResult.getCheckedAt());
  }

  private MonitoredEndpoint endpoint(Long id) {
    MonitoredEndpoint endpoint = new MonitoredEndpoint();
    endpoint.setId(id);
    endpoint.setIntervalSeconds(60);
    return endpoint;
  }

  private CheckResult result(Long endpointId, boolean success) {
    CheckResult result = new CheckResult();
    result.setEndpointId(endpointId);
    result.setSuccess(success);
    result.setCheckedAt(Instant.now());
    return result;
  }
}
