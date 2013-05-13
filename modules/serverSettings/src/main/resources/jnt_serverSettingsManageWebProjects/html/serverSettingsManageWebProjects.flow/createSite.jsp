<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<template:addResources type="javascript" resources="jquery.min.js,admin-bootstrap.js"/>

<c:if test="${!empty flowRequestContext.messageContext.allMessages}">
            <c:forEach var="error" items="${flowRequestContext.messageContext.allMessages}">
                <div class="alert alert-error">
                    <button type="button" class="close" data-dismiss="alert">&times;</button>
                        ${fn:escapeXml(error.text)}
                </div>
            </c:forEach>
</c:if>
<div class="box-1">
    <form id="createSiteForm" action="${flowExecutionUrl}" method="POST">
        <h2><fmt:message key="serverSettings.manageWebProjects.createWebProject"/></h2>

        <fieldset>
            <div class="container-fluid">
                <div class="row-fluid">
                    <div class="span4">
                        <label for="title"><fmt:message key="label.name"/> <span class="text-error"><strong>*</strong></span> </label>
                        <input class="span12" type="text" id="title" name="title" value="${fn:escapeXml(siteBean.title)}"/>
                    </div>
                    <div class="span4">
                        <label for="serverName"><fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/> <span class="text-error"><strong>*</strong></span> </label>
                        <input class="span12" type="text" id="serverName" name="serverName" value="${fn:escapeXml(siteBean.serverName)}"/>
                    </div>
                </div>
                <div class="row-fluid">
                    <div class="span4">
                        <label for="siteKey"><fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/> <span class="text-error"><strong>*</strong></span> </label>
                        <input class="span12" type="text" id="siteKey" name="siteKey" value="${fn:escapeXml(siteBean.siteKey)}"/>
                    </div>
                    <div class="span4">
                        <label for="description"><fmt:message key="label.description"/></label>
                        <textarea class="span12" id="description" name="description">${fn:escapeXml(siteBean.description)}</textarea>
                    </div>
                </div>
                <div class="row-fluid">
                    <div class="span4">
                        <label for="defaultSite">
                            <input type="checkbox" name="defaultSite" id="defaultSite" <c:if test="${siteBean.defaultSite}">checked="checked"</c:if> /> <fmt:message key="serverSettings.manageWebProjects.webProject.defaultSite"/>
                        </label>
                        <input type="hidden" name="_defaultSite"/>
                    </div>
                    <div class="span4">
                        <label for="createAdmin">
                            <input type="checkbox" name="createAdmin" id="createAdmin" <c:if test="${siteBean.createAdmin}">checked="checked"</c:if> /> <fmt:message key="serverSettings.manageWebProjects.webProject.createAdmin"/>
                        </label>
                        <input type="hidden" name="_createAdmin"/>
                    </div>
                </div>
            </div>
        </fieldset>
        <div class="container-fluid">
            <div class="row-fluid">
                <div class="span12">
                    <button class="btn btn-primary" type="submit" name="_eventId_next">
                        <i class="icon-chevron-right icon-white"></i>
                        &nbsp;<fmt:message key='label.next'/>
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