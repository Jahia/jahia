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

<%@include file="/admin/include/header.inc"%>
<%@page import   = "java.util.*"%>
<%@page import   = "org.jahia.data.*"%>
<%@page import   = "org.jahia.services.pages.*"%>
<%@page import="org.jahia.bin.*"%>

<%
    List  errors  = (List) request.getAttribute("manageTemplatesErrors");
    String errorString = "";

    if (errors != null && errors.size() > 0) {

        StringBuffer buf                 =  new StringBuffer();

        buf.append("<ul>");

        for (int i=0; i<errors.size(); i++)
        {
            buf.append("<li>").append(errors.get(i));
        }
        buf.append("</ul>");

        errorString =  buf.toString();

    }



    JahiaPageDefinition templ 	= (JahiaPageDefinition)request.getAttribute("templ");

    Iterator  allPagesInfosIterator  = (Iterator) request.getAttribute("allPagesInfosIterator");
    Iterator  allTemplatesIterator   = (Iterator) request.getAttribute("allTemplatesIterator");
    Integer      totalCriticalPages        = (Integer)     request.getAttribute("totalCriticalPages");
    Integer      basePageID                = (Integer)     request.getAttribute("basePageID");
    Integer      baseTemplateID            = (Integer)     request.getAttribute("baseTemplateID");
    Integer      homePageID                = (Integer)     request.getAttribute("homePageID");
    String       requestURI 			   = (String)      request.getAttribute("requestURI");
    JahiaSite    site                      = (JahiaSite)   request.getAttribute("site");
	Boolean		 canDeleteTemplate         = (Boolean)     request.getAttribute("canDeleteTemplate");
    int          selectSize                = (totalCriticalPages.intValue() < 15) ? totalCriticalPages.intValue() : 15;

%>

<script type="text/javascript">
<!--

function sendForm(subAction)
{
    document.mainForm.subaction.value=subAction;
    document.mainForm.action="<%=requestURI%>?do=templates&sub=swap&templid=<%=templ.getID()%>";
    document.mainForm.submit();
}

//-->
</script>

<div id="topTitle">
	<div id="topTitleLogo">
		<img src="<%=URL%>images/icons/admin/briefcase_document.gif" width="48" height="48" />
  </div>
  <h1 id="topTitleLabel"><fmt:message key="org.jahia.admin.manageTemplates.label"/><br><% if ( site!= null ){%><fmt:message key="org.jahia.admin.site.label"/>&nbsp;<%=site.getServerName()%><%}%></h1>
</div>

<div id="adminMainContent">

    <form name="mainForm" action="" method="post">
              <table cellpadding="0" cellspacing="0" border="0">
              <tr>
                  <td valign="top">
                      <b><%=templ.getName()%>
                         <fmt:message key="org.jahia.admin.templates.ManageTemplates.stillUsed.label"/></b>
                      <br><br>
                      	<% if ( canDeleteTemplate.booleanValue() ){ %>
                          	<fmt:message key="org.jahia.admin.templates.ManageTemplates.beforeDeleting.label"/><br>
                          <% } else { %>
                          	<fmt:message key="org.jahia.admin.templates.ManageTemplates.cannotDelete.label"/><br>
                          <% } %>
                      <br>
                      <select class="input" name="pageids" size="10" multiple >
                      <%
                          while(allPagesInfosIterator.hasNext()) {
                              ContentPage contentPage = (ContentPage) allPagesInfosIterator.next();
                              String pageTitle = contentPage.getTitle(jData.params().getEntryLoadRequest());
                              if ( pageTitle == null ){
                              	pageTitle = "No Title - [pid=" + contentPage.getID() + "]";
                              }
                              %>
                              <option value="<%=contentPage.getID()%>"><%=pageTitle%></option>
                      <% } %>
                          <option value="BLANK">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>

                      </select>
                      <br>&nbsp;<br>
	            	<% if ( canDeleteTemplate.booleanValue() ){ %>
                        <fmt:message key="org.jahia.admin.templates.ManageTemplates.selectTemplate.label"/><br><br>
                        <select class="input" name="templateid">
                            <option value="0">--&nbsp;&nbsp;<fmt:message key="org.jahia.admin.chooseATemplate.label"/>&nbsp;&nbsp;--&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
                        <%
                            while(allTemplatesIterator.hasNext()) {
                                JahiaPageDefinition pageDefinition = (JahiaPageDefinition) allTemplatesIterator.next();
                                %>
                                <option value="<%=pageDefinition.getID()%>" <%if(pageDefinition.getID()==baseTemplateID.intValue()){%>selected<%}%>><%=pageDefinition.getName()%></option>
                        <% } %>
                        </select>
                  	<% } %>
                  </td>
              </tr>
              </table>
          	<% if ( canDeleteTemplate.booleanValue() ){ %>
               <div class="buttonList" style="text-align: right; padding : 10px">
                 <div class="button">
                     <a href="javascript:sendForm('swap');"><fmt:message key="org.jahia.admin.save.label"/></a>
                 </div>
               </div>
      			<% } %>

        <input name="subaction" type="hidden" value="">
    </form>


  <div id="operationMenu">
  	<div id="operationMenuLabel">
			<fmt:message key="org.jahia.admin.otherOperations.label"/>&nbsp;:
		</div>
		<ul id="operationList">
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=display")%>'><fmt:message key="org.jahia.admin.templates.ManageTemplates.backToTemplatesList.label"/></a>
      </li>     		
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
      </li>     		
    </ul>
  </div>


</div>

<%@include file="/admin/include/footer.inc"%>
