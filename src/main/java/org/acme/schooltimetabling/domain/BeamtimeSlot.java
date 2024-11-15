package org.acme.schooltimetabling.domain;

import java.time.LocalDate;
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
@JsonIdentityInfo(scope = BeamtimeSlot.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class BeamtimeSlot {

  @PlanningId
  private String id;

  private LocalDate date;

  private int dailyIndex;

  private BeamMode beamMode;

  @JsonIgnore
  private long index;

  public BeamtimeSlot(LocalDate date, int dailyIndex, BeamMode beamMode) {
    this.id = "%1$s/%2$s".formatted(date.format(DateTimeFormatter.ISO_DATE), dailyIndex);
    this.date = date;
    this.dailyIndex = dailyIndex;
    this.beamMode = beamMode;

    index = this.date.getLong(ChronoField.EPOCH_DAY) * 24 + dailyIndex;
  }

}
