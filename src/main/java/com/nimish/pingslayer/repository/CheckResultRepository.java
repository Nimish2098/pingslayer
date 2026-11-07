package com.nimish.pingslayer.repository;

import com.nimish.pingslayer.model.CheckResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long>
{
  List<CheckResult> findTop50ByEndpointIdOrderByCheckedAtDesc(Long endpointId);
}