<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="javascript" resources="jquery.min.js,jquery.cuteTime.js"/>
<template:addWrapper name="hidden.blogWrapper"/>
<script language="javascript1.5">
    $(document).ready(function () {
        $('.timestamp').cuteTime({ refresh: 60000 });
    });
</script>
<script type="text/javascript">
   function noAccent(chaine) {
      temp = chaine.replace(/[àâä]/gi,"a");
      temp = temp.replace(/[éèêë]/gi,"e");
      temp = temp.replace(/[îï]/gi,"i");
      temp = temp.replace(/[ôö]/gi,"o");
      temp = temp.replace(/[ùûü]/gi,"u");
      return temp;
   }
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
                        document.blogForm.action = '${currentNode.name}/'+noAccent(document.blogForm.elements['jcr:title'].value.replace(' ',''));
                        document.blogForm.submit();
                    "
                />
    </p>
    </form>


        <h4>New Blogs</h4>
        <ul class="recent-blogs">
            <template:area forceCreation="true" areaType="jnt:topBlog"  path="topBlog"/>
        </ul>



<div class="clear"></div>

    <div class="mapshortcuts"><!--start bottomshortcuts-->
        <h5>New entries</h5>
        <ul class="footer-recent-posts">
            <template:area forceCreation="true" areaType="jnt:topBlogContent"  path="topBlogContent"/>
        </ul>
    </div>


    <div class="mapshortcuts"><!--start bottomshortcuts-->
        <h5>New comments</h5>
        <ul class="footer-recent-comments">
             <template:area forceCreation="true" areaType="jnt:topBlogComment"  path="topBlogComment"/>
        </ul>
    </div>



