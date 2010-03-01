<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<jcr:nodeProperty node="${currentNode}" name="j:defaultCategory" var="pressReleaseContainerCatKeys"/>

<h4><jcr:nodeProperty node="${currentNode}" name='jcr:title'/></h4>

<jcr:nodeProperty node="${currentNode}" name='date' var="datePress"/>
<span class="pressRealeseDate"><fmt:formatDate value="${currentNode.properties.date.time}" pattern="dd/MM/yyyy"/></span>

<div>${currentNode.properties.body.string}</div>
<jcr:nodeProperty node="${currentNode}" name="pdfVersion" var="pdfVersion"/>
<c:if test="${not empty pdfVersion}">
    <div><strong><fmt:message key="label.download"/> :</strong>
        <a href="${pdfVersion.node.url}">${pdfVersion.node.name}</a></div>
</c:if>
<c:if test="${!empty pressReleaseContainerCatKeys }">
            <span class="pressRealeseCategory">
                <fmt:message key='label.category'/> : <ui:displayCategoryTitle
                    categoryKeys="${pressReleaseContainerCatKeys}"/>
            </span>
</c:if>