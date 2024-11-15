package org.acme.schooltimetabling.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DatePreference {

  private LocalDate start;

  private LocalDate endIncluded;

  private boolean acceptable;


  public boolean contains(LocalDate date){
    return (date.isEqual(start) ||  date.isAfter(start))
        && ( date.isEqual(endIncluded) || date.isBefore(endIncluded));
  }
}
