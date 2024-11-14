package org.acme.schooltimetabling.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(of = { "date", "startTime" })
@NoArgsConstructor
@JsonIdentityInfo(scope = Shift.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Shift {

    @PlanningId
    private String id;

    private LocalDate date;

    private int shiftNum;

    private LocalTime startTime;

    private LocalTime endTime;

    public Shift(LocalDate date, int shiftNum) {
        this.id = "%1$s/%2$s".formatted(date.format(DateTimeFormatter.ISO_DATE), shiftNum);
        this.date = date;
        this.shiftNum = shiftNum;

        this.startTime = switch (shiftNum){
            case 0 -> LocalTime.MIDNIGHT;
            case 1 -> LocalTime.of(8, 0);
            default -> LocalTime.of(16, 0);
        };
        this.endTime = switch (shiftNum){
            case 0 -> LocalTime.of(7, 59, 59);
            case 1 -> LocalTime.of(15, 59, 59);
            default -> LocalTime.of(23, 59, 59);
        };
    }

}
