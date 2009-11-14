<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

 <jcr:nodeProperty node="${currentNode}" name="jcr:title" var="title"/>
 <jcr:nodeProperty node="${currentNode}" name="abstract" var="abstract"/>
 <jcr:nodeProperty node="${currentNode}" name="image" var="image"/>
 <%--<jcr:nodeProperty node="${currentNode}" name="link" var="link"/>--%>

        <div class="spacer"><!--start spacer -->
            <div class="box box-fixed-height">
                <a href="<template:module template="link" path="link"/>" alt="${title.string}"><img src="${image.node.url}" class="floatleft" alt="${title.string}"/></a>
            </div>
        <div class="clear"> </div>
        </div><!--stop spacer -->
