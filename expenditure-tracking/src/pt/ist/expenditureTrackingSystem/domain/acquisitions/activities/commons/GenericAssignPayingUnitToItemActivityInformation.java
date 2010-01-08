package pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.commons;

import java.util.ArrayList;
import java.util.List;

import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.RequestItem;
import pt.ist.expenditureTrackingSystem.domain.dto.UnitItemBean;
import pt.ist.expenditureTrackingSystem.domain.organization.Unit;

public class GenericAssignPayingUnitToItemActivityInformation<P extends PaymentProcess> extends ActivityInformation<P> {

    protected RequestItem item;
    protected List<UnitItemBean> beans;

    public GenericAssignPayingUnitToItemActivityInformation(P process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
	this.beans = new ArrayList<UnitItemBean>();
    }

    public RequestItem getItem() {
	return item;
    }

    public void setItem(RequestItem item) {
	this.item = item;
	for (Unit unit : getProcess().getPayingUnits()) {
	    this.beans.add(new UnitItemBean(unit, item));
	}
    }

    public List<UnitItemBean> getBeans() {
	return beans;
    }

    public void setBeans(List<UnitItemBean> beans) {
	this.beans = beans;
    }

    @Override
    public boolean hasAllneededInfo() {
	return getItem() != null && isForwardedFromInput();
    }
}
