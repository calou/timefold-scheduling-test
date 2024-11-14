package org.acme.schooltimetabling.solver;

import java.time.Duration;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import org.acme.schooltimetabling.domain.Session;

public class TimetableConstraintProvider implements ConstraintProvider {

  @Override
  public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
    return new Constraint[]{
        // Hard constraints
        beamlineConflict(constraintFactory),
        // proposalConflict(constraintFactory),
        //studentGroupConflict(constraintFactory),
        // Soft constraints
        //teacherBeamlineStability(constraintFactory),
        proposalTimeEfficiency(constraintFactory)//,
        //studentGroupSubjectVariety(constraintFactory)
    };
  }

  // A beamline can accommodate at most one session at the same time.
  Constraint beamlineConflict(ConstraintFactory constraintFactory) {
    return constraintFactory
        // Select each pair of 2 different sessions ...
        .forEachUniquePair(Session.class,
                           // ... in the same shifts ...
                           Joiners.equal(Session::getShift),
                           // ... on the same beamline ...
                           Joiners.equal(Session::getBeamline))
        // ... and penalize each pair with a hard weight.
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Beamline conflict");
  }

    /*
    Constraint proposalConflict(ConstraintFactory constraintFactory) {
        // A teacher can teach at most one session at the same time.
        return constraintFactory
                .forEachUniquePair(Session.class,
                                   Joiners.equal(Session::getShift),
                                   Joiners.equal(Session::getBeamline),
                                   Joiners.equal(Session::getProposal))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

     */

    /*
    Constraint teacherBeamlineStability(ConstraintFactory constraintFactory) {
        // A proposal prefers to teach in a single room.
        return constraintFactory
                .forEachUniquePair(Session.class,
                                   Joiners.equal(Session::getProposal))
                .filter((session1, session2) -> session1.getBeamline() != session2.getBeamline())
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher Beamline stability");
    }*/

  Constraint proposalTimeEfficiency(ConstraintFactory constraintFactory) {
    // A teacher prefers to teach sequential lessons and dislikes gaps between lessons.
    return constraintFactory
        .forEach(Session.class)
        .join(Session.class, Joiners.equal(Session::getProposal)
              //,Joiners.equal((session) -> session.getShift().getDate())
        )
        .filter((session1, session2) -> {
          var indexDiff = session2.getShift()
                                  .getShiftIndex() - session1.getShift()
                                                             .getShiftIndex();
          return indexDiff > 0l && indexDiff < 3l;
        })
        .reward(HardSoftScore.ONE_SOFT)
        .asConstraint("Teacher time efficiency");
  }

    /*
    Constraint studentGroupSubjectVariety(ConstraintFactory constraintFactory) {
        // A student group dislikes sequential lessons on the same subject.
        return constraintFactory
                .forEach(Session.class)
                .join(Session.class,
                      Joiners.equal(Session::getSubject),
                      Joiners.equal(Session::getStudentGroup),
                      Joiners.equal((session) -> session.getShift().getDate()))
                .filter((session1, session2) -> {
                    Duration between = Duration.between(session1.getShift().getEndTime(),
                                                        session2.getShift().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Student group subject variety");
    }

     */

}
