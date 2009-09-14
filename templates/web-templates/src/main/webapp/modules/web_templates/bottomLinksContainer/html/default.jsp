<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<div class="column-item">
    <div class="spacer"><!--start mapshortcuts-->
        <div class="mapshortcuts">
            <h4><template:module template="link" path="link"/></h4>
<%--            <div class="sitemap">
                <ul>
                    <c:forEach items="${currentNode.children}"  var="child">
                        <c:if test="${jcr:isNodeType(child, 'jnt:page')}">
                            <li>
                                <jcr:nodeProperty node="${child}" name="jcr:title" var="title"/>
                                <a href="${url.base}${child.path}.html">${title.string}</a>
                            </li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>--%>
        </div>
    </div>
    <div class="clear"></div>
</div>