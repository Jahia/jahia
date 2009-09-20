<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>

<jcr:nodeProperty node="${currentNode}" name="title" var="title"/>
<jcr:nodeProperty node="${currentNode}" name="body" var="body"/>
<jcr:nodeProperty node="${currentNode}" name="file" var="file"/>
<jcr:nodeProperty node="${currentNode}" name="preview" var="preview"/>
<jcr:nodeProperty node="${currentNode}" name="author" var="author"/>
<jcr:nodeProperty node="${currentNode}" name="source" var="source"/>
<jcr:nodeProperty node="${currentNode}" name="date" var="date"/>


<c:set var="loop" value="0"/>
<div class="box4-text">

    <div class="publicationListItem"><!--start publicationListItem -->
        <div class="publicationListSpace"><!--start publicationListSpace -->
            <div class="publicationPhoto">
                <a href="${file.node.downloadUrl}">
                    <c:if test="${!empty preview}"><img src="${preview.node.url}" alt=""></c:if>
                   
                </a>
            </div>

            <div class="publicationBody"><!--start publicationBody -->
                <h5>${title.string}</h5>

                <p class="publicationAuthor"><c:if test="${!empty author}"><fmt:message
                        key="web_templates_publicationContainer.author"/>: ${author}</c:if></p>

                <p class="publicationSource"><c:if test="${!empty source}"><fmt:message
                        key="web_templates_publicationContainer.source"/>: ${source}</c:if></p>

                <p class="publicationDate"><c:if test="${!empty date && date !=''}">${date}</c:if></p>

                <div class="publicationDescription">${body}</div>
                <div class="publicationAction">
                    <c:if test="${file.file.size > 0}">
                    <fmt:formatNumber var="num" pattern="### ### ###.##" type="number"
                                      value="${(file.file.size/1024)}"/>
                    <a class="publicationDownload" href="${file.node.url}"><fmt:message
                            key="web_templates_publicationContainer.download"/></a><span
                        class="publicationDocSize">(${num} KB)</c:if>
                        </span>
                </div>
                <div class="clear"></div>
            </div>
            <!--stop publicationBody -->
            <div class="clear"></div>
        </div>
        <!--stop publicationListSpace -->
        <div class="clear"></div>
    </div>
    <!--stop publicationListItem -->
    <c:choose>
        <c:when test="${loop == '0'}">
            <c:set var="loop" value="1"/>
        </c:when>
        <c:otherwise>
            <div class="clear"></div>
            <c:set var="loop" value="0"/>
        </c:otherwise>
    </c:choose>

</div>