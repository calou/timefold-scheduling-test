package org.acme.schooltimetabling.solver;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.schooltimetabling.domain.Session;
import org.acme.schooltimetabling.domain.Beamline;
import org.acme.schooltimetabling.domain.Shift;
import org.acme.schooltimetabling.domain.Timetable;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TimetableConstraintProviderTest {

    private static final Beamline BEAMLINE_1 = new Beamline("1", "Room1");
    private static final Beamline BEAMLINE_2 = new Beamline("2", "Room2");
    private static final Shift SHIFT_1 = new Shift("1", DayOfWeek.MONDAY, LocalTime.NOON);
    private static final Shift SHIFT_2 = new Shift("2", DayOfWeek.TUESDAY, LocalTime.NOON);
    private static final Shift SHIFT_3 = new Shift("3", DayOfWeek.TUESDAY, LocalTime.NOON.plusHours(1));
    private static final Shift SHIFT_4 = new Shift("4", DayOfWeek.TUESDAY, LocalTime.NOON.plusHours(3));

    @Inject
    ConstraintVerifier<TimetableConstraintProvider, Timetable> constraintVerifier;

    @Test
    void roomConflict() {
        Session firstSession = new Session("1", "Subject1", "Teacher1", "Group1", SHIFT_1, BEAMLINE_1);
        Session conflictingSession = new Session("2", "Subject2", "Teacher2", "Group2", SHIFT_1, BEAMLINE_1);
        Session nonConflictingSession = new Session("3", "Subject3", "Teacher3", "Group3", SHIFT_2, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomConflict)
                .given(firstSession, conflictingSession, nonConflictingSession)
                .penalizesBy(1);
    }

    @Test
    void teacherConflict() {
        String conflictingTeacher = "Teacher1";
        Session firstSession = new Session("1", "Subject1", conflictingTeacher, "Group1", SHIFT_1, BEAMLINE_1);
        Session conflictingSession = new Session("2", "Subject2", conflictingTeacher, "Group2", SHIFT_1, BEAMLINE_2);
        Session nonConflictingSession = new Session("3", "Subject3", "Teacher2", "Group3", SHIFT_2, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherConflict)
                .given(firstSession, conflictingSession, nonConflictingSession)
                .penalizesBy(1);
    }

    @Test
    void studentGroupConflict() {
        String conflictingGroup = "Group1";
        Session firstSession = new Session("1", "Subject1", "Teacher1", conflictingGroup, SHIFT_1, BEAMLINE_1);
        Session conflictingSession = new Session("2", "Subject2", "Teacher2", conflictingGroup, SHIFT_1, BEAMLINE_2);
        Session nonConflictingSession = new Session("3", "Subject3", "Teacher3", "Group3", SHIFT_2, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::studentGroupConflict)
                .given(firstSession, conflictingSession, nonConflictingSession)
                .penalizesBy(1);
    }

    @Test
    void teacherRoomStability() {
        String teacher = "Teacher1";
        Session sessionInFirstRoom = new Session("1", "Subject1", teacher, "Group1", SHIFT_1, BEAMLINE_1);
        Session sessionInSameRoom = new Session("2", "Subject2", teacher, "Group2", SHIFT_1, BEAMLINE_1);
        Session sessionInDifferentRoom = new Session("3", "Subject3", teacher, "Group3", SHIFT_1, BEAMLINE_2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherRoomStability)
                .given(sessionInFirstRoom, sessionInDifferentRoom, sessionInSameRoom)
                .penalizesBy(2);
    }

    @Test
    void teacherTimeEfficiency() {
        String teacher = "Teacher1";
        Session singleSessionOnMonday = new Session("1", "Subject1", teacher, "Group1", SHIFT_1, BEAMLINE_1);
        Session firstTuesdaySession = new Session("2", "Subject2", teacher, "Group2", SHIFT_2, BEAMLINE_1);
        Session secondTuesdaySession = new Session("3", "Subject3", teacher, "Group3", SHIFT_3, BEAMLINE_1);
        Session thirdTuesdaySessionWithGap = new Session("4", "Subject4", teacher, "Group4", SHIFT_4, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherTimeEfficiency)
                .given(singleSessionOnMonday, firstTuesdaySession, secondTuesdaySession, thirdTuesdaySessionWithGap)
                .rewardsWith(1); // Second tuesday lesson immediately follows the first.

        // Reverse ID order
        Session altSecondTuesdaySession = new Session("2", "Subject2", teacher, "Group3", SHIFT_3, BEAMLINE_1);
        Session altFirstTuesdaySession = new Session("3", "Subject3", teacher, "Group2", SHIFT_2, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherTimeEfficiency)
                .given(altSecondTuesdaySession, altFirstTuesdaySession)
                .rewardsWith(1); // Second tuesday lesson immediately follows the first.
    }

    @Test
    void studentGroupSubjectVariety() {
        String studentGroup = "Group1";
        String repeatedSubject = "Subject1";
        Session mondaySession = new Session("1", repeatedSubject, "Teacher1", studentGroup, SHIFT_1, BEAMLINE_1);
        Session firstTuesdaySession = new Session("2", repeatedSubject, "Teacher2", studentGroup, SHIFT_2, BEAMLINE_1);
        Session secondTuesdaySession = new Session("3", repeatedSubject, "Teacher3", studentGroup, SHIFT_3, BEAMLINE_1);
        Session thirdTuesdaySessionWithDifferentSubject = new Session("4", "Subject2", "Teacher4", studentGroup,
                                                                      SHIFT_4, BEAMLINE_1);
        Session sessionInAnotherGroup = new Session("5", repeatedSubject, "Teacher5", "Group2", SHIFT_1, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::studentGroupSubjectVariety)
                .given(mondaySession,
                       firstTuesdaySession,
                       secondTuesdaySession,
                       thirdTuesdaySessionWithDifferentSubject,
                       sessionInAnotherGroup)
                .penalizesBy(1); // Second tuesday lesson immediately follows the first.
    }

}
