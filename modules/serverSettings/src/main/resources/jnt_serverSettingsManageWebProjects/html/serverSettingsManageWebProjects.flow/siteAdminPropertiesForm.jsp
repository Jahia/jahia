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
                        ${error.text}
                </div>
            </c:forEach>
</c:if>
<div class="box-1">
    <form action="${flowExecutionUrl}" method="POST">
        <h2><fmt:message key="serverSettings.manageWebProjects.createWebProject"/></h2>
        <fieldset>
            <div class="container-fluid">
                <div class="row-fluid">
                    <div class="span4">
                        <label for="username"><fmt:message key="label.username"/></label>
                        <input class="span12" type="text" id="username" value="${siteBean.adminProperties.username}" name="username"/>
                    </div>
                    <div class="span4">
                        <label for="firstName"><fmt:message key="label.firstName"/></label>
                        <input class="span12" type="text" id="firstName" value="${siteBean.adminProperties.firstName}" name="firstName"/>
                    </div>
                </div>

                <div class="row-fluid">
                    <div class="span4">
                        <label for="lastName"><fmt:message key="label.lastName"/></label>
                        <input class="span12" type="text" id="lastName" value="${siteBean.adminProperties.lastName}" name="lastName"/>
                    </div>
                    <div class="span4">
                        <label for="email"><fmt:message key="label.email"/></label>
                        <input class="span12" type="text" id="email" value="${siteBean.adminProperties.email}" name="email"/>
                    </div>
                </div>
                <div class="row-fluid">
                    <div class="span4">
                        <label for="organization"><fmt:message key="label.organization"/></label>
                        <input class="span12" type="text" id="organization" value="${siteBean.adminProperties.organization}" name="organization" />
                    </div>
                    <div class="span4">
                        <label for="password"><fmt:message key="label.password"/></label>
                        <input class="span12" type="password" id="password" name="password" autocomplete="off"/>

                        <label for="passwordConfirm"><fmt:message key="label.confirmPassword"/></label>
                        <input class="span12" type="password" id="passwordConfirm" name="passwordConfirm" autocomplete="off"/>
                    </div>
                </div>
            </div>
        </fieldset>
        <div class="row-fluid">
            <div class="span12">
                <button class="btn" type="submit" name="_eventId_previous">
                    <i class="icon-chevron-left"></i>
                    &nbsp;<fmt:message key='label.previous'/>
                </button>
                <button class="btn btn-primary" type="submit" name="_eventId_next">
                    <i class="icon-chevron-right icon-white"></i>
                    &nbsp;<fmt:message key='label.next'/>
                </button>
            </div>
        </div>
    </form>
</div>