package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;

import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.GenericAcquisitionProcessActivity;

public class RemoveProjectFundAllocation extends GenericAcquisitionProcessActivity {

    @Override
    protected boolean isAccessible(RegularAcquisitionProcess process) {
	return process.isProjectAccountingEmployee();
    }

    @Override
    protected boolean isAvailable(RegularAcquisitionProcess process) {
	return (checkActiveConditions(process) || checkCanceledConditions(process))
		&& process.hasAllocatedFundsForAllProjectFinancers(getLoggedPerson())
		&& !process.hasAnyNonProjectFundAllocationId();
    }

    private boolean checkActiveConditions(RegularAcquisitionProcess process) {
	return super.isAvailable(process) && process.getAcquisitionProcessState().isInAllocatedToSupplierState();
    }

    private boolean checkCanceledConditions(RegularAcquisitionProcess process) {
	return super.isAvailable(process) && process.getAcquisitionProcessState().isCanceled();
    }

    @Override
    protected void process(RegularAcquisitionProcess process, Object... objects) {
	process.getAcquisitionRequest().resetProjectFundAllocationId(getLoggedPerson());
	RemoveFundAllocationExpirationDate removeFundAllocationExpirationDate = new RemoveFundAllocationExpirationDate();
	if (process.getAcquisitionProcessState().isCanceled() && !process.getAcquisitionRequest().hasAllFundAllocationId()
		&& removeFundAllocationExpirationDate.isActive(process)) {
	    removeFundAllocationExpirationDate.execute(process);
	}
    }
}
