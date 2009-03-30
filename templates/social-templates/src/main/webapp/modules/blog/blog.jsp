<%@ include file="../../common/declarations.jspf" %>

<template:containerList name="blogEntries" id="blogEntriesPagination" windowSize="5" actionMenuNameLabelKey="article" actionMenuNamePostFix="manage">
    <template:container cacheKey="pagination" id="blogEntry" displayExtensions="false">
        <template:field name="date" display="false" var="date"/>
        <template:getContentObjectCategories valueID="blogEntryCatKeys"
                                         objectKey="contentContainer_${pageScope.blogEntry.ID}"/>
        <div class="post"><!--start post-->
            <div class="post-date"><span><fmt:formatDate pattern="MM" value="${date.date}"/></span><fmt:formatDate pattern="dd" value="${date.date}"/></div>
            <div class="post-info"><template:metadata contentBean="${blogEntry}" metadataName="created" asDate="true"/></div>
            <h2 class="post-title"><a href="#"><template:field name="title"/></a></h2>

            <p class="post-info">Par <a href="#"><template:metadata metadataName="author" contentBean="${blogEntry}"/>.</a> <fmt:formatDate pattern="dd MMMM aaaa h:m" value="${date.date}"/>-
            <c:if test="${!empty blogEntryCatKeys }">
                <c:forEach var="blogEntryCatKey" items="${fn:split(blogEntryCatKeys, '$$$')}">
                    &nbsp;<a href="#"><ui:displayCategoryTitle categoryKeys="${blogEntryCatKey}"/></a>
                </c:forEach>
             </c:if>
            </p>
            <template:metadata metadataName="keywords" contentBean="${blogEntry}" var="keywords"/>
            <ul class="post-tags">
                <c:forEach var="keyword" items="${fn:split(keywords, ',')}">
                    <li><a href="#"><c:out value="${keyword}"/></a></li>
                </c:forEach>
            </ul>
            <div class="post-content"><p><template:field name="content"/></p>
            </div>
            <p class="read-more"><a title="#" href="#">Lire la suite ...</a></p>

            <p class="post-info-links">
                <a class="comment_count" href="#">aucun commentaire</a>
                <a class="ping_count" href="#">aucun rétrolien</a>
            </p>
        </div>
    </template:container>
</template:containerList>