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
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="h" uri="http://www.jahia.org/tags/functions" %>
<%@ tag body-content="empty" description="Renders document type selection control with all node types available." %>
<%@ attribute name="value" required="false" type="java.lang.String" %>
<%@ attribute name="display" required="false" type="java.lang.Boolean"
              description="Should we display an input control for this query element or create a hidden one? In case of the hidden input field, the value should be provided."
        %>
<utility:useConstants var="jcr" className="org.jahia.api.Constants" scope="application"/>
<c:set var="value" value="${h:default(param.src_documentType, value)}"/>
<c:set var="display" value="${h:default(display, true)}"/>
<c:if test="${display}">
    <select name="src_documentType">
        <option value=""><utility:resourceBundle resourceName="searchForm.any" defaultValue="any"/></option>
        <jcr:nodeType ntname="${jcr.nt_file}">
            <option value="${type.name}" ${value == type.name ? 'selected="selected"' : ''}>
                <jcr:nodeTypeLabel/></option>
        </jcr:nodeType>
        <jcr:nodeType ntname="${jcr.nt_folder}">
            <option value="${type.name}" ${value == type.name ? 'selected="selected"' : ''}>
                <jcr:nodeTypeLabel/></option>
        </jcr:nodeType>
        <jcr:nodeTypes baseType="${jcr.jahiamix_extension}">
            <option value="${type.name}" ${value == type.name ? 'selected="selected"' : ''}>
                <jcr:nodeTypeLabel/></option>
        </jcr:nodeTypes>
    </select>
</c:if>
<c:if test="${!display}"><input type="hidden" name="src_documentType" value="${value}"/></c:if>