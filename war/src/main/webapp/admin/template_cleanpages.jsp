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
