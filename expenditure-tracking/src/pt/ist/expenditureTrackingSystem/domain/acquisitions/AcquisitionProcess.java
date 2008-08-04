package pt.ist.expenditureTrackingSystem.domain.acquisitions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pt.ist.expenditureTrackingSystem.applicationTier.Authenticate.User;
import pt.ist.expenditureTrackingSystem.domain.DomainException;
import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
import pt.ist.expenditureTrackingSystem.domain.RoleType;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AbstractActivity;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AddAcquisitionProposalDocument;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AllocateFundsPermanently;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.ApproveAcquisitionProcess;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.CreateAcquisitionRequest;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.CreateAcquisitionRequestItem;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.DeleteAcquisitionProcess;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.FundAllocation;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.FundAllocationExpirationDate;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.PayAcquisition;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.ReceiveInvoice;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.SubmitForApproval;
import pt.ist.expenditureTrackingSystem.domain.authorizations.Authorization;
import pt.ist.expenditureTrackingSystem.domain.dto.CreateAcquisitionProcessBean;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;
import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
import pt.ist.fenixWebFramework.security.UserView;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixframework.pstm.Transaction;

public class AcquisitionProcess extends AcquisitionProcess_Base {

    private static List<GenericAcquisitionProcessActivity> activities;

    static {
	activities = new ArrayList<GenericAcquisitionProcessActivity>();
	activities.add(new AddAcquisitionProposalDocument());
	activities.add(new AllocateFundsPermanently());
	activities.add(new ApproveAcquisitionProcess());
	activities.add(new CreateAcquisitionRequest());
	activities.add(new CreateAcquisitionRequestItem());
	activities.add(new DeleteAcquisitionProcess());
	activities.add(new FundAllocation());
	activities.add(new FundAllocationExpirationDate());
	activities.add(new PayAcquisition());
	activities.add(new ReceiveInvoice());
	activities.add(new SubmitForApproval());
    }

    protected AcquisitionProcess() {
	super();
	this.setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
	new AcquisitionProcessState(this, AcquisitionProcessStateType.IN_GENESIS);
	new AcquisitionRequest(this);
    }

    protected AcquisitionProcess(String fiscalCode, String costCenter, String project, String subproject, String recipient,
	    String receptionAddress) {
	super();
	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
	new AcquisitionProcessState(this, AcquisitionProcessStateType.IN_GENESIS);
	new AcquisitionRequest(this, fiscalCode, costCenter, project, subproject, recipient, receptionAddress);
    }

    public static boolean isCreateNewAcquisitionProcessAvailable() {
	return UserView.getUser() != null;
    }

    @Service
    public static AcquisitionProcess createNewAcquisitionProcess(final CreateAcquisitionProcessBean createAcquisitionProcessBean) {
	if (!isCreateNewAcquisitionProcessAvailable()) {
	    throw new DomainException("error.acquisitionProcess.invalid.state.to.run.createNewAcquisitionProcess");
	}
	return new AcquisitionProcess(createAcquisitionProcessBean.getFiscalIdentificationCode(), createAcquisitionProcessBean
		.getCostCenter(), createAcquisitionProcessBean.getProject(), createAcquisitionProcessBean.getSubproject(),
		createAcquisitionProcessBean.getRecipient(), createAcquisitionProcessBean.getReceptionAddress());
    }

    public Person getRequestor() {
	return getAcquisitionRequest().getRequester();
    }

    public boolean isResponsibleForUnit() {
	User user = UserView.getUser();
	if (user == null) {
	    return false;
	}

	String costCenter = getAcquisitionRequest().getCostCenter();

	for (Authorization authorization : user.getPerson().getAuthorizations()) {
	    if (authorization.getUnit().getCostCenter().equals((costCenter))) {
		return true;
	    }
	}
	return false;

    }

    public void delete() {
	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
	acquisitionRequest.delete();
	removeExpenditureTrackingSystem();
	Transaction.deleteObject(this);
    }

    public boolean isEditRequestItemAvailable() {
	User user = UserView.getUser();
	return user != null && user.getPerson().equals(getRequestor())
		&& isProcessInState(AcquisitionProcessStateType.IN_GENESIS);
    }

    public boolean isPendingApproval() {
	return isProcessInState(AcquisitionProcessStateType.SUBMITTED_FOR_APPROVAL);
    }

    public boolean isProcessInState(AcquisitionProcessStateType state) {
	return getLastAcquisitionProcessStateType().equals(state);
    }

    protected AcquisitionProcessState getLastAcquisitionProcessState() {
	return Collections.max(getAcquisitionProcessStates(), AcquisitionProcessState.COMPARATOR_BY_WHEN);
    }

    protected AcquisitionProcessStateType getLastAcquisitionProcessStateType() {
	return getLastAcquisitionProcessState().getAcquisitionProcessStateType();
    }

    public AcquisitionProcessState getAcquisitionProcessState() {
	return getLastAcquisitionProcessState();
    }

    public AcquisitionProcessStateType getAcquisitionProcessStateType() {
	return getLastAcquisitionProcessStateType();
    }

    public List<AbstractActivity<AcquisitionProcess>> getActiveActivities() {
	List<AbstractActivity<AcquisitionProcess>> activitiesResult = new ArrayList<AbstractActivity<AcquisitionProcess>>();

	for (AbstractActivity<AcquisitionProcess> activity : activities) {
	    if (activity.isActive(this)) {
		activitiesResult.add(activity);
	    }
	}

	return activitiesResult;
    }

    public boolean isPersonAbleToExecuteActivities() {
	for (AbstractActivity<AcquisitionProcess> activity : activities) {
	    if (activity.isActive(this)) {
		return true;
	    }
	}
	return false;
    }

    public boolean isAcquisitionProcessed() {
	return isProcessInState(AcquisitionProcessStateType.ACQUISITION_PROCESSED);
    }

    public boolean isInvoiceReceived() {
	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
	return isProcessInState(AcquisitionProcessStateType.INVOICE_RECEIVED) && acquisitionRequest.isInvoiceReceived();
    }

    public Unit getUnit() {
	final AcquisitionRequest acquisitionRequest = getAcquisitionRequest();
	return Unit.findUnitByCostCenter(acquisitionRequest.getCostCenter());
    }

    public boolean isAllowedToViewCostCenterExpenditures() {
	try {
	    return getUnit() != null && isResponsibleForUnit() || userHasRole(RoleType.ACCOUNTABILITY);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new Error(e);
	}
    }

    protected boolean userHasRole(final RoleType roleType) {
	final User user = UserView.getUser();
	return user != null && user.getPerson().hasRoleType(roleType);
    }

    public boolean isAllowedToViewSupplierExpenditures() {
	return userHasRole(RoleType.ACQUISITION_CENTRAL);
    }

    public GenericAcquisitionProcessActivity getActivityByName(String activityName) {
	for (GenericAcquisitionProcessActivity activity : activities) {
	    if (activity.getName().equals(activityName)) {
		return activity;
	    }
	}
	return null;
    }

    public List<OperationLog> getOperationLogsInState(AcquisitionProcessStateType state) {
	List<OperationLog> logs = new ArrayList<OperationLog>();
	for (OperationLog log : getOperationLogsSet()) {
	    if (log.getState() == state) {
		logs.add(log);
	    }
	}
	return logs;
    }
}
