package module.workingCapital.domain;

import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import module.organization.domain.Person;
import myorg.domain.User;
import myorg.domain.exceptions.DomainException;
import myorg.domain.util.Money;

import org.joda.time.DateTime;

import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
import pt.ist.expenditureTrackingSystem.domain.organization.Project;
import pt.ist.expenditureTrackingSystem.domain.organization.SubProject;
import pt.ist.expenditureTrackingSystem.domain.organization.Unit;

public class WorkingCapital extends WorkingCapital_Base {

    public static WorkingCapital find(final WorkingCapitalYear workingCapitalYear, final Unit unit) {
	for (final WorkingCapital workingCapital : unit.getWorkingCapitalsSet()) {
	    if (workingCapital.getWorkingCapitalYear() == workingCapitalYear) {
		return workingCapital;
	    }
	}
	return null;
    }

    public static WorkingCapital find(final Integer year, final Unit unit) {
	for (final WorkingCapital workingCapital : unit.getWorkingCapitalsSet()) {
	    if (workingCapital.getWorkingCapitalYear().getYear().intValue() == year.intValue()) {
		return workingCapital;
	    }
	}
	return null;
    }

    public WorkingCapital() {
        super();
        setWorkingCapitalSystem(WorkingCapitalSystem.getInstance());
    }

    public WorkingCapital(final WorkingCapitalYear workingCapitalYear, final Unit unit, final Person movementResponsible) {
	this();
	if (find(workingCapitalYear, unit) != null) {
	    throw new DomainException("message.working.capital.exists.for.year.and.unit");
	}
	setWorkingCapitalYear(workingCapitalYear);
	setUnit(unit);
	if (movementResponsible == null) {
	    throw new DomainException("message.working.capital.movementResponsible.cannot.be.null");
	}
	setMovementResponsible(movementResponsible);
	new WorkingCapitalProcess(this);
    }

    public WorkingCapital(final Integer year, final Unit unit, final Person movementResponsible) {
	this(WorkingCapitalYear.findOrCreate(year), unit, movementResponsible);
    }

    public SortedSet<WorkingCapitalInitialization> getSortedWorkingCapitalInitializations() {
	final SortedSet<WorkingCapitalInitialization> result = new TreeSet<WorkingCapitalInitialization>(WorkingCapitalInitialization.COMPARATOR_BY_REQUEST_CREATION);
	result.addAll(getWorkingCapitalInitializationsSet());
	return result;
    }

    public Authorization findUnitResponsible(final Person person, final Money amount) {
	final Unit unit = getUnit();
	return findUnitResponsible(person, amount, unit);
    }

    private Authorization findUnitResponsible(final Person person, final Money amount, final Unit unit) {
	if (unit != null && person != null) {
//	    boolean hasAtLeastOneResponsible = false;
	    for (final Authorization authorization : unit.getAuthorizationsSet()) {
		if (authorization.isValid() && authorization.getMaxAmount().isGreaterThanOrEqual(amount)) {
//		    hasAtLeastOneResponsible = true;
		    if (authorization.getPerson().getUser() == person.getUser()) {
			return authorization;
		    }
		}
	    }
//	    if (!hasAtLeastOneResponsible) {
		final Unit parent = unit.getParentUnit();
		return findUnitResponsible(person, amount, parent);
//	    }
	}
	return null; 
    }

    public boolean isPendingAproval() {
	final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
	return workingCapitalInitialization != null && workingCapitalInitialization.isPendingAproval();
    }

    public boolean isPendingAproval(final User user) {
	final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
	return workingCapitalInitialization != null && workingCapitalInitialization.isPendingAproval(user);
    }

    public boolean isPendingVerification(final User user) {
	if (isAccountingResponsible(user)) {
	    final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
	    return workingCapitalInitialization != null && workingCapitalInitialization.isPendingVerification();
	}
	return false;
    }

    public boolean isPendingAuthorization(User user) {
	final WorkingCapitalSystem workingCapitalSystem = WorkingCapitalSystem.getInstance(); 
	if (workingCapitalSystem.isManagementeMember(user)) {
	    final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
	    return workingCapitalInitialization != null && workingCapitalInitialization.isPendingAuthorization();
	}
	return false;
    }

