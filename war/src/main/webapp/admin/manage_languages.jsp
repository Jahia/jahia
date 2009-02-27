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

<%@page import   = "org.jahia.bin.JahiaAdministration,org.jahia.params.*,org.jahia.services.sites.JahiaSite,org.jahia.services.sites.SiteLanguageMapping,org.jahia.services.sites.SiteLanguageSettings,org.jahia.utils.*,org.jahia.resourcebundle.*" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Set" %>
<%@include file="/admin/include/header.inc" %>
<%
ParamBean jParams   = (ParamBean) request.getAttribute( "org.jahia.params.ParamBean" );
String theURL = "";
Iterator languageList = (Iterator)request.getAttribute("languageList");
Iterator mappingList = (Iterator) request.getAttribute("mappingList");
Set languageSet = (Set) request.getAttribute("languageSet");
Boolean mixLanguages = (Boolean) request.getAttribute("mixLanguages");
String requestURI = (String)request.getAttribute("requestURI");
String contextRoot = (String)request.getContextPath();
String homePageLanguageStr = "";
Set homePageLanguageSet = (Set) request.getAttribute("homePageLanguageSet");
Iterator iterator = homePageLanguageSet.iterator();
String langCode = "";
while ( iterator.hasNext() ){
langCode = (String)iterator.next();
homePageLanguageStr += "'delete_" + langCode + "'";
if ( iterator.hasNext() ){
homePageLanguageStr += ",";
}
}
Locale currentLocale = request.getLocale();
if (session != null) {
if (session.getAttribute(ProcessingContext.SESSION_LOCALE) != null) {
currentLocale = (Locale) session.getAttribute(ProcessingContext.
SESSION_LOCALE);
}
} %>
<script language="javascript">
    <!--

    function sendForm()
    {
        if ( checkHomePageLanguage() ){
            document.mainForm.submit();
        }
    }

    function checkHomePageLanguage(){
        var homePageLanguage = [<%=homePageLanguageStr%>];

        var count = 0;

        for (var i = 0; i < document.mainForm.length; i++)
        {
            var inputName = document.mainForm.elements[i].name;
            if ( inputName.indexOf('delete_',0) != -1 && document.mainForm.elements[i].checked ){
                for ( var j=0 ; j<homePageLanguage.length ; j++ ){
                    if ( homePageLanguage[j] == inputName ){
                        count++;
                    }
                }
            }
            if ( <%=languageSet.size()%>== count
                || count == homePageLanguage.length ){
                alert("<%=JahiaTools.html2text(JahiaResourceBundle.getAdminResource("org.jahia.admin.languages.ManageSiteLanguages.homePageLangWarning",
                    jParams, jParams.getLocale()))%>");
                return false;
            }
        }
        return true;
    }

    //-->
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.manageLanguages.label"/>: <% if ( currentSite!= null ){ %><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName() %>&nbsp;&nbsp;<%} %></h2>
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
                <div class="head">
                  <div class="object-title">
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.configuredLanguages.label"/>&nbsp;
                  </div>
                </div>
                <div class="content-body">
                  <div id="operationMenu">
                    * = <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.languagesUsedByHomePage.label"/>&nbsp;
                  </div>
                </div>
                <form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"siteLanguages","&sub=commit")%>' method="post">
                  <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" style="width: 100%">
                    <thead>
                      <tr>
                        <th>
                          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.rank.label"/>
                        </th>
                        <th>
                          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.language.label"/>
                        </th>
                        <th style="text-align:center">
                          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.active.label"/>
                        </th>
                        <th style="text-align:center">
                          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.mandatory.label"/>
                        </th><!--
                        <th style="text-align:center"  ><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.delete.label"/></th>
                        -->
                      </tr>
                    </thead>
                    <tbody>
                      <%
                      int count = 0;
                      if (!languageList.hasNext()) { %>
                      <tr>
                        <td colspan="5">
                          <b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.noLanguageDefined.label"/></b>
                        </td>
                      </tr><%
                      } else {
                      while (languageList.hasNext()) {
                      count++;
                      SiteLanguageSettings curSetting = (SiteLanguageSettings) languageList.next();
                      Locale curLocale = LanguageCodeConverters.languageCodeToLocale(curSetting.getCode());
                      if (count % 2 == 0) { %>
                      <tr class="evenLine">
                        <%
                        } else { %>
                        <tr class="oddLine">
                          <%
                          } %>
                          <td align="left">
                            <input type="text" size="2" name="rank_<%=curSetting.getCode()%>" value="<%=count%>">
                          </td>
                          <td align="left">
                            <%=curLocale.getDisplayName(currentLocale) %>(<%=curLocale.toString() %>)<% if (homePageLanguageSet.contains(curLocale.toString())){ %>*<%} %>
                          </td>
                          <td align="center">
                            <% if (curSetting.isActivated()) { %>
                            <input type="checkbox" name="active_<%=curSetting.getCode()%>" value="<%=count%>" checked><% } else { %>
                            <input type="checkbox" name="active_<%=curSetting.getCode()%>" value="<%=count%>"><% } %>
                          </td>
                          <td align="center">
                            <% if (curSetting.isMandatory()) { %>
                            <input type="checkbox" name="mandatory_<%=curSetting.getCode()%>" value="<%=count+1%>" checked><% } else { %>
                            <input type="checkbox" name="mandatory_<%=curSetting.getCode()%>" value="<%=count+1%>"><% } %>
                          </td><!--
                          <td  align="center">
                          <input type="checkbox" name="delete_<%=curSetting.getCode()%>" value="<%=count+2%>">
                          </td>
                          -->
                        </tr><%
                        }
                        } %>
                        </tbody>
                      </table>
                      <div class="head headtop">
                        <div class="object-title">
                          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.options.label"/>
                        </div>
                      </div><%
                      if (mixLanguages.booleanValue()) { %>
                      <input type="checkbox" name="mixLanguages" id="mixLanguages" checked="checked"/><%
                      } else { %>
                      <input type="checkbox" name="mixLanguages" id="mixLanguages"/><%
                      } %>
                      <label for="mixLanguages"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.mixLanguages.label"/></label>
                      <br>
                      <% if (request.getAttribute("jahiaErrorMessage")!=null) { %>
                      <br>
                      <div class="text2" style="text-align:center">
                        <%=request.getAttribute("jahiaErrorMessage") %>
                      </div>
                      <%
                      } %>
                      <br>
                      <br>
                      <div style="text-align:center">
                        <table border="0" cellpadding="5" cellspacing="0">
                          <tr>
                            <td>
                            	<b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.availableLanguages.label"/></b><br/>
                              <select name="language_list" multiple="" size="10">
                                <%
                                Iterator localeIter = LanguageCodeConverters.getSortedLocaleList(currentLocale).iterator();
                                while (localeIter.hasNext()) {
                                Locale curLocale = (Locale) localeIter.next();
                                // we must now check if this language wasn't already inserted in
                                // the site.
                                if (!languageSet.contains(curLocale.toString())) {
                                String displayName = "";
                                displayName = curLocale.getDisplayName(currentLocale); %>
                                <option value="<%=curLocale%>"><%=displayName %>(<%=curLocale.toString() %>)</option>
                                <%
                                }
                                } %>
                              </select>
                            </td>
                            <td>
                                <span class="dex-PushButton">
                                    <span class="first-child">
                                        <a class="add-lang" href="javascript:sendForm();" title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.admin.languages.ManageSiteLanguages.addLanguages.label'/>"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.addLanguages.label"/></a>
                                    </span>
                                </span>
                            </td>
                          </tr>
                        </table>
                      </div>
                      </form>
                      <br/>
                      <div class="head headtop">
                        <div class="object-title">
                          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.languageMappings.label"/>
                        </div>
                      </div>
                      <table style="clear:both" class="text">
                        <tr>
                          <td>
                            <b></b>
                          </td>
                        </tr>
                        <% if (!mappingList.hasNext()) { %>
                        <tr>
                          <td>
                            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.noMappings.label"/>
                          </td>
                        </tr><%
                        }
                        while (mappingList.hasNext()) {
                        SiteLanguageMapping curMapping = (SiteLanguageMapping) mappingList.next();
                        Locale fromLocale = LanguageCodeConverters.languageCodeToLocale(curMapping.getFromLanguageCode());
                        Locale toLocale = LanguageCodeConverters.languageCodeToLocale(curMapping.getToLanguageCode()); %>
                        <tr>
                          <td>
                            <%=fromLocale.getDisplayName(currentLocale) %>(<%=fromLocale.toString() %>)-&gt; <%=toLocale.getDisplayName(currentLocale) %>(<%=toLocale.toString() %>)
                          </td>
                        </tr>
                        <%
                        } %>
                      </table>
                    </td>
                    </tr>
                  </tbody>
                  </table>
                </div>
              </div>
              <div id="actionBar">
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-edit" href='<%=JahiaAdministration.composeActionURL(request,response,"siteLanguages","&sub=displayMappings")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.languages.ManageSiteLanguages.editLanguageMappings.label"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-ok" href="javascript:sendForm();"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.admin.save.label'/></a>
                  </span>
                </span>
              </div>
            </div><%@include file="/admin/include/footer.inc" %>
