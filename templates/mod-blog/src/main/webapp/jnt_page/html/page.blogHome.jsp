<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="javascript" resources="jquery.min.js,jquery.cuteTime.js"/>
<template:addWrapper name="hidden.blogWrapper"/>
<template:addResources type="inlinejavascript">
$(document).ready(function () {
    $('.timestamp').cuteTime({ refresh: 60000 });
});
</template:addResources>
                
    <form name="blogForm" method="post" action="${currentNode.name}/">
    <h4><fmt:message key="jnt_blog.createNewBlog"/></h4>
    <p>
        <fmt:message key="jnt_blog.createNewBlogNamed"/>:&nbsp;<input type="text" name="jcr:title" value=""/>

    <input type="hidden" name="j:template" value="blog"/>
    <input type="hidden" name="autoCheckin" value="true"/>
    <input type="hidden" name="nodeType" value="jnt:page"/>
    <input type="hidden" name="normalizeNodeName" value="true"/>

	<fmt:message key='jnt_blog.noTitle' var="noTitle"/>
        <input
                class="button"
                type="button"
                tabindex="16"	
                value="<fmt:message key='save'/>"
                onclick="
                        if (document.blogForm.elements['jcr:title'].value == '') {
                            alert('${noTitle}');
                            return false;
                        }
                        document.blogForm.action = '${currentNode.name}/' + encodeURIComponent(document.blogForm.elements['jcr:title'].value);
                        document.blogForm.submit();
                    "
                />
    </p>
    </form>


        <h4><fmt:message key="jnt_blog.newBlogs"/></h4>
        <ul class="recent-blogs">
            <template:area forceCreation="true" areaType="jnt:topBlog"  path="topBlog"/>
        </ul>



<div class="clear"></div>

    <div class="mapshortcuts"><!--start bottomshortcuts-->
        <h5><fmt:message key="jnt_blog.newEntries"/></h5>
        <ul class="footer-recent-posts">
            <template:area forceCreation="true" areaType="jnt:topBlogContent"  path="topBlogContent"/>
        </ul>
    </div>


    <div class="mapshortcuts"><!--start bottomshortcuts-->
        <h5><fmt:message key="jnt_blog.newComments"/></h5>
        <ul class="footer-recent-comments">
             <template:area forceCreation="true" areaType="jnt:topBlogComment"  path="topBlogComment"/>
        </ul>
    </div>