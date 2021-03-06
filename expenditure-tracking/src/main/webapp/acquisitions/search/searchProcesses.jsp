<%@page import="pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType"%>
<%@page import="pt.ist.expenditureTrackingSystem.domain.ExpenditureTrackingSystem"%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/fenix-renderers" prefix="fr"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic"%>
<%@ taglib uri="http://fenix-ashes.ist.utl.pt/collection-pager" prefix="cp"%>


<%@page import="pt.ist.expenditureTrackingSystem.domain.acquisitions.search.SearchPaymentProcess"%>

<div class="helpicon" title="Ajuda">
	<a href="https://fenix-ashes.ist.utl.pt/fenixWiki/Qualidade/Aquisicoes/PesquisaAquisicoes" target="_blank"><img src="<%= request.getContextPath() + "/images/icon_help.gif" %>"></a>
</div>

<h2><bean:message key="process.label.searchProcesses" bundle="EXPENDITURE_RESOURCES"/></h2>

<bean:define id="schema" value="search.default" toScope="request" />

<logic:present name="searchBean" property="searchClass">
	<bean:define id="simpleName" name="searchBean"
		property="searchClass.simpleName" />
	<bean:define id="schema" value='<%= "search." + simpleName%>'/>
</logic:present>

<div class="searchoptions">
	<table style="width: 70%">
		<tr>
			<td>
				<fr:form id="mySearchesForm" action="/search.do">
					<html:hidden property="method" value="mySearches"/>
					<fr:edit id="mySearches" name="mySearches" schema='viewMySavedSearches'>
						<fr:layout name="tabular">
							<fr:property name="classes" value=""/>
						</fr:layout>
						<fr:destination name="mySearch" path="/search.do?method=mySearches"/>
					</fr:edit>
				</fr:form>
			</td>
			<td>
				| <html:link page="/search.do?method=configurateMySearches"><bean:message key="link.configureSearches" bundle="EXPENDITURE_RESOURCES"/></html:link> | <a href="#" onClick="javascript:document.getElementById('advancedSearch').style.display='block'"><bean:message key="link.advancedSearch" bundle="EXPENDITURE_RESOURCES"/> »</a>
			</td>
		</tr>
	</table>
</div>

<div id="advancedSearch" style="display: none;">
<div class="mbottom15" style="border: 3px solid #eaeaea; background: #fafafa; padding: 0.5em 1em 1em 1em;">
	
	<span class="color888 smalltxt">Para mais opções de pesquisa, seleccione o tipo de aquisição.</span>
	
	<fr:form id="searchBeanForm" action="/search.do">
		<html:hidden property="method" value="search"/>
		<fr:edit id="searchBean" name="searchBean" schema='<%= schema %>' >
			<fr:layout name="tabular">
				<fr:property name="classes" value="form"/>
			</fr:layout>
			<fr:destination name="typeSelector" path="/search.do?method=changeSelectedClass"/>
		</fr:edit>
		<html:submit styleClass="inputbutton"><bean:message key="button.search" bundle="EXPENDITURE_RESOURCES"/> </html:submit> | <a href="#" onClick="javascript:document.getElementById('advancedSearch').style.display='none'">« <bean:message key="link.closeAdvancedSearch" bundle="EXPENDITURE_RESOURCES"/></a>
	</fr:form>
</div>
</div>

<logic:equal name="advanced" value="true">
	<script type="text/javascript">
		 document.getElementById('advancedSearch').style.display='block';
	</script>
</logic:equal>


