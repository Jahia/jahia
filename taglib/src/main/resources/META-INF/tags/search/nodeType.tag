<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ tag body-content="empty" description="Renders node type selection control with all node types available." %>
<%@ tag dynamic-attributes="attributes"%>
<%@ attribute name="value" required="false" type="java.lang.String" %>
<%@ attribute name="selectionOptions" required="false" type="java.lang.String"
              description="Comma separated list of node types to search in that are available for user selection. [any,nt:file,nt:folder]" %>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided." %>
<utility:useConstants var="jcr" className="org.jahia.api.Constants" scope="application"/>
<c:set var="selectionOptions" value="${functions:default(fn:replace(selectionOptions, ' ', ''), 'any,nt:file,nt:folder')}"/>
<c:set var="value" value="${functions:default(param.src_nodeType, value)}"/>
<c:set var="display" value="${functions:default(display, true)}"/>
<c:if test="${display}">
    <select ${functions:attributes(attributes)} name="src_nodeType">
    	<c:forTokens items="${selectionOptions}" delims="," var="option">
    		<c:if test="${option == 'any'}">
    			<option value=""><fmt:message key="searchForm.any"/></option>
    		</c:if>
    		<c:if test="${option != 'any'}">
    			<jcr:nodeType name="${option}" var="type"/>
            	<option value="${type.name}" ${value == type.name ? 'selected="selected"' : ''}>${jcr:label(type)}</option>
    		</c:if>
    	</c:forTokens>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_nodeType" value="${fn:escapeXml(value)}"/></c:if>