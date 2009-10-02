<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<div class="box4 ">
	<div class="box4-topright"></div>
		<div class="box4-topleft"></div>
		<h3 class="box4-header"><span class="publicationTitle">Publications</span></h3>
		<div class="box4-bottomright"></div>
		<div class="box4-bottomleft"></div>
		<div class="clear"> </div>
</div>
<c:forEach items="${currentNode.nodes}" var="node" varStatus="status">
    <template:module node="${node}">
        <template:param name="loop" value="${status.count}"/>
    </template:module>
</c:forEach>