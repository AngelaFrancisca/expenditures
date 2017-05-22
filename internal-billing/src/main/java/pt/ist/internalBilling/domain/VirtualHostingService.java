package pt.ist.internalBilling.domain;

import pt.ist.expenditureTrackingSystem.domain.util.DomainException;

public class VirtualHostingService extends VirtualHostingService_Base {
    
    VirtualHostingService(final String title, final String description) {
        setTitle(title);
        setDescription(description);
    }

    @Override
    protected void createServiceRequest(final BillableServiceRequest serviceRequest) {
        throw new DomainException("InternalBillingResources", "error.incompatible.service.request.type",
                getClass().getName(), serviceRequest.getClass().getName());
    }
    
}
