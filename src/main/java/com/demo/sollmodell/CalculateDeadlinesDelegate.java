package com.demo.sollmodell;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class CalculateDeadlinesDelegate implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {
    Object eventDateObj = execution.getVariable("eventDate");
    if (eventDateObj == null) {
      throw new IllegalArgumentException("Variable 'eventDate' ist nicht gesetzt.");
    }

    LocalDate eventDate = toLocalDate(eventDateObj);
    LocalDate sixMonthsBefore = eventDate.minusMonths(6);

    execution.setVariable("sixMonthsBefore", toDate(sixMonthsBefore));
  }

  private static LocalDate toLocalDate(Object obj) {
    if (obj instanceof Date) {
      Date d = (Date) obj;
      return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
    if (obj instanceof String) {
      String s = (String) obj;
      return LocalDate.parse(s); // erwartet ISO: yyyy-MM-dd
    }
    throw new IllegalArgumentException("Unsupported type for eventDate: " + obj.getClass());
  }

  private static Date toDate(LocalDate localDate) {
    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }
}
