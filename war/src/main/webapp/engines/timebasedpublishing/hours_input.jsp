<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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
