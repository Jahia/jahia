<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="note.css"/>
<%-- Get all contents --%>

<%

session.setAttribute("url", request.getParameter("url"));
session.setAttribute("title", request.getParameter("title"));
session.setAttribute("note", request.getParameter("note"));
%>

<div id="bodywrapperpopup">
<div id="content">
<div class="header">
          <div class="leftside logo-top"><a href="#"><img class="logo" src="${url.currentModule}/images/logo.png" alt="logo" /></a></div>
          <div class="rightside infousers"><p>Bonjour, <a href="#">${renderContext.user.name} </a><br /></p>
          </div>
<div class="clear"></div></div>

<div class="Form"><!--start Form -->
            <form action="${url.base}/users/${renderContext.user.name}/thinkbox/*" method="post">
						    <input type="hidden" name="nodeType" value="jnt:thinkBoxItem"/>
               <div style="float: left;">
                <p><label for="url" ><fmt:message key='note.field.url'/>: </label>
                <input type="text" name="url" id="url" class="field" value="${sessionScope.url}" tabindex="4" />
                </p>
                <p><label for="title" ><fmt:message key='note.field.title'/>: </label>
                <input type="text" name="jcr:title" id="title" class="field" value="${sessionScope.title}" tabindex="5" />
                </p>
                     
                    
		              <div class="divButton">
		              <input type="submit" id="submit2" class="button" value="<fmt:message key='note.btn.send'/>" tabindex="14" /><input type="reset" id="reset" class="button" value="<fmt:message key='note.btn.reset'/>" tabindex="15"/>
		              </div>
               
               </div>
               
               
               <div style="margin-left: 10px; float: left;">
                <p>
                <label for="description" ><fmt:message key='note.field.description'/>:</label>
                <textarea name="description" id="description" class="field" cols="45" rows="5" tabindex="6"></textarea>
                </p>
                <p>
                <label for="description" ><fmt:message key='note.field.note'/>:</label>
                <textarea name="note" id="note" class="field" cols="45" rows="5" tabindex="6">${sessionScope.note}</textarea>
                </p>
                </div>
               
            </form>
          </div>
</div>
</div>






