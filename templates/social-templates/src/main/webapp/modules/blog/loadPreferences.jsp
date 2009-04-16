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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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