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

<%@ page language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.views.engines.*" %>
<%@ page import="org.jahia.views.engines.versioning.pages.PagesVersioningViewHelper" %>

<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<jsp:useBean id="jspSource" class="java.lang.String" scope="request"/>

<%

    String actionURL = (String)request.getAttribute("ContentVersioning.ActionURL");
    String engineView = (String)request.getAttribute("engineView");
    final PagesVersioningViewHelper pagesVersViewHelper =
            (PagesVersioningViewHelper) request.getAttribute(JahiaEngineViewHelper.ENGINE_VIEW_HELPER);

    final Map engineMap = (Map) request.getAttribute("jahia_session_engineMap");
    final String theScreen = (String) engineMap.get("screen");
%>
<!-- versioning/pages/operation_choices.jsp (start) -->
<%@include file="common-javascript.inc" %>

<div class="menuwrapper">
    <%@ include file="../../../../engines/tools.inc" %>
    <div class="content">
        <div id="editor" class="mainPanel">
            <h4 class="versioningIcon">
                <fmt:message key="org.jahia.engines.include.actionSelector.PageVersioning.label"/>
            </h4>

            <h5><fmt:message key="org.jahia.engines.version.stepOneOfThree"/></h5>

            <p>
                <strong><fmt:message key="org.jahia.engines.version.selectTheTaskToPerform"/></strong>. </p>
            <p>
                <fmt:message key="org.jahia.engines.version.availableTasks"/>:</p>
            <ul class="noStyle">
                <li>
                    <input type="radio" name="operationType"
                           value="1" <%if(pagesVersViewHelper.getOperationType()==1){%>
                           checked="checked"<%}%>/> A) &nbsp;
                    <fmt:message key="org.jahia.engines.version.undoStagingModification"/>
                </li>
                <li>
                    <input type="radio" name="operationType"
                           value="2" <%if(pagesVersViewHelper.getOperationType()==2){%>
                           checked=checked<%}%>/> B) &nbsp;
                    <fmt:message key="org.jahia.engines.version.restoreArchivedContent"/>
                </li>
                <li>
                    <input type="radio" name="operationType"
                           value="3" <%if(pagesVersViewHelper.getOperationType()==3){%>
                           checked=checked<%}%>/> C) &nbsp;
                    <fmt:message key="org.jahia.engines.version.restoreDeletedPages"/>
                </li>
            </ul>

            <div class="navBox">
                <div class="nextStep">
                    <div class="button">
                        <a href="javascript:sendForm('showSiteMap');"><fmt:message key="org.jahia.engines.version.proceedToStep"/> 2 >></a>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="clearing">&nbsp;</div>
</div>
<!-- versioning/pages/operation_choices.jsp (end) -->
