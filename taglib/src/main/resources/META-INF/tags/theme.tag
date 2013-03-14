<%@ tag body-content="empty" description="Add the theme as a resource" %>

<%@ tag import="org.jahia.services.render.RenderContext" %>
<%@ tag import="org.jahia.services.render.filter.cache.ModuleCacheProvider" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:nodeProperty var="theme" node="${renderContext.mainResource.node}" name="j:theme" inherited="true"/>
<c:if test="${!empty theme}">
    <c:forEach var="themeFile" items="${jcr:getChildrenOfType(theme.node,'nt:file')}">
        <template:addResources type="css" resources="${themeFile.url}" />
    </c:forEach>
</c:if>

<c:forEach var="tpl" items="${previousTemplate.nextTemplates}">
    <jcr:node uuid="${tpl.node}" var="tplnode" />
    <jcr:nodeProperty var="theme" node="${tplnode}" name="j:theme" />
    <c:if test="${not empty theme.node}">
        <c:forEach var="themeFile" items="${jcr:getChildrenOfType(theme.node,'nt:file')}">
            <template:addResources type="css" resources="${themeFile.url}" />
        </c:forEach>
    </c:if>
<<<<<<< .working
</c:forEach>=======
  </c:forEach>
</c:catch>
<c:if test="${not empty catchException}">
<%
    //Exceptions are catched. Most of time it will be SessionInvalidate exceptions
    Logger log = Logger.getLogger(this.getClass().getName());
    log.warn("Error in theme tag ", (java.lang.Exception)jspContext.findAttribute("catchException"));
    //flush related output caches, that next requests use the correct theme
    RenderContext renderContext = (RenderContext)jspContext.findAttribute("renderContext");
    ModuleCacheProvider.getInstance().invalidate(renderContext.getMainResource().getNode().getPath(), false);
%>
</c:if>  >>>>>>> .merge-right.r45107
