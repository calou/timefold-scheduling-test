package org.acme.schooltimetabling.solver;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.schooltimetabling.domain.Lesson;
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
        Lesson firstLesson = new Lesson("1", "Subject1", "Teacher1", "Group1", SHIFT_1, BEAMLINE_1);
        Lesson conflictingLesson = new Lesson("2", "Subject2", "Teacher2", "Group2", SHIFT_1, BEAMLINE_1);
        Lesson nonConflictingLesson = new Lesson("3", "Subject3", "Teacher3", "Group3", SHIFT_2, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::roomConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void teacherConflict() {
        String conflictingTeacher = "Teacher1";
        Lesson firstLesson = new Lesson("1", "Subject1", conflictingTeacher, "Group1", SHIFT_1, BEAMLINE_1);
        Lesson conflictingLesson = new Lesson("2", "Subject2", conflictingTeacher, "Group2", SHIFT_1, BEAMLINE_2);
        Lesson nonConflictingLesson = new Lesson("3", "Subject3", "Teacher2", "Group3", SHIFT_2, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void studentGroupConflict() {
        String conflictingGroup = "Group1";
        Lesson firstLesson = new Lesson("1", "Subject1", "Teacher1", conflictingGroup, SHIFT_1, BEAMLINE_1);
        Lesson conflictingLesson = new Lesson("2", "Subject2", "Teacher2", conflictingGroup, SHIFT_1, BEAMLINE_2);
        Lesson nonConflictingLesson = new Lesson("3", "Subject3", "Teacher3", "Group3", SHIFT_2, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::studentGroupConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void teacherRoomStability() {
        String teacher = "Teacher1";
        Lesson lessonInFirstRoom = new Lesson("1", "Subject1", teacher, "Group1", SHIFT_1, BEAMLINE_1);
        Lesson lessonInSameRoom = new Lesson("2", "Subject2", teacher, "Group2", SHIFT_1, BEAMLINE_1);
        Lesson lessonInDifferentRoom = new Lesson("3", "Subject3", teacher, "Group3", SHIFT_1, BEAMLINE_2);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherRoomStability)
                .given(lessonInFirstRoom, lessonInDifferentRoom, lessonInSameRoom)
                .penalizesBy(2);
    }

    @Test
    void teacherTimeEfficiency() {
        String teacher = "Teacher1";
        Lesson singleLessonOnMonday = new Lesson("1", "Subject1", teacher, "Group1", SHIFT_1, BEAMLINE_1);
        Lesson firstTuesdayLesson = new Lesson("2", "Subject2", teacher, "Group2", SHIFT_2, BEAMLINE_1);
        Lesson secondTuesdayLesson = new Lesson("3", "Subject3", teacher, "Group3", SHIFT_3, BEAMLINE_1);
        Lesson thirdTuesdayLessonWithGap = new Lesson("4", "Subject4", teacher, "Group4", SHIFT_4, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherTimeEfficiency)
                .given(singleLessonOnMonday, firstTuesdayLesson, secondTuesdayLesson, thirdTuesdayLessonWithGap)
                .rewardsWith(1); // Second tuesday lesson immediately follows the first.

        // Reverse ID order
        Lesson altSecondTuesdayLesson = new Lesson("2", "Subject2", teacher, "Group3", SHIFT_3, BEAMLINE_1);
        Lesson altFirstTuesdayLesson = new Lesson("3", "Subject3", teacher, "Group2", SHIFT_2, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::teacherTimeEfficiency)
                .given(altSecondTuesdayLesson, altFirstTuesdayLesson)
                .rewardsWith(1); // Second tuesday lesson immediately follows the first.
    }

    @Test
    void studentGroupSubjectVariety() {
        String studentGroup = "Group1";
        String repeatedSubject = "Subject1";
        Lesson mondayLesson = new Lesson("1", repeatedSubject, "Teacher1", studentGroup, SHIFT_1, BEAMLINE_1);
        Lesson firstTuesdayLesson = new Lesson("2", repeatedSubject, "Teacher2", studentGroup, SHIFT_2, BEAMLINE_1);
        Lesson secondTuesdayLesson = new Lesson("3", repeatedSubject, "Teacher3", studentGroup, SHIFT_3, BEAMLINE_1);
        Lesson thirdTuesdayLessonWithDifferentSubject = new Lesson("4", "Subject2", "Teacher4", studentGroup,
                                                                   SHIFT_4, BEAMLINE_1);
        Lesson lessonInAnotherGroup = new Lesson("5", repeatedSubject, "Teacher5", "Group2", SHIFT_1, BEAMLINE_1);
        constraintVerifier.verifyThat(TimetableConstraintProvider::studentGroupSubjectVariety)
                .given(mondayLesson, firstTuesdayLesson, secondTuesdayLesson, thirdTuesdayLessonWithDifferentSubject,
                        lessonInAnotherGroup)
                .penalizesBy(1); // Second tuesday lesson immediately follows the first.
    }

}
