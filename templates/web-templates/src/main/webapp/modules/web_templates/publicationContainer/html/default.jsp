<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<jcr:nodeProperty node="${currentNode}" name="title" var="titlePublication"/>
<jcr:nodeProperty node="${currentNode}" name="body" var="bodyPublication"/>
<jcr:nodeProperty node="${currentNode}" name="file" var="filePublication"/>
<jcr:nodeProperty node="${currentNode}" name="preview" var="previewPublication"/>
<jcr:nodeProperty node="${currentNode}" name="author" var="authorPublication"/>
<jcr:nodeProperty node="${currentNode}" name="source" var="sourcePublication"/>
<jcr:nodeProperty node="${currentNode}" name="date" var="datePublication"/>


<c:set var="loop" value="0"/>
<div class="box4-text">

	<div class="publicationListItem"><!--start publicationListItem -->
		<div class="publicationListSpace"><!--start publicationListSpace -->
			<div class="publicationPhoto">
                <a href="${filePublication.file.downloadUrl}" >
                    <c:if test="${!empty preview}"><img src="${previewsPublication.file.image}" alt=""></c:if>
                    <c:if test="${!previewsPublication.file.image}"><img src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/no_preview.png'/>" alt="no preview"/></c:if>
                </a>
            </div>

				<div class="publicationBody"><!--start publicationBody -->
					    <h5>${titlePublication.string}</h5>
					    <p class="publicationAuthor"><c:if test="${!empty author}"><fmt:message key="web_templates_publicationContainer.author"/>: ${authorPublication.string}</c:if></p>
					    <p class="publicationSource"><c:if test="${!empty source}"><fmt:message key="web_templates_publicationContainer.source"/>: ${authorPublication.source}</c:if></p>
					    <p class="publicationDate"><c:if test="${!empty date && date !=''}">${datePublication.date}</c:if></p>
					    <div class="publicationDescription">${bodyPublication.string}</div>
					    <div class="publicationAction">
					        <c:if test="${filePublication.file.size > 0}">
                                <fmt:formatNumber var="num" pattern="### ### ###.##" type="number" value="${(filePublication.file.size/1024)}"/>
                                <a class="publicationDownload" href="${filePublication.file.downloadUrl}" ><fmt:message key="web_templates_publicationContainer.download"/></a><span class="publicationDocSize">(${num} KB)</c:if>
                        </span>
					</div>
					<div class="clear"> </div>
				</div><!--stop publicationBody -->
			<div class="clear"> </div>
		</div><!--stop publicationListSpace -->
		<div class="clear"> </div>
	</div><!--stop publicationListItem -->
    <c:choose>
        <c:when test="${loop == '0'}">
           <c:set var="loop" value="1"/>
        </c:when>
        <c:otherwise>
            <div class="clear"> </div>
            <c:set var="loop" value="0"/>
        </c:otherwise>
    </c:choose>

</div>