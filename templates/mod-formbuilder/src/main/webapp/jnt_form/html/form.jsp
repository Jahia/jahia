<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<h2><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h2>


<div class="intro">
    ${currentNode.propertiesAsString['j:intro']}
</div>
<form action="${url.base}${currentNode.path}/*" method="post">
    <input type="hidden" name="nodeType" value="jnt:post"/>
    <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
    <%-- Define the output format for the newly created node by default html or by stayOnNode--%>
    <input type="hidden" name="newNodeOutputFormat" value="html"/>
  <c:forEach items="${jcr:getNodes(currentNode,'jnt:formElement'}" var="formElement">
      <template:module node="${formElement}" template="default"/>
  </c:forEach>
</form>
<template:module autoCreateType="jnt:formElement" path="${currentNode.path}"/>

<div>
    <h2>Responses</h2>
    <c:forEach items="${jcr:getNodes(currentNode,'jnt:responseToForm'}" var="response">
      <template:module node="${response}" template="default"/>
  </c:forEach>
</div>