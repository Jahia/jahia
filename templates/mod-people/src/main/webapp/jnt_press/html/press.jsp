<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<ul class="pressRealeseList">
    <li class="pressRealeseItem">
        <jcr:nodeProperty node="${currentNode}" name='date' var="datePress"/>
        <span class="pressRealeseDate"><fmt:formatDate value="${datePress.date.time}" pattern="dd/MM/yyyy"/></span>
        <jcr:nodeProperty node="${currentNode}" name="j:defaultCategory" var="pressReleaseContainerCatKeys"/>
        <c:if test="${!empty pressReleaseContainerCatKeys }">
            <span class="pressRealeseCategory">
                <fmt:message key='category'/> : <ui:displayCategoryTitle
                    categoryKeys="${pressReleaseContainerCatKeys}"/>
            </span>
        </c:if>
        <h4><a href="${url.base}${currentNode.path}.detail.html"><jcr:nodeProperty node="${currentNode}" name='jcr:title'/></a></h4>

        <div class="clear"></div>
    </li>
</ul>