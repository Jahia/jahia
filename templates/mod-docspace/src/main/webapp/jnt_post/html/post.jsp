<%@ page contentType="text/html; UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jcr:nodeProperty node="${currentNode}" name="jcr:createdBy" var="createdBy"/>
<jcr:nodeProperty node="${currentNode}" name="content" var="content"/>
<li class="docspaceitemcomment">
<span class="public floatright"><input name="" type="checkbox" value="" /> public</span>
<div class="image">
		<div class="itemImage itemImageLeft"><a href="#"><img alt="" src="${url.currentModule}/css/img/userbig2.png"/></a></div>
</div>

                <h5  class="title"><jcr:nodeProperty node="${currentNode}" name="jcr:title"/></h5><span class="docspacedate"> <jcr:nodeProperty node="${currentNode}" name="jcr:lastModified" var="lastModified"/><span class="timestamp"><fmt:formatDate
value="${lastModified.time}" pattern="yyyy/MM/dd HH:mm"/></span></span>
                <p><span class="author"><a
            href="${url.base}/users/${createdBy.string}">${createdBy.string}</a></span>
                ${content.string}
                </p>
                <div class='clear'></div>
</li>