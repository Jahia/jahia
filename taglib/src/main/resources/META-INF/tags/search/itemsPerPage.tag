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

<%@ tag body-content="empty" description="Renders items per page drop down box." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<%@ attribute name="value" required="false" type="java.lang.Integer" description="The initial value." %>
<%@ attribute name="options" required="false" type="java.lang.String"
              description="Allowed options as a comma separated list of value." %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions"%>
<c:set var="display" value="${h:default(display, true)}"/>
<c:set var="value" value="${h:default(param['src_itemsPerPage'], h:default(value, '10'))}"/>
<c:set var="options" value="${h:default(options, '5,10,20,30,50,100')}"/>
<c:set target="${attributes}" property="name" value="src_itemsPerPage"/>
<c:if test="${display}">
    <select name="src_itemsPerPage">
        <c:forTokens items="${options}" delims="," var="opt">
            <option value="${opt}" ${opt == value ? 'selected="selected"' : ''}>${opt}</option>
        </c:forTokens>
    </select>
</c:if>
<c:if test="${!display}">
    <input type="hidden" ${h:attributes(attributes)} value="${fn:escapeXml(value)}"/>
</c:if>