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
        description="Renders the link/button to the GWT-based user/group selector (requires GWT Edit module to be loaded)."
        %>
<%@ attribute name="mode" required="false" type="java.lang.String" description="The selection mode: users, groups or both. [both]" %>
<%@ attribute name="onSelect" required="false" type="java.lang.String" description="The JavaScript function to be called after a user/group is selected. The selected principal type (u or g), principal key and principal name will be passed as arguments to this function. If the function returns true, the principal name will be also set into the field value. Otherwise nothing will be done by this tag." %>
<%@ attribute name="fieldId" required="false" type="java.lang.String" description="The HTML element ID of the input field, where the proncipal name should be stored. If not provided, nothing will be done by this tag." %>
<%@ attribute name="label" required="false" type="java.lang.String" description="The label of the link for openning the user/group window." %>
<%@ attribute name="multiple" required="false" type="java.lang.Boolean" description="Allow multiple principal selection? [false]" %>
<%@ tag dynamic-attributes="attributes" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<c:set target="${attributes}" property="jahiatype" value="usergroup"/>
<c:set target="${attributes}" property="mode" value="${mode}"/>
<c:set target="${attributes}" property="onSelect" value="${onSelect}"/>
<c:set target="${attributes}" property="fieldId" value="${fieldId}"/>
<c:set target="${attributes}" property="singleSelectionMode" value="${not h:default(multiple, false)}"/>
<c:set var="elementId" value='<%= "usergroup_" + org.apache.commons.id.IdentifierUtils.nextLongIdentifier() %>'/>
<c:set target="${attributes}" property="id" value="${h:default(attributes.id, elementId)}"/>
<span ${h:attributes(attributes)} label="${empty label ? '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' : label}"></span>