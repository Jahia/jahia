<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<template:addResources type="javascript" resources="jquery.min.js,jquery.cuteTime.js"/>
<template:addWrapper name="hidden.blogWrapper"/>
<script language="javascript1.5">
    $(document).ready(function () {
        $('.timestamp').cuteTime({ refresh: 60000 });
    });
</script>

                
    <form name="blogForm" method="post" action="${currentNode.name}/"/>
    <h4>Create New Blog</h4>
    <p>
        <fmt:message key="jnt_blog.createNewBlog"/> :
        <input type="text" name="jcr:title" value=""/>

    <input type="hidden" name="j:template" value="blog"/>
    <input type="hidden" name="autoCheckin" value="true">
    <input type="hidden" name="nodeType" value="jnt:page">

        <input
                class="button"
                type="button"
                tabindex="16"	
                value="<fmt:message key='save'/>"
                onclick="
                        if (document.blogForm.elements['jcr:title'].value == '') {
                            alert('you must fill the title ');
                            return false;
                        }
                        document.blogForm.action = '${currentNode.name}/'+document.blogForm.elements['jcr:title'].value.replace(' ','');
                        document.blogForm.submit();
                    "
                />
    </p>
    </form>


        <h4>New Blogs</h4>	
        <ul class="recent-blogs">
            <c:forEach items="${currentNode.nodes}" var="blog">
                <c:if test="${jcr:isNodeType(blog, 'jnt:page')}">
                <li><h5><a href="${url.base}${blog.path}.html"><jcr:nodeProperty node="${blog}" name="jcr:title" var="title"/><c:if test="${!empty title}">${title.string}</c:if><c:if test="${empty title}">no title</c:if></a></h5>
                </li>
                </c:if>
            </c:forEach>
        </ul>



<div class="clear"></div>

    <jcr:sql var="lastPosts"
             sql="select * from [jnt:blogContent] as blogContent  where isdescendantnode(blogContent, ['${currentNode.path}']) order by blogContent.[jcr:lastModified] desc"/>
    <div class="mapshortcuts"><!--start bottomshortcuts-->
        <h5>New entries</h5>
        <ul class="footer-recent-posts">
            <c:forEach items="${lastPosts.nodes}" var="post">
                <li><a href="${url.base}${post.path}.html"><jcr:nodeProperty node="${post}" name="jcr:title"/></a>
                    <div class="small"><jcr:nodeProperty node="${post}" name="jcr:lastModified" var="lastModified"/><span class="timestamp"><fmt:formatDate
            value="${lastModified.time}" pattern="yyyy/MM/dd HH:mm"/></span></div>
                </li>
            </c:forEach>
        </ul>
    </div>


    <div class="mapshortcuts"><!--start bottomshortcuts-->
        <jcr:sql var="lastComments"
                 sql="select * from [jnt:post] as comments  where isdescendantnode(comments, ['${currentNode.path}']) order by comments.[jcr:lastModified] desc"/>
        <h5>New comments</h5>
        <ul class="footer-recent-comments">
            <c:forEach items="${lastComments.nodes}" var="comment">
                <li><a href="${url.base}${comment.parent.parent.path}.html"><jcr:nodeProperty node="${comment}" name="jcr:title"/></a>
                    <div class="small"><jcr:nodeProperty node="${comment}" name="jcr:lastModified" var="lastModified"/><span class="timestamp"><fmt:formatDate
            value="${lastModified.time}" pattern="yyyy/MM/dd HH:mm"/></span></div>
                </li>
            </c:forEach>
        </ul>
    </div>



