/*
 * @(#)ExpenditureTrackingSystem.java
 *
 * Copyright 2009 Instituto Superior Tecnico
 * Founding Authors: Luis Cruz, Nuno Ochoa, Paulo Abrantes
 * 
 *      https://fenix-ashes.ist.utl.pt/
 * 
 *   This file is part of the Expenditure Tracking Module.
 *
 *   The Expenditure Tracking Module is free software: you can
 *   redistribute it and/or modify it under the terms of the GNU Lesser General
 *   Public License as published by the Free Software Foundation, either version 
 *   3 of the License, or (at your option) any later version.
 *
 *   The Expenditure Tracking Module is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with the Expenditure Tracking Module. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package pt.ist.expenditureTrackingSystem.domain;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import module.dashBoard.WidgetRegister;
import module.dashBoard.WidgetRegister.WidgetAditionPredicate;
import module.dashBoard.domain.DashBoardPanel;
import module.dashBoard.widgets.WidgetController;
import module.organization.presentationTier.actions.OrganizationModelAction;
import module.workflow.widgets.ProcessListWidget;
import myorg.applicationTier.Authenticate.UserView;
import myorg.domain.ModuleInitializer;
import myorg.domain.MyOrg;
import myorg.domain.User;
import myorg.domain.VirtualHost;
import myorg.domain.util.Money;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessYear;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchProcessValues;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchProcessValuesArray;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess.ProcessClassification;
import pt.ist.expenditureTrackingSystem.presentationTier.actions.organization.OrganizationModelPlugin.ExpendituresView;
import pt.ist.expenditureTrackingSystem.presentationTier.widgets.ActivateEmailNotificationWidget;
import pt.ist.expenditureTrackingSystem.presentationTier.widgets.MyProcessesWidget;
import pt.ist.expenditureTrackingSystem.presentationTier.widgets.MySearchesWidget;
import pt.ist.expenditureTrackingSystem.presentationTier.widgets.PendingRefundWidget;
import pt.ist.expenditureTrackingSystem.presentationTier.widgets.PendingSimplifiedWidget;
import pt.ist.expenditureTrackingSystem.presentationTier.widgets.PrioritiesWidget;
import pt.ist.expenditureTrackingSystem.presentationTier.widgets.SearchByInvoiceWidget;
import pt.ist.expenditureTrackingSystem.presentationTier.widgets.TakenProcessesWidget;
import pt.ist.expenditureTrackingSystem.presentationTier.widgets.UnreadCommentsWidget;
import pt.ist.expenditureTrackingSystem.util.AquisitionsPendingProcessCounter;
import pt.ist.expenditureTrackingSystem.util.RefundPendingProcessCounter;
import pt.ist.fenixWebFramework.services.Service;
import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.RequestChecksumFilter;
import pt.ist.fenixWebFramework.servlets.filters.contentRewrite.RequestChecksumFilter.ChecksumPredicate;
import dml.runtime.RelationAdapter;

/**
 * 
 * @author Diogo Figueiredo
 * @author Pedro Santos
 * @author João Neves
 * @author Bruno Santos
 * @author Paulo Abrantes
 * @author Luis Cruz
 * @author Pedro Amaral
 * 
 */
public class ExpenditureTrackingSystem extends ExpenditureTrackingSystem_Base implements ModuleInitializer {

    public static class VirtualHostMyOrgRelationListener extends RelationAdapter<VirtualHost, MyOrg> {

	@Override
	public void beforeRemove(VirtualHost vh, MyOrg myorg) {
	    vh.removeExpenditureTrackingSystem();
	    super.beforeRemove(vh, myorg);
	}
    }

    public static WidgetAditionPredicate EXPENDITURE_TRACKING_PANEL_PREDICATE = new WidgetAditionPredicate() {
	@Override
	public boolean canBeAdded(DashBoardPanel panel, User userAdding) {
	    return (ExpenditureUserDashBoardPanel.class.isAssignableFrom(panel.getClass()));
	}
    };

    public static WidgetAditionPredicate EXPENDITURE_SERVICES_ONLY_PREDICATE = new WidgetAditionPredicate() {

	@Override
	public boolean canBeAdded(DashBoardPanel panel, User userAdding) {
	    return EXPENDITURE_TRACKING_PANEL_PREDICATE.canBeAdded(panel, userAdding)
		    && (isAcquisitionCentralGroupMember(userAdding)
			    || !userAdding.getExpenditurePerson().getAccountingUnits().isEmpty() || !userAdding
			    .getExpenditurePerson().getProjectAccountingUnits().isEmpty());
	}
    };

