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
<%@ page language="java"%>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
  Boolean enableImmediatePublication = (Boolean)request.getAttribute("enableImmediatePublication");
  if ( enableImmediatePublication == null ){
    enableImmediatePublication = Boolean.TRUE;
  }
%>
<input class="input immediatePublication" type="radio" name="enableImmediatePublication" value="true" <%if(enableImmediatePublication.booleanValue()){%>checked<%}%> ${inherited ? ' disabled="disabled"' : ''}>
<fmt:message key="org.jahia.engines.timebasedpublishing.allowedToStartImmediately"/>
<input class="input immediatePublication" type="radio" name="enableImmediatePublication" value="false" <%if(!enableImmediatePublication.booleanValue()){%>checked<%}%> ${inherited ? ' disabled="disabled"' : ''}>
<fmt:message key="org.jahia.engines.timebasedpublishing.onlyAtNextFullPeriod"/>

