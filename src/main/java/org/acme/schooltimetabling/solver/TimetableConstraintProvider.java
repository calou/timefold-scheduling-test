package org.acme.schooltimetabling.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import org.acme.schooltimetabling.domain.Session;
import org.acme.schooltimetabling.domain.StaffMember;

import java.math.BigDecimal;
import java.math.MathContext;

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
        fairLocalContactAssignments(constraintFactory),
        preventLocalContactWithoutAnyAssignment(constraintFactory),

        // Soft constraints
        consecutiveProposalSession(constraintFactory),
        proposalSessionProximity(constraintFactory),
        proposalPreferredDatesConstraint(constraintFactory),
        localContactSessionProximity(constraintFactory)
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
        .filter(session -> !session.getBeamtimeSlot()
                                   .getBeamMode()
                                   .equals(session.getProposal()
                                                  .getBeamMode()))
        .penalize(HardSoftScore.ONE_HARD)
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
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Local contact beamline");
  }


  Constraint fairLocalContactAssignments(ConstraintFactory constraintFactory) {
    return constraintFactory.forEach(Session.class)
                            .groupBy(ConstraintCollectors.loadBalance(session -> session.getLocalContact()
                                                                                        .getStaffMember()))
                            .penalize(HardSoftScore.ONE_SOFT,
                                                balance -> balance.unfairness().intValue())
                            .asConstraint("fair local contact assignments");
  }

  Constraint preventLocalContactWithoutAnyAssignment(ConstraintFactory constraintFactory) {
    return constraintFactory.forEach(StaffMember.class)
                            .ifNotExists(Session.class,
                                         Joiners.filtering((staffMember, session) -> session.getLocalContact()
                                                                                            .getStaffMember()
                                                                                            .equals(staffMember)))
                            .penalize(HardSoftScore.ONE_HARD)
                            .asConstraint("prevent localContact without any assignment");
  }

  Constraint proposalUnacceptableDates(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEach(Session.class)
        .filter(this::isSessionWithDatePrefs)
        .filter(this::isSessionDateUnacceptable)
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("Proposal unacceptable dates");
  }

  Constraint proposalPreferredDatesConstraint(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEach(Session.class)
        .filter(this::isSessionWithDatePrefs)
        .filter(this::isSessionDatePreferred)
        .reward(HardSoftScore.ofSoft(3))
        .asConstraint("Proposal preferred dates");
  }

  // only one session per proposal is allowed at a given time
  Constraint proposalConflict(ConstraintFactory constraintFactory) {

    return constraintFactory
        .forEachUniquePair(Session.class,
                           Joiners.equal(Session::getBeamtimeSlot),
                           Joiners.equal(Session::getProposal))
        .penalize(HardSoftScore.ONE_HARD)
        .asConstraint("proposal single session conflict");
  }

  // Consecutive sessions are preferred for a given proposal
  Constraint consecutiveProposalSession(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEachUniquePair(Session.class,
                           Joiners.equal(Session::getProposal))
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
    return constraintFactory.forEach(Session.class)
        .join(Session.class, Joiners.equal(Session::getProposal))
        .impact(HardSoftScore.ONE_SOFT, (session1, session2) -> {
          var indexDiff = session2.getBeamtimeSlot()
                                  .getIndex() - session1.getBeamtimeSlot()
                                                        .getIndex();
          return (int) (-1 * Math.abs(indexDiff / 3));
        })
        .asConstraint("Proposal session proximity");
  }
  
  Constraint localContactSessionProximity(ConstraintFactory constraintFactory) {
    return constraintFactory
        .forEachUniquePair(Session.class,
                           Joiners.equal(Session::getBeamline),
                           Joiners.equal(session -> session.getLocalContact().getStaffMember()))
        .filter((session1, session2) -> {
          var indexDiff = session2.getBeamtimeSlot()
                                  .getIndex() - session1.getBeamtimeSlot()
                                                        .getIndex();
          return Math.abs(indexDiff) == 1l;
        })
        .reward(HardSoftScore.ONE_SOFT)
        .asConstraint("local contact session proximity");
  }

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
