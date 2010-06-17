<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:if test="${syndFeed == null}">
    <jcr:nodeProperty node="${currentNode}" name="url" var="feedUrl"/>
    Invalid feed url: ${feedUrl.string}
</c:if>
<c:if test="${syndFeed != null}">
    <div class="syndFeed">
        <div class="syndFeedTitle">
            <a href="${syndFeed.link}"> "${syndFeed.title}</a>
            <c:if test="${not empty syndFeed.image}">
                  <img src="${syndFeed.image.url}" title="${syndFeed.image.title}" alt="${syndFeed.image.description}"/>
            </c:if>
        </div>
        <div class="syndFeedEntries">
            <c:forEach items="${syndFeed.entries}" var="syndEntry" begin="0" end="${entriesCount - 1}">
                <div class="syndEntryTitle">
                    <a href="${syndEntry.link}"> ${syndEntry.title} [${syndEntry.updatedDate}]</a>
                </div>
                <div class="syndEntryDescription">
                        ${syndEntry.description.value}
                </div>
                <div class="syndEntryContents">
                    <c:forEach items="${syndEntry.contents}" var="syndContent">
                        <div class="syndEntryContent">
                                ${syndContent.value}
                        </div>
                    </c:forEach>
                </div>
                <div class="syndEntryAuthor">${fn:escapeXml(syndEntry.author)}</div>
            </c:forEach>
        </div>
    </div>
</c:if>