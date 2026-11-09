package com.nimish.pingslayer.repository;

import com.nimish.pingslayer.model.EndpointStatus;
import com.nimish.pingslayer.model.HealthLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthLogRepository extends JpaRepository<HealthLog, Long>
{
  List<HealthLog> findTop50ByEndpointIdOrderByCheckedAtDesc(Long endpointId);
  long countByEndpointIdAndStatus(Long endpointId, EndpointStatus status); // for uptime %
}