<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<a class="atopblogcontents" href="${url.base}${currentNode.path}.html"><jcr:nodeProperty node="${jcr:getParentOfType(currentNode,'jnt:page')}"
                                                                                         name="jcr:title"/>&nbsp;-&nbsp;<jcr:nodeProperty
        node="${currentNode}" name="jcr:title"/></a>
<jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/>
<span class="bloglistinfo timestamp"><fmt:formatDate value="${lastModified.time}" pattern="yyyy/MM/dd HH:mm"/></span>

