package org.acme.schooltimetabling.rest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.schooltimetabling.domain.Proposal;
import org.acme.schooltimetabling.domain.Session;
import org.acme.schooltimetabling.domain.Beamline;
import org.acme.schooltimetabling.domain.Shift;
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
    List<Shift> shifts = new ArrayList<>(270);

    for (long i = 0; i < 90L; i++) {
      var date = LocalDate.now()
                          .plusDays(i);
      shifts.addAll(List.of(
          new Shift(date, 0),
          new Shift(date, 1),
          new Shift(date, 2)
      ));
    }

    List<Beamline> beamlines = new ArrayList<>(3);
    long nextRoomId = 0L;
    beamlines.add(new Beamline("ID21"));
    beamlines.add(new Beamline("ID22"));
    beamlines.add(new Beamline("CM01"));

    List<Session> sessions = new ArrayList<>();

    final BiConsumer<String, RequestedBeamline> sessionCreate = (finalNumber, requestedBeamline) -> {
      var proposal = new Proposal(finalNumber);
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
       .forEach(sessionCreate);

    var beamline2 = beamlines.get(1);
    Map.of("MX-1234", new RequestedBeamline(beamline2, 30),
           "LT-0101", new RequestedBeamline(beamline2, 30),
           "IN-666", new RequestedBeamline( beamline2, 40)
       )
       .forEach(sessionCreate);


    var beamline3 = beamlines.get(2);
    Map.of("MX-1234", new RequestedBeamline(beamline3, 10),
           "STD-0001", new RequestedBeamline(beamline3, 5),
           "LT-0101", new RequestedBeamline(beamline3, 2),
           "IN-666", new RequestedBeamline(beamline3, 40)
       )
       .forEach(sessionCreate);


    return Response.ok(new Timetable(demoData.name(), shifts, beamlines, sessions))
                   .build();
  }


  private record RequestedBeamline(Beamline beamline, int shift){}
}
