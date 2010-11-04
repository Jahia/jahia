<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<div class="addArticle"><!--start preferences-->
    <h3><a href="${url.base}${renderContext.mainResource.node.path}.blog-new.html"class="addArticle"><fmt:message key="jnt_blog.addNew"/></a></h3>
</div>