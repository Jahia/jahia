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