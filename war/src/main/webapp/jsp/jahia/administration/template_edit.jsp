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
<%@page import   = "java.util.*,org.jahia.services.pages.*"%>
<%@page import="org.jahia.bin.*"%>
<%

    String theURL = "";

    JahiaPageDefinition templ 	= (JahiaPageDefinition)request.getAttribute("templ");
    String currAction			= (String)request.getAttribute("currAction");
    List   criticalPages        = (List)request.getAttribute("criticalPages");
    Boolean canDelete			= (Boolean)request.getAttribute("canDelete");

    String requestURI 			= (String)request.getAttribute("requestURI");
    String contextRoot 			= (String)request.getContextPath();
    JahiaSite site 				= (JahiaSite) request.getAttribute("site");
    String templatesContext 	= (String)request.getAttribute("templatesContext");

    //System.out.println("criticalPages : " + criticalPages);

%>

<script type="text/javascript">
<!--

function setVisible(who){

    if ( who.checked ){
        document.mainForm.visible_status.checked = true;
    }
}


function keepVisible(who,what) {
    if ( who.checked ){
        what.checked = true;
    }
}


function sendForm(subAction)
{
    document.mainForm.subaction.value=subAction;
    document.mainForm.action="<%=requestURI%>?do=templates&sub=edit&templid=<%=templ.getID()%>";
    document.mainForm.submit();
}



//-->
</script>

<div id="topTitle">
	<div id="topTitleLogo">
		<img src="<%=URL%>images/icons/admin/briefcase_document.gif" width="48" height="48" />
  </div>
  <h1 id="topTitleLabel"><internal:adminResourceBundle resourceName="org.jahia.admin.manageTemplates.label"/><br><% if ( site!= null ){%><internal:adminResourceBundle resourceName="org.jahia.admin.site.label"/>&nbsp;<%=site.getServerName()%><%}%></h1>
</div>

<div id="adminMainContent">

        <form name="mainForm" action="" method="post">

        <%  if ( currAction != null && currAction.equals("confirmdelete") ){  %>
        
        <h2>
          <internal:adminResourceBundle resourceName="org.jahia.admin.templates.ManageTemplates.deleteTemplate.label"/>
        </h2>
        
        <p>
          <internal:adminResourceBundle resourceName="org.jahia.admin.templates.ManageTemplates.confirmDeleteTemplate.label"/>
        </p>

        <table border="0" cellpadding="5" cellspacing="0" style="width:100%" class="topAlignedTable">
        <tr>
            <td>
                <internal:adminResourceBundle resourceName="org.jahia.admin.templates.ManageTemplates.templateName.label"/>
            </td>
            <td>
                : <b><%=templ.getName()%></b>
            </td>
        </tr>
        <tr>
            <td>
                <internal:adminResourceBundle resourceName="org.jahia.admin.templates.ManageTemplates.templateFileName.label"/>&nbsp;
            </td>
            <td>
                : <%=templ.getSourcePath().substring(templatesContext.length(),templ.getSourcePath().length())%>
            </td>
        </tr>
        <tr>
            <td>
                <internal:adminResourceBundle resourceName="org.jahia.admin.availableToUsers.label"/>&nbsp;
            </td>
            <td>
                :
                <% if (templ.isAvailable() ){
                %><internal:adminResourceBundle resourceName="org.jahia.admin.yes.label"/><% } else { %><internal:adminResourceBundle resourceName="org.jahia.admin.no.label"/><% } %>
            </td>
        </tr>

        <!--
        <tr>
            <td></td>
            <td><b>Delete&nbsp;And&nbsp;Undeploy&nbsp;</b>&nbsp;<input type="checkbox" name="undeploy"></td>
        </tr>
        -->
        </table>
                
        <div class="buttonList" style="text-align: right; padding : 10px">
          
          <input type="hidden" name="subaction" value="">
          <div class="button">
            <a href="<%=requestURI%>?do=templates&sub=display"><internal:adminResourceBundle resourceName="org.jahia.admin.cancel.label"/></a>
          </div>
          <% if ( canDelete.booleanValue() ) {%>
            <div class="button">
              <a href="javascript:sendForm('delete');"><internal:adminResourceBundle resourceName="org.jahia.admin.delete.label"/></a>
            </div>
          <% } %>
        </div>

        <%  } else {  %>
        
        <h2>
          <internal:adminResourceBundle resourceName="org.jahia.admin.templates.ManageTemplates.templateDetails.label"/>&nbsp;:
        </h2>

        <table border="0" cellpadding="0" width="100%" class="topAlignedTable">
        <tr>
            <td align="right">
                <internal:adminResourceBundle resourceName="org.jahia.admin.name.label"/>&nbsp;:
            </td>
            <td>
                <input name="templName" class="input" type="text" size="<%=inputSize%>" value="<%=templ.getName()%>">
            </td>
        </tr>
        <tr>
            <td align="right">
                <internal:adminResourceBundle resourceName="org.jahia.admin.templates.ManageTemplates.sourcePath.label"/>&nbsp;:
            </td>
            <td>
                <%=templ.getSourcePath().substring(templatesContext.length(),templ.getSourcePath().length())%>
            </td>
        </tr>
        <tr>
            <td align="right">
                <internal:adminResourceBundle resourceName="org.jahia.admin.templates.ManageTemplates.available.label"/>&nbsp;:
            </td>
            <td>
                <input class="input" type="checkbox" name="visible_status" value="<%=templ.getID()%>" <% if (templ.isAvailable() ){	%> checked<% } %>>
            </td>
        </tr>
        <tr>
            <td align="right">
                <internal:adminResourceBundle resourceName="org.jahia.admin.templates.ManageTemplates.setAsDefault.label"/>&nbsp;:
            </td>
            <td >
                <input type="checkbox" name ="isDefault" value="<%=templ.getID()%>" <% if ( site.getDefaultTemplateID() == templ.getID() ){ %>checked<% } %> onclick="setVisible(this)">
            </td>
        </tr>
        </table>
        
        <div class="buttonList" style="text-align: right; padding : 10px">
          <input type="hidden" name="subaction" value="">
          <div class="button">
            <a href="javascript:document.mainForm.reset()"><internal:adminResourceBundle resourceName="org.jahia.admin.resetChanges.label"/></a>
          </div>
          <div class="button">                            
            <a href="javascript:sendForm('save');"><internal:adminResourceBundle resourceName="org.jahia.admin.save.label"/></a>
          </div>
          <% if ( canDelete.booleanValue() ) {%>
            <div class="button">
              <a href="javascript:sendForm('confirmdelete');"><internal:adminResourceBundle resourceName="org.jahia.admin.delete.label"/></a>
            </div>
          <% } %>
        </div>

        <% } %>

        </form>

  <div id="operationMenu">
  	<div id="operationMenuLabel">
			<internal:adminResourceBundle resourceName="org.jahia.admin.otherOperations.label"/>&nbsp;:
		</div>
		<ul id="operationList">
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=display")%>'><internal:adminResourceBundle resourceName="org.jahia.admin.templates.ManageTemplates.backToTemplatesList.label"/></a>
      </li>     		
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><internal:adminResourceBundle resourceName="org.jahia.admin.backToMenu.label"/></a>
      </li>     		
    </ul>
  </div>

</div>

<%@include file="/jsp/jahia/administration/include/footer.inc"%>
