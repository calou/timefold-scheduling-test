package org.acme.schooltimetabling.domain;

import lombok.Data;
import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(of = {"id" })
@NoArgsConstructor
@PlanningEntity
public class Session {

    @PlanningId
    private String id;

    private String subject;
    private String teacher;
    private String studentGroup;

    @JsonIdentityReference
    @PlanningVariable
    private Shift shift;

    @JsonIdentityReference
    @PlanningVariable
    private Beamline beamline;

    public Session(String id, String subject, String teacher, String studentGroup) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.studentGroup = studentGroup;
    }

}
