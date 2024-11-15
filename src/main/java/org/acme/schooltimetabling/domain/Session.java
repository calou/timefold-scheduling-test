package org.acme.schooltimetabling.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString(of = {"id" })
@NoArgsConstructor
@PlanningEntity
public class Session {

    @PlanningId
    private String id;

    private Proposal proposal;

    @JsonProperty("beamtimeSlot_id")
    @JsonIdentityReference
    @PlanningVariable
    private BeamtimeSlot beamtimeSlot;

    @JsonProperty("beamline_id")
    @JsonIdentityReference
    private Beamline beamline;

    public Session(Proposal proposal, Beamline beamline) {
        this.id = UUID.randomUUID().toString();
        this.proposal = proposal;
        this.beamline = beamline;
    }

}
