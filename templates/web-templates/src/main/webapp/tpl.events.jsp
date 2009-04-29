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
<%@ include file="common/declarations.jspf" %>
<%-- This file only describe the template --%>

<%--Define special variable for header (particular script/css)--%>
<c:set var="templateHeader" scope="request">
    <script type="text/javascript" src='<utility:resolvePath value='javascript/jQuery.js'/>'></script>
    <script type="text/javascript" src='<utility:resolvePath value='javascript/jquery-ui-1.6rc6.js'/>'></script>
    <script type="text/javascript" src='<utility:resolvePath value='javascript/i18n/ui.datepicker-${requestScope.currentRequest.locale}.js'/>'></script>
    <script type="text/javascript">
        jQuery.noConflict();
    </script>
</c:set>

<%-- Define layout file --%>
<jsp:include page="positioning.jsp">
    <jsp:param name="position" value="position2"/>    
    <jsp:param name="mainArea" value="areas/introduction_eventslist.jsp"/>
    <jsp:param name="areaB" value="areas/calendar_boxes.jsp"/>
    <jsp:param name="footerNav" value="true"/>
    <jsp:param name="useGWT" value="true"/>
</jsp:include>
