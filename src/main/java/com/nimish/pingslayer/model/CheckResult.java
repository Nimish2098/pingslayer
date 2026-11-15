package com.nimish.pingslayer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "check_result")
public class CheckResult {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long endpointId;
  private Integer statusCode;
  private Long responseTimeMs;
  private boolean success;
  private String error;
  private Instant checkedAt;   // maps to timestamptz
  // getters/setters
}