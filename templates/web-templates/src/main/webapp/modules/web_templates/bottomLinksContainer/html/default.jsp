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
            <jcr:node var="links" path="${currentNode}/link"/>
            <div class="sitemap">
                <ul>
                    <c:forEach items="${currentNode.children}"  var="link">
                        <c:if test="${jcr:isNodeType(link, 'jnt:page')}">
                            <li>
                                <jcr:nodeProperty node="${link}" name="jcr:title" var="title"/>
                                <a href="${baseUrl}${link.path}.html">${title.string}</a>
                            </li>
                        </c:if>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </div>
    <div class="clear"></div>
</div>