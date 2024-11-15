package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class LocalContactCandidate {

  @PlanningId
  private String id;

  private Beamline beamline;

  private StaffMember staffMember;

  public LocalContactCandidate(Beamline beamline, StaffMember staffMember) {
    this.id = UUID.randomUUID().toString();
    this.beamline = beamline;
    this.staffMember = staffMember;
  }
}
