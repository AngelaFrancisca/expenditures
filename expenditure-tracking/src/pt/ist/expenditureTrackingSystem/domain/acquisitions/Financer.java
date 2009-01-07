package pt.ist.expenditureTrackingSystem.domain.acquisitions;

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import pt.ist.expenditureTrackingSystem.domain.DomainException;
import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;
import pt.ist.expenditureTrackingSystem.domain.util.Money;
import pt.ist.fenixframework.pstm.Transaction;
import pt.utl.ist.fenix.tools.util.Strings;
import pt.utl.ist.fenix.tools.util.i18n.Language;

public class Financer extends Financer_Base {

    protected Financer() {
	super();
	setOjbConcreteClass(getClass().getName());
	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
    }

    public Financer(final Acquisition acquisitionRequest, final CostCenter costCenter) {
	this();
	if (acquisitionRequest == null || costCenter == null) {
	    throw new DomainException("error.financer.wrong.initial.arguments");
	}
	if (acquisitionRequest.hasPayingUnit(costCenter)) {
	    throw new DomainException("error.financer.acquisition.request.already.has.paying.unit");
	}

	setFundedRequest(acquisitionRequest);
	setUnit(costCenter);
	setAccountingUnit(costCenter.getAccountingUnit());
    }

    public boolean isProjectFinancer() {
	return false;
    }

    public void delete() {
	if (checkIfCanDelete()) {
	    removeExpenditureTrackingSystem();
	    removeFundedRequest();
	    removeUnit();
	    Transaction.deleteObject(this);
	}
    }

    private boolean checkIfCanDelete() {
	if (hasAnyUnitItems()) {
	    throw new DomainException("acquisitionProcess.message.exception.cannotRemovePayingUnit.alreadyAssignedToItems");
	}
	return true;
    }

    public Money getAmountAllocated() {
	Money amount = Money.ZERO;
	for (UnitItem unitItem : getUnitItems()) {
	    if (unitItem.getRoundedRealShareValue() != null) {
		amount = amount.add(unitItem.getRoundedRealShareValue());
	    } else if (unitItem.getRoundedShareValue() != null) {
		amount = amount.add(unitItem.getRoundedShareValue());
	    }
	}
	return amount;
    }

    public Money getRealShareValue() {
	Money amount = Money.ZERO;
	for (UnitItem unitItem : getUnitItemsSet()) {
	    amount = amount.addAndRound(unitItem.getRealShareValue());
	}
	return amount;
    }

    public Money getShareValue() {
	Money amount = Money.ZERO;
	for (UnitItem unitItem : getUnitItemsSet()) {
	    amount = amount.addAndRound(unitItem.getShareValue());
	}
	return amount;
    }

    public boolean isRealUnitShareValueLessThanUnitShareValue() {
	return getRealShareValue().isLessThanOrEqual(getShareValue());
    }

    public boolean isAccountingEmployee(final Person person) {
	return getAccountingUnit().hasPeople(person);
    }

    public boolean isProjectAccountingEmployee(Person person) {
	return getUnit().isProjectAccountingEmployee(person);
    }

    protected String getAllocationIds(final String id, final String key) {
	final ResourceBundle resourceBundle = ResourceBundle.getBundle("resources.AcquisitionResources", Language.getLocale());
	final StringBuilder stringBuilder = new StringBuilder();
	stringBuilder.append('[');
	stringBuilder.append(resourceBundle.getObject(key));
	stringBuilder.append(' ');
	stringBuilder.append(id == null || id.isEmpty() ? "-" : id);
	stringBuilder.append(']');
	return stringBuilder.toString();
    }

    public String getFundAllocationIds() {
	return getAllocationIds(getFundAllocationId(), "financer.label.allocation.id.prefix.giaf");
    }

    public String getEffectiveFundAllocationIds() {
	Strings strings = getEffectiveFundAllocationId();
	if (strings != null && !strings.isEmpty()) {
	    StringBuilder buffer = new StringBuilder("");

	    for (String allocationId : strings) {
		buffer.append(getAllocationIds(allocationId, "financer.label.allocation.id.prefix.giaf"));
		buffer.append(' ');
	    }
	    return buffer.toString();
	}
	return getAllocationIds(null, "financer.label.allocation.id.prefix.giaf");
    }

    public boolean hasAllocatedFundsForAllProject() {
	return true;
    }

    public boolean hasAllocatedFundsPermanentlyForAllProjectFinancers() {
	return true;
    }

    public void addEffectiveFundAllocationId(String effectiveFundAllocationId) {
	if (StringUtils.isEmpty(effectiveFundAllocationId)) {
	    throw new DomainException("acquisitionProcess.message.exception.effectiveFundAllocationCannotBeNull");
	}
	Strings strings = getEffectiveFundAllocationId();
	if (strings == null) {
	    strings = new Strings(effectiveFundAllocationId);
	    setEffectiveFundAllocationId(strings);
	} else {
	    strings.add(effectiveFundAllocationId);
	}
    }

    public CostCenter getFinancerCostCenter() {
	return getUnit() != null ? getUnit().getCostCenterUnit() : null;
    }

    public Set<AccountingUnit> getCostCenterAccountingUnits() {
	Set<AccountingUnit> res = new HashSet<AccountingUnit>();
	res.add(getFinancerCostCenter().getAccountingUnit());
	res.add(AccountingUnit.readAccountingUnitByUnitName("10"));
	return res;
    }

    public boolean isAccountingEmployeeForOnePossibleUnit(Person person) {
	for (AccountingUnit accountingUnit : getCostCenterAccountingUnits()) {
	    if (accountingUnit.hasPeople(person)) {
		return true;
	    }
	}
	return false;
    }

    public boolean hasFundAllocationId() {
	return getFundAllocationId() != null;
    }

    public boolean hasAnyFundsAllocated() {
	return hasAllocatedFundsForAllProject() && hasFundAllocationId();
    }

}
