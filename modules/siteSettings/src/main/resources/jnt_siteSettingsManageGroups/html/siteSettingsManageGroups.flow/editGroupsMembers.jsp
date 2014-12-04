<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="user" uri="http://www.jahia.org/tags/user" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<%--@elvariable id="mailSettings" type="org.jahia.services.mail.MailSettings"--%>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<%--@elvariable id="flowExecutionUrl" type="java.lang.String"--%>
<%--@elvariable id="memberSearchCriteria" type="org.jahia.services.usermanager.SearchCriteria"--%>

<c:set var="prefix" value="g:"/>
<c:set var="displayGroups" value="selected"/>
<%@include file="common/editMembersHead.jsp" %>
<template:addResources type="javascript" resources="datatables/jquery.dataTables.js,i18n/jquery.dataTables-${currentResource.locale}.js,datatables/dataTables.bootstrap-ext.js"/>
<template:addResources type="css" resources="datatables/css/bootstrap-theme.css,tablecloth.css"/>
<template:addResources>
    <script type="text/javascript">
        $(document).ready(function () {
            var oldStart = 0;
            $('#groupMemberships').dataTable({
                "sDom": "<'row-fluid'<'span6'l><'span6'<'refresh_modules'>f>r>t<'row-fluid'<'span6'i><'span6'p>>",
                "iDisplayLength": 25,
                "sPaginationType": "bootstrap",
                "aaSorting": [], //this option disable sort by default, the user steal can use column names to sort the table
                "fnDrawCallback": function (o) {
                    // auto scroll to top on paginate
                    if ( o._iDisplayStart != oldStart ) {
                        var targetOffset = $('#groupMemberships').offset().top;
                        $('html,body').animate({scrollTop: targetOffset}, 350);
                        oldStart = o._iDisplayStart;
                    }
                }
            });
        });
    </script>
</template:addResources>

<form action="${flowExecutionUrl}" method="post" id="saveForm">
    <input id="addedMembers" type="hidden" name="addedMembers"/>
    <input id="removedMembers" type="hidden" name="removedMembers"/>
    <button class="btn btn-primary" type="submit" name="_eventId_save" id="saveButton" disabled="disabled">
        <i class="icon-ok icon-white"></i>
        &nbsp;<fmt:message key="label.save"/>
    </button>
</form>

<div>
    <c:set var="principalsCount" value="${fn:length(principals)}"/>
    <c:set var="principalsFound" value="${principalsCount > 0}"/>

    <table class="table table-bordered table-striped table-hover" id="groupMemberships">
        <thead>
        <tr>
            <th width="2%"><input type="checkbox" name="selectedAllMembers" id="cbSelectedAllMembers"/></th>
            <th><fmt:message key="label.name"/></th>
            <c:if test="${multipleProvidersAvailable}">
                <th width="10%"><fmt:message key="column.provider.label"/></th>
            </c:if>
        </tr>
        </thead>
        <tbody>
        <c:choose>
            <c:when test="${!principalsFound}">
                <tr>
                    <td colspan="${multipleProvidersAvailable ? '3' : '2'}"><fmt:message key="label.noItemFound"/></td>
                </tr>
            </c:when>
            <c:otherwise>
                <c:forEach items="${principals}" var="principal" varStatus="loopStatus">
                    <tr>
                        <td><input class="selectedMember" type="checkbox" name="selectedMembers" value="${principal.groupKey}" ${functions:contains(members, principal) ? 'checked="checked"' : ''}/> </td>
                        <td>
                                ${fn:escapeXml(user:displayName(principal))}
                        </td>
                        <c:if test="${multipleProvidersAvailable}">
                            <fmt:message var="i18nProviderLabel" key="providers.${principal.providerName}.label"/>
                            <td>${fn:escapeXml(fn:contains(i18nProviderLabel, '???') ? principal.providerName : i18nProviderLabel)}</td>
                        </c:if>
                    </tr>
                </c:forEach>
            </c:otherwise>
        </c:choose>
        </tbody>
    </table>
</div>




