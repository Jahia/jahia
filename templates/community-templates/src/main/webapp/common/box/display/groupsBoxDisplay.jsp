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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%--
Copyright 2002-2008 Jahia Ltd

Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
Version 1.0 (the "License"), or (at your option) any later version; you may
not use this file except in compliance with the License. You should have
received a copy of the License along with this program; if not, you may obtain
a copy of the License at

 http://www.jahia.org/license/

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ include file="../../declarations.jspf" %>
<template:containerList name="groupsContainer" id="groups"
                       actionMenuNamePostFix="groupQueries" actionMenuNameLabelKey="groupQueries.add">
    <template:container id="groupsContainer" actionMenuNamePostFix="groupQuery" actionMenuNameLabelKey="groupQuery.update">
        <template:field name='groupDisplayLimit' var="boxGroupDisplayLimit" display="false"/>
        <template:field name='groupQuery' var="boxGroupQuery" display="false"/>
        <h4>
            <fmt:message key="queryResult"/>
        </h4>
        <ui:groupList
                displayLimit="${not empty boxGroupDisplayLimit ? boxGroupDisplayLimit.integer : 5}"
                query="${not empty boxGroupQuery ? boxGroupQuery.text : '*'}"
                scope="all"
                membersLimit="12"
                membersVisibility="true"
                separator="<br/>"
                styleClass=""/>
    </template:container>
    <br class="clear"/>
</template:containerList>
