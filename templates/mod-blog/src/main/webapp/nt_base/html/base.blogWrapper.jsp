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


<div class="aboutMeListItem"><!--start aboutMeListItem -->
    <h3>A propos de moi</h3>

    <div class="aboutMePhoto">
        <img src="${url.currentModule}/images/user.png" alt="photo"/></div>
    <div class="aboutMeBody"><!--start aboutMeBody -->
        <h5>Prenom Nom</h5>

        <p class="aboutMeAge">Age : 35ans</p>

        <div class="clear"></div>

    </div>
    <!--stop aboutMeBody -->
    <p class="aboutMeResume">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce auctor dapibus nibh. Proin
        viverra arcu eget lorem.Maecenas ligula ligula, tristique in, venenatis posuere...</p>

    <div class="aboutMeAction">
        <a class="aboutMeMore" href="javascript:;" onclick="ShowHideLayer(1);" title="title">En savoir plus...</a>
    </div>
    <div id="box1" class="collapsible"><!--start collapsible -->
        <h3>Titre de niveau 2 (h3)</h3>

        <p> Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Ut ut ligula. Pellentesque rhoncus. Sed erat orci,
            tincidunt et, ultrices sed, posuere ut, lectus. In volutpat. Donec malesuada tellus in ante. Nullam erat
            leo, aliquet sit amet, convallis sit amet, euismod vel, urna. Aliquam pulvinar. Fusce lacus nibh, vulputate
            sed, condimentum ut, viverra ac, sem. Sed vitae diam. Nullam blandit. In adipiscing massa nec ligula. Nullam
            leo. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nullam erat
            leo, aliquet sit amet, convallis sit amet, euismod vel, urna. Aliquam pulvinar. Fusce lacus nibh, vulputate
            sed, condimentum ut, viverra ac, sem. Sed vitae diam. Nullam blandit. In adipiscing massa nec ligula. Nullam
            leo. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Nullam erat
            leo, aliquet sit amet, convallis sit amet, euismod vel, urna. Aliquam pulvinar. Fusce lacus nibh, vulputate
            sed, condimentum ut, viverra ac, sem. Sed vitae diam. Nullam blandit. In adipiscing massa nec ligula. Nullam
            leo. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos.</p>
    </div>
    <!--stop collapsible -->

    <div class="clear"></div>
</div>
<!--stop aboutMeListItem -->

<div class="archives">
    <h3>Archives</h3>
    <h4>2009</h4>
    <ul>
        <li><a title="" href="base.blogWrapper.jsp#">novembre</a>(1)</li>

    </ul>
    <h4>2007</h4>
    <ul>
        <li><a title="" href="base.blogWrapper.jsp#">avril</a>(11)</li>
        <li><a title="" href="base.blogWrapper.jsp#">mars</a>(5)</li>
        <li><a title="" href="base.blogWrapper.jsp#">février</a>(1)</li>

    </ul>
    <h4>2006</h4>
    <ul>
        <li><a title="" href="base.blogWrapper.jsp#">août</a>(1)</li>
        <li><a title="" href="base.blogWrapper.jsp#">mars</a>(1)</li>
    </ul>

    <h4>2005</h4>
    <ul>
        <li><a title="" href="base.blogWrapper.jsp#">octobre</a>(1)</li>
        <li><a title="" href="base.blogWrapper.jsp#">septembre</a>(1)</li>
        <li><a title="" href="base.blogWrapper.jsp#">août</a>(2)</li>

    </ul>
    <h4>2004</h4>
    <ul>
        <li><a title="" href="base.blogWrapper.jsp#">septembre</a>(1)</li>
        <li><a title="" href="base.blogWrapper.jsp#">août</a>(1)</li>
    </ul>

</div>

<%--todo: call jnt:tagCloud from mod default instead of duplicate the code--%>
<c:set var="usageThreshold" value="${not empty currentNode.properties['j:usageThreshold'] ? currentNode.properties['j:usageThreshold'].string : 1}"/>
<jcr:node var="tagsRoot" path="${renderContext.siteNode.path}/tags"/>
<div class="tags">
<h3><c:if test="${not empty currentNode.properties['jcr:title'] && not empty currentNode.properties['jcr:title'].string}" var="titleProvided">${fn:escapeXml(currentNode.properties['jcr:title'].string)}</c:if><c:if test="${not titleProvided}"><fmt:message key="tags"/></c:if></h3>
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