package org.acme.schooltimetabling.rest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

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
                            schema = @Schema(implementation = Timetable.class)))})
    @Operation(summary = "Find an unsolved demo timetable by ID.")
    @GET
    @Path("/{demoDataId}")
    public Response generate(@Parameter(description = "Unique identifier of the demo data.",
            required = true) @PathParam("demoDataId") DemoData demoData) {
        List<Shift> shifts = new ArrayList<>(270);

        for (long i = 0; i < 90L; i++) {
            var date = LocalDate.now().plusDays(i);
            shifts.addAll(List.of(
               new Shift(date, 0),
               new Shift(date, 1),
               new Shift(date, 2)
            ));
        }

        List<Beamline> beamlines = new ArrayList<>(3);
        long nextRoomId = 0L;
        beamlines.add(new Beamline(Long.toString(nextRoomId++), "Room A"));
        beamlines.add(new Beamline(Long.toString(nextRoomId++), "Room B"));
        beamlines.add(new Beamline(Long.toString(nextRoomId++), "Room C"));
        if (demoData == DemoData.LARGE) {
            beamlines.add(new Beamline(Long.toString(nextRoomId++), "Room D"));
            beamlines.add(new Beamline(Long.toString(nextRoomId++), "Room E"));
            beamlines.add(new Beamline(Long.toString(nextRoomId++), "Room F"));
        }

        List<Session> sessions = new ArrayList<>();
        long nextLessonId = 0L;
        sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "9th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "9th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Physics", "M. Curie", "9th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "9th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Biology", "C. Darwin", "9th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "History", "I. Jones", "9th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "English", "I. Jones", "9th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "English", "I. Jones", "9th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "9th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "9th grade"));
        if (demoData == DemoData.LARGE) {
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "ICT", "A. Turing", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physics", "M. Curie", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Geography", "C. Darwin", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Geology", "C. Darwin", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "History", "I. Jones", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "English", "I. Jones", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Drama", "I. Jones", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Art", "S. Dali", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Art", "S. Dali", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "9th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "9th grade"));
        }

        sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Physics", "M. Curie", "10th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "10th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "French", "M. Curie", "10th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Geography", "C. Darwin", "10th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "History", "I. Jones", "10th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "English", "P. Cruz", "10th grade"));
        sessions.add(new Session(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "10th grade"));
        if (demoData == DemoData.LARGE) {
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "ICT", "A. Turing", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physics", "M. Curie", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Biology", "C. Darwin", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Geology", "C. Darwin", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "History", "I. Jones", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "English", "P. Cruz", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "English", "P. Cruz", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Drama", "I. Jones", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Art", "S. Dali", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Art", "S. Dali", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "10th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "10th grade"));

            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "ICT", "A. Turing", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physics", "M. Curie", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "French", "M. Curie", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physics", "M. Curie", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Geography", "C. Darwin", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Biology", "C. Darwin", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Geology", "C. Darwin", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "History", "I. Jones", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "History", "I. Jones", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "English", "P. Cruz", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "English", "P. Cruz", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "English", "P. Cruz", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Drama", "P. Cruz", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Art", "S. Dali", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Art", "S. Dali", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "11th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "11th grade"));

            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Math", "A. Turing", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "ICT", "A. Turing", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physics", "M. Curie", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Chemistry", "M. Curie", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "French", "M. Curie", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physics", "M. Curie", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Geography", "C. Darwin", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Biology", "C. Darwin", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Geology", "C. Darwin", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "History", "I. Jones", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "History", "I. Jones", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "English", "P. Cruz", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "English", "P. Cruz", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "English", "P. Cruz", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Spanish", "P. Cruz", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Drama", "P. Cruz", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Art", "S. Dali", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Art", "S. Dali", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "12th grade"));
            sessions.add(new Session(Long.toString(nextLessonId++), "Physical education", "C. Lewis", "12th grade"));
        }
        return Response.ok(new Timetable(demoData.name(), shifts, beamlines, sessions)).build();
    }

}
