package org.acme.schooltimetabling.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

  private BeamMode beamMode;

  @JsonIgnore
  private long shiftIndex;

  public Shift(LocalDate date, int shiftNum, BeamMode beamMode) {
    this.id = "%1$s/%2$s".formatted(date.format(DateTimeFormatter.ISO_DATE), shiftNum);
    this.date = date;
    this.shiftNum = shiftNum;
    this.beamMode = beamMode;

    shiftIndex = this.date.getLong(ChronoField.EPOCH_DAY) * 24 + shiftNum;
  }

}
