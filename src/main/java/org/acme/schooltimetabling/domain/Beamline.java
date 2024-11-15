package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@ToString(of = { "id" })
@NoArgsConstructor
@JsonIdentityInfo(scope = Beamline.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Beamline {

    @PlanningId
    private String id;

    private String name;

    public Beamline(String name) {
        this.name = name;
        this.id = name.toLowerCase();
    }
}
