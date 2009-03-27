<%@ include file="../../common/declarations.jspf" %>

<template:containerList name="blogEntries" id="blogEntriesPagination" windowSize="5">
    <template:container cacheKey="pagination" id="blogEntry" displayExtensions="false">
        <template:field name="date" display="false" var="date"/>

        <div class="post"><!--start post-->
            <div class="post-date"><span><fmt:formatDate pattern="MM" value="${date.date}"/></span><fmt:formatDate pattern="dd" value="${date.date}"/></div>
            <h2 class="post-title"><a href="#"><template:field name="title"/></a></h2>

            <p class="post-info">Par <a href="#"><template:metadata metadataName="author" contentBean="${blogEntry}"/>.</a> <fmt:formatDate pattern="dd MMMM aaaa h:m" value="${date.date}"/>-
                <a href="#">Categorie</a>
            </p>
            <ul class="post-tags">
                <li><a href="#">Tag 1</a></li>
                <li><a href="#">Tag 2</a></li>
                <li><a href="#">Tag 3</a></li>
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