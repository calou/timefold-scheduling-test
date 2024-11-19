package org.acme.schooltimetabling.domain;

import java.util.UUID;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(of = { "id" })
@NoArgsConstructor
@PlanningEntity
public class Session {

  @PlanningId
  private String id;

  private Proposal proposal;

  @PlanningVariable
  private BeamtimeSlot beamtimeSlot;

  @PlanningVariable
  private LocalContact localContact;

  private Beamline beamline;

  public Session(Proposal proposal, Beamline beamline) {
    this.id = UUID.randomUUID().toString();
    this.proposal = proposal;
    this.beamline = beamline;
  }

}
