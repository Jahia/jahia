<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<template:addResources type="css" resources="news.css"/>
    <!-- Role : ${currentNode.properties.role.string} -->
        <jcr:nodeProperty node="${currentNode}" name="image" var="newsImage"/>
        <jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>

        <div  class="thin_border"></div>
<a href="${url.base}${currentNode.path}.detail.html">
<div class="pteaser2_dynH">
<!--img width="52" height="52" alt="" src="${newsImage.node.url}"/-->        <p>
 ${functions:removeHtmlTags(title.string)}
<img alt="" src="http://mobile.bluewin.ch/images/c3/arrow_right_grey.gif" class="teaserArrow" width="9" height="10" />
</p>
<div class="clear"></div>

</div>
</a>
