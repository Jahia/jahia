<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<template:addResources type="javascript" resources="jquery.min.js"/>
<template:addResources type="javascript" resources="jquery.form.js"/>

<c:set var="targetNodePath" value="${renderContext.mainResource.node.path}"/>
<c:if test="${!empty param.targetNodePath}">
    <c:set var="targetNodePath" value="${param.targetNodePath}"/>
</c:if>
<c:if test="${!empty currentNode.properties.folder}">
    <c:set var="targetNodePath" value="${currentNode.properties.folder.node.path}"/>
</c:if>
<script type="text/javascript">
    function insertImgSyntax(content) {
        document.formWiki.wikiContent.value += content;
    }
</script>
<div id="fileList${currentNode.identifier}">
    <template:addResources type="css" resources="fileList.css"/>
    <jcr:node var="targetNode" path="${targetNodePath}"/>
    <ul class="filesList">
        <li><fmt:message key="label.dblClickToAddInWiki"/></li>
        <c:forEach items="${targetNode.nodes}" var="subchild">
            <c:if test="${jcr:isNodeType(subchild, 'jnt:file')}">
                <li>
                    <c:if test="${fn:startsWith(subchild.fileContent.contentType,'image/')}">
                        <div onclick="return false;" ondblclick="insertImgSyntax('\n [[image:${subchild.url}||width=${subchild.properties["j:width"].string} height=${subchild.properties["j:height"].string}]]')">
                            <img name="" width="100" src="${subchild.url}" alt="${fn:escapeXml(subchild.name)}" onmousedown="return false;" />
                                ${fn:escapeXml(not empty title.string ? title.string : subchild.name)}
                        </div>
                    </c:if>
                    <c:if test="${!fn:startsWith(subchild.fileContent.contentType,'image/')}">
                        <div onclick="return false;" ondblclick="insertImgSyntax('\n[[${fn:escapeXml(not empty title.string ? title.string : subchild.name)}>>${subchild.url}]]')">
                            <a href="${subchild.url}" title="${fn:escapeXml(subchild.name)}" onmousedown="return false;" >
                                    ${fn:escapeXml(not empty title.string ? title.string : subchild.name)}
                            </a>
                        </div>
                    </c:if>
                    <c:if test="${jcr:hasPermission(subchild,'jcr:removeNode')}">
                        <c:url var="urlNodePath" value="${url.base}${currentNode.path}.html.ajax">
                            <c:param name="targetNodePath" value="${targetNode.path}"/>
                        </c:url>
                        <form action="<c:url value='${url.base}${subchild.path}'/>" method="post"
                              id="jahia-wiki-item-delete-${subchild.UUID}">
                            <input type="hidden" name="jcrMethodToCall" value="delete"/>
                            <button><fmt:message key="label.delete"/></button>
                            <script type="text/javascript">
                                $(document).ready(function() {
                                    // bind 'myForm' and provide a simple callback function
                                    var options = {
                                        success: function() {
                                            $('#fileList${currentNode.identifier}').load('${urlNodePath}');
                                        }
                                    }
                                    $('#jahia-wiki-item-delete-${subchild.UUID}').ajaxForm(options);
                                });
                            </script>
                        </form>
                    </c:if>
                </li>
            </c:if>
        </c:forEach>
    </ul>
</div>
<template:addCacheDependency path="${targetNodePath}"/>
