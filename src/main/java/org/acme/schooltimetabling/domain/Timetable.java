package org.acme.schooltimetabling.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@PlanningSolution
public class Timetable {

    @Getter
    private String name;

    @Getter
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Shift> shifts;

    @Getter
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Room> rooms;

    @Getter
    @PlanningEntityCollectionProperty
    private List<Lesson> lessons;

    @Getter
    @PlanningScore
    private HardSoftScore score;


    @Getter
    @Setter
    // Ignored by Timefold, used by the UI to display solve or stop solving button
    private SolverStatus solverStatus;

    public Timetable(String name, HardSoftScore score, SolverStatus solverStatus) {
        this.name = name;
        this.score = score;
        this.solverStatus = solverStatus;
    }

    public Timetable(String name, List<Shift> shifts, List<Room> rooms, List<Lesson> lessons) {
        this.name = name;
        this.shifts = shifts;
        this.rooms = rooms;
        this.lessons = lessons;
    }

}
