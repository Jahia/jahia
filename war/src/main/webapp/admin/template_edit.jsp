<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

--%>
<%@include file="/admin/include/header.inc"%>
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
  <h1 id="topTitleLabel"><fmt:message key="org.jahia.admin.manageTemplates.label"/><br><% if ( site!= null ){%><fmt:message key="org.jahia.admin.site.label"/>&nbsp;<%=site.getServerName()%><%}%></h1>
</div>

<div id="adminMainContent">

        <form name="mainForm" action="" method="post">

        <%  if ( currAction != null && currAction.equals("confirmdelete") ){  %>
        
        <h2>
          <fmt:message key="org.jahia.admin.templates.ManageTemplates.deleteTemplate.label"/>
        </h2>
        
        <p>
          <fmt:message key="org.jahia.admin.templates.ManageTemplates.confirmDeleteTemplate.label"/>
        </p>

        <table border="0" cellpadding="5" cellspacing="0" style="width:100%" class="topAlignedTable">
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.templates.ManageTemplates.templateName.label"/>
            </td>
            <td>
                : <b><%=templ.getName()%></b>
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.templates.ManageTemplates.templateFileName.label"/>&nbsp;
            </td>
            <td>
                : <%=templ.getSourcePath().substring(templatesContext.length(),templ.getSourcePath().length())%>
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.availableToUsers.label"/>&nbsp;
            </td>
            <td>
                :
                <% if (templ.isAvailable() ){
                %><fmt:message key="org.jahia.admin.yes.label"/><% } else { %><fmt:message key="org.jahia.admin.no.label"/><% } %>
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
            <a href="<%=requestURI%>?do=templates&sub=display"><fmt:message key="org.jahia.admin.cancel.label"/></a>
          </div>
          <% if ( canDelete.booleanValue() ) {%>
            <div class="button">
              <a href="javascript:sendForm('delete');"><fmt:message key="org.jahia.admin.delete.label"/></a>
            </div>
          <% } %>
        </div>

        <%  } else {  %>
        
        <h2>
          <fmt:message key="org.jahia.admin.templates.ManageTemplates.templateDetails.label"/>&nbsp;:
        </h2>

        <table border="0" cellpadding="0" width="100%" class="topAlignedTable">
        <tr>
            <td align="right">
                <fmt:message key="org.jahia.admin.name.label"/>&nbsp;:
            </td>
            <td>
                <input name="templName" class="input" type="text" size="<%=inputSize%>" value="<%=templ.getName()%>">
            </td>
        </tr>
        <tr>
            <td align="right">
                <fmt:message key="org.jahia.admin.templates.ManageTemplates.sourcePath.label"/>&nbsp;:
            </td>
            <td>
                <%=templ.getSourcePath().substring(templatesContext.length(),templ.getSourcePath().length())%>
            </td>
        </tr>
        <tr>
            <td align="right">
                <fmt:message key="org.jahia.admin.templates.ManageTemplates.available.label"/>&nbsp;:
            </td>
            <td>
                <input class="input" type="checkbox" name="visible_status" value="<%=templ.getID()%>" <% if (templ.isAvailable() ){	%> checked<% } %>>
            </td>
        </tr>
        <tr>
            <td align="right">
                <fmt:message key="org.jahia.admin.templates.ManageTemplates.setAsDefault.label"/>&nbsp;:
            </td>
            <td >
                <input type="checkbox" name ="isDefault" value="<%=templ.getID()%>" <% if ( site.getDefaultTemplateID() == templ.getID() ){ %>checked<% } %> onclick="setVisible(this)">
            </td>
        </tr>
        </table>
        
        <div class="buttonList" style="text-align: right; padding : 10px">
          <input type="hidden" name="subaction" value="">
          <div class="button">
            <a href="javascript:document.mainForm.reset()"><fmt:message key="org.jahia.admin.resetChanges.label"/></a>
          </div>
          <div class="button">                            
            <a href="javascript:sendForm('save');"><fmt:message key="org.jahia.admin.save.label"/></a>
          </div>
          <% if ( canDelete.booleanValue() ) {%>
            <div class="button">
              <a href="javascript:sendForm('confirmdelete');"><fmt:message key="org.jahia.admin.delete.label"/></a>
            </div>
          <% } %>
        </div>

        <% } %>

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
