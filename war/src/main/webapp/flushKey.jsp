<%@ page import="net.sf.ehcache.Cache" %>
<%@ page import="net.sf.ehcache.CacheManager" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.cache.ehcache.EhCacheProvider" %>
<%@ page import="org.jahia.services.render.filter.cache.CacheFilter" %>
<%@ page import="java.util.Set" %>

<%
    EhCacheProvider provider = (EhCacheProvider) ServicesRegistry.getInstance().getCacheService().getCacheProviders().get(
            "EH_CACHE");
    CacheManager cacheManager = provider.getCacheManager();
    Cache htmlCache = cacheManager.getCache(CacheFilter.CACHE_NAME);
    Cache depCache = cacheManager.getCache(CacheFilter.DEPS_CACHE_NAME);
    String keyToFlush = request.getParameter("keyToFlush");
    String nodePath = keyToFlush.split("__")[0];
    Set<String> deps = (Set<String>) depCache.get(nodePath).getValue();
    for (String dep : deps) {
        htmlCache.remove(dep);
        htmlCache.remove(dep.replaceAll("__template__(.*)__lang__", "__template__hidden.load__lang__"));
    }
    response.sendRedirect(request.getHeader("referer"));
%>

