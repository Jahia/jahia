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
<template:containerList name="eventContainer" id="event"
                       actionMenuNamePostFix="event" actionMenuNameLabelKey="event.add">
    <template:container id="eventContainer" displayActionMenu="false">
        <ui:actionMenu contentObjectName="eventContainer" namePostFix="event" labelKey="event.update">
            <ul>
                <li>Title : <template:field name="eventTitle"/></li>
                <li>Date begin : <template:field name="eventDateBegin"/></li>
                <li>Date end : <template:field name="eventDateEnd"/></li>
                <li>Content : <template:field name="eventContent"/></li>
                <%@ include file="locationBoxDisplay.jsp" %>
            </ul>
        </ui:actionMenu>
    </template:container>
</template:containerList>