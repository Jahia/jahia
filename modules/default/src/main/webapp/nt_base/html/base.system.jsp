<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<table width="100%" cellspacing="0" cellpadding="5" border="0" class="evenOddTable">
    <thead>
    <tr>
        <th width="5%" align="center">
            <c:if test="${jcr:isNodeType(currentNode.parent,'jnt:contentFolder') || jcr:isNodeType(currentNode.parent,'jnt:folder')}">
                <a title="parent" href="${url.base}${currentNode.parent.path}.html"><img height="16" width="16"
                                                                                         border="0"
                                                                                         style="cursor: pointer;"
                                                                                         title="parent" alt="parent"
                                                                                         src="${url.currentModule}/images/icons/folder_up.png"/></a>
            </c:if>
        </th>
    </tr>
    </thead>
</table>
<template:module node="${currentNode}" template="default" editable="false"/> 