<%--
    Copyright © 2014 Instituto Superior T�cnico

    This file is part of the Internal Billing Module.

    The Internal Billing Module is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The Internal Billing Module is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with MGP Viewer.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="pt.ist.internalBilling.domain.InternalBillingService"%>
<%@page import="org.fenixedu.bennu.core.domain.User"%>
<%@page import="java.util.TreeSet"%>
<%@page import="java.util.Set"%>
<%@page import="org.joda.time.DateTime"%>
<%@page import="java.time.YearMonth"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="module.finance.util.Money"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@page import="pt.ist.expenditureTrackingSystem.domain.organization.Unit"%>
<%@page import="pt.ist.internalBilling.domain.BillableService"%>
<%@page import="pt.ist.internalBilling.domain.Billable"%>
<%@page import="pt.ist.internalBilling.domain.BillableTransaction"%>
<%@page import="java.util.Collection"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<% final String contextPath = request.getContextPath(); %>
<script src='<%= contextPath + "/webjars/jquery-ui/1.11.1/jquery-ui.js" %>'></script>
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
<script src="https://code.highcharts.com/highcharts.js"></script>
<script src="https://code.highcharts.com/modules/heatmap.js"></script>
<script src="https://code.highcharts.com/modules/exporting.js"></script>


<% Unit unit = (Unit) request.getAttribute("unit"); %>
<% Collection<BillableTransaction> transactions = (Collection<BillableTransaction>) request.getAttribute("transactions"); %>
<% Integer year = (Integer) request.getAttribute("year"); %>
<% Integer month = (Integer) request.getAttribute("month"); %>
<% final String view = request.getParameter("view"); %>
<div class="page-header">
    <h2 class="ng-scope">
        <spring:message code="label.internalBilling.transactions" text="Movimentos"/>
        <%= year %>/<%= (month < 10 ? "0" : "") + month %>
        :
        <%= unit.getPresentationName() %>
    </h2>
</div>
<div class="page-body">

<ul class="nav nav-tabs">
    <li <% if (view == null || view.isEmpty()) { %>class="active"<% } %>>
<a href="<%= contextPath + "/internalBilling/unit/" + unit.getExternalId() + "/transactions/" + year + "-" + (month < 10 ? "0" : "") + month %>">
    <spring:message code="label.internalBilling.transactions.all" text="All"/>
</a>
    </li>
    <li <% if ("byDay".equals(view)) { %>class="active"<% } %>>
<a href="<%= contextPath + "/internalBilling/unit/" + unit.getExternalId() + "/transactions/" + year + "-" + (month < 10 ? "0" : "") + month + "?view=byDay"  %>">
    <spring:message code="label.internalBilling.transactions.byDay" text="By Day"/>
</a>
    </li>
    <li <% if ("byUser".equals(view)) { %>class="active"<% } %>>
<a href="<%= contextPath + "/internalBilling/unit/" + unit.getExternalId() + "/transactions/" + year + "-" + (month < 10 ? "0" : "") + month + "?view=byUser"  %>">
    <spring:message code="label.internalBilling.transactions.byUser" text="By User"/>
</a>
    </li>
</ul>

<br/>

<% if (InternalBillingService.canViewUnitServices(unit)) { %>

<% if ("byUser".equals(view)) { %>
    <% final Set<User> users = new TreeSet<User>(User.COMPARATOR_BY_NAME); %>
    <% for (final BillableTransaction transaction : transactions) { %>
        <% users.add(transaction.getUser()); %>
    <% } %>

<div id="container" style="height: 400px; min-width: 310px; max-width: 800px; margin: 0 auto"></div>

<script type="text/javascript">

$(function () {

    $(document).ready(function () {

        // Build the chart
        Highcharts.chart('container', {
            chart: {
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false,
                type: 'pie'
            },
            title: {
                text: ''
            },
            tooltip: {
                pointFormat: '{series.name}: <b>{point.y}</b> ({point.percentage:.1f}%)'
            },
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: false
                    },
                    showInLegend: true
                }
            },
            series: [{
                name: '<spring:message code="label.internalBilling.transactions" text="Transactions"/>',
                colorByPoint: true,
                point: {
                    events: {
                        click: function(e) {
                        	var divId = 'details' + this.user;
                        	toggle_visibility(divId);
                        }
                    }
                },
                data: [<% boolean isFirst = true;
                          for (final User user : users) {
                             Money total = Money.ZERO;
                             for (final BillableTransaction transaction : transactions) {
                                 if (transaction.getUser() == user) {
                                     total = total.add(transaction.getValue());
                                 }
                             }
                             if (isFirst) isFirst = false; else {%>,<%}%>{name:'<%= user.getProfile().getDisplayName() %>', y: <%= total.toFormatStringWithoutCurrency().replace(',', '.') %>, user: '<%= user.getUsername() %>'}
                       <% }%>]
            }]
        });
    });
});

