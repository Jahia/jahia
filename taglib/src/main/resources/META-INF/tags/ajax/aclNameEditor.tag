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
<%@ tag body-content="empty" description="Includes the GWT ACL name editor control" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://www.jahia.org/tags/functions" prefix="functions" %>
<%@ attribute name="aclId" required="true" rtexprvalue="true" type="java.lang.Integer" description="The corresponding ACL ID to store selected user/groups." %>
<%@ attribute name="fieldId" required="true" rtexprvalue="true" type="java.lang.String" description="The HTML ID of the input field to store the selected value." %>
<%@ attribute name="fieldName" required="false" rtexprvalue="true" type="java.lang.String" description="The name of the input field to store the selected value. If this value is specified the hidden input element is created by this tag. If this value is empty, no field is created." %>
<%@ attribute name="readOnly" required="false" rtexprvalue="true" type="java.lang.Boolean" description="Do we allow editing of this value? [false]" %>
<%@ attribute name="height" required="false" rtexprvalue="true" type="java.lang.Integer" description="The control height. [120px]" %>
<%@ attribute name="allowSiteSelection" required="false" rtexprvalue="true" type="java.lang.Boolean" description="Do we allow the user to select another site? [false]" %>
<%@ tag dynamic-attributes="attributes" %>
<c:if test="${empty requestScope['org.jahia.tags.aclNameEditor.index']}">
    <c:set var="org.jahia.tags.aclNameEditor.index" value="-1" scope="request"/>
</c:if>
<c:set var="org.jahia.tags.aclNameEditor.index" value="${requestScope['org.jahia.tags.aclNameEditor.index'] + 1}" scope="request"/>
<c:set target="${attributes}" property="id" value="gwtaclnameeditor-${requestScope['org.jahia.tags.aclNameEditor.index']}"/>
<c:set target="${attributes}" property="aclid" value="${aclId}"/>
<c:set target="${attributes}" property="fieldId" value="${fieldId}"/>
<c:set target="${attributes}" property="readOnly" value="${functions:default(readOnly, 'false')}"/>
<c:set target="${attributes}" property="height" value="${functions:default(height, '120px')}"/>
<c:set target="${attributes}" property="aclContext" value="${functions:default(allowSiteSelection, 'false') == 'true' ? 'siteSelector' : 'currentSite'}"/>
<c:if test="${not empty fieldName}">
<input type="hidden" name="${fieldName}" id="${fieldId}" value=""/>
</c:if>
<c:if test="${empty requestScope['org.jahia.tags.aclNameEditor.resourcesIncluded']}">
    <c:set var="org.jahia.tags.aclNameEditor.resourcesIncluded" value="true" scope="request"/>
    <internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.principal.label"
                                aliasResourceName="ae_principal"/>
    <internal:gwtResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newUsers.label"
                                aliasResourceName="um_adduser"/>
    <internal:gwtResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newGroups.label"
                                aliasResourceName="um_addgroup"/>
    <internal:gwtResourceBundle resourceName="org.jahia.engines.rights.ManageRights.remove.label"
                                aliasResourceName="ae_remove"/>
</c:if>
<span ${functions:attributes(attributes)}></span>