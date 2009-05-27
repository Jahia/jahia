<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jcr:node name="jcrnode" path="/content/shared"/>
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