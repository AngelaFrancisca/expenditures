package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.comparators.ComparatorChain;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem;
import pt.ist.expenditureTrackingSystem.domain.SavedSearch;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcess;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.PaymentProcessYear;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.RefundProcessStateType;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchPaymentProcess;
import pt.ist.expenditureTrackingSystem.domain.dto.UserSearchBean;
import pt.ist.expenditureTrackingSystem.domain.dto.VariantBean;
import pt.ist.expenditureTrackingSystem.domain.organization.AccountingUnit;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;
import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
import pt.ist.expenditureTrackingSystem.domain.organization.Unit;
import pt.ist.expenditureTrackingSystem.presentationTier.actions.BaseAction;
import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;
import pt.utl.ist.fenix.tools.util.CollectionPager;

@Mapping(path = "/search")
public class SearchPaymentProcessesAction extends BaseAction {

    private static final int REQUESTS_PER_PAGE = 50;

    private ActionForward search(final ActionMapping mapping, final HttpServletRequest request, SearchPaymentProcess searchBean,
	    boolean advanced) {

	return search(mapping, request, searchBean, advanced, false);
    }

    private ActionForward search(final ActionMapping mapping, final HttpServletRequest request, SearchPaymentProcess searchBean,
	    boolean advanced, boolean skipSearch) {
	Person loggedPerson = getLoggedPerson();

	List<PaymentProcess> processes = new ArrayList<PaymentProcess>();
	if (!skipSearch) {
	    processes.addAll(searchBean.search());

	    ComparatorChain chain = new ComparatorChain();
	    chain.addComparator(new Comparator<PaymentProcess>() {

		@Override
		public int compare(PaymentProcess process1, PaymentProcess process2) {
		    return process1.getPaymentProcessYear().getYear().compareTo(process2.getPaymentProcessYear().getYear());
		}

	    });
	    chain.addComparator(new Comparator<PaymentProcess>() {

		@Override
		public int compare(PaymentProcess process1, PaymentProcess process2) {
		    return process1.getAcquisitionProcessNumber().compareTo(process2.getAcquisitionProcessNumber());

		}

	    });

	    Collections.sort(processes, chain);
	}
	final CollectionPager<SearchPaymentProcess> pager = new CollectionPager<SearchPaymentProcess>((Collection) processes,
		REQUESTS_PER_PAGE);

	request.setAttribute("collectionPager", pager);
	request.setAttribute("numberOfPages", Integer.valueOf(pager.getNumberOfPages()));

	final String pageParameter = request.getParameter("pageNumber");
	final Integer page = StringUtils.isEmpty(pageParameter) ? Integer.valueOf(1) : Integer.valueOf(pageParameter);
	request.setAttribute("pageNumber", page);
	request.setAttribute("resultPage", pager.getPage(page));

	request.setAttribute("results", pager.getPage(page));
	request.setAttribute("searchBean", searchBean);
	request.setAttribute("person", loggedPerson);

	UserSearchBean userSearchBean = new UserSearchBean(loggedPerson);
	if (searchBean.isSearchObjectAvailable()) {
	    userSearchBean.setSelectedSearch(searchBean.getSavedSearch());
	}
	request.setAttribute("savingName", new VariantBean());
	request.setAttribute("mySearches", userSearchBean);
	request.setAttribute("advanced", advanced);
	request.setAttribute("pagerString", getJumpParameters(searchBean));
	return forward(request, "/acquisitions/search/searchProcesses.jsp");
    }

