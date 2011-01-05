package module.mission.domain;

import java.util.Collection;
import java.util.Set;

import module.mission.domain.util.ParticipantAuthorizationChain.AuthorizationChain;
import module.organization.domain.Accountability;
import module.organization.domain.AccountabilityType;
import module.organization.domain.FunctionDelegation;
import module.organization.domain.Person;
import module.organization.domain.Unit;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.User;

import org.joda.time.DateTime;

public class PersonMissionAuthorization extends PersonMissionAuthorization_Base {
    
    public PersonMissionAuthorization() {
        super();
        setMissionSystem(MissionSystem.getInstance());
    }

    public PersonMissionAuthorization(final Person subject, AuthorizationChain authorizationChain) {
	this();
	setSubject(subject);
	final Unit unit = authorizationChain.getUnit();
	setUnit(unit);
	if (authorizationChain.getNext() != null) {
	    setNext(new PersonMissionAuthorization(subject, authorizationChain.getNext()));
	}
    }

    public PersonMissionAuthorization(final Mission mission, final Person subject, final AuthorizationChain authorizationChain) {
	this(subject, authorizationChain);
	setMission(mission);
    }

    public void delete() {
	if (hasNext()) {
	    getNext().delete();
	}
	removePrevious();
	removeSubject();
	removeUnit();
	removeMission();
	removeAuthority();
	removeMissionSystem();
	deleteDomainObject();
    }

    public boolean canAuthoriseParticipantActivity() {
	final User user = UserView.getCurrentUser();
	return user != null && canAuthoriseParticipantActivity(user.getPerson());
    }

    public boolean canAuthoriseParticipantActivity(final Person person) {
	if (person == getSubject()) {
	    return false;
	}
	final MissionSystem instance = MissionSystem.getInstance();
	final Set<AccountabilityType> accountabilityTypes = instance.getAccountabilityTypesThatAuthorize();
	//final AccountabilityType accountabilityType = IstAccountabilityType.PERSONNEL_RESPONSIBLE_MISSIONS.readAccountabilityType();
	return (!hasAuthority() && !hasDelegatedAuthority() && getUnit().hasChildAccountabilityIncludingAncestry(accountabilityTypes, person))
		|| (hasNext() && getNext().canAuthoriseParticipantActivity(person));
    }

    public boolean canUnAuthoriseParticipantActivity() {
	final User user = UserView.getCurrentUser();
	return user != null && canUnAuthoriseParticipantActivity(user.getPerson());
    }

    public boolean canUnAuthoriseParticipantActivity(final Person person) {
	if (person == getSubject()) {
	    return false;
	}
	final MissionSystem instance = MissionSystem.getInstance();
	final Set<AccountabilityType> accountabilityTypes = instance.getAccountabilityTypesThatAuthorize();
	//final AccountabilityType accountabilityType = IstAccountabilityType.PERSONNEL_RESPONSIBLE_MISSIONS.readAccountabilityType();
	return (hasAuthority() || hasDelegatedAuthority()) && canUnAuthorise(person, accountabilityTypes) && ((!hasNext()) || (!getNext().hasAuthority() && !getNext().hasDelegatedAuthority()));
    }

    private boolean canUnAuthorise(final Person person, final Collection<AccountabilityType> accountabilityTypes) {
	final Unit unitForAuthorizationCheck = hasDelegatedAuthority() && hasPrevious() ? getPrevious().getUnit() : getUnit();
	return unitForAuthorizationCheck.hasChildAccountabilityIncludingAncestry(accountabilityTypes, person)
			|| (hasNext() && getNext().canUnAuthorise(person, accountabilityTypes));
    }

    public boolean canUnAuthoriseSomeParticipantActivity(final Person person) {
	return canUnAuthoriseParticipantActivity(person) || (hasNext() && getNext().canUnAuthoriseSomeParticipantActivity(person));
    }

    @Override
    public void setAuthority(final Person authority) {
        super.setAuthority(authority);
        final DateTime authorizationDateTime = authority == null ? null : new DateTime();
        setAuthorizationDateTime(authorizationDateTime);
        if (hasNext()) {
            getNext().setDelegatedAuthority(authority);
        }
    }

    private void setDelegatedAuthority(final Person authority) {
	if (authority == null) {
	    setDelegatedAuthority((FunctionDelegation) null);
	} else {	    
	    final Unit unit = getUnit();
	    final MissionSystem instance = MissionSystem.getInstance();
	    final Set<AccountabilityType> accountabilityTypes = instance.getAccountabilityTypesThatAuthorize();
	    for (final Accountability accountability : authority.getParentAccountabilitiesSet()) {
		final AccountabilityType accountabilityType = accountability.getAccountabilityType();
		if (accountabilityTypes.contains(accountabilityType)) {
		    final FunctionDelegation functionDelegation = accountability.getFunctionDelegationDelegator();
		    if (functionDelegation != null) {
			final Accountability parentAccountability = functionDelegation.getAccountabilityDelegator();
			if (unit == parentAccountability.getParent()) {
			    setDelegatedAuthority(functionDelegation);
			    return ;
			}
		    }
		}
	    }
	}
    }

    public boolean hasAnyAuthorization() {
	return (getAuthorizationDateTime() != null && (hasAuthority() || hasDelegatedAuthority())) || (hasNext() && getNext().hasAnyAuthorization());
    }

    public boolean isAuthorized() {
	return (!hasNext() && (hasAuthority() || hasDelegatedAuthority())) || (hasNext() && getNext().isAuthorized());
    }

    public int getChainSize() {
	return hasNext() ? getNext().getChainSize() + 1 : 1;
    }

    public Mission getAssociatedMission() {
	final Mission mission = getMission();
	return mission != null || !hasPrevious() ? mission : getPrevious().getAssociatedMission();
    }

    public void clearAuthorities() {
	if (hasNext()) {
	    getNext().clearAuthorities();
	}
	removeAuthority();
    }

    public MissionProcess getMissionProcess() {
	return hasMission() ? getMission().getMissionProcess() : (hasPrevious() ? getPrevious().getMissionProcess() : null);
    }

}
