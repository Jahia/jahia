<%@ page import="org.jahia.utils.FileUtils" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
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
<div id="fileList${currentNode.identifier}">
    <template:addResources type="css" resources="fileList.css"/>
    <jcr:node var="targetNode" path="${targetNodePath}"/>
    <ul class="filesList">
        <c:forEach items="${targetNode.nodes}" var="subchild">
            <c:if test="${jcr:isNodeType(subchild, 'jnt:file')}">
                <li>
                    <jcr:nodeProperty node="${subchild}" name="jcr:title" var="title"/>
                    <c:set var="isImage" value="${fn:startsWith(subchild.fileContent.contentType,'image/')}" />
                    <c:choose>
                        <c:when test="${isImage}">
                            <div>
                                <img width="100" src="${subchild.url}"  alt="${fn:escapeXml(subchild.name)}" onmousedown="return false;" />
                                    ${fn:escapeXml(not empty title ? title : subchild.name)}
                            </div>
                        </c:when>
                        <c:otherwise>
                            <c:set var="title" value="${fn:escapeXml(not empty title.string ? title.string : subchild.name)}"/>
                            <div>
                                <span class="icon <%=FileUtils.getFileIcon( ((JCRNodeWrapper) pageContext.findAttribute("subchild")).getName()) %>"></span>
                                <a href="${subchild.url}" onmousedown="return false;" title="${title}">${title}</a>
                            </div>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${jcr:hasPermission(subchild,'jcr:removeNode')}">
                        <template:tokenizedForm>
                            <form action="<c:url value='${url.base}${subchild.path}'/>" method="post"
                                  id="jahia-blog-item-delete-${subchild.UUID}">
                                <input type="hidden" name="jcrMethodToCall" value="delete"/>
                                <button><fmt:message key="label.delete"/></button>
                                <c:choose>
                                    <c:when test="${isImage}">
                                        <button onclick="CKEDITOR.instances.editContent.insertHtml('<img src=\'${subchild.url}\'/>'); return false;"><fmt:message key="label.add" /></button>
                                    </c:when>
                                    <c:otherwise>
                                        <button onclick="CKEDITOR.instances.editContent.insertHtml('<a href=\'${subchild.url}\' title=\'${title}\'>${title}</a>'); return false;"><fmt:message key="label.add" /></button>
                                    </c:otherwise>
                                </c:choose>
                                <script type="text/javascript">
                                    <c:url var="urlPath" value="${url.base}${currentNode.path}.html.ajax">
                                    <c:param name="targetNodePath" value="${targetNode.path}"/>
                                    </c:url>
                                    $(document).ready(function() {
                                        // bind 'myForm' and provide a simple callback function
                                        var options = {
                                            success: function() {
                                                $('#fileList${currentNode.identifier}').load('${urlPath}');
                                                var dataText =CKEDITOR.instances.editContent.getData();
                                                while ((i = dataText.search('${subchild.url}')) > 0 ) {
                                                    var before = dataText.substring(0,i);
                                                    var after = dataText.substring(i);
                                                    dataText = before.substring(0,before.lastIndexOf('<'));
                                                    if (after.substring(after.indexOf(">")-1,after.indexOf(">")) == "/") {
                                                        dataText += after.substring(after.indexOf("/>") + 2);
                                                    } else {
                                                        dataText += after.substring(after.indexOf("/a>") + 3);
                                                    }
                                                }
                                                CKEDITOR.instances.editContent.setData(dataText);
                                            }
                                        }
                                        $('#jahia-blog-item-delete-${subchild.UUID}').ajaxForm(options);
                                    });
                                </script>
                            </form>
                        </template:tokenizedForm>
                    </c:if>
                </li>
            </c:if>
        </c:forEach>
    </ul>
</div>
<template:addCacheDependency path="${targetNodePath}"/>
