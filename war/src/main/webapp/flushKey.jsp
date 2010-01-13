<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.jahia.services.render.filter.cache.*" %>
<%@ page import="java.util.Set" %>
<%@ page import="net.sf.ehcache.Ehcache" %>

<%
    ModuleCacheProvider cacheProvider = ModuleCacheProvider.getInstance();
    Ehcache cache = cacheProvider.getCache();
	Ehcache depCache = cacheProvider.getDependenciesCache();
    String keyToFlush = request.getParameter("keyToFlush");
    String nodePath = cacheProvider.getKeyGenerator().getPath(keyToFlush);
    Set<String> deps = (Set<String>) depCache.get(nodePath).getValue();
    for (String dep : deps) {
        cache.remove(dep);
        cache.remove(cacheProvider.getKeyGenerator().replaceField(dep, "template", "hidden.load"));
    }
    response.sendRedirect(request.getHeader("referer"));
%>

