package org.acme.schooltimetabling.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(of = { "id" })
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(scope = Beamline.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Beamline {

    @PlanningId
    private String id;

    private String name;

}
