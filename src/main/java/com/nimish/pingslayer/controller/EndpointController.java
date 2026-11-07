package com.nimish.pingslayer.controller;

import com.nimish.pingslayer.model.CheckResult;
import com.nimish.pingslayer.model.MonitoredEndpoint;
import com.nimish.pingslayer.repository.CheckResultRepository;
import com.nimish.pingslayer.repository.EndpointRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/endpoints")
@RequiredArgsConstructor
public class EndpointController {
  private final EndpointRepository endpoints;
  private final CheckResultRepository results;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MonitoredEndpoint create(@Valid @RequestBody MonitoredEndpoint ep) {
    ep.setId(null);
    return endpoints.save(ep);
  }

  @GetMapping
  public List<MonitoredEndpoint> list() { return endpoints.findAll(); }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) { endpoints.deleteById(id); }

  @GetMapping("/{id}/results")
  public List<CheckResult> history(@PathVariable Long id) {
    return results.findTop50ByEndpointIdOrderByCheckedAtDesc(id);
  }
}