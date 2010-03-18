<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://www.jahia.org/tags/jcr" prefix="jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jcr:nodeProperty node="${currentNode}" name="image" var="image"/>

<div class="maincontent">
    <h3 class="title edit${currentNode.identifier}" jcr:id="jcr:title" jcr:url="${url.base}${currentNode.path}">
        <jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h3>
    <c:if test="${!empty image}">
        <div class="imagefloat${currentNode.properties.align.string} file${currentNode.identifier}" jcr:id="image"
             jcr:url="${url.base}${currentNode.path}">
            <img src="${image.node.url}" alt="${image.node.url}"/>
        </div>
    </c:if>
    <c:if test="${empty image}">
        <div class="imagefloat${currentNode.properties.align.string} file${currentNode.identifier}" jcr:id="image"
             jcr:url="${url.base}${currentNode.path}">
            <span>click here to attach a file</span>
        </div>
    </c:if>
        <span jcr:id="body" class="ckeditorEdit${currentNode.identifier}"
              id="ckeditorEdit${currentNode.identifier}${scriptPropName}"
              jcr:url="${url.base}${currentNode.path}">${currentNode.properties.body.string}</span>
</div>
<br class="clear"/>