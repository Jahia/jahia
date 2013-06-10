<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--@elvariable id="flowRequestContext" type="org.springframework.webflow.execution.RequestContext"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js,admin-bootstrap.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<div>
    <p>
        <c:forEach items="${flowRequestContext.messageContext.allMessages}" var="message">
            <c:if test="${message.severity eq 'ERROR'}">
                <div class="alert alert-error">
                    <button type="button" class="close" data-dismiss="alert">&times;</button>
                        ${message.text}
                </div>
            </c:if>
        </c:forEach>
    </p>
    <h2><fmt:message key="siteSettings.groups.create"/></h2>
    <div class="box-1">
        <form action="${flowExecutionUrl}" method="post" autocomplete="off">
            <fieldset title="<fmt:message key="serverSettings.user.profile"/>">
                <div class="container-fluid">
                    <div class="row-fluid">
                        <p>
                            <fmt:message key="label.noteThat"/>:
                            <ul>
                                <li><fmt:message key="siteSettings.groups.errors.groupname.unique"/></li>
                                <li><fmt:message key="siteSettings.groups.errors.groupname.syntax"/></li>
                            </ul>
                        </p>
                    </div>
                    <div class="row-fluid">
                        <div class="span4">
                            <label for="groupname"><fmt:message key="label.name"/> <span class="text-error"><strong>*</strong></span></label>
                            <input type="text" name="groupname" class="span12" id="groupname" value="${fn:escapeXml(group.groupname)}"/>
                        </div>
                    </div>
                </div>
            </fieldset>
            
            <fieldset>
                <div class="container-fluid">
                    <div class="row-fluid">
                        <div class="span12">
                            <button class="btn btn-primary" type="submit" name="_eventId_add">
                                <i class="icon-plus icon-white"></i>
                                &nbsp;<fmt:message key="label.add"/>
                            </button>
                            <button class="btn" type="submit" name="_eventId_cancel">
                                <i class="icon-ban-circle"></i>
                                &nbsp;<fmt:message key="label.cancel"/>
                            </button>
                        </div>
                    </div>
                </div>

            </fieldset>
        </form>
    </div>
</div>