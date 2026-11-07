package com.nimish.pingslayer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class CheckResult {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Long endpointId;
  private Integer statusCode;      // null if no response (timeout/DNS/refused)
  private long responseTimeMs;
  private boolean success;
  private String error;
  private Instant checkedAt;
}