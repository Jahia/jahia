<%@ page import="java.util.Iterator" %>
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
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/queryLib" prefix="query" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>


<template:containerList name="fileContainer" id="files"
                        actionMenuNamePostFix="files" actionMenuNameLabelKey="files.add">
    <ul>
        <template:container id="fileContainer" displayActionMenu="false">

            <template:field name="fileDisplayDetails" var="displayDetails" display="false"/>
            <li class="document">
                <ui:actionMenu contentObjectName="fileContainer" namePostFix="file" labelKey="file.update">
                    <template:field name="file"/>
                    <template:field name="fileDesc"/>
                </ui:actionMenu>
            </li>
        </template:container>
    </ul>
</template:containerList>