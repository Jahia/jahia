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
<%@ include file="../../common/declarations.jspf" %>

<template:containerList name="preferences" id="preferencesContainerList" displayActionMenu="false">
    <template:container id="preference" cache="off" displayActionMenu="false">
        <template:field name="blogPrefsMaxEntries" var="blogPrefsMaxEntries" defaultValue="10"
                        display="false"/>
        <c:set var="blogPrefsMaxEntries" value="${blogPrefsMaxEntries}" scope="session"/>

        <template:field name="lastCommentsMaxEntries" var="lastCommentsMaxEntries" defaultValue="10"
                        display="lastCommentsMaxEntries"/>
        <c:set var="lastCommentsMaxEntries" value="${lastCommentsMaxEntries}" scope="session"/>

        <template:field name="lastEntriesMaxEntries" var="lastEntriesMaxEntries" defaultValue="10"
                        display="false"/>
        <c:set var="lastEntriesMaxEntries" value="${lastEntriesMaxEntries}" scope="session"/>

        <template:field name="lastname" var="lastname" defaultValue="10"
                        display="false"/>
        <c:set var="lastname" value="${lastname}" scope="session"/>

        <template:field name="firstname" var="firstname" defaultValue="10"
                        display="false"/>
        <c:set var="firstname" value="${firstname}" scope="session"/>

        <template:field name="birthdate" var="birthdate" defaultValue="10"
                        display="false"/>
        <c:set var="birthdate" value="${birthdate}" scope="session"/>

        <template:field name="description" var="description" defaultValue=""
                        display="false"/>
        <c:set var="description" value="${description}" scope="session"/>

        <template:field name="picture" var="picture" defaultValue=""
                        display="false"/>
        <c:set var="picture" value="${picture}" scope="session"/>

        <template:field name="intro" var="intro" defaultValue=""
                        display="false"/>
        <c:set var="intro" value="${intro}" scope="session"/>

        <template:field name="footer" var="footer" defaultValue=""
                        display="false"/>
        <c:set var="footer" value="${footer}" scope="session"/>

        </template:container>
</template:containerList>