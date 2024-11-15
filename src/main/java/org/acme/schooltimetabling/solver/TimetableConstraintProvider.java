package org.acme.schooltimetabling.solver;

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
        proposalConflict(constraintFactory),
        beamModeConflict(constraintFactory),
        //studentGroupConflict(constraintFactory),
        // Soft constraints
        //teacherBeamlineStability(constraintFactory),
        consecutiveProposalSession(constraintFactory),
        proposalSessionProximity(constraintFactory)
        //studentGroupSubjectVariety(constraintFactory)
    };
  }

  // A beamline can accommodate at most one session at the same time.
  Constraint beamlineConflict(ConstraintFactory constraintFactory) {
    return constraintFactory
        // Select each pair of 2 different sessions ...
        .forEachUniquePair(Session.class,
                           // ... in the same shifts ...
                           Joiners.equal(Session::getBeamtimeSlot),
                           // ... on the same beamline ...
                           Joiners.equal(Session::getBeamline))
        // ... and penalize each pair with a hard weight.
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Beamline conflict");
  }

  Constraint beamModeConflict(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEach(Session.class)
        .filter( session -> !session.getBeamtimeSlot().getBeamMode().equals(session.getProposal().getBeamMode()) )
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Session mode");
  }

  // only one session per proposal is allowed at a given time
  Constraint proposalConflict(ConstraintFactory constraintFactory) {

    return constraintFactory
        .forEachUniquePair(Session.class,
                           Joiners.equal(Session::getBeamtimeSlot),
                           Joiners.equal(Session::getProposal))
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Teacher conflict");
  }

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

  // Consecutive sessions are prefered for a given proposal
  Constraint consecutiveProposalSession(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEach(Session.class)
        .join(Session.class, Joiners.equal(Session::getProposal)
        )
        .filter((session1, session2) -> {
          var indexDiff = session2.getBeamtimeSlot()
                                  .getIndex() - session1.getBeamtimeSlot()
                                                        .getIndex();
          return Math.abs(indexDiff) == 1l;
        })
        .reward(HardSoftScore.ONE_SOFT)
        .asConstraint("Consecutive proposal sessions");
  }


  Constraint proposalSessionProximity(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEach(Session.class)
        .join(Session.class, Joiners.equal(Session::getProposal))
        .impact(HardSoftScore.ONE_SOFT, (session1, session2) -> {
          var indexDiff = session2.getBeamtimeSlot()
                                  .getIndex() - session1.getBeamtimeSlot()
                                                        .getIndex();
          return  (int)(-1 * Math.abs(indexDiff/3)) ;
        })
        .asConstraint("Proposal session proximity");
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
