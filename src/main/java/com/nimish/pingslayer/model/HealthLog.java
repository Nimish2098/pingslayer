package com.nimish.pingslayer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(indexes = @Index(name = "idx_log_endpoint_time", columnList = "endpointId, checkedAt"))
public class HealthLog {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long endpointId;
  @Enumerated(EnumType.STRING)
  private EndpointStatus status;      // UP or DOWN for this check
  private Integer statusCode;         // null if no response
  private long latencyMs;
  private String errorMessage;
  private Instant checkedAt;
}