package pt.ist.expenditureTrackingSystem.domain.acquisitions;

import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import myorg.domain.util.Money;

import org.apache.commons.lang.StringUtils;

import pt.ist.expenditureTrackingSystem.domain.DomainException;
import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
import pt.ist.expenditureTrackingSystem.domain.organization.CostCenter;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;
import pt.utl.ist.fenix.tools.util.Strings;
import pt.utl.ist.fenix.tools.util.i18n.Language;

public class Financer extends Financer_Base {

    protected Financer() {
	super();
	setOjbConcreteClass(getClass().getName());
	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
    }

    public Financer(final RequestWithPayment acquisitionRequest, final CostCenter costCenter) {
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
	    removeAccountingUnit();
	    getAllocatedInvoices().clear();
	    deleteDomainObject();
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
	    if (unitItem.getRealShareValue() != null) {
		amount = amount.addAndRound(unitItem.getRealShareValue());
	    }
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
	return false;
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
	}
	if (!strings.contains(effectiveFundAllocationId)) {
	    strings.add(effectiveFundAllocationId);
	}
	setEffectiveFundAllocationId(strings);

	allocateInvoices();

    }

    private void allocateInvoices() {
	getAllocatedInvoices().clear();
	Set<PaymentProcessInvoice> invoices = new HashSet<PaymentProcessInvoice>();
	for (UnitItem unitItem : getUnitItems()) {
	    invoices.addAll(unitItem.getConfirmedInvoices());
	}
	getAllocatedInvoices().addAll(invoices);
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

    public boolean hasEffectiveFundAllocationId() {
	return getEffectiveFundAllocationId() != null;
    }

    public boolean hasAnyFundsAllocated() {
	return hasAllocatedFundsForAllProject() && hasFundAllocationId();
    }

    public boolean isTreasuryMember(Person person) {
	return getUnit().isTreasuryMember(person);
    }

    public boolean isProjectAccountingEmployeeForOnePossibleUnit(Person person) {
	return false;
    }

    public boolean hasAllInvoicesAllocated() {
	List<PaymentProcessInvoice> allocatedInvoices = getAllocatedInvoices();
	for (UnitItem unitItem : getUnitItems()) {
	    if (!allocatedInvoices.containsAll(unitItem.getConfirmedInvoices())) {
		return false;
	    }
	}
	return true;
    }

    public void resetEffectiveFundAllocationId() {
	setEffectiveFundAllocationId(null);
	getAllocatedInvoices().clear();
    }

    public boolean isApproved() {
	List<UnitItem> unitItems = getUnitItems();
	for (UnitItem unitItem : unitItems) {
	    if (!unitItem.isApproved()) {
		return false;
	    }
	}
	return !unitItems.isEmpty();
    }

    public boolean isAuthorized() {
	List<UnitItem> unitItems = getUnitItems();
	for (UnitItem unitItem : unitItems) {
	    if (!unitItem.getItemAuthorized()) {
		return false;
	    }
	}
	return !unitItems.isEmpty();
    }

    public boolean isWithInvoicesConfirmed() {
	List<UnitItem> unitItems = getUnitItems();
	for (UnitItem unitItem : unitItems) {
	    if (!unitItem.isWithAllInvoicesConfirmed()) {
		return false;
	    }
	}
	return !unitItems.isEmpty();
    }

    public boolean isFundAllocationPresent() {
	return getFundAllocationId() != null;
    }

    public boolean isEffectiveFundAllocationPresent() {
	return getEffectiveFundAllocationId() != null;
    }
}
