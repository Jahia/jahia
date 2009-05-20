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
<%@page import   = "org.jahia.bin.*,org.jahia.params.*,java.util.*,org.jahia.services.sites.*,org.jahia.registries.*,org.jahia.utils.*" %>
<%@include file="/admin/include/header.inc" %>
<%!
private SiteLanguageMapping getLanguageMapping(String fromLanguageCode, List mappings) {
Iterator mappingIter = mappings.iterator();
while (mappingIter.hasNext()) {
SiteLanguageMapping curMapping = (SiteLanguageMapping) mappingIter.next();
if (curMapping.getFromLanguageCode().equals(fromLanguageCode)) {
return curMapping;
}
}
return null;
}
public String padString(String input, int newLength) {
StringBuffer result = new StringBuffer(input);
for (int i=input.length(); i < newLength; i++) {
result.append("&nbsp;");
}
return result.toString();
} %>
<%
String theURL = "";
Iterator languageList = (Iterator)request.getAttribute("languageList");
Iterator mappingList = (Iterator) request.getAttribute("mappingList");
List languageMappings = (List) request.getAttribute("languageMappings");
Set languageSet = (Set) request.getAttribute("languageSet");
Map iso639ToLocale = (Map) request.getAttribute("iso639ToLocale");
String requestURI = (String)request.getAttribute("requestURI");
String contextRoot = (String)request.getContextPath();
Locale currentLocale = request.getLocale();
if (session != null) {
if (session.getAttribute(ProcessingContext.SESSION_LOCALE) != null) {
currentLocale = (Locale) session.getAttribute(ProcessingContext.
SESSION_LOCALE);
}
} 
stretcherToOpen   = 1;
%>
<script type="text/javascript">
  function sendForm(){
      document.mainForm.submit();
  }
</script>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.editLanguageMappings.label"/>
    <br>
    <% if ( currentSite!= null ){ %><fmt:message key="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName() %><%} %>
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
              <div class="head">
                <div class="object-title">
                  <fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.configuredMappings.label"/>
                </div>
              </div>
              <form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"siteLanguages","&sub=commitMappings")%>' method="post">
                <table width="100%" cellpadding="5" cellspacing="0" border="0">
                  <%
                  if (iso639ToLocale.size() == 0) { %>
                  <tr>
                    <td colspan="2">
                      <fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.noMappingConfigured.label"/>
                    </td>
                  </tr><%
                  }
                  Iterator iso639Iter = iso639ToLocale.keySet().iterator();
                  int count = 0;
                  while (iso639Iter.hasNext()) {
                  count++;
                  String curIso639Code = (String) iso639Iter.next();
                  Locale curIso639Locale = new Locale(curIso639Code, "");
                  List curLocales = (List) iso639ToLocale.get(curIso639Code);
                  SiteLanguageMapping curMapping = getLanguageMapping(curIso639Code, languageMappings);
                  if (count % 2 == 0) { %>
                  <tr class="oddLine">
                    <%
                    } else { %>
                    <tr class="evenLine">
                      <%
                      } %>
                      <td align="right">
                        <%=curIso639Locale.getDisplayName(currentLocale) %>(<%=curIso639Locale.toString() %>) 
                        <br>
                        <fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.isMappedTo.label"/>
                      </td>
                      <td align="left">
                        &nbsp;
                        <select name="mapping_<%=curIso639Code%>" size="3" class="fontfix">
                          <%
                          Iterator localesEnum = curLocales.iterator();
                          while (localesEnum.hasNext()) {
                          Locale curLocale = (Locale) localesEnum.next();
                          if (curMapping.getToLanguageCode().equals(curLocale.toString())) { %>
                          <option value="<%=curLocale.toString()%>" selected="">
                          <%
                          } else { %>
                          <option value="<%=curLocale.toString()%>"><%
                            } %>
                            <%=padString(curLocale.getDisplayName(currentLocale) + "(" + curLocale.toString() + ")", 40) %>
                          </option>
                          <%
                          } %>
                        </select>
                      </td>
                    </tr>
                    <%
                    } %>
                    </table>                    
                  </form>
                  <p>
                    <fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.hint.label"/>
                  </p>
                  </div>
                </div>
</div>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
          <div id="actionBar">
            <span class="dex-PushButton">
              <span class="first-child">
                <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"siteLanguages","&sub=display")%>'><fmt:message key="org.jahia.admin.languages.ManageSiteLanguages.backToLanguageSettings.label"/></a>
              </span>
            </span>
            <span class="dex-PushButton">
              <span class="first-child">
                <a class="ico-ok" href="javascript:document.mainForm.submit();"><fmt:message key='org.jahia.admin.save.label'/></a>
              </span>
            </span>
          </div>
          </div><%@include file="/admin/include/footer.inc" %>