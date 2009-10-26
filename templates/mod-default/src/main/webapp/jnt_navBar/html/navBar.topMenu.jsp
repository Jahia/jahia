<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<jcr:node var="page" path="/content/sites/${renderContext.site.siteKey}/home" />

<div id="navigationN1"><!--start navigationN1-->
    <ul class="level_1">
    <c:if test="${currentNode != null}">

        <jcr:nodeProperty node="${page}" name="jcr:title" var="title"/>

        <li class="item_1 standard first">
        <a href="${url.base}${page.path}.html">${title.string}</a>
        </li>
    </c:if>

    <c:forEach items="${page.children}" var="child" varStatus="status">
        <c:if test="${jcr:isNodeType(child, 'jnt:page')}">
            <li class="item_${status.index + 2} standard ">
            <jcr:nodeProperty node="${child}" name="jcr:title" var="title"/>
            <a href="${url.base}${child.path}.html">${title.string}</a>
            </li>
        </c:if>
    </c:forEach>
    </ul>
</div><!--stop navigationN1-->