    public boolean isAvailable(final User user) {
	if (user == null) {
	    return false;
	}
	final WorkingCapitalSystem workingCapitalSystem = WorkingCapitalSystem.getInstance();
	if (user == getMovementResponsible().getUser()
		|| isAccountingResponsible(user)
		|| isAccountingEmployee(user)
		|| workingCapitalSystem.isManagementeMember(user)
		|| isTreasuryMember(user)
		|| findUnitResponsible(user.getPerson(), Money.ZERO) != null) {
	    return true;
	}
	return isRequester(user);
    }

    public User getRequester() {
	final WorkingCapitalInitialization workingCapitalInitialization = Collections.min(getWorkingCapitalInitializationsSet(), WorkingCapitalInitialization.COMPARATOR_BY_REQUEST_CREATION);
	return workingCapitalInitialization.getRequestor().getUser();
    }

    public boolean isRequester(final User user) {
	for (final WorkingCapitalInitialization workingCapitalInitialization : getWorkingCapitalInitializationsSet()) {
	    if (user == workingCapitalInitialization.getRequestor().getUser()) {
		return true;
	    }
	}
	return false;
    }

    public boolean isCanceledOrRejected() {
	final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
	return workingCapitalInitialization != null && workingCapitalInitialization.isCanceledOrRejected();
    }

    public WorkingCapitalInitialization getWorkingCapitalInitialization() {
	return Collections.max(getWorkingCapitalInitializationsSet(), WorkingCapitalInitialization.COMPARATOR_BY_REQUEST_CREATION);
    }

    public boolean hasAnyPendingWorkingCapitalRequests() {
	for (final WorkingCapitalRequest workingCapitalRequest : getWorkingCapitalRequestsSet()) {
	    if (!workingCapitalRequest.isRequestProcessedByTreasury()) {
		return true;
	    }
	}
	return false;
    }

    public boolean isTreasuryMember(final User user) {
	final Unit unit = getUnit();
	return unit.isTreasuryMember(user.getExpenditurePerson());
    }

    public Money getAvailableCapital() {
	Money result = Money.ZERO;
	for (final WorkingCapitalRequest workingCapitalRequest : getWorkingCapitalRequestsSet()) {
	    if (workingCapitalRequest.isRequestProcessedByTreasury()) {
		result = result.add(workingCapitalRequest.getRequestedValue());
	    }
	}
	return result;
    }

    public WorkingCapitalTransaction getLastTransaction() {
	final Set<WorkingCapitalTransaction> workingCapitalTransactionsSet = getWorkingCapitalTransactionsSet();
	return workingCapitalTransactionsSet.isEmpty() ? null : Collections.max(workingCapitalTransactionsSet, WorkingCapitalTransaction.COMPARATOR_BY_NUMBER);
    }

    public SortedSet<WorkingCapitalTransaction> getSortedWorkingCapitalTransactions() {
	final SortedSet<WorkingCapitalTransaction> result = new TreeSet<WorkingCapitalTransaction>(WorkingCapitalTransaction.COMPARATOR_BY_NUMBER);
	result.addAll(getWorkingCapitalTransactionsSet());
	return result;
    }

    public Money getBalance() {
	final WorkingCapitalTransaction workingCapitalTransaction = getLastTransaction();
	return workingCapitalTransaction == null ? Money.ZERO : workingCapitalTransaction.getBalance();
    }

    public boolean hasAcquisitionPendingApproval() {
	for (final WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
	    final WorkingCapitalTransaction workingCapitalTransaction = workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
	    if (workingCapitalTransaction.isPendingApproval()) {
		return true;
	    }	    
	}
	return false;
    }

    public boolean hasAcquisitionPendingApproval(final User user) {
	final Money valueForAuthorization = Money.ZERO;
	return hasAcquisitionPendingApproval() && findUnitResponsible(user.getPerson(), valueForAuthorization) != null;
    }

