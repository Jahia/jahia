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

<template:containerList name="mediaContainer" id="media"
                        actionMenuNamePostFix="media" actionMenuNameLabelKey="media.add">
    <template:container id="mediaContainer" displayActionMenu="false">
        <ui:actionMenu contentObjectName="mediaContainer" namePostFix="media" labelKey="media.update">
            <ul>
                <li>Author : <template:field name="mediaAuthor"/></li>
                <li>Date : <template:field name="mediaDate"/></li>
                <li>Note : <template:field name="mediaNote"/></li>
                <li>Credit : <template:field name="mediaCredit"/></li>
                <li>file : <template:field name='mediaFile'/></li>
            </ul>
        </ui:actionMenu>
    </template:container>
</template:containerList>