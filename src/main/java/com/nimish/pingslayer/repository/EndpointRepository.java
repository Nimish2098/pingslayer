package com.nimish.pingslayer.repository;

import com.nimish.pingslayer.model.MonitoredEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EndpointRepository extends JpaRepository<MonitoredEndpoint, Long>
{
  List<MonitoredEndpoint> findByActiveTrue();
}
