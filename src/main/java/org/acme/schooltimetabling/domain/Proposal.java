package org.acme.schooltimetabling.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proposal {

  private String finalNumber;

  // Must match shifts beamMode
  private BeamMode beamMode;

  private List<DatePreference> datePreferences;

  public Proposal(String finalNumber, BeamMode beamMode) {
    this.finalNumber = finalNumber;
    this.beamMode = beamMode;
  }
}
