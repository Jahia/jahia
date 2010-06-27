<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<template:addResources type="css" resources="blog.css"/>
<%--Filters Settings--%>
<div class="grid_11">
<div class="boxblog">
        <div class="boxblogshadow boxblogpadding16 boxblogmarginbottom16">
            <div class="boxblog-inner">
                <div class="boxblog-inner-border"><!--start boxblog -->
${wrappedContent}
    <div class="clear"></div>
                </div>
            </div>
        </div>
    </div>
    <!--stop boxblog -->
    </div>

<!--stop grid_12-->
<div class='grid_5'><!--start grid_5-->
    <img src="${url.currentModule}/images/jahia-apps-blog.png" alt="jahia-apps-blog"/>

    <div class="boxblog">
        <div class="boxbloggrey boxblogpadding16 boxblogmarginbottom16">
            <div class="boxblog-inner">
                <div class="boxblog-inner-border"><!--start boxblog -->

                    <c:if test="${jcr:isNodeType(currentNode, 'jnt:blogContent')}">
                        <c:set var="blogHome" value="${url.base}${currentResource.node.parent.path}.html"/>
                    </c:if>
                    <c:if test="${!jcr:isNodeType(currentNode, 'jnt:blogContent')}">
                        <c:set var="blogHome" value="${url.current}"/>
                    </c:if>

                    <div id="search">
                        <h3><label for="search"><fmt:message key="search"/> </label></h3>

                        <form method="get" action="${blogHome}">
                            <fieldset>
                                <p>
                                    <input type="text" value="${param.textSearch}" name="textSearch" class="search" tabindex="4"/>
                                    <input type="submit" value="GO" class="gobutton" tabindex="5"/>
                                </p>
                            </fieldset>
                        </form>
                    </div>
                    <jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
                    <template:module path="/users/${createdBy.string}" forcedTemplate="blog" editable="false"/>
                    <!--stop aboutMeListItem -->
                    <h3><fmt:message key="archives"/></h3>
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
                    <div class="tags">
                        <h3><fmt:message key="tags"/></h3>
                        <jcr:sql var="tags" sql="select * from [jnt:tag] where ischildnode(['${renderContext.site.path}/tags']) order by [j:nodename]"/>
                        <c:set var="totalUsages" value="0"/>
                        <jsp:useBean id="filteredTags" class="java.util.LinkedHashMap"/>
                        <c:forEach items="${tags.nodes}" var="tag">
                            <c:set var="count" value="${functions:length(tag.weakReferences)}"/>
                            <c:if test="${usageThreshold <= 0 || count >= usageThreshold}">
                                <c:set target="${filteredTags}" property="${tag.name}" value="${tag}"/>
                                <c:set var="totalUsages" value="${totalUsages + count}"/>
                            </c:if>
                        </c:forEach>

                        <c:if test="${not empty filteredTags}">
                            <ul>
                                <c:forEach items="${filteredTags}" var="tag">
                                    <c:set var="tagCount" value="${functions:length(tag.value.weakReferences)}"/>
                                    <li><a
                                            class="tag${functions:round(10 * tagCount / totalUsages)}0"
                                            title="${tag.value.name} (${tagCount} / ${totalUsages})"
                                            href="${blogHome}?addTag=${tag.value.name}"
                                            >${tag.value.name}</a></li>
                                </c:forEach>
                            </ul>
                        </c:if>
                        <c:if test="${empty filteredTags}">
                            <fmt:message key="tags.noTags"/>
                        </c:if>
                    </div>

                    <c:if test="${!empty tagFilter}">
                        <div class="filterList">
                            <h3><fmt:message key="currentFilters"/> </h3>

                            <c:set var="tagsMap" value="${fn:split(tagFilter, '$$$')}"/>

                            <ul>
                                <c:forEach var="tag" items="${tagsMap}">
                                    <li><a href="${url.current}?removeTag=${tag}" title="delete">${tag}
                                        <img src="${url.currentModule}/images/delete.png" alt="delete" />
                                    </a></li>
                                </c:forEach>
                            </ul>

                            <div class="clear"></div>
                            <p class="filterListDeleteAll"><a title="#" href="${blogHome}?removeAllTags=true"><fmt:message key="currentFilters.deleteAll"/></a></p>
                            <div class="clear"></div>
                        </div>
                    </c:if>

                    <div class="clear"></div>
                </div>

            </div>
        </div>
    </div>
    <!--stop boxblog -->

    <div class='clear'></div>
</div>
<!--stop grid_5-->