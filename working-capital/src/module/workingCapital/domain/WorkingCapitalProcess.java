package module.workingCapital.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.ProcessFile;
import module.workflow.domain.WorkflowProcess;
import module.workingCapital.domain.activity.ApproveActivity;
import module.workingCapital.domain.activity.ApproveWorkingCapitalAcquisitionActivity;
import module.workingCapital.domain.activity.AuthorizeActivity;
import module.workingCapital.domain.activity.CancelWorkingCapitalAcquisitionActivity;
import module.workingCapital.domain.activity.CancelWorkingCapitalInitializationActivity;
import module.workingCapital.domain.activity.EditWorkingCapitalActivity;
import module.workingCapital.domain.activity.PayCapitalActivity;
import module.workingCapital.domain.activity.RegisterWorkingCapitalAcquisitionActivity;
import module.workingCapital.domain.activity.RejectVerifyWorkingCapitalAcquisitionActivity;
import module.workingCapital.domain.activity.RejectWorkingCapitalAcquisitionActivity;
import module.workingCapital.domain.activity.RejectWorkingCapitalInitializationActivity;
import module.workingCapital.domain.activity.RequestCapitalActivity;
import module.workingCapital.domain.activity.SubmitForValidationActivity;
import module.workingCapital.domain.activity.UnApproveActivity;
import module.workingCapital.domain.activity.UnApproveWorkingCapitalAcquisitionActivity;
import module.workingCapital.domain.activity.UnAuthorizeActivity;
import module.workingCapital.domain.activity.UnVerifyActivity;
import module.workingCapital.domain.activity.UnVerifyWorkingCapitalAcquisitionActivity;
import module.workingCapital.domain.activity.UndoCancelOrRejectWorkingCapitalInitializationActivity;
import module.workingCapital.domain.activity.VerifyActivity;
import module.workingCapital.domain.activity.VerifyWorkingCapitalAcquisitionActivity;
import myorg.domain.User;

public class WorkingCapitalProcess extends WorkingCapitalProcess_Base {

    public static final Comparator<WorkingCapitalProcess> COMPARATOR_BY_UNIT_NAME = new Comparator<WorkingCapitalProcess>() {
	@Override
	public int compare(WorkingCapitalProcess o1, WorkingCapitalProcess o2) {
	    final int c = o1.getWorkingCapital().getUnit().getName().compareTo(o2.getWorkingCapital().getUnit().getName());
	    return c == 0 ? o2.hashCode() - o1.hashCode() : c;
	}
    };

    private static final List<WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> activities;

    static {
	final List<WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> activitiesAux = new ArrayList<WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>>();
	activitiesAux.add(new CancelWorkingCapitalInitializationActivity());
	activitiesAux.add(new ApproveActivity());
	activitiesAux.add(new UnApproveActivity());
	activitiesAux.add(new VerifyActivity());
	activitiesAux.add(new UnVerifyActivity());
	activitiesAux.add(new AuthorizeActivity());
	activitiesAux.add(new UnAuthorizeActivity());
	activitiesAux.add(new RejectWorkingCapitalInitializationActivity());
	activitiesAux.add(new UndoCancelOrRejectWorkingCapitalInitializationActivity());
	activitiesAux.add(new RequestCapitalActivity());
	activitiesAux.add(new PayCapitalActivity());
	activitiesAux.add(new RegisterWorkingCapitalAcquisitionActivity());
	activitiesAux.add(new CancelWorkingCapitalAcquisitionActivity());
	activitiesAux.add(new EditWorkingCapitalActivity());
	activitiesAux.add(new ApproveWorkingCapitalAcquisitionActivity());
	activitiesAux.add(new RejectWorkingCapitalAcquisitionActivity());
	activitiesAux.add(new UnApproveWorkingCapitalAcquisitionActivity());
	activitiesAux.add(new VerifyWorkingCapitalAcquisitionActivity());
	activitiesAux.add(new RejectVerifyWorkingCapitalAcquisitionActivity());
	activitiesAux.add(new UnVerifyWorkingCapitalAcquisitionActivity());
	activitiesAux.add(new SubmitForValidationActivity());
	activities = Collections.unmodifiableList(activitiesAux);
    }

    public WorkingCapitalProcess() {
	super();
    }

   public WorkingCapitalProcess(final WorkingCapital workingCapital) {
	this();
	setWorkingCapital(workingCapital);
    }

    @Override
    public <T extends WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation>> List<T> getActivities() {
	return (List) activities;
    }

    @Override
    public boolean isActive() {
	return true;
    }

    public boolean isPendingAproval(final User user) {
	return getWorkingCapital().isPendingAproval(user);
    }

    public boolean isPendingVerification(User user) {
	return getWorkingCapital().isPendingVerification(user);
    }

    public boolean isPendingAuthorization(User user) {
	return getWorkingCapital().isPendingAuthorization(user);
    }

    @Override
    public User getProcessCreator() {
	return getWorkingCapital().getRequester();
    }

    @Override
    public void notifyUserDueToComment(final User user, final String comment) {
	// do nothing.
    }

    @Override
    public List<Class<? extends ProcessFile>> getAvailableFileTypes() {
	List<Class<? extends ProcessFile>> availableFileTypes = super.getAvailableFileTypes();
	availableFileTypes.add(WorkingCapitalInvoiceFile.class);
	return availableFileTypes;
    }

    @Override
    public List<Class<? extends ProcessFile>> getUploadableFileTypes() {
	return super.getAvailableFileTypes();
    }

    public void submitAcquisitionsForValidation() {
	final WorkingCapital workingCapital = getWorkingCapital();
	workingCapital.submitAcquisitionsForValidation();
    }

    @Override
    public List<Class<? extends ProcessFile>> getDisplayableFileTypes() {
	final List<Class<? extends ProcessFile>> fileTypes = new ArrayList<Class<? extends ProcessFile>>();
	fileTypes.addAll(super.getDisplayableFileTypes());
	fileTypes.remove(WorkingCapitalInvoiceFile.class);
        return fileTypes;
    }

}
