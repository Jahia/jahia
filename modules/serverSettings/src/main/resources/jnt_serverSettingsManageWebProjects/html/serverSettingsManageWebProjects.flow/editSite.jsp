<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources>
    <script type="text/javascript">
        $(document).ready(function() {
            $('#${currentNode.identifier}-next').click(workInProgress)
        });
    </script>
</template:addResources>

<h2>Edit site</h2>

<c:if test="${!empty flowRequestContext.messageContext.allMessages}">
            <c:forEach var="error" items="${flowRequestContext.messageContext.allMessages}">
                <div class="alert alert-error">
                    <button type="button" class="close" data-dismiss="alert">&times;</button>
                        ${fn:escapeXml(error.text)}
                </div>
            </c:forEach>
</c:if>
<div class="box-1">
    <form action="${flowExecutionUrl}" method="POST">
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
                        <p><fmt:message key="serverSettings.manageWebProjects.webProject.templateSet"/>: ${fn:escapeXml(selectedSite.templatePackageName)}&nbsp;(${fn:escapeXml(selectedSite.templateFolder)})</p>

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
                        <label for="defaultSite"><fmt:message key="serverSettings.manageWebProjects.webProject.defaultSite"/>:</label>
                        <c:choose>
                            <c:when test="${siteBean.defaultSite}">
                                <p><fmt:message key="serverSettings.manageWebProjects.webProject.isDefault"/></p>
                                <input type="hidden" name="defaultSite" value="true"/>
                            </c:when>
                            <c:otherwise>
                                <input style="margin-bottom:15px;" type="checkbox" name="defaultSite" id="defaultSite" ${siteBean.defaultSite ? 'checked="checked"' : ''}/>
                            </c:otherwise>
                        </c:choose>
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

        </fieldset>
        <div class="container-fluid">
            <div class="row-fluid">
                <div class="span12">
                    <button class="btn btn-primary" type="submit" name="_eventId_next" id="${currentNode.identifier}-next">
                        <i class="icon-ok icon-white"></i>
                        &nbsp;<fmt:message key='label.save'/>
                    </button>
                    <button class="btn" type="submit" name="_eventId_cancel">
                        <i class="icon-ban-circle"></i>
                        &nbsp;<fmt:message key='label.cancel' />
                    </button>
                </div>
            </div>
        </div>

    </form>
</div>