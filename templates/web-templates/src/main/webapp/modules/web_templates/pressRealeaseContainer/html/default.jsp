<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>


<jcr:nodeProperty node="${currentNode}" name="date" var="datePR"/>
<jcr:nodeProperty node="${currentNode}" name="title" var="titlePR"/>
                                                                

    <li class="pressRealeseItem">
            <span class="pressRealeseDate"><fmt:formatDate value="${datePR.date}" pattern="dd/MM/yyyy"/></span>
            <template:getContentObjectCategories var="pressReleaseContainerCatKeys" objectKey="contentContainer_${pageScope.pressReleaseContainer.ID}"/>
        <c:if test="${!empty pressReleaseContainerCatKeys }">
            <span class="pressRealeseCategory">
                <fmt:message key='category'/> : <ui:displayCategoryTitle categoryKeys="${pressReleaseContainerCatKeys}"/>
            </span>
        </c:if>
            <c:url var="detailsUrl" value="${currentPage.url}" context="/">
                <c:param name="template" value="tpl.pressReleasesDetail"/>
                <c:param name="queryPath" value="${pressReleaseContainer.JCRPath}"/>
            </c:url>
            <h4><a href="${detailsUrl}">${titlePR}</a></h4>
        <div class="clear"> </div>
    </li>
