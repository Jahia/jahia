<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ tag body-content="empty"
        description="Renders subscribe/unsubscribe link, which allows current user to subscribe/unsubscribe to the notifications about changes in the content object."
        %>
<%@ attribute name="source" required="true" type="java.lang.String"
              description="The key of the content object to observe for changes."
        %>
<%@ attribute name="event" required="false" type="java.lang.String"
              description="The event type to catch. If not specified, the contentPublished event is used. [contentPublished]"
        %>
<%@ attribute name="user" required="false" type="java.lang.String"
              description="The username to use for a subscription. If not specified, the current user is used."
        %>
<%@ attribute name="confirmationRequired" required="false" type="java.lang.Boolean"
              description="Does the subscription require confirmation (an e-mail with the confirmation link will be sent first)? [false]"
        %>
<%@ tag dynamic-attributes="attributes" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<c:set target="${attributes}" property="jahiatype" value="subscription"/>
<c:set var="elementId" value="subscription_${source}"/>
<c:set target="${attributes}" property="id" value="${h:default(id, elementId)}"/>
<c:set target="${attributes}" property="source" value="${source}"/>
<c:set target="${attributes}" property="event" value="${h:default(event, 'contentPublished')}"/>
<c:set target="${attributes}" property="user" value="${h:default(user, jahia.user.username)}"/>
<c:set target="${attributes}" property="confirmationRequired" value="${h:default(confirmationRequired, 'false')}"/>
<c:if test="${empty requestScope['org.jahia.tags.subscribeButton.resourcesIncluded']}">
    <c:set var="org.jahia.tags.subscribeButton.resourcesIncluded" value="true" scope="request"/>
    <utility:gwtResourceBundle resourceName="subscriptions.button.operation.failure"/>
    <utility:gwtResourceBundle resourceName="subscriptions.button.provideEmailAddress"/>
    <utility:gwtResourceBundle resourceName="subscriptions.button.subscribe.title"/>
    <utility:gwtResourceBundle resourceName="subscriptions.button.subscribe.confirm"/>
    <utility:gwtResourceBundle resourceName="subscriptions.button.subscribe.success"/>
    <utility:gwtResourceBundle resourceName="subscriptions.button.subscribe.success.confirmationEmail"/>
    <utility:gwtResourceBundle resourceName="subscriptions.button.unsubscribe.confirm"/>
    <utility:gwtResourceBundle resourceName="subscriptions.button.unsubscribe.success"/>
    <utility:gwtResourceBundle resourceName="subscriptions.button.unsubscribe.title"/>
</c:if>
<span ${h:attributes(attributes)}></span>