<%@ page import="org.jahia.bin.Jahia" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<template:addResources type="css" resources="960-fluid-admin-jahia.css, jahia-admin.css, contentlist.css, mainresource.css"/>
<div id="bodywrapper" >
<div class="container container_16">
    <div id="topTitle">
        <div class="grid_16">
                  <h1 class="hide">Jahia</h1>
                  <h2 class="edit">&nbsp;</h2>
        </div>
    </div>
</div>


<!--stop topheader-->




<div class="container container_16"><!--start container_16-->
	<div class='grid_16'><!--start grid_16-->
<div id="content"><!--start content-->
<div class="headtop">
    <div class="object-title"><fmt:message key="content.folder"/>
    </div>
</div>
    <c:choose>
        <c:when test="${not empty inWrapper and inWrapper eq false}">
            <div class="mainResourceArea">
                Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed dignissim tellus in metus viverra pharetra. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Mauris eu risus elit. Donec nibh diam, commodo in adipiscing et, euismod sed orci. Donec eu metus eget mauris fringilla pretium. Mauris vehicula, arcu malesuada malesuada varius, est leo porttitor lacus, id fermentum lacus eros ac sem. Proin non nunc magna, nec euismod diam. Ut faucibus dignissim erat sit amet sagittis. Aenean vestibulum, odio a imperdiet semper, diam lacus egestas velit, non lobortis libero massa et risus. Nunc quis sagittis est. Duis non orci vel quam posuere rutrum. Fusce et fringilla lorem. Nam tempus, dolor pretium consequat bibendum, odio leo feugiat odio, vitae pulvinar velit ipsum sit amet augue. Fusce ultrices ultricies tortor. Nunc vel pulvinar ipsum. Cras et nibh turpis, ac ornare leo. Cras elementum magna et risus porta accumsan. Duis dui leo, tincidunt at blandit non, euismod eu odio.
            </div>
        </c:when>
        <c:otherwise>
            <template:module node="${renderContext.mainResource.node}" template="system" />
        </c:otherwise>
    </c:choose>
    <div class="clear"></div></div>
        <div class='clear'></div></div><!--stop grid_16-->
    <div class='clear'></div></div><!--start container_16-->


<!--stop content-->
<div id="copyright">
    <%=Jahia.COPYRIGHT%>&nbsp;Jahia <%=Jahia.VERSION + "." + Jahia.getPatchNumber() + " r" + Jahia.getBuildNumber() %></div>

<div class="clear"></div></div><!--stop bodywrapper-->