<logic:notEmpty name="results">
	<bean:size id="listSize" name="collectionPager" property="collection"/>
		<bean:define id="pagerString" name="pagerString"/>
		
	<p class="mvert05">
		<logic:present name="mySearches" property="selectedSearch">
			<a href="#" onclick="javascript:
				var form = document.getElementById('mySearchesForm');
				var oldMethod = form.method.value;
				form.method.value='exportMySearchToExcel';
				form.submit();
				form.method.value=oldMethod">
				
				<img border="0" src="<%= request.getContextPath() + "/images/excel.gif" %>">
				<bean:message key="link.xlsFileToDownload" bundle="ACQUISITION_RESOURCES"/>
			</a>
		</logic:present>
		<logic:notPresent name="mySearches" property="selectedSearch">
			<a href="#" onclick="javascript:
				var form = document.getElementById('searchBeanForm');
				var oldMethod = form.method.value;
				form.method.value='exportCurrentSearchToExcel';
				form.submit();
				form.method.value=oldMethod">
				
				<img border="0" src="<%= request.getContextPath() + "/images/excel.gif" %>">
				<bean:message key="link.xlsFileToDownload" bundle="ACQUISITION_RESOURCES"/>
			</a>
		</logic:notPresent>
		<% if (ExpenditureTrackingSystem.isManager() || ExpenditureTrackingSystem.isAcquisitionCentralManagerGroupMember()
		        || ExpenditureTrackingSystem.isAcquisitionCentralGroupMember()) { %>
		<%
		    	final SearchPaymentProcess sb = (SearchPaymentProcess) request.getAttribute("searchBean");
		    	if (sb.getAcquisitionProcessStateType() != null && sb.getAcquisitionProcessStateType() == AcquisitionProcessStateType.SUBMITTED_FOR_FUNDS_ALLOCATION
			    		&& ExpenditureTrackingSystem.getInstance().processesNeedToBeReverified()) {
		%>
						<html:link page="/acquisitionProcess.do?method=allocateAllPendingFundsToSupplier">
							<bean:message key="link.allocateAllPendingFundsToSupplier" bundle="ACQUISITION_RESOURCES"/>
						</html:link>
		<%
				}
			}
		%>
	</p>


	<table class="width100pc">
	<tr>
	<td>
		<em><bean:message key="label.numberOfFoundProcesses" bundle="ACQUISITION_RESOURCES" arg0="<%= listSize.toString() %>"/>.</em> 
		<logic:equal name="searchBean" property="searchObjectAvailable" value="false">
			<a href="#" onClick="javascript:document.getElementById('saveSearch').style.display='block'"><bean:message key="label.saveSearch" bundle="EXPENDITURE_RESOURCES"/></a>
		</logic:equal>
	</td>
	<td class="aright">
	<cp:collectionPages url='<%= "/search.do?method=searchJump" + pagerString + "&sortBy=" + (request.getParameter("sortBy") != null ? request.getParameter("sortBy") : "") %>' 
			pageNumberAttributeName="pageNumber" numberOfPagesAttributeName="numberOfPages" numberOfVisualizedPages="10"/>
	</td>
	</tr>
	</table>
	
	<div id="saveSearch" style="display: none;">
		<logic:present name="invalidName">
		<div class="error1">
			<span><bean:message key="message.info.mustHaveAName" bundle="EXPENDITURE_RESOURCES"/></span>
		</div>
			<script type="text/javascript">
				 document.getElementById('saveSearch').style.display='block';
			</script>
		</logic:present>
		<div class="infobox_strong">
			<div style="padding: 0.5em 0;">
				<fr:form id="saveForm" action="/search.do?method=saveSearch">
					<fr:edit id="beanToSave" name="searchBean" visible="false"/>
			 		<bean:message key="label.name" bundle="EXPENDITURE_RESOURCES"/>:
			 		<fr:edit id="searchName" name="savingName" slot="string">
				 		<fr:layout>
							<fr:property name="size" value="40"/>
						</fr:layout>
			 		</fr:edit>
					<html:submit styleClass="inputbutton"><bean:message key="label.save" bundle="EXPENDITURE_RESOURCES"/></html:submit>
					<bean:define id="cancelLabel">
						<bean:message key="renderers.form.cancel.name" bundle="RENDERER_RESOURCES"/>
					</bean:define>
					<input type="button" class="inputbutton" onclick="javascript:document.getElementById('saveSearch').style.display='none';" value="<%= cancelLabel %>"/> 
				</fr:form>
			</div>
		</div>
	</div>


	<fr:view name="results" schema="viewProcessesInList">
			<fr:layout name="tabular-sortable">
				<fr:property name="classes" value="tview1 width100pc"/>
				<fr:property name="columnClasses" value="width30px,,,,,,,,nowrap,,,,,,,,,,,,,,,"/>
				<fr:property name="linkFormat(view)" value="/workflowProcessManagement.do?method=viewProcess&processId=\${externalId}"/>
				<fr:property name="bundle(view)" value="EXPENDITURE_RESOURCES"/>
				<fr:property name="key(view)" value="link.view"/>
				<fr:property name="order(view)" value="1"/>
				
				<fr:property name="sortParameter" value="sortBy"/>
	       		<fr:property name="sortUrl" value='<%= "/search.do?method=searchJump" + pagerString + "&pageNumber=" + (request.getParameter("pageNumber") != null ? request.getParameter("pageNumber") : 1)%>' />
			    <fr:property name="sortBy" value='<%= request.getParameter("sortBy") == null ? "acquisitionProcessId=asc" : request.getParameter("sortBy") %>'/>
				<fr:property name="sortIgnored" value="true"/>					
				<fr:property name="sortableSlots" value="acquisitionProcessId, typeShortDescription, request.requestItemsCount, suppliersDescription, request.requester.firstAndLastName, dateFromLastActivity, request.requestingUnit.name, processStateDescription" />
	
			</fr:layout>
	</fr:view>	
	<p class="aright mtop05">
		<cp:collectionPages url='<%= "/search.do?method=searchJump" + pagerString + "&sortBy=" + (request.getParameter("sortBy") != null ? request.getParameter("sortBy") : "") %>' 
			pageNumberAttributeName="pageNumber" numberOfPagesAttributeName="numberOfPages" numberOfVisualizedPages="10"/>
	</p>
	
</logic:notEmpty>

<logic:empty name="results">
	<p><em><bean:message key="process.label.searchResultEmpty" bundle="EXPENDITURE_RESOURCES"/></em></p>
</logic:empty>