</script>

    <% for (final User user : users) { %>
        <div class="panel panel-default">
            <div class="panel-heading" onclick="toggle_visibility('details<%= user.getUsername() %>');">
                <img class="img-circle" width="30" height="30" alt="" src="<%= user.getProfile().getAvatarUrl() %>">
                <a href="<%= contextPath + "/internalBilling/user/" + user.getExternalId() %>">
                    <%= user.getProfile().getDisplayName() %>
                </a>
                <a class="btn btn-default" href="#" style="float: right; margin-left: 5px;">
                    <spring:message code="label.internalBilling.billing.details.details" text="Details"/>
                </a>
            </div>
            <div class="panel-body" style="display: none;" id="details<%= user.getUsername() %>">
        <table class="table">
            <thead>
                <tr>
                    <th width="11%">
                        <spring:message code="label.internalBilling.billing.details.txDate" text="Transaction Date"/>
                    </th>
                    <th>
                        <spring:message code="label.internalBilling.billing.details.value" text="Value"/>
                    </th>
                    <th width="14%">
                        <spring:message code="label.internalBilling.billing.details.label" text="Description"/>
                    </th>
                    <th>
                        <spring:message code="label.internalBilling.billing.details.details" text="Details"/>
                    </th>
                </tr>
            </thead>
            <tbody>
                <% for (final BillableTransaction transaction : transactions) { %>
                    <% if (transaction.getUser() == user) { %>
                    <% final Billable billable = transaction.getBillable(); %>
                    <% final BillableService service = billable == null ? null : billable.getBillableService(); %>
                    <tr>
                        <td>
                            <%= transaction.getTxDate().toString("yyyy-MM-dd HH:mm") %>
                        </td>
                        <td>
                            <%= transaction.getValue().getValue().toString() %>
                        </td>
                        <td>
                            <%= transaction.getLabel() %>
                        </td>
                        <td>
                            <%= transaction.getDescription() %>
                        </td>
                    </tr>
                    <% } %>   
                <% } %>
            </tbody>
        </table>
            </div>
        </div>
    <% } %>

