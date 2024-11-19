package org.acme.schooltimetabling.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoftbigdecimal.HardSoftBigDecimalScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.core.api.score.stream.common.LoadBalance;
import org.acme.schooltimetabling.domain.LocalContact;
import org.acme.schooltimetabling.domain.Session;

import java.math.BigDecimal;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count;

public class TimetableConstraintProvider implements ConstraintProvider {

  @Override
  public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
    return new Constraint[]{
        // Hard constraints
        beamlineConflict(constraintFactory),
        proposalConflict(constraintFactory),
        beamModeConflict(constraintFactory),
        proposalUnacceptableDates(constraintFactory),
        localContactBeamlineConflict(constraintFactory),
        localBeamlineUnicityConflict(constraintFactory),
        fairLocalContactAssignments(constraintFactory),

        // Soft constraints
        consecutiveProposalSession(constraintFactory),
        proposalSessionProximity(constraintFactory),
        maxTenSlotsPerLocalContactAssignments(constraintFactory),
        proposalPreferredDatesConstraint(constraintFactory)
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
        .penalize(HardSoftBigDecimalScore.ONE_HARD)
        .asConstraint("Beamline conflict");
  }

  Constraint beamModeConflict(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEach(Session.class)
        .filter(session -> !session.getBeamtimeSlot()
                                   .getBeamMode()
                                   .equals(session.getProposal()
                                                  .getBeamMode()))
        .penalize(HardSoftBigDecimalScore.ONE_HARD)
        .asConstraint("Session mode");
  }

  Constraint localContactBeamlineConflict(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEach(Session.class)
        .filter(session -> !session.getLocalContact()
                                   .getBeamline()
                                   .getId()
                                   .equals(session.getBeamline()
                                                  .getId()))
        .penalize(HardSoftBigDecimalScore.ONE_HARD)
        .asConstraint("Local contact beamline");
  }

  Constraint localBeamlineUnicityConflict(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEachUniquePair(Session.class,
                           // ... in the same shifts ...
                           Joiners.equal(Session::getBeamtimeSlot),
                           // ... on the same beamline ...
                           Joiners.equal(session -> session.getLocalContact()
                                                           .getStaffMember()))
        .penalize(HardSoftBigDecimalScore.ONE_HARD)
        .asConstraint("Local contact unicity beamline");
  }

  Constraint fairLocalContactAssignments(ConstraintFactory constraintFactory) {
    return constraintFactory.forEach(LocalContact.class)
                            .groupBy(ConstraintCollectors.loadBalance(LocalContact::getStaffMember))
                            .penalizeBigDecimal(HardSoftBigDecimalScore.ONE_SOFT, LoadBalance::unfairness)
                            .asConstraint("fair local contact assignments");
  }

  Constraint maxTenSlotsPerLocalContactAssignments(ConstraintFactory constraintFactory) {
    return constraintFactory.forEach(Session.class)
                                .groupBy(
                                    session -> session.getLocalContact().getStaffMember(),
                                    session -> session.getBeamtimeSlot().getDate(),
                                    count())
                            .penalize(HardSoftBigDecimalScore.ONE_SOFT, ((staffMember, date, count) -> count > 10 ? 1 : 0))
                            .asConstraint("Max 10 slots per LocalContact per day");
  }

  Constraint proposalUnacceptableDates(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEach(Session.class)
        .filter(this::isSessionWithDatePrefs)
        .filter(this::isSessionDateUnacceptable)
        .penalize(HardSoftBigDecimalScore.ONE_HARD)
        .asConstraint("Proposal unacceptable dates");
  }

  Constraint proposalPreferredDatesConstraint(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEach(Session.class)
        .filter(this::isSessionWithDatePrefs)
        .filter(this::isSessionDatePreferred)
        .reward(HardSoftBigDecimalScore.of(BigDecimal.ZERO, BigDecimal.valueOf(3L)))
        .asConstraint("Proposal preferred dates");
  }

  // only one session per proposal is allowed at a given time
  Constraint proposalConflict(ConstraintFactory constraintFactory) {

    return constraintFactory
        .forEachUniquePair(Session.class,
                           Joiners.equal(Session::getBeamtimeSlot),
                           Joiners.equal(Session::getProposal))
        .penalize(HardSoftBigDecimalScore.ONE_HARD)
        .asConstraint("proposal single session conflict");
  }

  // Consecutive sessions are preferred for a given proposal
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
        .reward(HardSoftBigDecimalScore.ONE_SOFT)
        .asConstraint("Consecutive proposal sessions");
  }


  Constraint proposalSessionProximity(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEach(Session.class)
        .join(Session.class, Joiners.equal(Session::getProposal))
        .impact(HardSoftBigDecimalScore.ONE_SOFT, (session1, session2) -> {
          var indexDiff = session2.getBeamtimeSlot()
                                  .getIndex() - session1.getBeamtimeSlot()
                                                        .getIndex();
          return (int) (-1 * Math.abs(indexDiff / 3));
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
                .penalize(HardSoftBigDecimalScore.ONE_SOFT)
                .asConstraint("Student group subject variety");
    }

     */


  private boolean isSessionWithDatePrefs(Session session) {
    return session.getProposal()
                  .getDatePreferences() != null && !session.getProposal()
                                                           .getDatePreferences()
                                                           .isEmpty();
  }

  private boolean isSessionDateUnacceptable(Session session) {
    return session.getProposal()
                  .getDatePreferences()
                  .stream()
                  .anyMatch(datePrefs -> !datePrefs.isAcceptable() && datePrefs.contains(session.getBeamtimeSlot()
                                                                                                .getDate()));
  }

  private boolean isSessionDatePreferred(Session session) {
    return session.getProposal()
                  .getDatePreferences()
                  .stream()
                  .anyMatch(datePrefs -> datePrefs.isAcceptable() && datePrefs.contains(session.getBeamtimeSlot()
                                                                                               .getDate()));
  }

}
