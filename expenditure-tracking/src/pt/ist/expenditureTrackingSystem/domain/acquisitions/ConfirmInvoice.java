package pt.ist.expenditureTrackingSystem.domain.acquisitions;

import pt.ist.expenditureTrackingSystem.domain.acquisitions.activities.AbstractActivity;

public class ConfirmInvoice extends AbstractActivity<AcquisitionProcess> {

    @Override
    protected boolean isAccessible(AcquisitionProcess process) {
	return process.isResponsibleForUnit();
    }

    @Override
    protected boolean isAvailable(AcquisitionProcess process) {
	return process.isInvoiceReceived();
    }

    @Override
    protected void process(AcquisitionProcess process, Object... objects) {
	new AcquisitionProcessState(process, AcquisitionProcessStateType.INVOICE_CONFIRMED);
    }

}