    public ActionForward searchJump(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {

	SearchPaymentProcess searchBean = materializeBeanFromRequest(request);
	return search(mapping, request, searchBean, false);
    }

    public ActionForward search(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {

	SearchPaymentProcess searchBean = getRenderedObject("searchBean");
	Person loggedPerson = getLoggedPerson();
	if (searchBean == null) {
	    searchBean = loggedPerson.hasDefaultSearch() ? new SearchPaymentProcess(loggedPerson.getDefaultSearch())
		    : new SearchPaymentProcess();
	    return search(mapping, request, searchBean, false);
	} else {
	    searchBean.setSavedSearch(null);
	    return search(mapping, request, searchBean, true);
	}

    }

    public ActionForward viewSearch(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {
	SavedSearch search = getDomainObject(request, "searchOID");
	return search(mapping, request, new SearchPaymentProcess(search), false);
    }

    public ActionForward saveSearch(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {

	SearchPaymentProcess searchBean = getRenderedObject("beanToSave");
	String name = getRenderedObject("searchName");
	if (name != null && name.length() > 0) {
	    searchBean.persistSearch(name);
	    RenderUtils.invalidateViewState("searchName");
	} else {
	    request.setAttribute("invalidName", true);
	}
	return search(mapping, request, searchBean, true);
    }

    public ActionForward changeSelectedClass(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	SearchPaymentProcess searchBean = getRenderedObject("searchBean");

	RenderUtils.invalidateViewState("searchBean");
	return search(mapping, request, searchBean, true, true);
    }

    public ActionForward mySearches(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {

	UserSearchBean bean = getRenderedObject("mySearches");
	SavedSearch search = bean.getSelectedSearch();
	if (search == null) {
	    search = getLoggedPerson().getDefaultSearch();
	    bean.setSelectedSearch(search);
	    RenderUtils.invalidateViewState("mySearches");
	}
	return search(mapping, request, new SearchPaymentProcess(search), false);
    }

    public ActionForward configurateMySearches(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	List<SavedSearch> systemSearches = ExpenditureTrackingSystem.getInstance().getSystemSearches();
	List<SavedSearch> userSearches = getLoggedPerson().getSaveSearches();
	request.setAttribute("systemSearches", systemSearches);
	request.setAttribute("userSearches", userSearches);

	return forward(request, "/acquisitions/search/manageMySearches.jsp");
    }

    public ActionForward deleteMySearch(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {

	SavedSearch search = getDomainObject(request, "savedSearchOID");
	Person person = getLoggedPerson();
	if (person == search.getPerson()) {
	    search.delete();
	}

	return configurateMySearches(mapping, form, request, response);
    }

    public ActionForward setSearchAsDefault(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) {

	SavedSearch search = getDomainObject(request, "savedSearchOID");
	Person person = getLoggedPerson();

	person.setDefaultSearch(search);
	return configurateMySearches(mapping, form, request, response);
    }

    private String getJumpParameters(SearchPaymentProcess searchBean) {
	StringBuilder builder = new StringBuilder("&processId=");
	if (searchBean.getProcessId() != null) {
	    builder.append(searchBean.getProcessId());
	}
	builder.append("&requestDocumentId=");
	if (searchBean.getRequestDocumentId() != null) {
	    builder.append(searchBean.getRequestDocumentId());
	}
	builder.append("&proposalId=");
	if (searchBean.getProposalId() != null) {
	    builder.append(searchBean.getProposalId());
	}
	builder.append("&refundeeName=");
	if (searchBean.getRefundeeName() != null) {
	    builder.append(searchBean.getRefundeeName());
	}
	builder.append("&requestingPerson=");
	if (searchBean.getRequestingPerson() != null) {
	    builder.append(searchBean.getRequestingPerson().getOID());
	}
	builder.append("&requestingUnit=");
	if (searchBean.getRequestingUnit() != null) {
	    builder.append(searchBean.getRequestingUnit().getOID());
	}
	builder.append("&savedSearch=");
	if (searchBean.getSavedSearch() != null) {
	    builder.append(searchBean.getSavedSearch().getOID());
	}
	builder.append("&supplier=");
	if (searchBean.getSupplier() != null) {
	    builder.append(searchBean.getSupplier().getOID());
	}
	builder.append("&accountingUnit=");
	if (searchBean.getAccountingUnit() != null) {
	    builder.append(searchBean.getAccountingUnit().getOID());
	}
	builder.append("&year=");
	if (searchBean.getPaymentProcessYear() != null) {
	    builder.append(searchBean.getPaymentProcessYear().getOID());
	}
	builder.append("&hasAvailableAndAccessibleActivityForUser=");
	builder.append(searchBean.getHasAvailableAndAccessibleActivityForUser());

	builder.append("&responsibleUnitSetOnly=");
	builder.append(searchBean.getResponsibleUnitSetOnly());

	builder.append("&showOnlyAcquisitionsExcludedFromSupplierLimit=");
	builder.append(searchBean.getShowOnlyAcquisitionsExcludedFromSupplierLimit());

	builder.append("&showOnlyAcquisitionsWithAdditionalCosts=");
	builder.append(searchBean.getShowOnlyAcquisitionsWithAdditionalCosts());

	builder.append("&acquisitionProcessStateType=");
	if (searchBean.getAcquisitionProcessStateType() != null) {
	    builder.append(searchBean.getAcquisitionProcessStateType().name());
	}

	builder.append("&refundProcessStateType=");
	if (searchBean.getRefundProcessStateType() != null) {
	    builder.append(searchBean.getRefundProcessStateType().name());
	}

	builder.append("&searchClass=");
	if (searchBean.getSearchClass() != null) {
	    builder.append(searchBean.getSearchClass().getName());
	}

	return builder.toString();
    }

    private SearchPaymentProcess materializeBeanFromRequest(HttpServletRequest request) {
	SearchPaymentProcess bean = new SearchPaymentProcess();
	bean.setProcessId(request.getParameter("processId"));
	bean.setRequestDocumentId(request.getParameter("requestDocumentId"));
	bean.setProposalId(request.getParameter("proposalId"));
	bean.setRefundeeName(request.getParameter("refundeeName"));

	bean.setRequestingPerson((Person) getDomainObject(request, "requestingPerson"));
	bean.setRequestingUnit((Unit) getDomainObject(request, "requestingUnit"));
	bean.setSavedSearch((SavedSearch) getDomainObject(request, "savedSearch"));
	bean.setSupplier((Supplier) getDomainObject(request, "supplier"));
	bean.setAccountingUnit((AccountingUnit) getDomainObject(request, "accountingUnit"));
	bean.setPaymentProcessYear((PaymentProcessYear) getDomainObject(request, "year"));

	bean.setHasAvailableAndAccessibleActivityForUser(Boolean.valueOf(request
		.getParameter("hasAvailableAndAccessibleActivityForUser")));
	bean.setResponsibleUnitSetOnly(Boolean.valueOf(request.getParameter("responsibleUnitSetOnly")));
	bean.setShowOnlyAcquisitionsExcludedFromSupplierLimit(Boolean.valueOf(request
		.getParameter("showOnlyAcquisitionsExcludedFromSupplierLimit")));
	bean.setShowOnlyAcquisitionsWithAdditionalCosts(Boolean.valueOf(request
		.getParameter("showOnlyAcquisitionsWithAdditionalCosts")));

	String searchClass = request.getParameter("searchClass");
	if (searchClass != null) {
	    try {
		Class clazz = Class.forName(searchClass);
		bean.setSearchClass(clazz);
	    } catch (Exception e) {
		// drop exception silently...
	    }
	}

	String type = request.getParameter("acquisitionProcessStateType");
	if (!StringUtils.isEmpty(type)) {
	    bean.setAcquisitionProcessStateType(AcquisitionProcessStateType.valueOf(type));
	}

	type = request.getParameter("refundProcessStateType");
	if (!StringUtils.isEmpty(type)) {
	    bean.setRefundProcessStateType(RefundProcessStateType.valueOf(type));
	}

	return bean;
    }

}
