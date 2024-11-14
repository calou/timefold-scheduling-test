package org.acme.schooltimetabling.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(of = { "dayOfWeek", "startTime" })
@NoArgsConstructor
@JsonIdentityInfo(scope = Shift.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Shift {

    @PlanningId
    private String id;

    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public Shift(String id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

}
