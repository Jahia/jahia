<%--


    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ tag body-content="empty"
        description="Renders subscribe/unsubscribe link, which allows current user to subscribe/unsubscribe to the notifications about changes in the content object."
        %>
<%@ attribute name="source" required="true" type="java.lang.String"
              description="The key of the content object to observe for changes."
        %>
<%@ attribute name="event" required="false" type="java.lang.String"
              description="The event type to catch. If not specified, the contentPublished event is used."
        %>
<%@ attribute name="user" required="false" type="java.lang.String"
              description="The username to use for a subscription. If not specified, the current user is used."
        %>
<%@ attribute name="confirmationRequired" required="false" type="java.lang.Boolean"
              description="Does the subscription require confirmation (an e-mail with the confirmation link will be sent first)?"
        %>
<%@ tag dynamic-attributes="attributes"
        %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"
        %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"
        %>
<c:set target="${attributes}" property="jahiatype" value="subscription"/>
<c:set var="elementId" value="subscription_${source}"/>
<c:set target="${attributes}" property="id" value="${h:default(id, elementId)}"/>
<c:set target="${attributes}" property="source" value="${source}"/>
<c:set target="${attributes}" property="event" value="${h:default(event, 'contentPublished')}"/>
<c:set target="${attributes}" property="user" value="${h:default(user, jahia.user.username)}"/>
<c:set target="${attributes}" property="confirmationRequired" value="${h:default(confirmationRequired, 'false')}"/>
<span ${h:attributes(attributes)}></span>