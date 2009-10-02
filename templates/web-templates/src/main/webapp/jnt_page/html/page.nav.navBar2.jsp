<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:navigationMenu node="${currentNode}" var="menu" kind="sideMenu"/>
<c:if test="${not empty menu}">
    <div id="navigationN2">

        <c:forEach items="${menu}" var="navMenuBean">
            <jcr:nodeProperty node="${navMenuBean.node}" name="jcr:title" var="title"/>
            <c:if test="${navMenuBean.firstInLevel}">
                <ul class="level_${navMenuBean.level}">
            </c:if>
            <c:if test="${!navMenuBean.firstInLevel}">
                </li>
            </c:if>
            <c:set var="liCssNavItem" value=""/>
            <c:set var="aCssNavItem" value=""/>
            <c:if test="${navMenuBean.firstInLevel}"><c:set var="liCssNavItem" value="${liCssNavItem} first"/></c:if>
            <c:if test="${navMenuBean.lastInLevel}"><c:set var="liCssNavItem" value="${liCssNavItem} last"/></c:if>
            <c:if test="${navMenuBean.inPath}"><c:set var="aCssNavItem" value="${aCssNavItem} inpath"/></c:if>
            <c:if test="${navMenuBean.selected}"><c:set var="aCssNavItem" value="${aCssNavItem} selected"/></c:if>
            <li class="${liCssNavItem}">
            <a class="${aCssNavItem}" href="${url.base}${navMenuBean.node.path}.html"
               alt="${title.string}"><span>${title.string}</span></a>
            <c:if test="${navMenuBean.lastInLevel}">
                </li>
                </ul>
            </c:if>
        </c:forEach>
    </div>
</c:if>