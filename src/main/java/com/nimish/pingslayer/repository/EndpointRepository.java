package com.nimish.pingslayer.repository;

import com.nimish.pingslayer.model.EndpointStatus;
import com.nimish.pingslayer.model.MonitoredEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

public interface EndpointRepository extends JpaRepository<MonitoredEndpoint, Long>
{
  List<MonitoredEndpoint> findByActiveTrue();
  @Modifying
  @Transactional
  @Query("update MonitoredEndpoint e set e.status = :status, e.lastCheckedAt = :at where e.id = :id")
  void updateStatus(@Param("id") Long id, @Param("status") EndpointStatus status, @Param("at") Instant at);
}
