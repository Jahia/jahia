<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<template:addResources type="javascript" resources="jquery.min.js,admin-bootstrap.js"/>


<c:if test="${!empty flowRequestContext.messageContext.allMessages}">
    <c:forEach var="error" items="${flowRequestContext.messageContext.allMessages}">
        <div class="alert alert-error">
            <button type="button" class="close" data-dismiss="alert">&times;</button>
                ${error.text}
        </div>
    </c:forEach>
</c:if>

<div class="box-1">
    <form action="${flowExecutionUrl}" method="POST">
        <c:set var="editingModules" value="${siteBean.editModules}"/>
        <h2><c:choose>
            <c:when test="${not editingModules}">
                <fmt:message key="serverSettings.manageWebProjects.createWebProject"/>
            </c:when>
            <c:otherwise>
                <fmt:message key="serverSettings.manageWebProjects.webProject.selectModules"/>
            </c:otherwise>
        </c:choose>
        </h2>
        <fieldset>
            <c:if test="${not editingModules}">
                <div>
                    <label for="templateSet"><strong><fmt:message key="serverSettings.webProjectSettings.pleaseChooseTemplateSet"/></strong></label>

                    <select class="span6" name="templateSet" id="templateSet">
                        <c:forEach items="${templateSets}" var="module">
                            <option value="${module.id}" ${siteBean.templateSet eq module.id || empty siteBean.templateSet && module.id eq defaultTemplateSetId ? 'selected="true"' : ''}>${module.name}&nbsp;(${module.id})</option>
                        </c:forEach>
                    </select>
                </div>
            </c:if>

            <div>
                <p><strong><fmt:message key="serverSettings.manageWebProjects.webProject.selectModules"/></strong></p>

                <input type="hidden" name="_modules"/>

                <div class="clearfix">
                    <c:forEach items="${modules}" var="module">
                        <div class="moduleListItem">
                            <label style="padding: 5px;" for="${module.id}" title="${module.description}">
                                <input type="checkbox" name="modules" id="${module.id}"
                                       value="${module.id}" ${functions:contains(siteBean.modules,module.id) ? 'checked="true"' : ''} /> ${module.name} (${module.id})
                            </label>
                        </div>
                    </c:forEach>
                </div>
            </div>


            <c:if test="${not editingModules}">
                <div>
                    <label for="language"><strong><fmt:message key="serverSettings.manageWebProjects.webProject.selectLanguage"/></strong></label>

                    <select class="span6" name="language" id="language">
                        <c:forEach items="${allLocales}" var="locale">
                            <option value="${locale}" ${siteBean.language eq locale ? 'selected="true"' : ''}>${locale.displayName}</option>
                        </c:forEach>
                    </select>
                </div>
            </c:if>


        </fieldset>
        <button class="btn" type="submit" name="_eventId_previous">
            <i class="icon-chevron-left"></i>
            &nbsp;<fmt:message key='label.previous'/>
        </button>
        <button class="btn btn-primary" type="submit" name="_eventId_next">
            <i class="icon-chevron-right icon-white"></i>
            &nbsp;<fmt:message key='label.next'/>
        </button>
    </form>
</div>