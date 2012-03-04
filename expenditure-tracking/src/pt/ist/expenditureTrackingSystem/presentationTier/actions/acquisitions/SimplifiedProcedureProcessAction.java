/*
 * @(#)SimplifiedProcedureProcessAction.java
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
package pt.ist.expenditureTrackingSystem.presentationTier.actions.acquisitions;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import module.workflow.presentationTier.actions.ProcessManagement;
import myorg.domain.exceptions.DomainException;
import myorg.domain.util.Money;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.SimplifiedProcedureProcess.ProcessClassification;
import pt.ist.expenditureTrackingSystem.domain.dto.CreateAcquisitionProcessBean;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;
import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixWebFramework.servlets.json.JsonObject;
import pt.ist.fenixWebFramework.struts.annotations.Mapping;

@Mapping(path = "/acquisitionSimplifiedProcedureProcess")
/**
 * 
 * @author Paulo Abrantes
 * @author Luis Cruz
 * 
 */
public class SimplifiedProcedureProcessAction extends RegularAcquisitionProcessAction {

    public ActionForward prepareCreateAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {
	CreateAcquisitionProcessBean acquisitionProcessBean = getRenderedObject();
	if (acquisitionProcessBean == null) {
	    acquisitionProcessBean = new CreateAcquisitionProcessBean(ProcessClassification.CCP);
	}
	request.setAttribute("acquisitionProcessBean", acquisitionProcessBean);
	return forward(request, "/acquisitions/createAcquisitionProcess.jsp");
    }

    public ActionForward prepareCreateAcquisitionProcessCT10000(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {
	CreateAcquisitionProcessBean acquisitionProcessBean = getRenderedObject();
	if (acquisitionProcessBean == null) {
	    acquisitionProcessBean = new CreateAcquisitionProcessBean(ProcessClassification.CT10000);
	}
	request.setAttribute("acquisitionProcessBean", acquisitionProcessBean);
	return forward(request, "/acquisitions/createAcquisitionProcess.jsp");
    }

    public ActionForward prepareCreateAcquisitionProcessCT75000(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {
	CreateAcquisitionProcessBean acquisitionProcessBean = getRenderedObject();
	if (acquisitionProcessBean == null) {
	    acquisitionProcessBean = new CreateAcquisitionProcessBean(ProcessClassification.CT75000);
	}
	request.setAttribute("acquisitionProcessBean", acquisitionProcessBean);
	return forward(request, "/acquisitions/createAcqusitionProcessCT75000.jsp");
    }

    public ActionForward prepareCreateAcquisitionProcessNormal(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {
	CreateAcquisitionProcessBean acquisitionProcessBean = getRenderedObject();
	if (acquisitionProcessBean == null) {
	    acquisitionProcessBean = new CreateAcquisitionProcessBean(ProcessClassification.NORMAL);
	}
	request.setAttribute("acquisitionProcessBean", acquisitionProcessBean);
	return forward(request, "/acquisitions/createAcquisitionProcessNormal.jsp");
    }

    public ActionForward createNewAcquisitionProcess(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {
	CreateAcquisitionProcessBean createAcquisitionProcessBean = getRenderedObject();
	final Person person = getLoggedPerson();
	createAcquisitionProcessBean.setRequester(person);
	SimplifiedProcedureProcess acquisitionProcess = null;
	try {
	    acquisitionProcess = SimplifiedProcedureProcess.createNewAcquisitionProcess(createAcquisitionProcessBean);
	} catch (DomainException e) {
	    addLocalizedMessage(request, e.getLocalizedMessage());
	    request.setAttribute("acquisitionProcessBean", createAcquisitionProcessBean);
	    return createAcquisitionProcessBean.getClassification().equals(ProcessClassification.CT75000) ? forward(request,
		    "/acquisitions/createAcqusitionProcessCT75000.jsp") : forward(request,
		    "/acquisitions/createAcquisitionProcess.jsp");
	}
	return ProcessManagement.forwardToProcess(acquisitionProcess);
    }

    public ActionForward addSupplierInCreationPostBack(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	CreateAcquisitionProcessBean acquisitionProcessBean = getRenderedObject("bean");
	Supplier supplierToAdd = acquisitionProcessBean.getSupplierToAdd();
	acquisitionProcessBean.addSupplierToList(supplierToAdd);
	acquisitionProcessBean.setSupplierToAdd(null);

	RenderUtils.invalidateViewState("bean");
	request.setAttribute("acquisitionProcessBean", acquisitionProcessBean);
	return forward(request, "/acquisitions/createAcqusitionProcessCT75000.jsp");
    }

    public ActionForward removeSupplierInCreationPostBack(final ActionMapping mapping, final ActionForm form,
	    final HttpServletRequest request, final HttpServletResponse response) {

	String index = request.getParameter("index");
	CreateAcquisitionProcessBean acquisitionProcessBean = getRenderedObject("bean-" + index);
	acquisitionProcessBean.removeSupplierFromList(Integer.valueOf(index).intValue());

	request.setAttribute("acquisitionProcessBean", acquisitionProcessBean);
	return forward(request, "/acquisitions/createAcqusitionProcessCT75000.jsp");
    }

    private static final String SUPPLIER_LIMIT_OK = "SOK";
    private static final String SUPPLIER_LIMIT_NOT_OK = "SNOK";

    public ActionForward checkSupplierLimit(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws IOException {

	if (getLoggedPerson() == null) {
	    return null;
	}

	Supplier supplier = getDomainObject(request, "supplierOid");
	Money softLimit = supplier.getSoftTotalAllocated();
	Money supplierLimit = supplier.getSupplierLimit();

	JsonObject reply = new JsonObject();

	reply.addAttribute("status", softLimit.isGreaterThanOrEqual(supplierLimit) ? SUPPLIER_LIMIT_NOT_OK : SUPPLIER_LIMIT_OK);
	reply.addAttribute("softLimit", softLimit.toFormatStringWithoutCurrency());
	reply.addAttribute("supplierLimit", supplierLimit.toFormatStringWithoutCurrency());

	writeJsonReply(response, reply);

	return null;
    }
}
