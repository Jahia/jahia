<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>

<div class="box4-text">

	<div class="publicationListItem"><!--start publicationListItem -->
		<div class="publicationListSpace"><!--start publicationListSpace -->
			<div class="publicationPhoto">
                <a href="${file.node.url}" >
                    <c:if test="${not empty preview}">
                        <img src="${preview.node.url}" alt="${preview.node.propertiesAsString['jcr:title']}">
                    </c:if>
                    <c:if test="${empty preview}">
                    <img src="<utility:resolvePath value='theme/${requestScope.currentTheme}/img/no_preview.png'/>" alt="no preview"/>
                    </c:if>
                </a>
            </div>

				<div class="publicationBody"><!--start publicationBody -->
					    <h5><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h5>
					    <p class="publicationAuthor"><c:if test="${!empty author}"><fmt:message key="publication.author"/>: ${currentNode.properties.author.string}</c:if></p>
					    <p class="publicationSource"><c:if test="${!empty source}"><fmt:message key="publication.source"/>: ${currentNode.properties.source.string}</c:if></p>
					    <p class="publicationDate"><c:if test="${!empty date && date !=''}">${currentNode.properties.date.string}</c:if></p>
					    <div class="publicationDescription">${currentNode.properties.body.string}</div>
					    <div class="publicationAction">
					        <c:if test="${file.node.fileContent.contentLength > 0}">
                                <fmt:formatNumber var="num" pattern="### ### ###.##" type="number" value="${(file.node.fileContent.contentLength/1024)}"/>
                                <a class="publicationDownload" href="${file.node.url}" ><fmt:message key="publication.download"/></a><span class="publicationDocSize">(${num} KB)</c:if>
                        </span>
					</div>
					<div class="clear"> </div>
				</div><!--stop publicationBody -->

			<div class="clear"> </div>
		</div><!--stop publicationListSpace -->
		<div class="clear"> </div>
	</div><!--stop publicationListItem -->
</div>