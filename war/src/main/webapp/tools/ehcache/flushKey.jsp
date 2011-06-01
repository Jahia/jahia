<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.jahia.services.render.filter.cache.*" %>
<%
    ModuleCacheProvider cacheProvider = ModuleCacheProvider.getInstance();
    String nodePath = cacheProvider.getKeyGenerator().getPath(request.getParameter("keyToFlush"));
    cacheProvider.invalidate(nodePath);
    response.sendRedirect(request.getHeader("referer"));
%>