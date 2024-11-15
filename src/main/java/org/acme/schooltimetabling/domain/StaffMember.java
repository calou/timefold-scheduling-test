package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
public class StaffMember {

  @PlanningId
  private String id;

  private String name;

  private List<DatePreference> datePreferences;

  public StaffMember(String name) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
  }

}
