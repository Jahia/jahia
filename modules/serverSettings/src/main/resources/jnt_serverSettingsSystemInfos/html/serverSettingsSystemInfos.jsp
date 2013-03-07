<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.jahia.bin.errors.ErrorFileDumper" %>
<%@ page import="org.jahia.tools.jvm.ThreadMonitor" %>
<%@ page import="java.util.Enumeration" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="javascript" resources="jquery.min.js,jquery-ui.min.js"/>
<template:addResources type="css" resources="jquery-ui.smoothness.css,jquery-ui.smoothness-jahia.css"/>
<script type="text/javascript">
    $(document).ready(function () {
        $("#accordion").accordion({collapsible:true, heightStyle:"content"});
    })
</script>

<p>&nbsp;</p>
<div id="accordion">

<h3><fmt:message key="org.jahia.admin.status.ManageStatus.title.systemInfoSection.label"/></h3>

<div>
    <table width="100%" border="0" cellspacing="0" cellpadding="5" style="table-layout: fixed;">
        <% Enumeration propertyNameEnum = System.getProperties().propertyNames();
            int maxWidth = 40;
            int propertyCounter = 0;
            String propertyLineClass = "evenLine";
            while (propertyNameEnum.hasMoreElements()) {
                if (propertyCounter % 2 == 0) {
                    propertyLineClass = "evenLine";
                } else {
                    propertyLineClass = "oddLine";
                }
                propertyCounter++;
                String curPropertyName = (String) propertyNameEnum.nextElement();
                String curPropertyValue = (String) System.getProperty(curPropertyName);
                pageContext.setAttribute("propName", curPropertyName);
                pageContext.setAttribute("propValue", curPropertyValue);
        %>
        <tr class="<%=propertyLineClass%>">
            <td style="width: 40%; overflow: hidden;" title="<c:out value='${propName}'/>">
                <strong><c:out value='${propName}'/></strong>
            </td>
            <td style="width: 60%; overflow: hidden;" title="<c:out value='${propValue}'/>">
                <%
                    while (curPropertyValue.length() > maxWidth) {
                        String curLine = curPropertyValue.substring(0, maxWidth);
                        out.println(curLine);
                        curPropertyValue = curPropertyValue.substring(maxWidth);
                    }
                    out.println(curPropertyValue);
                %>
            </td>
        </tr>
        <% } %>
    </table>
</div>
</div>