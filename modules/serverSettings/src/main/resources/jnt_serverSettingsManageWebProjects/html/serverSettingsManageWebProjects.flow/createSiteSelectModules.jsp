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
        <h2><fmt:message key="serverSettings.manageWebProjects.createWebProject"/></h2>
        <fieldset>
                <div>
                    <label for="templateSet"><strong><fmt:message key="serverSettings.webProjectSettings.pleaseChooseTemplateSet"/></strong></label>

                    <select name="templateSet" id="templateSet">
                        <c:forEach items="${allModules}" var="module">
                            <c:if test="${module.moduleType eq 'templatesSet' && module.rootFolder != 'templates-system'}">
                            <option value="${module.rootFolder}" ${siteBean.templateSet eq module.rootFolder ? 'selected="true"' : ''}>${module.name}&nbsp;(${module.rootFolder})</option>
                            </c:if>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <p><strong><fmt:message key="serverSettings.manageWebProjects.webProject.selectModules"/></strong></p>

                    <input type="hidden" name="_modules"/>
                    <div class="clearfix">
                        <c:forEach items="${allModules}" var="module">
                            <c:if test="${module.moduleType ne 'templatesSet' && module.moduleType ne 'system'}">
                                            <div class="moduleListItem" >
                                                <label style="padding: 5px;" for="${module.rootFolder}">
                                                    <input type="checkbox" name="modules" id="${module.rootFolder}" value="${module.rootFolder}" ${functions:contains(siteBean.modules,module.rootFolder) ? 'checked="true"' : ''} /> ${module.name} (${module.rootFolder})
                                                </label>
                                            </div>
                                        </c:if>
                        </c:forEach>
                    </div>
                </div>


                <div>
                    <label for="language"><strong><fmt:message key="serverSettings.manageWebProjects.webProject.selectLanguage"/></strong></label>

                    <select name="language" id="language">
                        <c:forEach items="${allLocales}" var="locale">
                            <option value="${locale}" ${siteBean.language eq locale ? 'selected="true"' : ''}>${locale.displayName}</option>
                        </c:forEach>
                    </select>
                </div>


            </fieldset>
        <input class="btn btn-primary" type="submit" name="_eventId_next" value="<fmt:message key='label.next'/>"/>
        <input class="btn" type="submit" name="_eventId_previous" value="<fmt:message key='label.previous'/>"/>
    </form>
</div>