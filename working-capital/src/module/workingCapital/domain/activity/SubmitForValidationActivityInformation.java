package module.workingCapital.domain.activity;

import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;
import module.workingCapital.domain.WorkingCapitalProcess;

import org.joda.time.DateTime;

public class SubmitForValidationActivityInformation extends ActivityInformation<WorkingCapitalProcess> {

    private boolean lastSubmission = new DateTime().getMonthOfYear() == 12;

    public SubmitForValidationActivityInformation(WorkingCapitalProcess process,
	    WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
	super(process, activity);
    }

    public boolean isLastSubmission() {
        return lastSubmission;
    }

    public void setLastSubmission(boolean lastSubmission) {
        this.lastSubmission = lastSubmission;
    }

    @Override
    public boolean hasAllneededInfo() {
        return super.hasAllneededInfo() && isForwardedFromInput();
    }

}