package module.workingCapital.domain;

import java.util.Comparator;

import jvstm.cps.ConsistencyPredicate;
import module.workingCapital.domain.util.WorkingCapitalConsistencyException;
import myorg.domain.User;
import myorg.domain.util.Money;

import org.apache.commons.lang.NotImplementedException;
import org.joda.time.DateTime;

public class WorkingCapitalTransaction extends WorkingCapitalTransaction_Base {

    public static Comparator<WorkingCapitalTransaction> COMPARATOR_BY_NUMBER = new Comparator<WorkingCapitalTransaction>() {

	@Override
	public int compare(final WorkingCapitalTransaction o1, final WorkingCapitalTransaction o2) {
	    int c = o1.getNumber().compareTo(o2.getNumber());
	    return c == 0 ? o2.getExternalId().compareTo(o1.getExternalId()) : c;
	}

    };

    public WorkingCapitalTransaction() {
	super();
	setWorkingCapitalSystem(WorkingCapitalSystem.getInstance());
	setTransationInstant(new DateTime());
    }

    @ConsistencyPredicate(WorkingCapitalConsistencyException.class)
    public boolean checkBalancePositive() {
	return !getBalance().isNegative();
    }

    @ConsistencyPredicate(WorkingCapitalConsistencyException.class)
    public boolean checkBalanceEqualsDebt() {
	return getBalance().equals(getDebt());
    }

    @Override
    public void setWorkingCapital(final WorkingCapital workingCapital) {
	final WorkingCapitalTransaction workingCapitalTransaction = workingCapital.getLastTransaction();
	int count = workingCapital.getWorkingCapitalTransactionsCount();
	super.setWorkingCapital(workingCapital);
	setNumber(Integer.valueOf(count + 1));
	setValue(Money.ZERO);
	if (workingCapitalTransaction == null) {
	    setAccumulatedValue(Money.ZERO);
	    setBalance(Money.ZERO);
	    setDebt(Money.ZERO);
	} else {
	    setAccumulatedValue(workingCapitalTransaction.getAccumulatedValue());
	    setBalance(workingCapitalTransaction.getBalance());
	    setDebt(workingCapitalTransaction.getDebt());
	}
    }

    public void addDebt(final Money value) {
	setBalance(getBalance().add(value));
	setDebt(getDebt().add(value));
    }

    public void restoreDebt(final Money debtValue, final Money accumulatedValue) {
	setAccumulatedValue(getAccumulatedValue().subtract(accumulatedValue));
	setBalance(getBalance().add(debtValue));
	setDebt(getDebt().add(debtValue));
    }

    public void addValue(final Money value) {
	setValue(getValue().add(value));
	setAccumulatedValue(getAccumulatedValue().add(value));
	setBalance(getBalance().subtract(value));
	setDebt(getDebt().subtract(value));
    }

    public void resetValue(final Money value) {
	final Money diffValue = value.subtract(getValue());
	setValue(value);
	restoreDebtOfFollowingTransactions(diffValue.multiply(-1), diffValue.multiply(-1));
    }

    public String getDescription() {
	return "";
    }

    public boolean isPayment() {
	return false;
    }

    public boolean isSubmission() {
	return false;
    }

    public boolean isAcquisition() {
	return false;
    }

    public boolean isRefund() {
	return false;
    }

    public boolean isLastTransaction() {
	final WorkingCapital workingCapital = getWorkingCapital();
	return workingCapital.getLastTransaction() == this;
    }

    public boolean isPendingApproval() {
	return false;
    }

    public boolean isApproved() {
	return false;
    }

    public void approve(final User user) {
    }

    public WorkingCapitalTransaction getNext() {
	final int current = getNumber().intValue();
	final WorkingCapital workingCapital = getWorkingCapital();
	for (final WorkingCapitalTransaction workingCapitalTransaction : workingCapital.getWorkingCapitalTransactionsSet()) {
	    if (workingCapitalTransaction.getNumber().intValue() == current + 1) {
		return workingCapitalTransaction;
	    }
	}
	return null;
    }

    protected void restoreDebtOfFollowingTransactions(final Money debtValue, final Money accumulatedValue) {
	restoreDebt(debtValue, accumulatedValue);
	final WorkingCapitalTransaction workingCapitalTransaction = getNext();
	if (workingCapitalTransaction != null) {
	    workingCapitalTransaction.restoreDebtOfFollowingTransactions(debtValue, accumulatedValue);
	}
    }

    protected void restoreDebtOfFollowingTransactions() {
	restoreDebtOfFollowingTransactions(getValue(), getValue());
    }

    public void reject(User loggedPerson) {
	restoreDebtOfFollowingTransactions();
    }

    public boolean isPendingVerification() {
	return false;
    }

    public boolean isVerified() {
	return false;
    }

    public void verify(final User user) {
    }

    public void rejectVerify(User loggedPerson) {
	restoreDebtOfFollowingTransactions();
    }

    public void unVerify() {
    }

    public void unApprove() {
    }

    public boolean isPaymentRequested() {
	return false;
    }

    public boolean isCanceledOrRejected() {
	return false;
    }

    public void cancel() {
	throw new NotImplementedException();
    }

    public boolean isPendingSubmission() {
	return false;
    }

    public WorkingCapitalTransaction getPreviousTransaction() {
	final WorkingCapital workingCapital = getWorkingCapital();
	WorkingCapitalTransaction previous = null;
	for (final WorkingCapitalTransaction workingCapitalTransaction : workingCapital.getWorkingCapitalTransactionsSet()) {
	    final DateTime transationInstant = workingCapitalTransaction.getTransationInstant();
	    if (transationInstant.isBefore(getTransationInstant())
		    && (previous == null || previous.getTransationInstant().isBefore(transationInstant))) {
		previous = workingCapitalTransaction;
	    }
	}
	return previous;
    }

}
