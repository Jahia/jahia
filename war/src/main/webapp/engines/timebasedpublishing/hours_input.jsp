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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
  String inputNamePrefix = (String)request.getAttribute("inputNamePrefix");
  String hours = (String)request.getAttribute("hours");
  String minutes = (String)request.getAttribute("minutes");
  Integer hoursMax = (Integer)request.getAttribute("hoursMax");
  if (hoursMax == null){
    hoursMax = new Integer(24);
  }
%>
<c:if test="${inherited}">
<input type="hidden" name="${inputNamePrefix}Hours" value="${hours}"/>
<input type="hidden" name="${inputNamePrefix}Minutes" value="${minutes}"/>
</c:if>
HH: <select class="input" name="<%=inputNamePrefix%>Hours" ${inherited ? 'disabled="disabled"' : ''}>
  <% 
    for ( int i=0; i<hoursMax.intValue(); i++ ){
    %>
     <option class="input" style="width: 20px;" value="<%=String.valueOf(i)%>" <%if (String.valueOf(i).equals(hours)){%>selected<%}%>/><fmt:message key='<%="org.jahia.engines.timebasedpublishing.hours."+String.valueOf(i)%>'/>
    <%
    }
  %>  
</select>
MM: <select class="input" name="<%=inputNamePrefix%>Minutes" ${inherited ? 'disabled="disabled"' : ''}>
  <% 
    for ( int i=0; i<60; i++ ){
    %>
     <option class="input" style="width: 20px;"  value="<%=String.valueOf(i)%>" <%if (String.valueOf(i).equals(minutes)){%>selected<%}%>/><%=org.apache.commons.lang.StringUtils.leftPad(String.valueOf(i),2,'0')%>
    <%
      i+= 4;
    }
  %>  
</select>