    public boolean hasAcquisitionPendingVerification() {
	for (final WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
	    final WorkingCapitalTransaction workingCapitalTransaction = workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
	    if (workingCapitalTransaction.isPendingVerification()) {
		return true;
	    }	    
	}
	return false;
    }

    public boolean hasAcquisitionPendingSubmission() {
	for (final WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
	    final WorkingCapitalTransaction workingCapitalTransaction = workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
	    if (workingCapitalTransaction.isPendingSubmission()) {
		return true;
	    }	    
	}
	return false;
    }

    public boolean hasAcquisitionPendingVerification(final User user) {
	return hasAcquisitionPendingVerification() && isAccountingEmployee(user);
    }

    private boolean hasVerifiedAcquisition() {
	for (final WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
	    final WorkingCapitalTransaction workingCapitalTransaction = workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
	    if (workingCapitalTransaction.isVerified()) {
		return true;
	    }	    
	}
	return false;
    }

    public boolean hasVerifiedAcquisition(User user) {
	return hasVerifiedAcquisition() && isAccountingEmployee(user);
    }

    public boolean hasAllPaymentsRequested() {
	if (!hasAnyWorkingCapitalTransactions()) {
	    return false;
	}
	for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
	    if (workingCapitalTransaction.isAcquisition() && !workingCapitalTransaction.isPaymentRequested()) {
		return false;
	    }
	}
	return true;
    }

    public boolean isMovementResponsible(final User user) {
	return hasMovementResponsible() && getMovementResponsible().getUser() == user;
    }

    public boolean hasApprovedAndUnSubmittedAcquisitions() {
	for (final WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
	    final WorkingCapitalAcquisitionTransaction workingCapitalAcquisitionTransaction = workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
	    if (workingCapitalAcquisitionTransaction.isApproved() && workingCapitalAcquisition.getSubmitedForVerification() == null) {
		return true;
	    }
	}
	return false;
    }

    public void submitAcquisitionsForValidation() {
	final DateTime now = new DateTime();
	for (final WorkingCapitalAcquisition workingCapitalAcquisition : getWorkingCapitalAcquisitionsSet()) {
	    final WorkingCapitalAcquisitionTransaction workingCapitalAcquisitionTransaction = workingCapitalAcquisition.getWorkingCapitalAcquisitionTransaction();
	    if (workingCapitalAcquisitionTransaction.isApproved() && workingCapitalAcquisition.getSubmitedForVerification() == null) {
		workingCapitalAcquisition.setSubmitedForVerification(now);
	    }
	}
    }

    public boolean canRequestCapital() {
	final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
	return workingCapitalInitialization != null
		&& !isCanceledOrRejected()
		&& workingCapitalInitialization.getLastSubmission() == null
		&& workingCapitalInitialization.isAuthorized()
		&& !hasAnyPendingWorkingCapitalRequests()
		&& hasCapitalPendingRequest();
    }

    public boolean canRequestCapitalRefund() {
	final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
	return workingCapitalInitialization != null
		&& !isCanceledOrRejected()
		&& workingCapitalInitialization.getLastSubmission() != null
		&& workingCapitalInitialization.getRefundRequested() == null
		&& workingCapitalInitialization.isAuthorized()
		&& !hasAnyPendingWorkingCapitalRequests()
		&& hasCapitalPendingRequest();
    }

    private boolean hasCapitalPendingRequest() {
	final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
	final WorkingCapitalTransaction lastWorkingCapitalTransaction = getLastTransaction();
	if ((lastWorkingCapitalTransaction == null && workingCapitalInitialization.getAuthorizedAnualValue().isPositive())
		|| (lastWorkingCapitalTransaction != null
			&& lastWorkingCapitalTransaction.getDebt().isLessThan(workingCapitalInitialization.getAuthorizedAnualValue()))) {
	    return true;
	}

	boolean hasSomeAcquisition = false;

	DateTime lastPayment = null;
	for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
	    if (workingCapitalTransaction.isPayment() && (lastPayment == null || lastPayment.isBefore(workingCapitalTransaction.getTransationInstant()))) {
		lastPayment = workingCapitalTransaction.getTransationInstant();
	    }
	}

	for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
	    if (workingCapitalTransaction.isAcquisition() && workingCapitalTransaction.getTransationInstant().isAfter(lastPayment)) {
		final WorkingCapitalAcquisitionTransaction workingCapitalAcquisitionTransaction = (WorkingCapitalAcquisitionTransaction) workingCapitalTransaction;
		final WorkingCapitalAcquisition workingCapitalAcquisition = workingCapitalAcquisitionTransaction.getWorkingCapitalAcquisition();
		if (workingCapitalAcquisition.getSubmitedForVerification() != null) {
		    if (workingCapitalTransaction.isVerified() && !workingCapitalAcquisition.isCanceledOrRejected()) {
			hasSomeAcquisition = true;
		    }
		}
	    }
	}
	return hasSomeAcquisition && lastWorkingCapitalTransaction.getAccumulatedValue().isPositive();
    }

    public boolean hasWorkingCapitalRequestPendingTreasuryProcessing() {
	for (final WorkingCapitalRequest workingCapitalRequest : getWorkingCapitalRequestsSet()) {
	    if (workingCapitalRequest.getProcessedByTreasury() == null) {
		return true;
	    }
	}
	return false;
    }

    public boolean isPendingAcceptResponsability() {
	for (final WorkingCapitalInitialization workingCapitalInitialization : getWorkingCapitalInitializationsSet()) {
	    if (!workingCapitalInitialization.isCanceledOrRejected() && workingCapitalInitialization.getAcceptedResponsability() == null) {
		return true;
	    }
	}
	return false;
    }

    public boolean canRequestValue(final Money requestedValue) {
	final WorkingCapitalInitialization workingCapitalInitialization = getWorkingCapitalInitialization();
	if (workingCapitalInitialization != null && !workingCapitalInitialization.isCanceledOrRejected()
		&& workingCapitalInitialization.isAuthorized()) {
	    final Money maxAuthorizedAnualValue = workingCapitalInitialization.getMaxAuthorizedAnualValue();
	    final Money allocatedValue = calculateAllocatedValue();
	    if (maxAuthorizedAnualValue.isGreaterThanOrEqual(allocatedValue)) {
		return true;
	    }
	}
	return false;
    }

    private Money calculateAllocatedValue() {
	Money result = Money.ZERO;
	for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
	    if (!workingCapitalTransaction.isCanceledOrRejected()) {
		if (workingCapitalTransaction.isPayment()) {
		    final WorkingCapitalPayment workingCapitalPayment = (WorkingCapitalPayment) workingCapitalTransaction;
		    final WorkingCapitalRequest workingCapitalRequest = workingCapitalPayment.getWorkingCapitalRequest();
		    final Money requestedValue = workingCapitalRequest.getRequestedValue();
		    result = result.add(requestedValue);
		} else if (workingCapitalTransaction.isRefund()) {
		    final WorkingCapitalRefund workingCapitalRefund = (WorkingCapitalRefund) workingCapitalTransaction;
		    final Money refundedValue = workingCapitalRefund.getRefundedValue();
		    result = result.subtract(refundedValue);
		}
	    }
	}
	return result;
    }

    public boolean isAccountingResponsible(final User user) {
	final Unit unit = getUnit();
	return user != null && unit != null && unit.isAccountingResponsible(user.getExpenditurePerson());
    }

    public boolean isAccountingEmployee(final User user) {
	final Unit unit = getUnit();
	if (unit != null && user != null) {
	    if (unit instanceof Project || unit instanceof SubProject) {
		unit.isProjectAccountingEmployee(user.getExpenditurePerson());
	    }
	    return unit.isAccountingEmployee(user.getExpenditurePerson());
	}
	return false;
    }

    public Money getPossibaySpent() {
	Money result = Money.ZERO;
	for (final WorkingCapitalTransaction workingCapitalTransaction : getWorkingCapitalTransactionsSet()) {
	    if (workingCapitalTransaction.isPayment()) {
		result = result.add(workingCapitalTransaction.getValue());
	    }
	    if (workingCapitalTransaction.isRefund()) {
		result = result.subtract(workingCapitalTransaction.getValue());
	    }
	}
	return result;
    }

}
