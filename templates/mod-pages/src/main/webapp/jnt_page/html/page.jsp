<%@ include file="../../common/declarations.jspf" %>
<jsp:useBean id="colMap" class="java.util.LinkedHashMap"/>
<c:if test="${jcr:isNodeType(currentNode, 'jmix:gridPage')}">
	<jcr:nodeProperty node="${currentNode}" name="column" var="column"/>
	<c:choose>
	    <c:when test="${column.string == '1col16'}">
	        <c:set target="${colMap}" property="col1" value="16"/>
	    </c:when>
          <c:when test="${column.string == '2col412'}">
	        <c:set target="${colMap}" property="col2" value="4"/>
	        <c:set target="${colMap}" property="col1" value="12"/>
	    </c:when>
        <c:when test="${column.string == '2col124'}">
	        <c:set target="${colMap}" property="col1" value="12"/>
	        <c:set target="${colMap}" property="col2" value="4"/>
	    </c:when>
	    <c:when test="${column.string == '2col106'}">
	        <c:set target="${colMap}" property="col1" value="10"/>
	        <c:set target="${colMap}" property="col2" value="6"/>
	    </c:when>
         <c:when test="${column.string == '2col610'}">
	        <c:set target="${colMap}" property="col2" value="6"/>
	        <c:set target="${colMap}" property="col1" value="10"/>
	    </c:when>
        <c:when test="${column.string == '2col88'}">
	        <c:set target="${colMap}" property="col1" value="8"/>
	        <c:set target="${colMap}" property="col2" value="8"/>
	    </c:when>
	    <c:when test="${column.string == '3col448'}">
	        <c:set target="${colMap}" property="col3" value="4"/>
	        <c:set target="${colMap}" property="col2" value="4"/>
	        <c:set target="${colMap}" property="col1" value="8"/>
	    </c:when>
        <c:when test="${column.string == '3col466'}">
	        <c:set target="${colMap}" property="col3" value="4"/>
	        <c:set target="${colMap}" property="col2" value="6"/>
	        <c:set target="${colMap}" property="col1" value="6"/>
	    </c:when>
        <c:when test="${column.string == '3col484'}">
	        <c:set target="${colMap}" property="col3" value="4"/>
	        <c:set target="${colMap}" property="col1" value="8"/>
	        <c:set target="${colMap}" property="col2" value="4"/>
	    </c:when>
        <c:when test="${column.string == '3col664'}">
	        <c:set target="${colMap}" property="col1" value="6"/>
	        <c:set target="${colMap}" property="col2" value="6"/>
	        <c:set target="${colMap}" property="col3" value="4"/>
	    </c:when>
         <c:when test="${column.string == '3col844'}">
	        <c:set target="${colMap}" property="col1" value="8"/>
	        <c:set target="${colMap}" property="col2" value="4"/>
	        <c:set target="${colMap}" property="col3" value="4"/>
	    </c:when>
        <c:otherwise>
            <c:set target="${colMap}" property="col1" value="10"/>
            <c:set target="${colMap}" property="col2" value="6"/>
        </c:otherwise>
	</c:choose>
</c:if>

<c:forEach items="${colMap}" var="col" varStatus="count">
    <c:choose>
        <c:when test="${col.value > 8}">
    <div class='grid_${col.value}'><!--start grid_${col.value}-->
        <div class="box">
            <div class="boxshadow boxpadding40 boxmarginbottom16">
                <div class="box-inner">
                    <div class="box-inner-border">
                        <template:module path="${col.key}" autoCreateType="jnt:contentList"/>
                        <div class='clear'></div>
                    </div>
                </div>
            </div>
        </div>
    </div><!--stop grid_${col.value}-->
        </c:when>

        <c:otherwise>
            <div class='grid_${col.value}'><!--start grid_${col.value}-->
                <template:module path="${col.key}" autoCreateType="jnt:contentList"/>
                <div class='clear'></div>
            </div><!--stop grid_${col.value}-->
        </c:otherwise>
    </c:choose>
</c:forEach>                       