    static {
	VirtualHost.MyOrgVirtualHost.addListener(new VirtualHostMyOrgRelationListener());

	ProcessListWidget.register(new AquisitionsPendingProcessCounter());
	ProcessListWidget.register(new RefundPendingProcessCounter());

	registerWidget(MySearchesWidget.class);
	registerWidget(UnreadCommentsWidget.class);
	registerWidget(TakenProcessesWidget.class);
	registerWidget(MyProcessesWidget.class);
	registerWidget(PendingRefundWidget.class);
	registerWidget(PendingSimplifiedWidget.class);
	registerWidget(ActivateEmailNotificationWidget.class);
	registerWidget(SearchByInvoiceWidget.class);
	WidgetRegister.registerWidget(PrioritiesWidget.class, EXPENDITURE_SERVICES_ONLY_PREDICATE);

	registerChecksumFilterException();
	OrganizationModelAction.partyViewHookManager.register(new ExpendituresView());
    }

    private static boolean isInitialized = false;

    public static ExpenditureTrackingSystem getInstance() {
	if (!isInitialized) {
	    if (initialize()) {
		callInitScripts();
	    }
	}
	final VirtualHost virtualHostForThread = VirtualHost.getVirtualHostForThread();
	return virtualHostForThread == null ? null : virtualHostForThread.getExpenditureTrackingSystem();
    }

    private static synchronized boolean initialize() {
	if (!isInitialized) {
	    isInitialized = true;
	    return true;
	}
	return false;
    }

    private static void callInitScripts() {
	migrateProcessNumbers();
	migrateSuppliers();
	migrateCPVs();
	migratePeople();
	checkISTOptions();
    }

    @Service
    private static Boolean checkISTOptions() {
	final MyOrg myOrg = MyOrg.getInstance();
	for (final VirtualHost virtualHost : myOrg.getVirtualHostsSet()) {
	    final ExpenditureTrackingSystem ets = virtualHost.getExpenditureTrackingSystem();
	    if (ets != null) {
		if (virtualHost.getHostname().equals("dot.ist.utl.pt")
			|| virtualHost.getHostname().equals("compras.ist.utl.pt")) {
		    ets.setRequireFundAllocationPriorToAcquisitionRequest(Boolean.TRUE);
		    ets.setRegisterDiaryNumbersAndTransactionNumbers(Boolean.FALSE);
		    ets.setRequireCommitmentNumber(Boolean.TRUE);
		} else {
		    ets.setRegisterDiaryNumbersAndTransactionNumbers(Boolean.TRUE);
		    ets.setRequireCommitmentNumber(Boolean.FALSE);
		}
	    }
	}
	return false;
    }

    @Service
    private static Boolean migratePeople() {
	final MyOrg myOrg = MyOrg.getInstance();
	if (!myOrg.hasAnyPeopleFromExpenditureTackingSystem()) {
	    final long start = System.currentTimeMillis();
	    System.out.println("Migrating people..");
	    for (final VirtualHost virtualHost : myOrg.getVirtualHostsSet()) {
		final ExpenditureTrackingSystem ets = virtualHost.getExpenditureTrackingSystem();
		if (ets != null) {
		    myOrg.getPeopleFromExpenditureTackingSystemSet().addAll(ets.getPeopleSet());
		}
	    }
	    final long end = System.currentTimeMillis();
	    System.out.println("Completed migration in: " + (end - start) + "ms.");
	}
	return Boolean.TRUE;
    }

    @Service
    private static Boolean migrateCPVs() {
	final MyOrg myOrg = MyOrg.getInstance();
	if (!myOrg.hasAnyCPVReferences()) {
	    final long start = System.currentTimeMillis();
	    System.out.println("Migrating cpv references..");
	    for (final VirtualHost virtualHost : myOrg.getVirtualHostsSet()) {
		final ExpenditureTrackingSystem ets = virtualHost.getExpenditureTrackingSystem();
		if (ets != null) {
		    myOrg.getCPVReferencesSet().addAll(ets.getCPVReferencesSet());
		}
	    }
	    final long end = System.currentTimeMillis();
	    System.out.println("Completed migration in: " + (end - start) + "ms.");
	}
	return Boolean.TRUE;
    }

