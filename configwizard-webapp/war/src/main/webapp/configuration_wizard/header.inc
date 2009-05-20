<?xml version="1.0" encoding="utf-8"?><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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
<fmt:setBundle basename="JahiaMessageResources" />
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
                      document.mainForm.starthsqlserver.value = '<%=js.get("jahia.database.starthsqlserver")%>';
                      document.mainForm.datasource.value = '<%=js.get("jahia.database.datasource")%>';
                      document.mainForm.hibernate_dialect.value = '<%=js.get("jahia.database.hibernate.dialect")%>';
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
          <fmt:message key="org.jahia.step.label"/>&nbsp;<%=step %> / <%=steps %>
          <% } else { %>
          &nbsp;
		  <% } %>
        </span>
      </div>
      <div id="content">
        <form name="mainForm" method="post" action="<%=baseURL%>">
          <input type="hidden" name="go" value="" /><input type="hidden" name="call" value="" />