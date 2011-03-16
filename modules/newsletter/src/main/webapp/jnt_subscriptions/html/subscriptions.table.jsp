<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:include view="hidden.header"/>

<h1><fmt:message key="jnt_subscriptions"/>:&nbsp;${moduleMap.listTotalSize}</h1>

<table width="100%" cellspacing="0" cellpadding="5" border="0" class="table">
    <thead>
    <tr>
        <th width="2%" style="white-space: nowrap; text-align: center;">#</th>
        <th width="28%"><fmt:message key="jnt_subscription.j_subscriber"/></th>
        <th width="25%"><fmt:message key="org.jahia.admin.lastName.label"/></th>
        <th width="25%"><fmt:message key="org.jahia.admin.firstName.label"/></th>
        <th width="10%" style="white-space: nowrap; text-align: center;"><fmt:message key="jnt_subscription.j_confirmed"/></th>
        <th width="10%" style="white-space: nowrap; text-align: center;"><fmt:message key="jnt_subscription.j_suspended"/></th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${moduleMap.currentList}" var="subscription" begin="${moduleMap.begin}" end="${moduleMap.end}" varStatus="status">
    	<c:set var="subscriber" value="${subscription.propertiesAsString['j:subscriber']}"/>
    	<c:if test="${not empty subscription.propertiesAsString['j:provider']}">
    		<c:set var="userKey" value="{${not empty subscription.propertiesAsString['j:provider']}}${subscription.propertiesAsString['j:subscriber']}"/>
    		<c:set var="registeredUser" value="${functions:lookupUser(userKey)}"/>
    	</c:if>
        <tr class="${status.count % 2 == 0 ? 'even' : 'odd'}">
        <td align="center">
        	${status.count}
        </td>
        <td>
            <div class="jahia-template-gxt" jahiatype="module" id="subscription-${subscription.identifier}" type="existingNode"
                 scriptInfo="" path="${subscription.path}" template="hidden.system" dragdrop="false">
              	${fn:escapeXml(not empty registeredUser ? registeredUser.username : subscriber)}
            </div>
        </td>
        <td>
        	${not empty registeredUser ? registeredUser.userProperties.properties['j:lastName'] : subscription.propertiesAsString['j:lastName']}
        </td>
        <td>
        	${not empty registeredUser ? registeredUser.userProperties.properties['j:firstName'] : subscription.propertiesAsString['j:firstName']}
        </td>
        <td>
            ${subscription.properties['j:confirmed'].boolean}
        </td>
        <td>
            ${subscription.properties['j:suspended'].boolean}
        </td>
        </tr>
    </c:forEach>

    <c:if test="${not omitFormatting}">
        <div class="clear"></div>
    </c:if>
    </tbody>
</table>

<c:if test="${moduleMap.editable and renderContext.editMode}">
    <template:module path="*"/>
</c:if>

<template:include view="hidden.footer"/>
