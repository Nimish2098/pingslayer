package com.nimish.pingslayer.component;

import com.nimish.pingslayer.service.PollingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PollingScheduler {
  private final PollingService pollingService;

  @Scheduled(fixedDelay = 5000)   // 5s resolution; each endpoint's own interval gates it
  public void tick() { pollingService.pollDueEndpoints(); }
}