    @Service
    private static Boolean migrateSuppliers() {
	final MyOrg myOrg = MyOrg.getInstance();
	if (!myOrg.hasAnySuppliers()) {
	    final long start = System.currentTimeMillis();
	    System.out.println("Migrating suppliers.");
	    for (final VirtualHost virtualHost : myOrg.getVirtualHostsSet()) {
		final ExpenditureTrackingSystem ets = virtualHost.getExpenditureTrackingSystem();
		if (ets != null) {
		    myOrg.getSuppliersSet().addAll(ets.getSuppliersSet());
		}
	    }
	    final long end = System.currentTimeMillis();
	    System.out.println("Completed migration in: " + (end - start) + "ms.");
	}
	return Boolean.TRUE;
    }

    @Service
    private static Boolean migrateProcessNumbers() {
	final VirtualHost virtualHostForThread = VirtualHost.getVirtualHostForThread();
	if (virtualHostForThread == null) {
	    return Boolean.FALSE; 
	}
	final ExpenditureTrackingSystem expenditureTrackingSystem = virtualHostForThread.getExpenditureTrackingSystem();
	if (expenditureTrackingSystem == null) {
	    return Boolean.FALSE;
	}
	final String prefix = expenditureTrackingSystem.getInstitutionalProcessNumberPrefix();
	if (prefix != null && !prefix.isEmpty()) {
	    final long start = System.currentTimeMillis();
	    System.out.println("Migrating acquisition process numbers.");
	    for (final PaymentProcessYear paymentProcessYear : expenditureTrackingSystem.getPaymentProcessYearsSet()) {
		for (final PaymentProcess paymentProcess : paymentProcessYear.getPaymentProcessSet()) {
		    paymentProcess.migrateProcessNumber();
		}
	    }
	    final long end = System.currentTimeMillis();
	    System.out.println("Completed migration in: " + (end - start) + "ms.");
	}
	return Boolean.TRUE;
    }

    private static void registerChecksumFilterException() {
	RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {

	    @Override
	    public boolean shouldFilter(HttpServletRequest request) {
		return !(request.getQueryString() != null && request.getQueryString().contains(
			"method=calculateShareValuesViaAjax"));
	    }

	});

	RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {
	    @Override
	    public boolean shouldFilter(HttpServletRequest httpServletRequest) {
		return !(httpServletRequest.getRequestURI().endsWith("/acquisitionSimplifiedProcedureProcess.do")
			&& httpServletRequest.getQueryString() != null && httpServletRequest.getQueryString().contains(
			"method=checkSupplierLimit"));
	    }
	});

	RequestChecksumFilter.registerFilterRule(new ChecksumPredicate() {
	    @Override
	    public boolean shouldFilter(HttpServletRequest httpServletRequest) {
		return !(httpServletRequest.getRequestURI().endsWith("/viewRCISTAnnouncements.do"));
	    }
	});

    }

    private static void initRoles() {
	for (final RoleType roleType : RoleType.values()) {
	    Role.getRole(roleType);
	}
    }

    private ExpenditureTrackingSystem(final VirtualHost virtualHost) {
	super();
//	setMyOrg(MyOrg.getInstance());
	setAcquisitionRequestDocumentCounter(0);
	virtualHost.setExpenditureTrackingSystem(this);

	new MyOwnProcessesSearch();
//	final SavedSearch savedSearch = new PendingProcessesSearch();
//	for (final Person person : getPeopleSet()) {
//	    person.setDefaultSearch(savedSearch);
//	}

	setAcquisitionCentralGroup(myorg.domain.groups.Role.getRole(RoleType.ACQUISITION_CENTRAL));

	setAcquisitionCentralManagerGroup(myorg.domain.groups.Role.getRole(RoleType.ACQUISITION_CENTRAL_MANAGER));

	setAccountingManagerGroup(myorg.domain.groups.Role.getRole(RoleType.ACCOUNTING_MANAGER));

	setProjectAccountingManagerGroup(myorg.domain.groups.Role.getRole(RoleType.PROJECT_ACCOUNTING_MANAGER));

	setTreasuryMemberGroup(myorg.domain.groups.Role.getRole(RoleType.TREASURY_MANAGER));

	setSupplierManagerGroup(myorg.domain.groups.Role.getRole(RoleType.SUPPLIER_MANAGER));

	setSupplierFundAllocationManagerGroup(myorg.domain.groups.Role.getRole(RoleType.SUPPLIER_FUND_ALLOCATION_MANAGER));

	setStatisticsViewerGroup(myorg.domain.groups.Role.getRole(RoleType.STATISTICS_VIEWER));

	setAcquisitionsUnitManagerGroup(myorg.domain.groups.Role.getRole(RoleType.AQUISITIONS_UNIT_MANAGER));

	setAcquisitionsProcessAuditorGroup(myorg.domain.groups.Role.getRole(RoleType.ACQUISITION_PROCESS_AUDITOR));

	setSearchProcessValuesArray(new SearchProcessValuesArray(SearchProcessValues.values()));

	setAcquisitionCreationWizardJsp("creationWizardPublicInstitution.jsp");

    }

    public String nextAcquisitionRequestDocumentID() {
	final String prefix = getInstitutionalRequestDocumentPrefix();
	return prefix + getAndUpdateNextAcquisitionRequestDocumentCountNumber();
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
	final ExpenditureTrackingSystem expenditureTrackingSystem = root.getExpenditureTrackingSystem();
	if (expenditureTrackingSystem != null) {
	}
    }

    private static void registerWidget(Class<? extends WidgetController> widgetClass) {
	WidgetRegister.registerWidget(widgetClass, EXPENDITURE_TRACKING_PANEL_PREDICATE);
    }

    @Service
    public static void createSystem(final VirtualHost virtualHost) {
	if (!virtualHost.hasExpenditureTrackingSystem() || virtualHost.getExpenditureTrackingSystem().getVirtualHostCount() > 1) {
	    new ExpenditureTrackingSystem(virtualHost);
	    initRoles();
	}
    }

    public static boolean isAcquisitionCentralGroupMember(final User user) {
	final ExpenditureTrackingSystem system = getInstance();
	return system != null && system.hasAcquisitionCentralGroup() && system.getAcquisitionCentralGroup().isMember(user);
    }

    public static boolean isAcquisitionCentralManagerGroupMember(final User user) {
	final ExpenditureTrackingSystem system = getInstance();
	return system != null && system.hasAcquisitionCentralManagerGroup()
		&& system.getAcquisitionCentralManagerGroup().isMember(user);
    }

    public static boolean isAccountingManagerGroupMember(final User user) {
	final ExpenditureTrackingSystem system = getInstance();
	return system != null && system.hasAccountingManagerGroup() && system.getAccountingManagerGroup().isMember(user);
    }

    public static boolean isProjectAccountingManagerGroupMember(final User user) {
	final ExpenditureTrackingSystem system = getInstance();
	return system != null && system.hasProjectAccountingManagerGroup()
		&& system.getProjectAccountingManagerGroup().isMember(user);
    }

    public static boolean isTreasuryMemberGroupMember(final User user) {
	final ExpenditureTrackingSystem system = getInstance();
	return system != null && system.hasTreasuryMemberGroup() && system.getTreasuryMemberGroup().isMember(user);
    }

    public static boolean isSupplierManagerGroupMember(final User user) {
	final ExpenditureTrackingSystem system = getInstance();
	return system != null && system.hasSupplierManagerGroup() && system.getSupplierManagerGroup().isMember(user);
    }

    public static boolean isSupplierFundAllocationManagerGroupMember(final User user) {
	final ExpenditureTrackingSystem system = getInstance();
	return system != null && system.hasSupplierFundAllocationManagerGroup()
		&& system.getSupplierFundAllocationManagerGroup().isMember(user);
    }

    public static boolean isStatisticsViewerGroupMember(final User user) {
	final ExpenditureTrackingSystem system = getInstance();
	return system != null && system.hasStatisticsViewerGroup() && system.getStatisticsViewerGroup().isMember(user);
    }

    public static boolean isAcquisitionsUnitManagerGroupMember(final User user) {
	final ExpenditureTrackingSystem system = getInstance();
	return system != null && system.hasAcquisitionsUnitManagerGroup()
		&& system.getAcquisitionsUnitManagerGroup().isMember(user);
    }

    public static boolean isAcquisitionsProcessAuditorGroupMember(final User user) {
	final ExpenditureTrackingSystem system = getInstance();
	return system != null && system.hasAcquisitionsProcessAuditorGroup()
		&& system.getAcquisitionsProcessAuditorGroup().isMember(user);
    }

    public static boolean isAcquisitionCentralGroupMember() {
	final User user = UserView.getCurrentUser();
	return isAcquisitionCentralGroupMember(user);
    }

    public static boolean isAcquisitionCentralManagerGroupMember() {
	final User user = UserView.getCurrentUser();
	return isAcquisitionCentralManagerGroupMember(user);
    }

    public static boolean isAccountingManagerGroupMember() {
	final User user = UserView.getCurrentUser();
	return isAccountingManagerGroupMember(user);
    }

    public static boolean isProjectAccountingManagerGroupMember() {
	final User user = UserView.getCurrentUser();
	return isProjectAccountingManagerGroupMember(user);
    }

    public static boolean isTreasuryMemberGroupMember() {
	final User user = UserView.getCurrentUser();
	return isTreasuryMemberGroupMember(user);
    }

    public static boolean isSupplierManagerGroupMember() {
	final User user = UserView.getCurrentUser();
	return isSupplierManagerGroupMember(user);
    }

    public static boolean isSupplierFundAllocationManagerGroupMember() {
	final User user = UserView.getCurrentUser();
	return isSupplierFundAllocationManagerGroupMember(user);
    }

    public static boolean isStatisticsViewerGroupMember() {
	final User user = UserView.getCurrentUser();
	return isStatisticsViewerGroupMember(user);
    }

    public static boolean isAcquisitionsUnitManagerGroupMember() {
	final User user = UserView.getCurrentUser();
	return isAcquisitionsUnitManagerGroupMember(user);
    }

    public static boolean isAcquisitionsProcessAuditorGroupMember() {
	final User user = UserView.getCurrentUser();
	return isAcquisitionsProcessAuditorGroupMember(user);
    }

    public static boolean isManager() {
	final User user = UserView.getCurrentUser();
	final myorg.domain.groups.Role role = myorg.domain.groups.Role.getRole(myorg.domain.RoleType.MANAGER);
	return role.isMember(user);
    }

    public boolean contains(final SearchProcessValues values) {
	return getSearchProcessValuesArray() != null && getSearchProcessValuesArray().contains(values);
    }

    public SortedSet<ProcessClassification> getAllowdProcessClassifications(final Class processType) {
	final SortedSet<ProcessClassification> classifications = new TreeSet<SimplifiedProcedureProcess.ProcessClassification>();
	for (final SearchProcessValues searchProcessValues : getSearchProcessValuesArray().getSearchProcessValues()) {
	    if (processType != null
		    && processType == searchProcessValues.getSearchClass()
		    && searchProcessValues.getSearchClassification() != null) {
		classifications.add(searchProcessValues.getSearchClassification());
	    }
	}
	return classifications;
    }

    @Service
    public void saveConfiguration(final String institutionalProcessNumberPrefix,
	    final String institutionalRequestDocumentPrefix, final String acquisitionCreationWizardJsp,
	    final SearchProcessValuesArray array, final Boolean invoiceAllowedToStartAcquisitionProcess,
	    final Boolean requireFundAllocationPriorToAcquisitionRequest, final Boolean registerDiaryNumbersAndTransactionNumbers,
	    final Money maxValueStartedWithInvoive, final Money valueRequireingTopLevelAuthorization,
	    final String documentationUrl, final String documentationLabel, final Boolean requireCommitmentNumber) {
	setInstitutionalProcessNumberPrefix(institutionalProcessNumberPrefix);
	setInstitutionalRequestDocumentPrefix(institutionalRequestDocumentPrefix);
	setAcquisitionCreationWizardJsp(acquisitionCreationWizardJsp);
	setSearchProcessValuesArray(array);
	setInvoiceAllowedToStartAcquisitionProcess(invoiceAllowedToStartAcquisitionProcess);
	setRequireFundAllocationPriorToAcquisitionRequest(requireFundAllocationPriorToAcquisitionRequest);
	setRegisterDiaryNumbersAndTransactionNumbers(registerDiaryNumbersAndTransactionNumbers);
	setMaxValueStartedWithInvoive(maxValueStartedWithInvoive);
	setValueRequireingTopLevelAuthorization(valueRequireingTopLevelAuthorization);
	setDocumentationUrl(documentationUrl);
	setDocumentationLabel(documentationLabel);
	setRequireCommitmentNumber(requireCommitmentNumber);
    }

    @Service
    public void setForVirtualHost(final VirtualHost virtualHost) {
	virtualHost.setExpenditureTrackingSystem(this);
    }

    public static boolean isInvoiceAllowedToStartAcquisitionProcess() {
	final ExpenditureTrackingSystem system = getInstance();
	final Boolean invoiceAllowedToStartAcquisitionProcess = system.getInvoiceAllowedToStartAcquisitionProcess();
	return invoiceAllowedToStartAcquisitionProcess != null && invoiceAllowedToStartAcquisitionProcess.booleanValue();
    }

    public boolean hasProcessPrefix() {
	final String prefix = getInstitutionalProcessNumberPrefix();
	return prefix != null && !prefix.isEmpty();
    }

}
