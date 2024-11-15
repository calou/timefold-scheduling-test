package org.acme.schooltimetabling.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = {"id"})
public class BeamMode {

  private String id;

  private String name;

  public BeamMode(String name) {
    this.id = UUID.randomUUID().toString();
    this.name = name;
  }
}
