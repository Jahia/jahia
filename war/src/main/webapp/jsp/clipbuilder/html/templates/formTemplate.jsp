<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
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
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@include file="/jsp/jahia/administration/include/header.inc"%>
<%@page import = "java.util.*"%>
<%@taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@taglib uri="http://struts.apache.org/tags-nested" prefix="nested"%>
<%@taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>

<%@taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%
stretcherToOpen   = 1;
%>
<style type="text/css">
  .boxtitleborder1 {
  	PADDING-LEFT: 5px; 
  	PADDING-RIGHT: 5px; 
  	PADDING-BOTTOM: 2px; 
  	PADDING-TOP: 2px; 
  	border: #006699 1px solid; 
  	color: #666666; 
  }
.topmenubuttons {
    padding : 5px;
    border-style : dashed;
    border-width : 1px;
    border-color : black;
}
.topmenugreen {
    font-weight: bold;
  	padding-right: 15px; 
  	padding-left: 15px;
  	padding-top : 5px;
  	padding-bottom : 5px;
  	background-color: #e7e7e7;
  	color : #000000;
}

.clipInputTable td {
    vertical-align : top;
}

.clipInputTable th {
  	text-align: left;
    vertical-align : top;
}

.bold {
  	font-weight: bold; 
  	color: #006699;
}
.waBG {
  	background-color: #e7e7e7;
}
.waInput {
  	color: #000000; 
}
.leftlevel2 {
	  padding-left: 20px; 
}

.wizardStep {
  width : 70px;
  border-width: 1px; 
  border-style : solid; 
  border-color : #cccccc;
}

input.fancyButton {
    text-decoration: none;
    background-image: url( <%=contextPath%>/jsp/jahia/css/bg_button_up.gif );
    border: 1px solid;
    border-color: #D0D0D0 #555555 #555555 #D0D0D0;
    font-size: 11px;
    cursor: default;
    color: black;
    font-weight : bold;
    padding: 3px 3px 3px 3px;
    height: 24px;
    margin-left: 6px;
  	font-family: Verdana,Arial,Helvetica,sans-serif;
}

input.wizard {
    border: 0px solid #c0c0c0;
    font-size: 10px;
    height: 39px;
}



</style>

  <script type="text/javascript">
  <!--
  function popitup(url) {
  	newwindow=window.open(url,'name','toolbar=0,resizable=yes,scrollable=yes');
  	if (window.focus) {newwindow.focus()}
  }
  
  // -->
  </script>

<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit"><bean:message key="htmlclipbuilder.title"/></h2>
</div>

<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
    cellspacing="0">
    <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/jsp/jahia/administration/include/tab_menu.inc"%>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
            <div class="dex-TabPanelBottom">
            <div class="tabContent">
                <%@include file="/jsp/jahia/administration/include/menu_site.inc"%>
            </div>
            <div id="content" class="fit">
  <table width="100%" cellpadding="0" cellspacing="0">
    <!-- Main table-->
    <!-- Wizard header-->
    <logic:present name="descriptionClipperForm">
      <tr>
        <td>
        <jsp:include flush="true" page="/jsp/clipbuilder/html/wizard.jsp"/>
        </td>
      </tr>
    </logic:present>
    <logic:notPresent name="descriptionClipperForm">
      <logic:present name="testClipperForm">
        <tr>
          <td>
          <jsp:include flush="true" page="/jsp/clipbuilder/html/testHeader.jsp"/>
          </td>
        </tr>
      </logic:present>
    </logic:notPresent>
    <!-- Action -->
    <tr>
      <td>
        <!-- put here the inculded jsp depending on the action -->
        <tiles:get name="jahia.webClipping.action"/>
      </td>
    </tr>
  </table>
  </div>
            
            </td>
        </tr>
    </tbody>
</table>
</div>
  <div id="actionBar">
    <span class="dex-PushButton"> 
      <span class="first-child">
        <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><internal:adminResourceBundle resourceName="org.jahia.admin.backToMenu.label"/></a>
      </span>
     </span>                     
  </div>
</div>
    
<%@include file="/jsp/jahia/administration/include/footer.inc"%>
