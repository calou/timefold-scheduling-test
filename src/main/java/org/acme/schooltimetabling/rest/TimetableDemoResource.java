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
import org.acme.schooltimetabling.domain.Proposal;
import org.acme.schooltimetabling.domain.Session;
import org.acme.schooltimetabling.domain.Beamline;
import org.acme.schooltimetabling.domain.BeamtimeSlot;
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
    List<BeamtimeSlot> beamtimeSlots = new ArrayList<>(270);
    var singleBunchBeamMode = new BeamMode("Single Bunch");
    var sevenEighthsBeamMode = new BeamMode("7/8 + 1 Filling 200mA");

    for (long day = 0; day < 7L; day++) {
      var date = LocalDate.now()
                          .plusDays(day);

      var beamMode = switch ((int)day){
        case 0,1,3,5 -> singleBunchBeamMode;
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

    List<Session> sessions = new ArrayList<>();


    final BiConsumer<String, RequestedBeamline> singleBunchSessionCreate = (finalNumber, requestedBeamline) -> {
      var proposal = new Proposal(finalNumber, singleBunchBeamMode);
      for (int i = 0; i < requestedBeamline.shift; i++) {
        sessions.add(new Session(proposal, requestedBeamline.beamline));
      }
    };

    final BiConsumer<String, RequestedBeamline> sevenEighthsSessionCreate = (finalNumber, requestedBeamline) -> {
      var proposal = new Proposal(finalNumber, sevenEighthsBeamMode);
      for (int i = 0; i < requestedBeamline.shift; i++) {
        sessions.add(new Session(proposal, requestedBeamline.beamline));
      }
    };

    var beamline1 = beamlines.get(0);
    Map.of("MX-1234", new RequestedBeamline(beamline1, 25),
           "STD-0001", new RequestedBeamline(beamline1, 12),
           "LT-0101", new RequestedBeamline(beamline1, 16),
           "IN-666", new RequestedBeamline(beamline1, 3)
       )
       .forEach(singleBunchSessionCreate);

    Map.of("MX-7/8", new RequestedBeamline(beamline1, 14),
           "STD-7/8", new RequestedBeamline(beamline1, 5)
       )
       .forEach(sevenEighthsSessionCreate);

    var beamline2 = beamlines.get(1);
    Map.of("MX-1234", new RequestedBeamline(beamline2, 30),
           "LT-0101", new RequestedBeamline(beamline2, 30),
           "IN-666", new RequestedBeamline( beamline2, 40)
       )
       .forEach(singleBunchSessionCreate);


    var beamline3 = beamlines.get(2);
    Map.of("MX-1234", new RequestedBeamline(beamline3, 10),
           "STD-0001", new RequestedBeamline(beamline3, 5),
           "LT-0101", new RequestedBeamline(beamline3, 2),
           "IN-666", new RequestedBeamline(beamline3, 40)
       )
       .forEach(singleBunchSessionCreate);

    Map.of("MX-7/8", new RequestedBeamline(beamline3, 14),
           "STD-7/8", new RequestedBeamline(beamline3, 5)
       )
       .forEach(sevenEighthsSessionCreate);

    return Response.ok(new Timetable(demoData.name(), beamtimeSlots, beamlines, sessions))
                   .build();
  }


  private record RequestedBeamline(Beamline beamline, int shift){}
}
