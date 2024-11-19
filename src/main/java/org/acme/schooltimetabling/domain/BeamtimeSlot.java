package org.acme.schooltimetabling.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = {"id"})
@ToString(of = { "date", "hour" })
@NoArgsConstructor
public class BeamtimeSlot {

  @PlanningId
  private String id;

  private LocalDate date;

  private int hour;

  private BeamMode beamMode;

  @JsonIgnore
  private long index;

  public BeamtimeSlot(LocalDate date, int hour, BeamMode beamMode) {
    this.id = "%1$s/%2$s".formatted(date.format(DateTimeFormatter.ISO_DATE), hour);
    this.date = date;
    this.hour = hour;
    this.beamMode = beamMode;

    index = this.date.getLong(ChronoField.EPOCH_DAY) * 24 + hour;
  }

  public LocalDateTime getStartsAt() {
    return LocalDateTime.of(date, LocalTime.of(hour, 0));
  }

  public LocalDateTime getEndsAt() {
    return LocalDateTime.of(date, LocalTime.of(hour, 59));
  }
}
