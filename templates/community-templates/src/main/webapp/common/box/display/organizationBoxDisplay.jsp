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
<%@ include file="../../declarations.jspf" %>
<template:containerList name="organizationContainer" id="organization"
                       actionMenuNamePostFix="organization" actionMenuNameLabelKey="organization.add">
    <template:container id="organismContainer">
        <ui:actionMenu contentObjectName="organizationContainer" namePostFix="organization" labelKey="organization.update">
            <ul>
                <li>name : <template:field name="organizationName"/></li>
                <li>acronym : <template:field name="organizationAcronym"/></li>
                <li>reference : <template:field name="organizationReference"/></li>
                <li>logo : <template:image file="organizationLogo"/></li>
            </ul>
        </ui:actionMenu>
    </template:container>
</template:containerList>
