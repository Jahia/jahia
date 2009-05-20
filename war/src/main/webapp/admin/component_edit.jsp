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
<%@page import   = "java.util.*,org.jahia.data.applications.*,org.jahia.services.sites.*"%>
<%@page import="org.jahia.bin.*"%>


<%

    String theURL = "";
    ApplicationBean appItem = (ApplicationBean)request.getAttribute("appItem");
    String currAction		= (String)request.getAttribute("currAction");
    

    String requestURI 		= (String)request.getAttribute("requestURI");
    String contextRoot 		= (String)request.getContextPath();
    stretcherToOpen   = 0;


    Iterator authSites = null;

    if ( request.getAttribute("authSites")!= null ){
        authSites 	= (Iterator)request.getAttribute("authSites");
    } else {
        List vec = new ArrayList();
        authSites = vec.iterator();
    }

    Integer nbShare = null;
    if ( request.getAttribute("nbShare")!= null ){
        nbShare = (Integer)request.getAttribute("nbShare");
    } else {
        nbShare = new Integer(0);
    }

%>

<script type="text/javascript">
        function sendForm(subAction)
        {
            document.mainForm.subaction.value=subAction;
            document.mainForm.action="<%=requestURI%>?do=sharecomponents&sub=edit&appid=<%=appItem.getID()%>";
            document.mainForm.submit();
        }
</script>

<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit"><fmt:message key="org.jahia.admin.manageComponents.label"/></h2>
</div>

<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
    cellspacing="0">
    <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/admin/include/tab_menu.inc"%>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
            <div class="dex-TabPanelBottom">
            <div class="tabContent">
            <jsp:include page="/admin/include/left_menu.jsp">
                <jsp:param name="mode" value="server"/>
            </jsp:include>
            
            <div id="content" class="fit">
            <div class="head headtop">
                <div class="object-title">
                    <fmt:message key="org.jahia.admin.users.ManageUsers.editUser.label"/>
                </div>
            </div>
            <div  class="content-item">

        <form name="mainForm" action="<%=requestURI%>?do=sharecomponents&sub=edit&appid=<%=appItem.getID()%>" method="post">
        <%
            if ( currAction != null && currAction.equals("confirmdelete") ){

        %>
        <div class="head">
            <div class="object-title"><fmt:message key="org.jahia.admin.components.ManageComponents.deleteComponents.label"/>
            </div>
        </div>
        <p>
        &nbsp;&nbsp;<fmt:message key="org.jahia.admin.components.ManageComponents.confirmDeleteComponents.label"/>
        </p>
        <table border="0" cellpadding="5" cellspacing="0" width="100%">
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.components.ManageComponents.applicationName.label"/>&nbsp;
                    </td>
            <td>
                :&nbsp;<b><%=appItem.getName()%></b>
                    </td>
                </tr>
                <tr>
            <td>
                <fmt:message key="org.jahia.admin.components.ManageComponents.applicationContext.label"/>&nbsp;
                    </td>
            <td>
                :&nbsp;<%=appItem.getContext()%>
                    </td>
                </tr>
                <tr>
            <td>
                <fmt:message key="org.jahia.admin.availableToUsers.label"/>&nbsp;
                    </td>
                    <td >
                :&nbsp;
                        <%
                            if (appItem.getVisibleStatus() == 1 ){
                        %><fmt:message key="org.jahia.admin.yes.label"/><% } else { %><fmt:message key="org.jahia.admin.no.label"/><% } %>
                    </td>
                </tr>
                <tr>
            <td style="vertical-align: top">
                <fmt:message key="org.jahia.admin.components.ManageComponents.applicationDesc.label"/>&nbsp;
                    </td>
            <td>
                &nbsp;<textarea name="appDescr" rows="6" cols="45"><%=appItem.getdesc()%></textarea>
                    </td>
                </tr>
                <tr>
            <td></td>
            <td><b><fmt:message key="org.jahia.admin.components.ManageComponents.deleteAndUndeploy.label"/>&nbsp;</b>&nbsp;<input type="checkbox" name="undeploy"></td>
                </tr>
                </table>
        
        <% } else { %>
        <div class="head">
            <div class="object-title">
                <fmt:message key="org.jahia.admin.components.ManageComponents.componentDetails.label"/>
            </div>
        </div>
  <table border="0" cellpadding="5" cellspacing="0" width="100%">
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.components.ManageComponents.applicationName.label"/>&nbsp;
                    </td>
            <td>
                :&nbsp;<input class="input" type="text" name="appName" value="<%=appItem.getName()%>" size="<%=inputSize%>">
                    </td>
                </tr>
                <tr>
            <td>
                <fmt:message key="org.jahia.admin.components.ManageComponents.applicationContext.label"/>&nbsp;
                    </td>
            <td>
                :&nbsp;<%=appItem.getContext()%>
                    </td>
                </tr>
                <tr>
            <td>
                <fmt:message key="org.jahia.admin.availableToUsers.label"/>&nbsp;
                    </td>
                    <td >
                :&nbsp;
                        <input type="checkbox" name ="visible_status" value="<%=appItem.getID()%>"
                        <%
                            if (appItem.getVisibleStatus() == 1 ){
                        %>checked<% } %>>
                    </td>
                </tr>
                <tr>
            <td style="vertical-align:top">
                <fmt:message key="org.jahia.admin.description.label"/>&nbsp;
                    </td>
            <td>
                &nbsp;<textarea class="input" name="appDescr" rows="6" cols='45'><%=appItem.getdesc()%></textarea>
                    </td>
                </tr>
                <% if ( nbShare.intValue()>0 ){ %>
                <tr>
            <td style="vertical-align:top">
                <fmt:message key="org.jahia.admin.components.ManageComponents.sharedWith.label"/>&nbsp;:
                    </td>
            <td style="vertical-align:top">
                        <% while ( authSites.hasNext() ){
                            JahiaSite aSite = (JahiaSite)authSites.next();
                        %>
                        <%=aSite.getServerName()%><br>
                        <% } %>
                    </td>
                </tr>
                <% } %>

                </table>
        
        
        <%
            }

        %>

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
  	
     <% if ( currAction != null && currAction.equals("confirmdelete") ){ %>
     <input type="hidden" name="subaction" value="">

      <span class="dex-PushButton"> 
          <span class="first-child">
          <a class="ico-cancel" href="<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=display")%>">
          <fmt:message key="org.jahia.admin.cancel.label"/>
        </a>
       </span>
      </span>
      <span class="dex-PushButton"> 
          <span class="first-child">
          <a class="ico-delete" href="javascript:sendForm('delete');">
          <fmt:message key="org.jahia.admin.delete.label"/>
        </a>
       </span>
      </span>

     <% } else { %>     
      <input type="hidden" name="subaction" value="">
      <span class="dex-PushButton">
        <span class="first-child"> 
          <a class="ico-restore" href="javascript:document.mainForm.reset();">
          <fmt:message key="org.jahia.admin.resetChanges.label"/>
          </a>
         </span> 
      </span>  
      <span class="dex-PushButton">
      <span class="first-child"> 
          <a class="ico-save" href="javascript:sendForm('save');">
          <fmt:message key="org.jahia.admin.save.label"/>
          </a>
          </span>
      </span>
      <span class="dex-PushButton">
      <span class="first-child"> 
          <a class="ico-delete" href="javascript:sendForm('confirmdelete');">
          <fmt:message key="org.jahia.admin.delete.label"/>
        </a>
        </span>
      </span> 
      
     <% } %>
            
     
  </div>

</div>



<%@include file="/admin/include/footer.inc"%>
