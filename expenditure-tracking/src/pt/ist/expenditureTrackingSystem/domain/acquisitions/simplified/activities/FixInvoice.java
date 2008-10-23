package pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities;

import pt.ist.expenditureTrackingSystem.domain.RoleType;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.RegularAcquisitionProcess;

public class FixInvoice extends ReceiveInvoice {

    @Override
    protected boolean isAccessible(RegularAcquisitionProcess process) {
	return userHasRole(RoleType.ACQUISITION_CENTRAL);
    }

    @Override
    protected boolean isAvailable(RegularAcquisitionProcess process) {
	return process.getAcquisitionProcessState().isInvoiceReceived();
    }

    @Override
    protected void process(RegularAcquisitionProcess process, Object... objects) {
	processInvoiceData(process, objects);
    }

}
