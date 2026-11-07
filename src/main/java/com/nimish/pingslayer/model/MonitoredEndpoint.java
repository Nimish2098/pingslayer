package com.nimish.pingslayer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
public class MonitoredEndpoint {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @NotBlank private String name;
  @NotBlank private String url;
  @NotBlank
  private String method = "GET";
  @Min(5) private int intervalSeconds = 60;
  @Min(1) @Max(60) private int timeoutSeconds = 5;
  private boolean active = true;
  private Instant lastCheckedAt;
}