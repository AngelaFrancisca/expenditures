package pt.ist.expenditureTrackingSystem.domain;

import module.organization.presentationTier.actions.OrganizationModelAction;
import myorg.domain.ModuleInitializer;
import myorg.domain.MyOrg;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;
import pt.ist.expenditureTrackingSystem.presentationTier.actions.organization.OrganizationModelPlugin.ExpendituresView;
import pt.ist.fenixWebFramework.services.Service;

public class ExpenditureTrackingSystem extends ExpenditureTrackingSystem_Base implements ModuleInitializer {

    private static boolean isInitialized = false;

    private static ThreadLocal<ExpenditureTrackingSystem> init = null;

    public static ExpenditureTrackingSystem getInstance() {
	if (init != null) {
	    return init.get();
	}

	if (!isInitialized) {
	    initialize();
	}
	final MyOrg myOrg = MyOrg.getInstance();
	return myOrg.getExpenditureTrackingSystem();
    }

    @Service
    public synchronized static void initialize() {
	if (!isInitialized) {
	    try {
		final MyOrg myOrg = MyOrg.getInstance();
		final ExpenditureTrackingSystem expendituretrackingSystem = myOrg.getExpenditureTrackingSystem();
		if (expendituretrackingSystem == null) {
		    new ExpenditureTrackingSystem();
		}
		init = new ThreadLocal<ExpenditureTrackingSystem>();
		init.set(myOrg.getExpenditureTrackingSystem());
		
		initRoles();
		initSystemSearches();
		OrganizationModelAction.partyViewHookManager.register(new ExpendituresView());
		isInitialized = true;
	    } finally {
		init = null;
	    }
	}
    }

    private static void initRoles() {
	for (final RoleType roleType : RoleType.values()) {
	    Role.getRole(roleType);
	}
    }

    protected static void initSystemSearches() {
	final ExpenditureTrackingSystem expendituretrackingSystem = getInstance();
	if (expendituretrackingSystem.getSystemSearches().isEmpty()) {
	    new MyOwnProcessesSearch();
	    final SavedSearch savedSearch = new PendingProcessesSearch();
	    for (final Person person : expendituretrackingSystem.getPeopleSet()) {
		person.setDefaultSearch(savedSearch);
	    }
	}
    }

    private ExpenditureTrackingSystem() {
	super();
	setMyOrg(MyOrg.getInstance());
	setAcquisitionRequestDocumentCounter(0);
    }

    public String nextAcquisitionRequestDocumentID() {
	return "D" + getAndUpdateNextAcquisitionRequestDocumentCountNumber();
    }

    public Integer nextAcquisitionRequestDocumentCountNumber() {
	return getAndUpdateNextAcquisitionRequestDocumentCountNumber();
    }

    private Integer getAndUpdateNextAcquisitionRequestDocumentCountNumber() {
	setAcquisitionRequestDocumentCounter(getAcquisitionRequestDocumentCounter().intValue() + 1);
	return getAcquisitionRequestDocumentCounter();
    }

    @Override
    public void init(final MyOrg root) {
	// nothing else to be done... getInstance() already did it.
    }

}
