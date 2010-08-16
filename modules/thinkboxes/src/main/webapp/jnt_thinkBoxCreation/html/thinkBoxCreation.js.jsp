<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.

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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<template:addResources type="css" resources="thinkbox.css"/>
<%-- Get all contents --%>


<%
// nasty code but I really don't know how to work with the POST method of the servlet...
// scaaaarryyy !
// TODO: Modify this code. We should be able to send easily other parameters.
%>
<% if (request.getParameter("submitedAjax") == null && request.getParameter("submited") == null) { %>
<script>window.location="${url.base}${currentNode.path}.js.html.ajax?submited=true"</script>
<% } else if (request.getParameter("submitedAjax") != null) {

session.setAttribute("url", request.getParameter("url"));
session.setAttribute("title", request.getParameter("title"));
session.setAttribute("noteTXT", request.getParameter("noteTXT"));
session.setAttribute("noteHTML", request.getParameter("noteHTML"));
%>

<div id="bodywrapperpopup">
<div id="content">
<div class="header">

          <div class="leftside logo-top"><img class="logo" src="${url.currentModule}/img/jahia-app-thinkbox-h50px.png" alt="thinkbox logo" /></div>
          <div class="rightside infousers"><p>Hello, <a href="#">${renderContext.user.name} </a><br /></p>
          </div>
<div class="clear"></div></div>

<div class="Form"><!--start Form -->
            <form name="jahiatbForm" id="jahiatbForm" action="${url.base}/users/${renderContext.user.name}/thinkbox/*" method="post" onSubmit="deleteNote();">

						    <input type="hidden" name="nodeType" value="jnt:thinkBoxItem"/>
						    <input type="hidden" name="redirectTo" value="${url.base}${currentNode.path}.js">

               <div style="float: left;">
                <p><label for="url" ><!--fmt:message key='note.field.url'/-->URL: </label>
                <input type="text" name="url" id="url" class="field" value="${sessionScope.url}" tabindex="4" />
                </p>
                <p><label for="title" ><!--fmt:message key='note.field.title'/-->Title: </label>
                <input type="text" name="title" id="title" class="field" value="${sessionScope.title}" tabindex="5" />
                </p>
                <p><label for="tag" ><!--fmt:message key='note.field.tag'/-->Tags: </label>
                <input type="text" name="j:newTag" id="tag" class="field" value="" tabindex="6" />
                </p>
               <p class="alert"><!--fmt:message key='note.field.tags.help'/-->Tags must be comma separated. ex: tag1,tag2,tag3</p>

               </div>



               <div style="margin-left: 10px; float: right;">
                <p>
                <label for="description" ><!--fmt:message key='note.field.description'/-->Description:</label>
                <textarea name="description" id="description" class="field" cols="45" rows="5" tabindex="6"></textarea>
                </p>
                <a href="#" onclick="switchNote();return false;">Switch Note Format</a>
                <p id="pnotefalse">
                <label for="description" ><!--fmt:message key='note.field.note'/-->Note Txt:</label>
                <textarea name="note" id="notefalse" class="field" cols="45" rows="5" tabindex="6">${sessionScope.noteTXT}</textarea>
                </p>
								<p id="pnotetrue">
                <label for="description" ><!--fmt:message key='note.field.note'/-->Note HTML:</label>
                <textarea name="note" id="notetrue" class="field" cols="45" rows="5" tabindex="6">${sessionScope.noteHTML}</textarea>
                </p>
                <div class="divButton">
		              <input type="submit" id="submit2" class="button" value="Send" tabindex="14" />
		              <input type="button" id="clear" class="button" value="Clear" tabindex="15" onclick="clearForm(document.getElementById('jahiatbForm'));"/>
		              <input type="reset" id="reset" class="button" value="Reset" tabindex="16"/>
		              </div>
                </div>

            </form>
          </div>
</div>
</div>

<script>
	var formatHTML = true;

	function switchNote() {
		document.getElementById("pnote"+formatHTML).style.display = "none";
		document.getElementById("pnote"+(!formatHTML)).style.display = "block";
		formatHTML = !formatHTML;

	}

	function clearForm(f) {
		for (i=0; i<f.length; i++) {
			e = f.elements[i];
			if (e.type != 'reset' && e.type != 'button' && e.type != 'submit' && e.type != 'hidden') {
				e.value = "";
			}
		}
	}

	function deleteNote() {
		e = document.getElementById("note"+(!formatHTML));
		e.parentNode.removeChild(e);
	}

	switchNote();
</script>

<%} else if (request.getParameter("submited") != null) {%>

<div id="bodywrapperpopup">
<div id="content">
<div class="header">

          <div class="leftside logo-top"><img class="logo" src="${url.currentModule}/img/jahia-app-thinkbox-h50px.png" alt="thinkbox logo" /></div>
          <div class="rightside infousers"><p>Hello, <a href="#">${renderContext.user.name} </a><br /></p>
          </div>
<div class="clear"></div></div>

<div class="Form">
Your note has been created.
 </div>
</div>
</div>

<%}%>