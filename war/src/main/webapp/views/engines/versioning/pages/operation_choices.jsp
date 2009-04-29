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
