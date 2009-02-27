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

<%@include file="/admin/include/header.inc" %>
<%@page language="java" %>
<%@page import="java.util.*" %>
<%@page import="org.jahia.services.sites.JahiaSite" %>
<%@page import="org.jahia.bin.*" %>
<%
String contenttypestr = (String) request.getAttribute("content-type");
response.setContentType(contenttypestr);
Properties includes = new Properties();
includes.setProperty("operations","/admin/search/include/operations.jsp");
includes.setProperty("index","/admin/search/include/index.jsp");
includes.setProperty("indexresult","/admin/search/include/indexresult.jsp");
includes.setProperty("optimize","/admin/search/include/optimize.jsp");
includes.setProperty("optimizeresult","/admin/search/include/optimizeresult.jsp");
includes.setProperty("error","/admin/search/include/error.jsp"); %>
<jsp:useBean id="url" class="java.lang.String" scope="request" /><% // http files path. %>
<jsp:useBean id="focus" class="java.lang.String" scope="request" /><% // autofocus input name in the form. %>
<jsp:useBean id="title" class="java.lang.String" scope="request" /><% // title %>
<jsp:useBean id="msg" class="java.lang.String" scope="request" /><% // bottom message [copyright or advertise]. %>
<!--jsp:useBean id="currentSite"  	class="org.jahia.services.sites.JahiaSite"  			scope="request"/--><% // the current site %>
<jsp:useBean id="subAction" class="java.lang.String" scope="request" /><% // the default screen %>
<jsp:useBean id="go" class="java.lang.String" scope="request" /><% // the default event %>
<%
Integer navigator = (Integer)request.getAttribute("navigator");
String  includeJsp = (String) request.getParameter("includejsp");
String  includePage = includes.getProperty(includeJsp);
if ( includePage == null ) {
includePage = includes.getProperty("error");
} %>
<script language="javascript" type="text/javascript">
  function focus() {
    <%if(!focus.equals("-none-")){%>
      document.formular.<%=focus%>.focus();
    <%}%>
  }
  
  function submitFormular(sub,go)
  {
      document.formular.action='<%=JahiaAdministration.composeActionURL(request,response,"search","&sub=")%>'+sub+'&go='+go;
      document.formular.method='POST';
      //safeSubmit(document.formular);
      document.formular.submit();
  }       
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.manageSearchEngine.label" /> <% if ( currentSite!= null ){ %>
    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.label" />&nbsp;<%=currentSite.getServerName() %>
    <%} %>
  </h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/admin/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
            <jsp:include page="/admin/include/left_menu.jsp">
                <jsp:param name="mode" value="site"/>
            </jsp:include>
            
            <div id="content" class="fit">
            <div class="head headtop">
                <div class="object-title">
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.manageSearchEngine.label"/>
                </div>
            </div>
            <div  class="content-item-noborder">
                <form name="formular" method="post" action='<%=JahiaAdministration.composeActionURL(request,response,"search","&sub=" + subAction + "&go=" + go)%>'>
                <!-- include page start -->
				<jsp:include page="<%=includePage%>" flush="true" />
				<!-- include page ends -->
              </form>
            </div>
			</div>
          </div>
        </div>
        </td>
      </tr>
    </tbody>
  </table>
</div>
<div id="actionBar">
<% if (includeJsp.equals("operations")) { %>
  <span class="dex-PushButton">
    <span class="first-child">        
      <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label" /></a>
    </span>
  </span>
  <span class="dex-PushButton">
    <span class="first-child">        
      <a class="ico-next" href="javascript:document.formular.submit();"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.nextStep.button.label"/></a>
    </span>
  </span>  
  <% } %>
  <% if (includeJsp.equals("index")) { %>
  <span class="dex-PushButton">
    <span class="first-child">        
      <a class="ico-back" href="javascript:submitFormular('<%=subAction%>','back');"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.previousStep.button.label"/></a>
    </span>
  </span>
  <span class="dex-PushButton">
    <span class="first-child">        
      <a class="ico-ok" href="javascript:submitFormular('<%=subAction%>','ok');"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.ok.label"/></a>
    </span>
  </span>  
  <% } %>
  <% if (includeJsp.equals("indexresult")) { %>
  
  <span class="dex-PushButton">
    <span class="first-child">        
       <a class="ico-ok" href="javascript:document.formular.submit();"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.ok.label"/></a>
    </span>
  </span>  
  <% } %>
  <% if (includeJsp.equals("error")) { %>
  
  <span class="dex-PushButton">
    <span class="first-child">        
        <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label" /></a>
    </span>
  </span>  
  <% } %>
</div>

<%@include file="/admin/include/footer.inc" %>
