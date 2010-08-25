<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>
<template:addResources type="css" resources="docspace.css,files.css,toggle-docspace.css,jquery.treeview.css"/>
<template:addResources type="javascript" resources="jquery.min.js,jquery.treeview.min.js"/>
<script type="text/javascript">
    $(document).ready(function() {
        $("#docspaceTree").treeview();
    });
</script>
<c:set var="pageNode" value="${jcr:getMeAndParentsOfType(renderContext.mainResource.node, 'jnt:page')[0]}"/>
<h4 class="boxdocspace-title2"><fmt:message key="docspace.label.docspace.title"/></h4>
<div class="boxdocspace"><!--start boxdocspace -->
    <div class="boxdocspacepadding16 boxdocspacemarginbottom16">
        <div class="boxdocspace-inner">
            <div class="boxdocspace-inner-border">
                <ul id="docspaceTree" class="filetree">
                    <c:if test="${!empty pageNode}">
                        <c:forEach var="node" items="${jcr:getChildrenOfType(pageNode,'jnt:folder')}">
                            <template:module node="${node}" template="hidden.docspace.tree"/>
                        </c:forEach>
                    </c:if>
                </ul>
            </div>
        </div>
    </div>
</div>