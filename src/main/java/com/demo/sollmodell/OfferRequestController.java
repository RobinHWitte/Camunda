package com.demo.sollmodell;


import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class OfferRequestController {

  private final RuntimeService runtimeService;

  public OfferRequestController(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }

  @PostMapping("/start")
  public ResponseEntity<?> start(@RequestBody Map<String, Object> body) {
    String requestId = (String) body.getOrDefault("requestId", "REQ-" + System.currentTimeMillis());
    Object eventDate = body.get("eventDate");

    ProcessInstance pi = runtimeService.startProcessInstanceByKey(
        "PROC_SPONSORING",
        requestId,
        Map.of(
            "requestId", requestId,
            "eventDate", eventDate
        )
    );

    return ResponseEntity.ok(Map.of(
        "processInstanceId", pi.getId(),
        "businessKey", pi.getBusinessKey(),
        "requestId", requestId
    ));
  }

  @PostMapping("/request-offer/{requestId}")
  public ResponseEntity<?> requestOffer(@PathVariable String requestId) {
    runtimeService
        .createMessageCorrelation("MSG_ANGEBOTSANFORDERUNG")
        .processInstanceBusinessKey(requestId)
        .setVariable("offerRequested", true)
        .correlate();

    return ResponseEntity.ok(Map.of(
        "status", "correlated",
        "requestId", requestId
    ));
  }
}

