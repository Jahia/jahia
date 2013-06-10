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
<%--@elvariable id="searchCriteria" type="org.jahia.modules.sitesettings.groups.SearchCriteria"--%>

<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js"/>
<template:addResources type="css" resources="admin-bootstrap.css"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>


<template:addResources>
<script type="text/javascript">
    $(document).ready(function () {
        $(".needGroupSelection").submit(function () {
            var selected = $("input[name='groupSelected']:checked").val();
            if(undefined==selected) {
                <fmt:message key="siteSettings.groups.selectGroup" var="i18nSelectGroup"/>
                alert('${functions:escapeJavaScript(i18nSelectGroup)}');
                return false;
            }
            $("input[name='selectedGroups']").val(selected);
            return true;
        })
    });
</script>
</template:addResources>

<c:set var="site" value="${renderContext.mainResource.node.resolveSite}"/>

<h2><fmt:message key="label.manageGroups"/> - ${fn:escapeXml(site.displayableName)}</h2>

<div class="box-1">
    <form class="form-inline " action="${flowExecutionUrl}" id="searchForm" method="post">
        <input type="hidden" id="searchIn" name="searchIn" value="allProps"/>
        <fieldset>
            <h2><fmt:message key="label.search"/></h2>
            <div class="input-append">
                <label style="display: none;"  for="searchString"><fmt:message key="label.search"/></label>
                <input class="span6" type="text" id="searchString" name="searchString"
                       value='${searchCriteria.searchString}'
                       onkeydown="if (event.keyCode == 13) submitForm('search');"/>
                <button class="btn btn-primary" type="submit"  name="_eventId_search">
                    <i class="icon-search icon-white"></i>
                    &nbsp;<fmt:message key='label.search'/>
                </button>
            </div>
            <br/>
            <br/>
            <c:if test="${fn:length(providers) > 0}"><span style="color: red">!!!CHANGE THIS TO > 1</span>
                <label for="storedOn"><span class="badge badge-info"><fmt:message key="label.on"/></span></label>
                <input type="radio" name="storedOn" value="everywhere" 
                    ${empty searchCriteria.storedOn || searchCriteria.storedOn == 'everywhere' ? ' checked="checked" ' : ''}   
                    onclick="$('.provCheck').attr('disabled',true);">&nbsp;<fmt:message
                    key="label.everyWhere"/>

                <input type="radio" name="storedOn" value="providers"
                       ${searchCriteria.storedOn == 'providers' ? 'checked="checked"' : ''}
                       onclick="$('.provCheck').removeAttr('disabled');"/>&nbsp;<fmt:message
                    key="label.providers"/>
                    
                <c:forEach items="${providers}" var="curProvider">
                    <input type="checkbox" class="provCheck" name="providers" value="${curProvider.key}"
                           ${searchCriteria.storedOn != 'providers' ? 'disabled="disabled"' : ''}
                           ${empty searchCriteria.providers || functions:contains(searchCriteria.providers, curProvider.key) ? 'checked="checked"' : ''}/>
                    <fmt:message var="i18nProviderLabel" key="providers.${curProvider.key}.label"/>
                    ${fn:escapeXml(fn:contains(i18nProviderLabel, '???') ? curProvider.key : i18nProviderLabel)}
                </c:forEach>
            </c:if>
        </fieldset>
    </form>
</div>


<div>
    <div>
        <form action="${flowExecutionUrl}" method="POST" style="display: inline;">
            <button class="btn" type="submit" name="_eventId_createGroup" >
                <i class="icon-plus"></i>
                &nbsp;<fmt:message key="siteSettings.groups.create"/>
            </button>
        </form>
        <form action="${flowExecutionUrl}" method="POST" class="needGroupSelection" style="display: inline;">
            <input type="hidden" name="selectedGroups"/>
            <button class="btn" type="submit" name="_eventId_editGroup" >
                <i class="icon-edit"></i>
                &nbsp;<fmt:message key="siteSettings.groups.edit"/>
            </button>
        </form>
        <form action="${flowExecutionUrl}" method="POST" class="needGroupSelection" style="display: inline;">
            <input type="hidden" name="selectedUsers"/>
            <button class="btn" type="submit" name="_eventId_removeGroup" >
                <i class="icon-remove"></i>
                &nbsp;<fmt:message key="siteSettings.groups.remove"/>
            </button>
        </form>
    </div>
    
    <p>
        <c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
        <c:if test="${message.severity eq 'INFO'}">
        <div class="alert alert-success">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
                ${message.text}
        </div>
        </c:if>
        <c:if test="${message.severity eq 'ERROR'}">
            <div class="alert alert-error">
                <button type="button" class="close" data-dismiss="alert">&times;</button>
                    ${message.text}
            </div>
        </c:if>
        </c:forEach>
    </p>

    <div>
        <table class="table table-bordered table-striped table-hover">
            <thead>
            <tr>
                <th width="5%">&nbsp;</th>
                <th width="50%" class="sortable"><fmt:message key="label.name"/></th>
                <th width="45%" class="sortable"><fmt:message key="label.properties"/></th>
            </tr>
            </thead>
            <tbody>
            <c:choose>
                <%--@elvariable id="groups" type="java.util.List"--%>
                <c:when test="${fn:length(groups) == 0}">
                    <tr>
                        <td colspan="3"><fmt:message key="label.noResults"/></td>
                    </tr>
                </c:when>
                <c:otherwise>
                    <c:forEach items="${groups}" var="grp">
                        <tr class="sortable-row">
                            <td><input type="radio" name="groupSelected" value="${fn:escapeXml(grp.groupKey)}"></td>
                            <td>${user:displayName(grp)}</td>
                            <td>???</td>
                        </tr>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
            </tbody>
        </table>
    </div>
</div>
