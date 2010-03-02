<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<c:if test="${not empty paginationActive and totalSize > 0 and nbPages > 1}">
    <div class="pagination"><!--start pagination-->

        <div class="paginationPosition"><span>Page ${currentPage} of ${nbPages} (${totalSize} results)</span>
        </div>
        <div class="paginationNavigation">
            <c:if test="${currentPage>1}">
                <a class="previousLink"
                   href="javascript:replace('${currentNode.UUID}','${url.current}?ajaxcall=true&begin=${ (currentPage-2) * pageSize }&end=${ (currentPage-1)*pageSize-1}','${renderContext.moduleParams.callback}')">Previous</a>
            </c:if>
            <c:forEach begin="1" end="${nbPages}" var="i">
                <c:if test="${i != currentPage}">
                    <span><a class="paginationPageUrl"
                             href="javascript:replace('${currentNode.UUID}','${url.current}?ajaxcall=true&begin=${ (i-1) * pageSize }&end=${ i*pageSize-1}','${renderContext.moduleParams.callback}')"> ${ i }</a></span>
                </c:if>
                <c:if test="${i == currentPage}">
                    <span class="currentPage">${ i }</span>
                </c:if>
            </c:forEach>

            <c:if test="${currentPage<nbPages}">
                <a class="nextLink"
                   href="javascript:replace('${currentNode.UUID}','${url.current}?ajaxcall=true&begin=${ currentPage * pageSize }&end=${ (currentPage+1)*pageSize-1}','${renderContext.moduleParams.callback}')">Next</a>
            </c:if>
        </div>

        <div class="clear"></div>
    </div>
    <!--stop pagination-->
</c:if>
