<?xml version="1.0" encoding="utf-8"?><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.bin.*" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="url" class="java.lang.String" scope="request"/>
<jsp:useBean id="focus" class="java.lang.String" scope="request"/>
<jsp:useBean id="jsp" class="java.lang.String" scope="request"/>
<jsp:useBean id="values" class="java.util.HashMap" scope="request"/>
<jsp:useBean id="step" class="java.lang.String" scope="request"/>
<jsp:useBean id="steps" class="java.lang.String" scope="request"/><%
final String contenttypestr = (String) request.getAttribute("content-type");
response.setContentType(contenttypestr);
final String locale = (String) values.get("welcome_newLocale");
final Integer navigator = (Integer) request.getAttribute("navigator");
int input_size = navigator == 0 ? 65 : 40;
request.setAttribute("input", Integer.toString(input_size));
final String baseURL = response.encodeURL(new StringBuffer().
append(request.getContextPath()).
append(JahiaConfigurationWizard.getServletPath()).
append("/").toString());
final String readmefilePath = response.encodeURL(new StringBuffer().append(request.getContextPath()).append("/html/startup/readme.html").toString());
final boolean openReadmeFile = jsp.equals("site_values.jsp"); %>
<fmt:setLocale value="<%=locale%>" />
<fmt:setBundle basename="JahiaInternalResources" />
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="<%=locale%>" lang="<%=locale%>">
<%
    String copyright=JahiaConfigurationWizard.COPYRIGHT;
%>	
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8" />
    <meta name="gwt:property" content="locale=<%=locale%>"/>
    <title><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.jahiaConfigurationWizard.label"/></title>
    <link rel="stylesheet" href="<%=url%>/configuration_wizard/css/styles.css" type="text/css"/>
    <script type="text/javascript">
        var jahia_gwt_messages={
    	    <fmt:bundle basename="JahiaAdministrationResources">
                "workInProgressTitle":"<fmt:message key='org.jahia.admin.workInProgressTitle'/>",
                "workInProgressProgressText":"<fmt:message key='org.jahia.admin.workInProgressProgressText'/>"
            </fmt:bundle>
        };
    </script>
    <script type="text/javascript" src="${pageContext.request.contextPath}/configuration_wizard/gwt/org.jahia.ajax.gwt.configwizard.ConfigWizard/org.jahia.ajax.gwt.configwizard.ConfigWizard.nocache.js"></script>
    <script type="text/javascript">
              function focus() {
                  document.body.style.cursor = "default";
              <% if (!focus.equals("-none-")){ %>
                  document.mainForm.<%=focus%>.focus();
              <% } %>
                  // so the "work in progress" window gets closed
                  document.mainForm.go.value = "close";
              }

              function submitFormular(arg, dest) {
                  document.mainForm.call.value = arg;
                  document.mainForm.go.value = dest;
                  <%if(openReadmeFile){%>
                   var openrf = document.getElementById('openReadmeFile');
                   if(openrf != null && openrf.checked){
                     //openReadmeFile();
                   }
                  <%}%>
                  document.mainForm.submit();
              }

              <%
                  final Iterator jsscripts = (Iterator) request.getAttribute("jsscripts");
                  int count = 0;
                  if (jsscripts != null) {
              %>

              function changeDatabase() {

              <% while (jsscripts.hasNext()) {
                 final Map js = (Map) jsscripts.next();
              %>
                  if (document.mainForm.script.options[<%=count%>].selected) {
                      document.mainForm.driver.value = '<%=js.get("jahia.database.driver")%>';
                      document.mainForm.dburl.value = '<%=JahiaTools.replacePattern((String)js.get("jahia.database.url"), "\\", "\\\\")%>';
                      document.mainForm.user.value = '<%=js.get("jahia.database.user")%>';
                      document.mainForm.pwd.value = '<%=js.get("jahia.database.pass")%>';
                      document.mainForm.datasource.value = '<%=js.get("jahia.database.datasource")%>';
                      document.mainForm.hibernate_dialect.value = '<%=js.get("jahia.database.hibernate.dialect")%>';
                      document.mainForm.database_type_mapping.value = '<%=js.get("jahia.jboss.datasource.typeMapping")%>';
                  }
              <% count++;
                  }
              %>
              }
              <% } %>

              function setWaitingCursor() {
                  if (typeof workInProgressOverlay != 'undefined') {
                      workInProgressOverlay.launch();
                  }
              }
              function showWorkInProgress() {
            	  setWaitingCursor();
              }

              function openReadmeFile() {
                      var params = "width=1100,height=500,left=0,top=0,resizable=yes,scrollbars=yes,status=no";
                      window.open('<%=readmefilePath%>', 'Readme', params);
              }

    </script>
  </head>
  <body onload="focus();">
    <div id="wrapper">
      <div id="header">
        <h1>Jahia</h1>
        <h2 class="edit"><fmt:message key="org.jahia.bin.JahiaConfigurationWizard.jahiaConfigurationWizard.label"/></h2>
        <span class="step">
          <% if (step != null && step.length() > 0) { %>
          <fmt:message key="label.step"/>&nbsp;<%=step %> / <%=steps %>
          <% } else { %>
          &nbsp;
		  <% } %>
        </span>
      </div>
      <div id="content">
        <form name="mainForm" method="post" action="<%=baseURL%>">
          <input type="hidden" name="go" value="" /><input type="hidden" name="call" value="" />