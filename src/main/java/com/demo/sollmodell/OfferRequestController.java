package com.demo.sollmodell;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class OfferRequestController {

  private static final String MESSAGE_NAME = "MSG_ANGEBOTSANFORDERUNG";

  private final RuntimeService runtimeService;

  public OfferRequestController(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }

  @PostMapping("/angebot")
  public ResponseEntity<Map<String, Object>> correlateAngebot(@RequestBody(required = false) Map<String, Object> body) {
    Map<String, Object> requestBody = body == null ? Collections.emptyMap() : body;

    String businessKey = asString(requestBody.get("businessKey"));
    Map<String, Object> variables = extractVariables(requestBody.get("variables"));

    if (businessKey != null && !businessKey.isBlank()) {
      runtimeService.createMessageCorrelation(MESSAGE_NAME)
          .processInstanceBusinessKey(businessKey)
          .setVariables(variables)
          .correlate();

      return ResponseEntity.ok(Map.of(
          "status", "correlated",
          "messageName", MESSAGE_NAME,
          "businessKey", businessKey
      ));
    }

    Execution waitingExecution = runtimeService.createExecutionQuery()
        .messageEventSubscriptionName(MESSAGE_NAME)
        .active()
        .orderByProcessInstanceId()
        .desc()
        .listPage(0, 1)
        .stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Keine wartende Prozessinstanz für Nachricht " + MESSAGE_NAME + " gefunden."));

    runtimeService.createMessageCorrelation(MESSAGE_NAME)
        .processInstanceId(waitingExecution.getProcessInstanceId())
        .setVariables(variables)
        .correlate();

    return ResponseEntity.ok(Map.of(
        "status", "correlated",
        "messageName", MESSAGE_NAME,
        "processInstanceId", waitingExecution.getProcessInstanceId()
    ));
  }

  private static String asString(Object value) {
    return value == null ? null : String.valueOf(value);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> extractVariables(Object value) {
    if (value instanceof Map<?, ?>) {
      return (Map<String, Object>) value;
    }
    return Collections.emptyMap();
  }
}
