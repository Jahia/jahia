<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jcr:node var="jcrnode" path="/content/shared"/>
<p>Access to node attributes directly :</p>
<ul>
    <li>Node: ${jcrnode.name}</li>
    <li>URL: ${jcrnode.url}</li>
    <li>Date: ${jcrnode.lastModifiedAsDate}</li>
    <li>File: ${jcrnode.file}</li>
    <li>Collection: ${jcrnode.collection}</li>
    <li>Download: <jcr:link path="${jcrnode.path}">link</jcr:link> or <jcr:link path="${jcrnode.path}" absolute="true">absolute link</jcr:link></li>
</ul>
<p>Access to specific property</p>
<jcr:nodeProperty node="${jcrnode}" name="jcr:created" var="createdDate" varDef="createdDef">
    <ul>
        <li>Creation Date : <fmt:formatDate value="${createdDate.date.time}" dateStyle="full"/></li>
        <li>Is Property Multi Valued : ${createdDef.multiple}</li>
    </ul>
</jcr:nodeProperty>
<p>Access to childs of a node</p>
<c:forEach items="${jcrnode.children}" var="child">
    <ul>
    <li>Node: ${child.name}</li>
    <li>URL: ${child.url}</li>
    <li>Date: ${child.lastModifiedAsDate}</li>
    <li>File: ${child.file}</li>
</ul>
</c:forEach>