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

<%@ include file="/jsp/jahia/administration/include/header.inc" %>
<%@ page import="org.jahia.bin.JahiaAdministration,
org.jahia.params.*,
org.jahia.services.importexport.ImportExportService,
org.jahia.utils.*,
org.jahia.resourcebundle.*,
java.util.Locale,
java.util.Properties" %>
<%
String theURL = "";
ParamBean jParams   = (ParamBean) request.getAttribute( "org.jahia.params.ParamBean" );
Locale currentLocale = request.getLocale();
if (session != null) {
if (session.getAttribute(ProcessingContext.SESSION_LOCALE) != null) {
currentLocale = (Locale) session.getAttribute(ProcessingContext.
SESSION_LOCALE);
}
}
Properties settings = currentSite.getSettings();
String[] targetSites = null;
String property = settings.getProperty(ImportExportService.PRODUCTION_TARGET_LIST_PROPERTY, "");
int length = 0;
if (!"".equals(property)) {
targetSites = property.split(",");
length = targetSites.length;
} %>
<script type="text/javascript">
function viewCronHelp() {
    var preview = window.open('${pageContext.request.contextPath}/jsp/jahia/administration/cron_help.html', 
            'CronHelpPopup',
            'width=800,height=400,status=0,menubar=0,resizable=1,scrollbars=1');
    preview.focus();
}
</script>
<script type="text/javascript">
  <!--
  var maxID = <%=length%>;
  var targetSites = new Array();
  var sitenames = new Array();
  var usernames = new Array();
  var passwords = new Array();
  var crons = new Array();
  var profiles = new Array();
  var aliases = new Array();
  var metadatas = new Array();
  var workflows = new Array();
  var acls = new Array();
  var publishes = new Array();
  <%
          for (int i = 0; i < length; i++) {
              String targetSite = settings.getProperty("prod_target_site_"+i,"");
              String sitename = settings.getProperty(ImportExportService.PRODUCTION_SITE_NAME_PROPERTY+i,"");
              String username = settings.getProperty(ImportExportService.PRODUCTION_USERNAME_PROPERTY+i,"");
              String password = new String(org.apache.axis.encoding.Base64.decode(settings.getProperty(ImportExportService.PRODUCTION_PASSWORD_PROPERTY+i,"")));
              String cron = settings.getProperty(ImportExportService.PRODUCTION_CRON_PROPERTY+i,"");
              String profile = settings.getProperty(ImportExportService.PRODUCTION_PROFILE_PROPERTY+i,"");
              String alias = settings.getProperty(ImportExportService.PRODUCTION_ALIAS_PROPERTY+i,"");
              String metadata = settings.getProperty(ImportExportService.PRODUCTION_METADATA_PROPERTY+i,"true");
              String workflow = settings.getProperty(ImportExportService.PRODUCTION_WORKFLOW_PROPERTY+i,"false");
              String acl = settings.getProperty(ImportExportService.PRODUCTION_ACL_PROPERTY+i,"true");
              String publish = settings.getProperty(ImportExportService.PRODUCTION_AUTO_PUBLISH_PROPERTY+i,"false");
              
  %>
      targetSites[<%=i%>] = '<%=targetSite%>';
      sitenames[<%=i%>] = '<%=sitename%>';
      usernames[<%=i%>] = '<%=username%>';
      passwords[<%=i%>] = '<%=password%>';
      crons[<%=i%>] = '<%=cron%>';
      profiles[<%=i%>] = '<%=profile%>';
      aliases[<%=i%>] = '<%=alias%>';
      metadatas[<%=i%>] = <%=metadata%>;
      workflows[<%=i%>] = <%=workflow%>;
      acls[<%=i%>] = <%=acl%>;
      publishes[<%=i%>] = <%=publish%>;
      
          <%}%>
  
  function selectTarget(id) {
      var form = document.mainForm;
      form.targetSite.value=targetSites[id];
      form.sitename.value=sitenames[id];
      form.username.value=usernames[id];
      form.password.value=passwords[id];
      form.cron.value=crons[id];
      form.profile.value=profiles[id];
      form.alias.value=aliases[id];
      form.metadata.checked= metadatas[id];
      form.workflow.checked= workflows[id];
      form.acl.checked= acls[id];
      form.publish.checked= publishes[id];
  }
  
  function addNewTarget() {
      if(validateForm()) {
      var form = document.mainForm;
      form.targetLists.options[maxID]=new Option(form.targetSite.value,maxID);
      targetSites[maxID] = form.targetSite.value;
      sitenames[maxID] = form.sitename.value;
      usernames[maxID] = form.username.value;
      passwords[maxID] = form.password.value;
      crons[maxID] = form.cron.value;
      profiles[maxID] = form.profile.value;
      aliases[maxID] = form.alias.value;
      metadatas[maxID] = form.metadata.checked;
      workflows[maxID] = form.workflow.checked;
      acls[maxID] = form.acl.checked;
      publishes[maxID] = form.publish.checked;
      maxID++;
      form.maxId.value=maxID;
      form.targetSite.value='';
      form.sitename.value='';
      form.username.value='';
      form.password.value='';
      form.cron.value='';
      form.profile.value='';
      form.alias.value='';
      form.metadata.checked=true;
      form.workflow.checked=false;
      form.acl.checked=true;
      form.publish.checked=false;
      }
  }
  function validateForm(){
      var regexp = /[\w:\/\*\?\-,#]/ig;
      var form = document.mainForm;
      if(!form.targetSite.value.match(regexp)) {
          alert("<%=JahiaTools.html2text(JahiaResourceBundle.getAdminResource("org.jahia.admin.productionManager.form.error.required.targetSite",
                  jParams, jParams.getLocale()))%>");
          return false;
      }
      if(!form.sitename.value.match(regexp)) {
          alert("<%=JahiaTools.html2text(JahiaResourceBundle.getAdminResource("org.jahia.admin.productionManager.form.error.required.sitename",
                  jParams, jParams.getLocale()))%>");
          return false;
      }
      if(!form.username.value.match(regexp)) {
          alert("<%=JahiaTools.html2text(JahiaResourceBundle.getAdminResource("org.jahia.admin.productionManager.form.error.required.username",
                  jParams, jParams.getLocale()))%>");
          return false;
      }
      if(!form.password.value.match(regexp)) {
          alert("<%=JahiaTools.html2text(JahiaResourceBundle.getAdminResource("org.jahia.admin.productionManager.form.error.required.password",
                  jParams, jParams.getLocale()))%>");
          return false;
      }
      if(!form.cron.value.match(regexp)) {
          alert("<%=JahiaTools.html2text(JahiaResourceBundle.getAdminResource("org.jahia.admin.productionManager.form.error.required.cron",
                  jParams, jParams.getLocale()))%>");
          return false;
      }
      if(!form.profile.value.match(regexp)) {
          alert("<%=JahiaTools.html2text(JahiaResourceBundle.getAdminResource("org.jahia.admin.productionManager.form.error.required.profile",
                  jParams, jParams.getLocale()))%>");
          return false;
      }
      if(!form.alias.value.match(regexp)) {
          alert("<%=JahiaTools.html2text(JahiaResourceBundle.getAdminResource("org.jahia.admin.productionManager.form.error.required.alias",
                  jParams, jParams.getLocale()))%>");
          return false;
      }
      return true;
  }
  function sendForm() {
      fillHiddenfields();
      document.mainForm.submit();
  }
  
  function applyChangesOnTarget(id){
      if(validateForm()) {
          var form = document.mainForm;
          targetSites[id] = form.targetSite.value;
          sitenames[id] = form.sitename.value;
          usernames[id] = form.username.value;
          passwords[id] = form.password.value;
          crons[id] = form.cron.value;
          profiles[id] = form.profile.value;
          aliases[id] = form.alias.value;
          metadatas[id] = form.metadata.checked;
          workflows[id] = form.workflow.checked;
          acls[id] = form.acl.checked;
          publishes[id] = form.publish.checked;
      }
  }
  
  function deleteTarget(id){
      var form = document.mainForm;
      var j = 0;
      for(i=0;i<targetSites.length;i++) {
          if(i != id) {
              targetSites[j]=targetSites[i];
              sitenames[j]=sitenames[i];
              usernames[j]=usernames[i];
              passwords[j]=passwords[i];
              crons[j]=crons[i];
              profiles[j]=profiles[i];
              aliases[j]=aliases[i];
              metadatas[j]=metadatas[i];
              workflows[j]=workflows[i];
              acls[j]=acls[i];
              publishes[j]=publishes[i];
              j++;
          }
      }
      targetSites.length=j;
      sitenames.length=j;
      usernames.length=j;
      passwords.length=j;
      crons.length=j;
      profiles.length=j;
      aliases.length=j;
      metadatas.length=j;
      workflows.length=j;
      acls.length=j;
      publishes.length=j;
      for(i=0;i<targetSites.length;i++) {
          form.targetLists.options[i] = new Option(targetSites[i],i);
      }
      form.targetLists.options.length = j;
      maxID--;
      form.maxId.value=maxID;
      form.targetSite.value='';
      form.sitename.value='';
      form.username.value='';
      form.password.value='';
      form.cron.value='';
      form.profile.value='';
      form.alias.value='';
      form.metadata.checked=true;
      form.workflow.checked=false;
      form.acl.checked=true;
      form.publish.checked=false;
  }
  
  function fillHiddenfields(){
      var form = document.mainForm;
      var targetSitesConcat = '';
      var passwordsConcat = '';
      var sitenamesConcat = '';
      var usernamesConcat = '';
      var cronsConcat = '';
      var profilesConcat = '';
      var aliasesConcat='';
      var metadatasConcat='';
      var workflowsConcat='';
      var aclsConcat='';
      var publishesConcat='';
      for(i=0;i<targetSites.length;i++) {
          targetSitesConcat = targetSitesConcat+targetSites[i]+',';
          sitenamesConcat = sitenamesConcat+sitenames[i]+',';
          usernamesConcat = usernamesConcat+usernames[i]+',';
          passwordsConcat = passwordsConcat+passwords[i]+',';
          cronsConcat = cronsConcat+crons[i]+',';
          profilesConcat = profilesConcat+profiles[i]+',';
          aliasesConcat = aliasesConcat+aliases[i]+',';
          metadatasConcat = metadatasConcat+metadatas[i]+',';
          workflowsConcat = workflowsConcat+workflows[i]+',';
          aclsConcat = aclsConcat+acls[i]+',';
          publishesConcat = publishesConcat+publishes[i]+',';
      }
      form.targetSites.value=targetSitesConcat;
      form.sitenames.value=sitenamesConcat;
      form.usernames.value=usernamesConcat;
      form.passwords.value=passwordsConcat;
      form.crons.value=cronsConcat;
      form.profiles.value=profilesConcat;
      form.aliases.value=aliasesConcat;
      form.metadatas.value=metadatasConcat;
      form.workflows.value=workflowsConcat;
      form.acls.value=aclsConcat;
      form.publishes.value=publishesConcat;
  }
  
  //-->
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.label"/> : <% if (currentSite != null) { %><internal:adminResourceBundle resourceName="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName() %><%} %></h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/jsp/jahia/administration/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
            <jsp:include page="/jsp/jahia/administration/include/left_menu.jsp">
                <jsp:param name="mode" value="site"/>
            </jsp:include>
            
            <div id="content" class="fit">
            <div class="head headtop">
                <div class="object-title">
                    <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.label"/>
                </div>
            </div>
            
            <div class="content-body">
            <!-- User operations -->
            <div id="operationMenu">
                <span class="dex-PushButton"> 
                    <span class="first-child">                  
                    <a class="ico-add" href="javascript:addNewTarget();"><internal:adminResourceBundle resourceName='org.jahia.admin.add.label'/></a>
                    </span> 
                </span>
                <span class="dex-PushButton"> 
                    <span class="first-child">                  
                    <a class="ico-apply" href="javascript:if(document.mainForm.targetLists.selectedIndex>=0)applyChangesOnTarget(document.mainForm.targetLists.options[document.mainForm.targetLists.selectedIndex].value);"><internal:adminResourceBundle resourceName='org.jahia.admin.apply.label'/></a>
                    </span> 
                </span>
                <span class="dex-PushButton"> 
                    <span class="first-child">                  
                    <a class="ico-delete" href="javascript:if(document.mainForm.targetLists.selectedIndex>=0)deleteTarget(document.mainForm.targetLists.options[document.mainForm.targetLists.selectedIndex].value);"><internal:adminResourceBundle resourceName='org.jahia.admin.delete.label'/></a>
                    </span> 
                </span>
                <span class="dex-PushButton">
                <span class="first-child">
                  <a class="ico-ok" href="javascript:sendForm();"><internal:adminResourceBundle resourceName='org.jahia.admin.save.label'/></a>
                </span>
              </span>
                </div>
            </div>
                
              <% if (request.getAttribute("warningMsg")!=null) { %>
			  <p class="errorbold">
                &nbsp;&nbsp;<%=request.getAttribute("warningMsg")==null?"":request.getAttribute("warningMsg") %>&nbsp;
              </p>
              <% } %>
			  
              <form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"productionManager","&sub=commit")%>' method="post">
                <input type="hidden" name="targetSites" value=""><input type="hidden" name="usernames" value=""><input type="hidden" name="sitenames" value=""><input type="hidden" name="passwords" value=""><input type="hidden" name="crons" value=""><input type="hidden" name="profiles" value=""><input type="hidden" name="aliases" value=""><input type="hidden" name="metadatas" value=""><input type="hidden" name="workflows" value=""><input type="hidden" name="acls" value=""><input type="hidden" name="publishes" value=""><input type="hidden" name="maxId" value="">
                <div class="head">
                  <div class="object-title">
                    <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.configuredSite.label"/>&nbsp;:
                  </div>
                </div>

                  <select size="10" name="targetLists" style="width:100%" onchange="javascript:selectTarget(this.options[this.selectedIndex].value);">
                  <%
                  for (int i = 0; i < length; i++) {
                  String targetSite = settings.getProperty("prod_target_site_"+i,""); %>
                  <option value="<%=i%>"><%=targetSite %></option>
                  <%
                  } %>
                </select>
                <table border="0" cellpadding="5">
                  <tbody>
                    <tr>
                      <td width="100%" colspan="2" align="center">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.targetSite"/>
                        <br>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.targetSite.example"/>
                      </td>
                      <td>
                        <input type="text" name="targetSite" value="">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.sitename"/>
                      </td>
                      <td>
                        <input type="text" name="sitename" value="">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.alias"/>
                      </td>
                      <td>
                        <input type="text" name="alias" value="">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.username"/>
                      </td>
                      <td>
                        <input type="text" name="username" value="">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.password"/>
                      </td>
                      <td>
                        <input type="password" name="password" value="">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.profile"/>
                      </td>
                      <td>
                        <input type="text" name="profile" value="">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.metadata"/>
                      </td>
                      <td>
                        <input type="checkbox" name="metadata" checked="true" value="">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.workflow"/>
                      </td>
                      <td>
                        <input type="checkbox" name="workflow" value="">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.acls"/>
                      </td>
                      <td>
                        <input type="checkbox" name="acl" checked="true" value="">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.publish"/>
                      </td>
                      <td>
                        <input type="checkbox" name="publish" checked="false" value="">
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <internal:adminResourceBundle resourceName="org.jahia.admin.productionManager.form.cron"/>&nbsp;<a href="#cronhelp" onclick="viewCronHelp(); return false;"><img src="${pageContext.request.contextPath}/jsp/jahia/engines/images/about.gif" alt="show cron help"/></a>
                      </td>
                      <td>
                        <input type="text" name="cron" value="">
                      </td>
                    </tr>                    
                    </table>					
                  </form>
                  </div>
                </td>
                </tr>
              </tbody>
              </table>
           
			</div>
          </div><%@include file="/jsp/jahia/administration/include/footer.inc" %>
