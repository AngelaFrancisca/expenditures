package module.workingCapital.domain.activity;

import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workingCapital.domain.WorkingCapital;
import module.workingCapital.domain.WorkingCapitalProcess;
import module.workingCapital.domain.WorkingCapitalTransaction;
import myorg.domain.User;
import myorg.util.BundleUtil;

public class UnApproveWorkingCapitalAcquisitionActivity extends WorkflowActivity<WorkingCapitalProcess, WorkingCapitalTransactionInformation> {

    @Override
    public String getLocalizedName() {
	return BundleUtil.getStringFromResourceBundle("resources/WorkingCapitalResources", "activity." + getClass().getSimpleName());
    }

    @Override
    public boolean isActive(final WorkingCapitalProcess missionProcess, final User user) {
	final WorkingCapital workingCapital = missionProcess.getWorkingCapital();
	return !workingCapital.isCanceledOrRejected()
		&& workingCapital.hasAcquisitionPendingVerification();
    }

    @Override
    protected void process(final WorkingCapitalTransactionInformation activityInformation) {
	final WorkingCapitalTransaction workingCapitalTransaction = activityInformation.getWorkingCapitalTransaction();
	workingCapitalTransaction.unApprove();
    }

    @Override
    public ActivityInformation<WorkingCapitalProcess> getActivityInformation(final WorkingCapitalProcess process) {
        return new WorkingCapitalTransactionInformation(process, this);
    }

    @Override
    public boolean isVisible() {
	return false;
    }

}