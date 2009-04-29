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
<%@ include file="../declarations.jspf" %>

<%-- Set up a variable to store the boxID value. This value will be used by the JSP files displaying the boxes --%>
<c:set var="boxID" scope="request" value="${param.name}"/>
<%-- Let us now display the main box list with all the different boxes it has --%>
<template:boxList name="${param.name}" id="${param.name}" actionMenuNamePostFix="boxes" actionMenuNameLabelKey="boxes.update">
    <template:container id="boxContainer" actionMenuNamePostFix="box" actionMenuNameLabelKey="box.update" cache="false">
        <template:field name="boxTitle" var="boxTitle" display="false"/>
        <%-- Set boxTitle to be use in box skinner template_rep/skins/myskin/myskin.jsp --%>
        <c:set var="boxTitle" scope="request" value="${boxTitle}"/>
        <%-- Let us invoke the box tag so it will dispatch the request to the correct JSP file used for box display --%>
        <template:box displayTitle="false" id="${param.name}" onError="${jahia.requestInfo.normalMode ? 'hide' : 'default'}"/>
    </template:container>
</template:boxList>