<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div id="one"><!--start tab One-->
    <form name="blogForm" method="post" action="${currentNode.name}/"/>
    <p>
        <fmt:message key="jnt_blog.createNewBlog"/> :
    </p>
    <p>
        <input type="text" name="jcr:title" value=""/>
    </p>
        <input type="hidden" name="j:template" value="blog"/>
        <input type="hidden" name="autoCheckin" value="true">
        <input type="hidden" name="nodeType" value="jnt:page">
    <p class="c_button">
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
</div>
<!--stop grid_10-->
