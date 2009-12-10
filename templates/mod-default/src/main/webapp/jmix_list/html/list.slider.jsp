<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<c:remove var="currentList" scope="request"/>
<template:module node="${currentNode}" forcedTemplate="hidden.load" editable="false" >
    <template:param name="forcedSkin" value="none" />
</template:module>

<template:addResources type="javascript" resources="jquery.min.js,jquery.bxSlider.js,jquery.bxSlider.load.js"/>
<template:addResources type="css" resources="jquery.bxSlider.css"/>

     <c:if test="${empty editable}">
		<c:set var="editable" value="false"/>
	</c:if>

<!-- récupérer une liste d'items de type teasers et boucler dessus -->


<div id="example1">

    <c:forEach items="${currentList}" var="child" varStatus="status">
        <c:if test="${jcr:isNodeType(child, 'jmix:thumbnail')}">
                <div class="item">

				<img src="${url.context}/repository/default${child.path}/thumbnail" alt=""><br/>
					<h3>${child.name}</h3>
					<p>A nice image, this text has to be replaced by metadata decription or another descriptive field if another type of content than images. ${currentNode.properties.abstract.string} </p>
                </div>
        </c:if>


	<c:if test="${jcr:isNodeType(child, 'jnt:news')}">
                <div class="item">
				 <jcr:nodeProperty node="${child}" name="image" var="newsImage"/>

					<img src="${newsImage.node.url}"/><br/>
					<h3><fmt:formatDate value="${child.properties.date.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate value="${currentNode.properties.date.time}" pattern="HH:mm" var="dateTimeNews"/><c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>: <jcr:nodeProperty node="${child}" name="jcr:title"/></h3>
					<p>${child.properties.desc.string}</p>

                </div>
        </c:if>
    </c:forEach>

    <template:module path="*"/>

</div>