<% } else if ("byDay".equals(view)) { %>

        <div id="container" style="height: 400px; min-width: 310px; max-width: 800px; margin: 0 auto"></div>

        <% final Map<String, Money> dayMap = new TreeMap<String, Money>(); %>
        <% for (final BillableTransaction transaction : transactions) { %>
                <% final int day = transaction.getTxDate().getDayOfMonth(); %>
                <% final String key = (day < 10 ? "0" : "") + day; %>
                <% final Money value = transaction.getValue(); %>
                <% if (!dayMap.containsKey(key)) { %>
                    <% dayMap.put(key, Money.ZERO); %>
                <% } %>
                <% dayMap.put(key, dayMap.get(key).add(value)); %>
        <% } %>
        <% for (int i = 1; i <= YearMonth.of(year, month).lengthOfMonth(); i++) { %>
    <div id="day<%= (i < 10 ? "0" : "") + i %>" style="display: none;" class="dayDiv">
        <table class="table">
            <thead>
                <tr>
                    <th width="11%">
                        <spring:message code="label.internalBilling.billing.details.txDate" text="Transaction Date"/>
                    </th>
                    <th>
                        <spring:message code="label.internalBilling.billing.details.value" text="Value"/>
                    </th>
                    <th width="14%">
                        <spring:message code="label.internalBilling.billing.details.label" text="Description"/>
                    </th>
                    <th>
                        <spring:message code="label.internalBilling.billing.details.details" text="Details"/>
                    </th>
                    <th width="35%">
                        <spring:message code="label.internalBilling.billing.details.user" text="User"/>
                    </th>
                </tr>
            </thead>
            <tbody>
                <% for (final BillableTransaction transaction : transactions) { %>
                    <% final DateTime dt = transaction.getTxDate(); %>
                    <% if (dt.getDayOfMonth() == i) { %>
                    <% final Billable billable = transaction.getBillable(); %>
                    <% final BillableService service = billable == null ? null : billable.getBillableService(); %>
                    <tr>
                        <td>
                            <%= transaction.getTxDate().toString("yyyy-MM-dd HH:mm") %>
                        </td>
                        <td>
                            <%= transaction.getValue().getValue().toString() %>
                        </td>
                        <td>
                            <%= transaction.getLabel() %>
                        </td>
                        <td>
                            <%= transaction.getDescription() %>
                        </td>
                        <td>
                            <img class="img-circle" width="30" height="30" alt=""
                                    src="<%= transaction.getUser().getProfile().getAvatarUrl() %>">
                                <a href="<%= contextPath + "/internalBilling/user/" + transaction.getUser().getExternalId() %>">
                                    <%= transaction.getUser().getProfile().getDisplayName() %>
                                </a>
                        </td>
                    </tr>
                    <% } %>   
                <% } %>
            </tbody>
        </table>
    </div>
        <% } %>


        <script type="text/javascript">
        $(function () {

            Highcharts.chart('container', {
                chart: {
                    type: 'column'
                },
                title: {
                    text: ''
                },
                xAxis: {
                    title: {
                        text: '<spring:message code="label.internalBilling.transactions.dayOfMonth" text="Day of Month"/>'
                    },
                    categories: [<%  boolean isFirstKey = true;
                                     for (final String key : dayMap.keySet()) {
                                 %><% if (isFirstKey) { isFirstKey = false; } else {%>,<% }%>'<%= key %>'<% } %>]
                },
                yAxis: {
                    min: 0,
                    title: {
                        text: '<spring:message code="label.internalBilling.transactions.value" text="Value"/>'
                    },
                    stackLabels: {
                        enabled: true,
                        style: {
                            fontWeight: 'bold',
                            color: (Highcharts.theme && Highcharts.theme.textColor) || 'gray'
                        }
                    }
                },
                legend: {
                	enabled: false
                },
                tooltip: {
                    headerFormat: '<b><spring:message code="label.internalBilling.transactions.dayOfMonth" text="Day of Month"/>: {point.x}</b><br/>',
                    pointFormat: '<spring:message code="label.internalBilling.transactions.value" text="Value"/>: {point.stackTotal}'
                },
                plotOptions: {
                    column: {
                        stacking: 'normal'
                    }
                },
                series: [{
                    name: 'Service',
                    point: {
                        events: {
                            click: function(e) {
                                var elements = document.getElementsByClassName('dayDiv')
                                for (var i = 0; i < elements.length; i++) {
                                    elements[i].style.display = 'none';
                                }
                            	document.getElementById('day' + this.category).style.display = "block";
                            }
                        }
                    },
                    data: [<%  boolean isFirstValue = true;
                    for (final Money value : dayMap.values()) {
                        %><% if (isFirstValue) { isFirstValue = false; } else {%>,<% }%><%= value.getValue().toString() %><% } %>]
                }]
            });

        });
        </script>

<% } else { %>
    <div>
        <table class="table">
            <thead>
                <tr>
                    <th width="11%">
                        <spring:message code="label.internalBilling.billing.details.txDate" text="Transaction Date"/>
                    </th>
                    <th>
                        <spring:message code="label.internalBilling.billing.details.value" text="Value"/>
                    </th>
                    <th width="14%">
                        <spring:message code="label.internalBilling.billing.details.label" text="Description"/>
                    </th>
                    <th>
                        <spring:message code="label.internalBilling.billing.details.details" text="Details"/>
                    </th>
                    <th width="35%">
                        <spring:message code="label.internalBilling.billing.details.user" text="User"/>
                    </th>
                </tr>
            </thead>
            <tbody>
                <% for (final BillableTransaction transaction : transactions) { %>
                    <% final Billable billable = transaction.getBillable(); %>
                    <% final BillableService service = billable == null ? null : billable.getBillableService(); %>
                    <tr>
                        <td>
                            <%= transaction.getTxDate().toString("yyyy-MM-dd HH:mm") %>
                        </td>
                        <td>
                            <%= transaction.getValue().getValue().toString() %>
                        </td>
                        <td>
                            <%= transaction.getLabel() %>
                        </td>
                        <td>
                            <%= transaction.getDescription() %>
                        </td>
                        <td>
                            <img class="img-circle" width="30" height="30" alt=""
                                    src="<%= transaction.getUser().getProfile().getAvatarUrl() %>">
                                <a href="<%= contextPath + "/internalBilling/user/" + transaction.getUser().getExternalId() %>">
                                    <%= transaction.getUser().getProfile().getDisplayName() %>
                                </a>
                        </td>
                    </tr>   
                <% } %>
            </tbody>
        </table>
    </div>
<% } %>

<% } %>

</div>

<script type="text/javascript">
function toggle_visibility(id) {
    var e = document.getElementById(id);
    if(e.style.display == 'block')
       e.style.display = 'none';
    else
       e.style.display = 'block';
 }
</script>