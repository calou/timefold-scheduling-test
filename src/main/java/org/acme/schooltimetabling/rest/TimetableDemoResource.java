package org.acme.schooltimetabling.rest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.schooltimetabling.domain.BeamMode;
import org.acme.schooltimetabling.domain.DatePreference;
import org.acme.schooltimetabling.domain.LocalContact;
import org.acme.schooltimetabling.domain.Proposal;
import org.acme.schooltimetabling.domain.Session;
import org.acme.schooltimetabling.domain.Beamline;
import org.acme.schooltimetabling.domain.BeamtimeSlot;
import org.acme.schooltimetabling.domain.StaffMember;
import org.acme.schooltimetabling.domain.Timetable;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Tag(name = "Demo data", description = "Timefold-provided demo school timetable data.")
@Path("demo-data")
public class TimetableDemoResource {

  public enum DemoData {
    SMALL,
    LARGE
  }

  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "List of demo data represented as IDs.",
                   content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                      schema = @Schema(implementation = DemoData.class, type = SchemaType.ARRAY))) })
  @Operation(summary = "List demo data.")
  @GET
  public DemoData[] list() {
    return DemoData.values();
  }

  @APIResponses(value = {
      @APIResponse(responseCode = "200", description = "Unsolved demo timetable.",
                   content = @Content(mediaType = MediaType.APPLICATION_JSON,
                                      schema = @Schema(implementation = Timetable.class))) })
  @Operation(summary = "Find an unsolved demo timetable by ID.")
  @GET
  @Path("/{demoDataId}")
  public Response generate(@Parameter(description = "Unique identifier of the demo data.",
                                      required = true) @PathParam("demoDataId") DemoData demoData) {
    var localContactCandidates = new ArrayList<LocalContact>();

    var paul = new StaffMember("Paul");
    var peter = new StaffMember("Peter");
    var prya = new StaffMember("Peter");
    var georges = new StaffMember("Georges");
    var gwendolin = new StaffMember("Gwendolin");
    var guenievre = new StaffMember("Guenievre");
    var camille = new StaffMember("Camille");
    var charles = new StaffMember("Charles");




    var beamtimeSlots = new ArrayList<BeamtimeSlot>(300);
    var singleBunchBeamMode = new BeamMode("Single Bunch");
    var sevenEighthsBeamMode = new BeamMode("7/8 + 1 Filling 200mA");

    var runStart = LocalDate.of(2024, 1, 1);

    for (long day = 0; day < 10L; day++) {
      var date = runStart.plusDays(day);

      var beamMode = switch ((int) day) {
        case 0, 2, 4, 6, 8, 10 -> singleBunchBeamMode;
        default -> sevenEighthsBeamMode;
      };
      for (int hour = 0; hour < 24; hour++) {
        beamtimeSlots.add(new BeamtimeSlot(date, hour, beamMode));
      }
    }

    List<Beamline> beamlines = new ArrayList<>(3);

    beamlines.add(new Beamline("ID21"));
    beamlines.add(new Beamline("ID22"));
    beamlines.add(new Beamline("CM01"));

    localContactCandidates.add(new LocalContact(beamlines.get(0), paul));
    localContactCandidates.add(new LocalContact(beamlines.get(0), peter));
    localContactCandidates.add(new LocalContact(beamlines.get(0), prya));

    localContactCandidates.add(new LocalContact(beamlines.get(1), georges));
    localContactCandidates.add(new LocalContact(beamlines.get(1), gwendolin));
    localContactCandidates.add(new LocalContact(beamlines.get(1), guenievre));

    localContactCandidates.add(new LocalContact(beamlines.get(2), camille));
    localContactCandidates.add(new LocalContact(beamlines.get(2), charles));

    List<Session> sessions = new ArrayList<>();

    var datePrefsMap = Map.of(
        "LT-0101", List.of(
            new DatePreference(runStart.plusDays(1L), runStart.plusDays(3L), false),
            new DatePreference(runStart.plusDays(7L), runStart.plusDays(9L), true)
        )
    );


    final BiConsumer<String, RequestedBeamline> singleBunchSessionCreate = (finalNumber, requestedBeamline) -> {
      var proposal = datePrefsMap.containsKey(finalNumber) ? new Proposal(finalNumber,
                                                                          singleBunchBeamMode,
                                                                          datePrefsMap.get(finalNumber))
                                                           : new Proposal(finalNumber, singleBunchBeamMode);
      for (int i = 0; i < requestedBeamline.numberOfSlots; i++) {
        sessions.add(new Session(proposal, requestedBeamline.beamline));
      }
    };

    final BiConsumer<String, RequestedBeamline> sevenEighthsSessionCreate = (finalNumber, requestedBeamline) -> {
      var proposal = new Proposal(finalNumber, sevenEighthsBeamMode);
      for (int i = 0; i < requestedBeamline.numberOfSlots; i++) {
        sessions.add(new Session(proposal, requestedBeamline.beamline));
      }
    };

    var beamline1 = beamlines.get(0);
    Map.of("MX-1234", new RequestedBeamline(beamline1, 25),
           "STD-0001", new RequestedBeamline(beamline1, 12),
           "LT-0101", new RequestedBeamline(beamline1, 16),
           "IN-452", new RequestedBeamline(beamline1, 3)
       )
       .forEach(singleBunchSessionCreate);

    Map.of("MX-7/8", new RequestedBeamline(beamline1, 14),
           "STD-7/8", new RequestedBeamline(beamline1, 5)
       )
       .forEach(sevenEighthsSessionCreate);

    var beamline2 = beamlines.get(1);
    Map.of("MX-1234", new RequestedBeamline(beamline2, 30),
           "LT-0101", new RequestedBeamline(beamline2, 12),
           "IN-666", new RequestedBeamline(beamline2, 40)
       )
       .forEach(singleBunchSessionCreate);


    var beamline3 = beamlines.get(2);
    Map.of("MX-1234", new RequestedBeamline(beamline3, 10),
           "STD-0001", new RequestedBeamline(beamline3, 5),
           "IN-666", new RequestedBeamline(beamline3, 40)
       )
       .forEach(singleBunchSessionCreate);

    Map.of("MX-7/8", new RequestedBeamline(beamline3, 14),
           "STD-7/8", new RequestedBeamline(beamline3, 5)
       )
       .forEach(sevenEighthsSessionCreate);

    return Response.ok(new Timetable(
                       demoData.name(),
                       beamtimeSlots,
                       beamlines,
                       sessions,
                       localContactCandidates)
                   )
                   .build();
  }


  private record RequestedBeamline(Beamline beamline, int numberOfSlots) {

  }
}
