<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<template:addResources type="javascript" resources="jquery.min.js,jquery.blockUI.js,workInProgress.js"/>
<fmt:message key="label.workInProgressTitle" var="i18nWaiting"/><c:set var="i18nWaiting" value="${functions:escapeJavaScript(i18nWaiting)}"/>
<fmt:message key="serverSettings.manageWebProjects.warningMsg.serverNameChange" var="i18nServerNameChangeWarn"/><c:set var="i18nServerNameChangeWarn" value="${functions:escapeJavaScript(i18nServerNameChangeWarn)}"/>
<%--@elvariable id="siteBean" type="org.jahia.modules.serversettings.flow.SiteBean"--%>
<template:addResources>
    <script type="text/javascript">
        $(document).ready(function() {
            $("#${currentNode.identifier}-editModules").click(function() {
               $("#${currentNode.identifier}-eventId").val("editModules");
            });

            $("#${currentNode.identifier}-cancel").click(function() {
                $("#${currentNode.identifier}-eventId").val("cancel");
            });

            $("#${currentNode.identifier}-updateSiteForm").submit(function(event) {
                if ($("#${currentNode.identifier}-eventId").val() != 'next'
                        ||'${siteBean.serverName}' == $("#serverName").val()
                        || confirm('${i18nServerNameChangeWarn}')) {
                    workInProgress('${i18nWaiting}');
                    return;
                }
                event.preventDefault();
            });
        });
    </script>
</template:addResources>

<h2><fmt:message key="serverSettings.manageWebProjects.editSite"/></h2>

<c:if test="${!empty flowRequestContext.messageContext.allMessages}">
            <c:forEach var="error" items="${flowRequestContext.messageContext.allMessages}">
                <div class="alert alert-error">
                    <button type="button" class="close" data-dismiss="alert">&times;</button>
                        ${fn:escapeXml(error.text)}
                </div>
            </c:forEach>
</c:if>
<div class="box-1">
    <form id="${currentNode.identifier}-updateSiteForm" action="${flowExecutionUrl}" method="POST">
        <fieldset>

            <div class="container-fluid">
                <div class="row-fluid">
                    <div class="span12">
                        <h3><fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/>: ${fn:escapeXml(siteBean.siteKey)}</h3>
                    </div>
                </div>
            </div>
            <div class="container-fluid">
                <div class="row-fluid">
                    <div class="span4">
                        <label for="title"><fmt:message key="label.name"/><span class="text-error"><strong>*</strong></span>:</label>
                        <input class="span12" type="text" id="title" name="title" value="${fn:escapeXml(siteBean.title)}"/>
                    </div>
                    <div class="span4">
                        <label for="serverName"><fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/><span class="text-error"><strong>*</strong></span>:</label>
                        <input class="span12" type="text" id="serverName" name="serverName" value="${fn:escapeXml(siteBean.serverName)}"/>
                    </div>
                </div>
            </div>

            <div class="container-fluid">
                <div class="row-fluid">
                    <div class="span12">
                        <p><fmt:message key="serverSettings.manageWebProjects.webProject.templateSet"/>: ${fn:escapeXml(siteBean.templatePackageName)}&nbsp;(${fn:escapeXml(siteBean.templateFolder)})</p>

                        <p><fmt:message key="label.modules"/>:</p>
                        <p style="line-height: 2em">
                            <c:forEach items="${siteBean.modulePackages}" var="module" varStatus="loopStatus">
                                <span class="badge badge-info">${module.name}</span>
                            </c:forEach>
                        </p>
                    </div>
                </div>
            </div>

            <div class="container-fluid">
                <div class="row-fluid">
                    <div class="span12">

                        <label for="defaultSite">
                            <c:if test="${numberOfSites > 1}">
                                <input type="checkbox" name="defaultSite" id="defaultSite"
                                       <c:if test="${siteBean.defaultSite}">checked="checked"</c:if> /> <fmt:message key="serverSettings.manageWebProjects.webProject.defaultSite"/>
                            </c:if>
                            <c:if test="${numberOfSites <= 1}">
                                <input type="checkbox" name="defaultSite" id="defaultSite" disabled="disabled" checked="checked"/> <fmt:message
                                    key="serverSettings.manageWebProjects.webProject.isDefault"/>
                            </c:if>
                        </label>


                        <%--<label for="defaultSite"><fmt:message key="serverSettings.manageWebProjects.webProject.defaultSite"/>:</label>
                        <c:choose>
                            <c:when test="${siteBean.defaultSite}">
                                <p><fmt:message key="serverSettings.manageWebProjects.webProject.isDefault"/></p>
                                <input type="hidden" name="defaultSite" value="true"/>
                            </c:when>
                            <c:otherwise>
                                <input style="margin-bottom:15px;" type="checkbox" name="defaultSite" id="defaultSite" ${siteBean.defaultSite ? 'checked="checked"' : ''}/>
                            </c:otherwise>
                        </c:choose>--%>
                    </div>
                </div>
            </div>

            <div class="container-fluid">
                <div class="row-fluid">
                    <div class="span8">
                        <label for="description"><fmt:message key="label.description"/>:</label>
                        <textarea class="span12" id="description" name="description">${fn:escapeXml(siteBean.description)}</textarea>
                    </div>
                </div>
            </div>

            <input type="hidden" id="${currentNode.identifier}-eventId" name="_eventId" value="next" />
        </fieldset>
        <div class="container-fluid">
            <div class="row-fluid">
                <div class="span12">
                    <button class="btn btn-primary" type="submit" id="${currentNode.identifier}-next">
                        <i class="icon-ok icon-white"></i>
                        &nbsp;<fmt:message key='label.save'/>
                    </button>
                    <button class="btn" type="submit" id="${currentNode.identifier}-editModules">
                        <i class="icon-th-large"></i>
                        &nbsp;<fmt:message key='serverSettings.manageWebProjects.webProject.selectModules' />
                    </button>
                    <button class="btn" type="submit" id="${currentNode.identifier}-cancel">
                        <i class="icon-ban-circle"></i>
                        &nbsp;<fmt:message key='label.cancel' />
                    </button>
                </div>
            </div>
        </div>

    </form>
</div>