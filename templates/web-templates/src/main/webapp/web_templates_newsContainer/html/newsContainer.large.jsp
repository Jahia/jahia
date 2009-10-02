<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>

 <jcr:nodeProperty node="${currentNode}" name="newsTitle" var="newsTitle"/>
 <jcr:nodeProperty node="${currentNode}" name="newsDate" var="newsDate"/>
 <jcr:nodeProperty node="${currentNode}" name="newsDesc" var="newsDesc"/>
 <jcr:nodeProperty node="${currentNode}" name="newsImage" var="newsImage"/>

    <div class="newsListItem"><!--start newsListItem -->
        <h4><a href="${url.base}${currentNode.path}.large.html">${newsTitle.string}</a></h4>

        <p class="newsInfo">
            <span class="newsLabelDate"><fmt:message key="news.date"/> :</span>
            <span class="newsDate">
                <fmt:formatDate value="${newsDate.date.time}" pattern="dd/MM/yyyy"/>&nbsp;<fmt:formatDate value="${newsDate.date.time}" pattern="HH:mm" var="dateTimeNews"/>
                <c:if test="${dateTimeNews != '00:00'}">${dateTimeNews}</c:if>
            </span>
        </p>
        <div class="newsImg-right"><a href="${url.current}"><img src="${newsImage.node.url}"/></a></div>
        <p class="newsResume">
            ${newsDesc.string}
        </p>

        <div class="more"><span><a href="${url.base}${currentNode.path}.large.html">
            <fmt:message key="news.readmore"/>
        </a></span></div>

        <%--<c:if test="${!empty newsContainerCatKeys }">
            <div class="newsMeta">
                    <span class="categoryLabel"><fmt:message key='category'/>  :</span>
                <ui:displayCategoryTitle categoryKeys="${newsContainerCatKeys}"/>
            </div>
        </c:if>--%>
        <div class="clear"> </div>
    </div>