<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<div id="${currentNode.UUID}">

    <c:remove var="currentList" scope="request"/>
    <template:module node="${currentNode}" forcedTemplate="hidden.load" editable="false">
        <template:param name="forcedSkin" value="none"/>
    </template:module>

    <c:if test="${empty editable}">
        <c:set var="editable" value="false"/>
    </c:if>

    <template:addResources type="javascript" resources="ajaxreplace.js"/>
    <template:initPager pageSize="${currentNode.properties['pageSize'].long}" totalSize="${fn:length(currentList)}"/>

    <c:forEach items="${currentList}" var="subchild" begin="${begin}" end="${end}">
        <p>
            <template:module node="${subchild}" template="${subNodesTemplate}" editable="${editable}">
                <c:if test="${not empty forcedSkin}">
                    <template:param name="forcedSkin" value="${forcedSkin}"/>
                </c:if>
                <c:if test="${not empty renderOptions}">
                    <template:param name="renderOptions" value="${renderOptions}"/>
                </c:if>
            </template:module>
        </p>
    </c:forEach>

    <div class="pagination"><!--start pagination-->

        <div class="paginationPosition"><span>Page ${currentPage} of ${nbPages} - ${tasks.nodes.size} results</span>
        </div>
        <div class="paginationNavigation">
            <c:if test="${currentPage>1}">
                <a class="previousLink"
                   href="javascript:replace('${currentNode.UUID}','${url.current}?ajaxcall=true&begin=${ (currentPage-2) * pageSize }&end=${ (currentPage-1)*pageSize-1}')">Previous</a>
            </c:if>
            <c:forEach begin="1" end="${nbPages}" var="i">
                <c:if test="${i != currentPage}">
                    <span><a class="paginationPageUrl"
                             href="javascript:replace('${currentNode.UUID}','${url.current}?ajaxcall=true&begin=${ (i-1) * pageSize }&end=${ i*pageSize-1}')"> ${ i }</a></span>
                </c:if>
                <c:if test="${i == currentPage}">
                    <span class="currentPage">${ i }</span>
                </c:if>
            </c:forEach>

            <c:if test="${currentPage<nbPages}">
                <a class="nextLink"
                   href="javascript:replace('${currentNode.UUID}','${url.current}?ajaxcall=true&begin=${ currentPage * pageSize }&end=${ (currentPage+1)*pageSize-1}')">Next</a>
            </c:if>
        </div>

        <div class="clear"></div>
    </div>
    <!--stop pagination-->
</div>
