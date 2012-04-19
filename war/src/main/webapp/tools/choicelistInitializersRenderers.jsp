<%@ page contentType="text/html;charset=UTF-8" language="java"
%><?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page import="org.jahia.services.content.nodetypes.initializers.ChoiceListInitializerService" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.jahia.services.content.nodetypes.initializers.ChoiceListInitializer" %>
<%@ page import="org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer" %>
<%@ page import="org.jahia.services.content.nodetypes.renderer.ChoiceListRendererService" %>
<%@ page import="org.jahia.services.content.nodetypes.renderer.ChoiceListRenderer" %>
<%@ page import="org.jahia.services.content.nodetypes.renderer.ModuleChoiceListRenderer" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions"%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <title>Choicelist initializers &amp; renderers</title>
    <link rel="stylesheet" href="tools.css" type="text/css" />
</head>
<%
    final Map<String,ChoiceListInitializer> initializers = ChoiceListInitializerService.getInstance().getInitializers();
    final int nbInitializers = initializers == null ? 0 : initializers.size();

    final Map<String,ChoiceListRenderer> renderers = ChoiceListRendererService.getInstance().getRenderers();
    final int nbRenderers = renderers == null ? 0 : renderers.size();
    int count = 0;
%>
<body>
<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>

<h1>Choicelist initializers &amp; renderers</h1>

<div style="float:left;width:40%">
    <h2>Choicelist initializers (<%=nbInitializers%> found)</h2>

    <% if (nbInitializers > 0) { %>
    <table border="1" cellspacing="0" cellpadding="5">
        <thead>
            <tr>
                <th>#</th>
                <th>Name</th>
                <th>Is defined by a module</th>
            </tr>
        </thead>
        <tbody>
        <% for (String key : initializers.keySet()) {
                final ChoiceListInitializer initializer = initializers.get(key);
        %>
            <tr>
                <td><%=++count%></td>
                <td title="<%=initializer.getClass().getName()%>"><%=key%></td>
                <td align="center"><%=initializer instanceof ModuleChoiceListInitializer%></td>
            </tr>
        <% } %>
        </tbody>
    </table>
    <% } %>
</div>

<div style="float:left;width:40%">
    <h2>Choicelist renderers (<%=nbRenderers%> found)</h2>

    <% if (nbRenderers > 0) { %>
    <table border="1" cellspacing="0" cellpadding="5">
        <thead>
        <tr>
            <th>#</th>
            <th>Name</th>
            <th>Is defined by a module</th>
        </tr>
        </thead>
        <tbody>
        <%
            count = 0;
            for (String key : renderers.keySet()) {
            final ChoiceListRenderer renderer = renderers.get(key);
        %>
        <tr>
            <td><%=++count%></td>
            <td title="<%=renderer.getClass().getName()%>"><%=key%></td>
            <td align="center"><%=renderer instanceof ModuleChoiceListRenderer%></td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <% } %>
</div>

<div style="clear:both"></div>

<p>
    <img src="<c:url value='/engines/images/icons/home_on.gif'/>" height="16" width="16" alt=" " align="top" />&nbsp;
    <a href="<c:url value='/tools/index.jsp'/>">to Jahia Tools overview</a>
</p>
</body>
</html>