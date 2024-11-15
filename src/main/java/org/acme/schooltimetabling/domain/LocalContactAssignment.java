package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString(of = { "id" })
@NoArgsConstructor
@PlanningEntity
public class LocalContactAssignment {

  @PlanningId
  private String id;

  @JsonProperty("beamtimeSlot_id")
  @JsonIdentityReference
  private BeamtimeSlot beamtimeSlot;

  @JsonProperty("beamline_id")
  @JsonIdentityReference
  private Beamline beamline;

  @JsonProperty("localContact")
  @JsonIdentityReference
  @PlanningVariable
  private LocalContactCandidate localContactCandidate;

  public LocalContactAssignment(BeamtimeSlot beamtimeSlot, Beamline beamline) {
    this.id = UUID.randomUUID().toString();
    this.beamtimeSlot = beamtimeSlot;
    this.beamline = beamline;
  }
}
