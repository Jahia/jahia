<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<template:addResources type="css" resources="blog.css"/>
<div class='grid_10'><!--start grid_10-->

    <div class="box"><!--start box -->
        <div class="boxshadow boxpadding40 boxmarginbottom16">
            <div class="box-inner">
                <div class="box-inner-border">

                    ${wrappedContent}
                    <div class="bottomanchor"><!--start anchor--><a href="base.blogWrapper.jsp#bodywrapper">Page Top </a>

                        <div class="clear"></div>
                    </div>
                    <!--stop anchor-->

                    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop box -->


    <div class='clear'></div>
</div>
<!--stop grid_10-->
<div class='grid_6'><!--start grid_6-->
    <img src="${url.currentModule}/images/jahia-apps-blog.png" alt="jahia-apps-blog"/>

<div class="box">

<div class="boxgrey boxpadding16 boxmarginbottom16">
<div class="box-inner">
<div class="box-inner-border"><!--start box -->
<div class="addArticle"><!--start preferences-->
    <h3><a class="addArticle"
    <c:if test="${jcr:isNodeType(currentNode, 'jnt:blogContent')}">
        href="${url.base}${currentResource.node.parent.path}.blogEdit.html"
    </c:if>
    <c:if test="${!jcr:isNodeType(currentNode, 'jnt:blogContent')}">
        href="${url.base}${currentResource.node.path}.blogEdit.html"
    </c:if>
    > Add an article</a></h3>
</div>

<div id="search">
    <h3><label for="search">Rechercher</label></h3>

    <form method="get" action="base.blogWrapper.jsp#">
        <fieldset>
            <p>
                <input type="text" value="" name="search" class="search" tabindex="4"/>
                <input type="submit" value="GO" class="gobutton" tabindex="5"/>
            </p>
        </fieldset>
    </form>
</div>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
    <template:module path="/users/${createdBy.string}" forcedTemplate="blog"/>
<!--stop aboutMeListItem -->
    <h3>Archives</h3>
<div class="archives">
    <c:if test="${jcr:isNodeType(currentNode, 'jnt:blogContent')}">
        <jcr:sql var="blogList"
                 sql="select * from [jnt:blogContent] as blogContent where isdescendantnode(blogContent,['${currentNode.parent.path}']) order by blogContent.[j:lastModifiedDate] desc"/>
    </c:if>
    <c:if test="${!jcr:isNodeType(currentNode, 'jnt:blogContent')}">
        <jcr:sql var="blogList"
                 sql="select * from [jnt:blogContent] as blogContent where isdescendantnode(blogContent,['${currentNode.path}']) order by blogContent.[j:lastModifiedDate] desc"/>
    </c:if>


<c:set var="oldMonth" value=""/>
<c:set var="oldYear" value=""/>
<c:set var="count" value="0"/>

<c:forEach items="${blogList.nodes}" var="entry" varStatus="status">
    <c:set var="count" value="${count + 1}"/>
   <fmt:formatDate value="${entry.properties['jcr:created'].date.time}" pattern="yyyy" var="currentYear"/>
   <c:if test="${oldYear != currentYear}">
        <c:if test="${oldYear != ''}">
            </ul>
        </c:if>
       <h4>${currentYear}</h4>
       <ul>
   </c:if>
    <c:set var="oldYear" value="${currentYear}"/>
    <fmt:formatDate value="${entry.properties['jcr:created'].date.time}" pattern="MMMM" var="currentMonth"/>
    <c:if test="${currentMonth != oldMonth && oldMonth != ''}">
            <li>${oldMonth} (${count})</li>
            <c:set var="count" value="0"/>
    </c:if>
    <c:set var="oldMonth" value="${currentMonth}"/>
</c:forEach>
       <li>${oldMonth} (${count})</li>
       </ul>
</div>

<%--todo: call jnt:tagCloud from mod default instead of duplicate the code--%>
<c:set var="usageThreshold" value="${not empty currentNode.properties['j:usageThreshold'] ? currentNode.properties['j:usageThreshold'].string : 1}"/>
<jcr:node var="tagsRoot" path="${renderContext.siteNode.path}/tags"/>
<div class="tags">
<h3>Tags</h3>
<jcr:sql var="tags" sql="select * from [jnt:tag] as sel where ischildnode(sel,['${tagsRoot.path}']) order by sel.[j:nodename]"/>
<c:set var="totalUsages" value="0"/>
<jsp:useBean id="filteredTags" class="java.util.LinkedHashMap"/>
<c:forEach items="${tags.nodes}" var="tag">
	<c:set var="count" value="${tag.references.size}"/>
	<c:if test="${usageThreshold <= 0 || count >= usageThreshold}">
		<c:set target="${filteredTags}" property="${tag.name}" value="${tag}"/>
		<c:set var="totalUsages" value="${totalUsages + count}"/>
	</c:if>
</c:forEach>

<c:if test="${not empty filteredTags}">
		<ul>
			<c:forEach items="${filteredTags}" var="tag">
				<c:set var="tagCount" value="${tag.value.references.size}"/>
				<li><a class="tag${functions:round(10 * tagCount / totalUsages)}0" title="${tag.value.name} (${tagCount} / ${totalUsages})">${tag.value.name}</a></li>
			</c:forEach>
		</ul>
</c:if>
<c:if test="${empty filteredTags}">
	<fmt:message key="tags.noTags"/>
</c:if>
</div>

<div class="filterList">
    <h3>Filtres de liste</h3>
    <ul>
        <li><a href="base.blogWrapper.jsp#" title="delete">Filtre 1<img src="${url.currentModule}/images/delete.png" alt="delete"/></a></li>
        <li><a href="base.blogWrapper.jsp#" title="delete">Filtre 2<img src="${url.currentModule}/images/delete.png" alt="delete"/></a></li>
        <li><a href="base.blogWrapper.jsp#" title="delete">Filtre 3<img src="${url.currentModule}/images/delete.png" alt="delete"/></a></li>
        <li><a href="base.blogWrapper.jsp#" title="delete">Filtre 4<img src="${url.currentModule}/images/delete.png" alt="delete"/></a></li>

        <li><a href="base.blogWrapper.jsp#" title="delete">Filtre 5<img src="${url.currentModule}/images/delete.png" alt="delete"/></a></li>
        <li><a href="base.blogWrapper.jsp#" title="delete">Filtre 6<img src="${url.currentModule}/images/delete.png" alt="delete"/></a></li>
        <li><a href="base.blogWrapper.jsp#" title="delete">Filtre 7<img src="${url.currentModule}/images/delete.png" alt="delete"/></a></li>
    </ul>
    <div class="clear"></div>
    <p class="filterListDeleteAll"><a title="#" href="base.blogWrapper.jsp#">Tout supprimer</a></p>

    <div class="clear"></div>
</div>

<div class="clear"></div>
</div>

</div>
</div>
</div>
<!--stop box -->

<div class='clear'></div>
</div>
<!--stop grid_6-->