<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<%--<jcr:node var="page" path="/sites/${renderContext.site.siteKey}/home" />--%>

<%--<div id="navigationN1"><!--start navigationN1-->--%>
    <%--<ul class="level_1">--%>
    <%--<c:if test="${currentNode != null}">--%>

        <%--<jcr:nodeProperty node="${page}" name="jcr:title" var="title"/>--%>

        <%--<li class="item_1 standard first">--%>
        <%--<a href="${url.base}${page.path}.html">${title.string}</a>--%>
        <%--</li>--%>
    <%--</c:if>--%>

    <%--<c:forEach items="${page.children}" var="child" varStatus="status">--%>
        <%--<c:if test="${jcr:isNodeType(child, 'jnt:page')}">--%>
            <%--<li class="item_${status.index + 2} standard ">--%>
            <%--<jcr:nodeProperty node="${child}" name="jcr:title" var="title"/>--%>
            <%--<a href="${url.base}${child.path}.html">${title.string}</a>--%>
            <%--</li>--%>
        <%--</c:if>--%>
    <%--</c:forEach>--%>
    <%--</ul>--%>
<%--</div><!--stop navigationN1-->--%>

<c:if test="${jcr:isNodeType(renderContext.mainResource.node, 'jnt:page')}">
    <c:set var="page" value="${renderContext.mainResource.node}" />
</c:if>
<c:if test="${!jcr:isNodeType(renderContext.mainResource.node, 'jnt:page')}">
    <c:set var="page" value="${jcr:getParentOfType(renderContext.mainResource.node, 'jnt:page')}" />
</c:if>

<jcr:navigationMenu node="${page}" var="menu" startLevel="${currentNode.properties['j:startLevel'].long}" maxDepth="${currentNode.properties['j:maxDepth'].long}" />
<c:if test="${not empty menu}">
    <div id="navigationN1">
        <ul class="level_1">

        <c:forEach items="${menu}" var="navMenuBean">
            <jcr:nodeProperty node="${navMenuBean.node}" name="jcr:title" var="title"/>

            <li class="item_${navMenuBean.itemCount + 2} standard ">
            <a href="${url.base}${navMenuBean.node.path}.html">${title.string}</a>
            </li>

        </c:forEach>
        </ul>
    </div>
</c:if>
