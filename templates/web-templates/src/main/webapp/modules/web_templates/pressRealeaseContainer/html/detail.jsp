<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>


<jcr:nodeProperty node="${currentNode}" name="date" var="datePR"/>
<jcr:nodeProperty node="${currentNode}" name="title" var="titlePR"/>
 <jcr:nodeProperty node="${currentNode}" name="pdfVersion" var="pdfVersion"/>
<jcr:nodeProperty node="${currentNode}" name="body" var="bodyPR"/>

<template:getContentObjectCategories var="pressReleaseContainerCatKeys"
                                             objectKey="contentContainer_${pageScope.pressReleaseContainer.ID}"/>

<h4>${titlePR.string}</h4>
<span class="pressRealeseDate"><fmt:formatDate value="${datePR.date}" pattern="dd/MM/yyyy"/></span>

        <div>${bodyPR.string}</div>
        <c:if test="${!empty pdfVersion.file.fileFieldTitle}">
            <div><strong><fmt:message key="web_templates_publicationContainer.download"/> :</strong>
                ${pdfVersion.downladUrl}</div>
        </c:if>
        <c:if test="${!empty pressReleaseContainerCatKeys }">
            <span class="pressRealeseCategory">
                <fmt:message key='category'/> : <ui:displayCategoryTitle categoryKeys="${pressReleaseContainerCatKeys}"/>
            </span>
        </c:if>

    <a class="returnLink" href="${requestScope.currentPage.url}" title='<fmt:message key="backToPreviousPage"/>'><fmt:message key="backToPreviousPage"/></a>

    <div class="clear"> </div>



