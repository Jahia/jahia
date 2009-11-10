<%@ include file="../../common/declarations.jspf" %>
<jsp:useBean id="colMap" class="java.util.LinkedHashMap"/>
<c:set target="${colMap}" property="col1" value="10"/>
<c:set target="${colMap}" property="col2" value="6"/>
<c:if test="${jcr:isNodeType(currentNode, 'jmix:gridPage')}">
	<jcr:nodeProperty node="${currentNode}" name="column" var="column"/>
	<c:choose>
	    <c:when test="${column.string == 'col1'}">
	        <c:set target="${colMap}" property="col1" value="16"/>
	    </c:when>
	    <c:when test="${column.string == 'col2'}">
	        <c:set target="${colMap}" property="col1" value="10"/>
	        <c:set target="${colMap}" property="col2" value="6"/>
	    </c:when>
	    <c:when test="${column.string == 'col3'}">
	        <c:set target="${colMap}" property="col2" value="4"/>
	        <c:set target="${colMap}" property="col1" value="8"/>
	        <c:set target="${colMap}" property="col3" value="4"/>
	    </c:when>
	</c:choose>
</c:if>

<c:forEach items="${colMap}" var="col" varStatus="count">
    <div class='grid_${col.value}'><!--start grid_${col.value}-->
        <template:module path="${col.key}" autoCreateType="jnt:contentList"/>
    <div class='clear'></div></div><!--stop grid_${col.value}-->
</c:forEach>