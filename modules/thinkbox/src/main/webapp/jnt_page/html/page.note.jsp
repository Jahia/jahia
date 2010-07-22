<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>

<template:template>
    <template:templateHead title="${fn:escapeXml(currentNode.properties['jcr:title'].string)}">
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <template:addResources type="css" resources="thinkbox.css" />
        <utility:applicationResources/>
        <template:includeResources/>
	</template:templateHead>
    <template:templateBody>

<%-- Get all contents --%>

<%

session.setAttribute("url", request.getParameter("url"));
session.setAttribute("title", request.getParameter("title"));
session.setAttribute("note", request.getParameter("note"));
session.setAttribute("share", request.getParameter("share"));
session.setAttribute("type", request.getParameter("type"));
%>

<div id="bodywrapperpopup">
<div id="content">
<div class="header">
          <div class="leftside logo-top"><a href="#"><img class="logo" src="${url.currentModule}/images/logo.png" alt="logo" /></a></div>
          <div class="rightside infousers"><p>Bonjour, <a href="#">${renderContext.user.name} </a><br /></p>
          </div>
<div class="clear"></div></div>

<div class="Form"><!--start Form -->
            <form action="${url.base}/users/${renderContext.user.name}/ispace/*" method="post">
						    <input type="hidden" name="nodeType" value="jnt:thinkitem"/>
						    <input type="hidden" name="stayOnNode" value="${url.base}${renderContext.mainResource.node.path}"/>
						    <%-- Define the output format for the newly created node by default html or by stayOnNode--%>
						    <input type="hidden" name="newNodeOutputFormat" value="html"/>
               <div style="float: left;">
                <p><label for="url" ><fmt:message key='note.field.url'/>: </label>
                <input type="text" name="url" id="url" class="field" value="${sessionScope.url}" tabindex="4" />
                </p>
                <p><label for="title" ><fmt:message key='note.field.title'/>: </label>
                <input type="text" name="jcr:title" id="title" class="field" value="${sessionScope.title}" tabindex="5" />
                </p>
               	
               	<p><label for="tags" ><fmt:message key='note.field.tags'/>: </label>
                <input type="text" name="tags" id="tags" class="field" value="" tabindex="7" />
                </p>

                <p class="alert"><fmt:message key='note.field.tags.help'/></p>



                  <p><fmt:message key='note.field.store.legend'/>: </p>
                    <input type="radio" name="type" value="note" id="note" tabindex="8" <% if (session.getAttribute("type") != null && session.getAttribute("type").toString().equals("note")){%>checked="true"<%}%> />
                    <label for="note"><fmt:message key='note.field.store.type.note'/></label>
                  
                    <input type="radio" name="type" value="bookmark" id="bookmark" tabindex="9" <% if (session.getAttribute("type") != null && session.getAttribute("type").toString().equals("bookmark")){%>checked="true"<%}%> /> 
                    <label for="bookmark"><fmt:message key='note.field.store.type.bookmark'/></label><br />

                 
                    <input type="radio" name="type" value="fullpage" id="fullpage" tabindex="10" <% if (session.getAttribute("type") != null && session.getAttribute("type").toString().equals("fullpage")){%>checked="true"<%}%> />
                    <label for="fullpage"><fmt:message key='note.field.store.type.page'/></label>
	
                  
                    <input type="radio" name="type" value="event" id="event" tabindex="11" <% if (session.getAttribute("type") != null && session.getAttribute("type").toString().equals("event")){%>checked="true"<%}%> />
                    <label for="event"><fmt:message key='note.field.store.type.event'/></label>
                                        
                    
                  <p><fmt:message key='note.field.share.legend'/>: </p>
                    <input type="radio" name="share" value="true" id="shareyes" tabindex="12" <% if (session.getAttribute("share") != null && session.getAttribute("share").toString().equals("true")){%>checked="true"<%}%> />
                    <label for="shareyes"><fmt:message key='note.field.share.value.true'/></label>
                  
                    <input type="radio" name="share" value="false" id="shareno" tabindex="13" <% if (session.getAttribute("share") != null && session.getAttribute("share").toString().equals("false")){%>checked="true"<%}%> />
                    <label for="shareno" ><fmt:message key='note.field.share.value.false'/></label>
               
                
                    
                    
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
</template:templateBody>
</template:template>