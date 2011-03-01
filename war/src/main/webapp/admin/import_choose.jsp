<%@include file="/admin/include/header.inc" %>
<%@page import="org.jahia.bin.JahiaAdministration" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.data.templates.JahiaTemplatesPackage" %>
<%@ page import="org.jahia.utils.i18n.JahiaResourceBundle" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.services.content.JCRNodeWrapper" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    List importsInfos = (List) session.getAttribute("importsInfos");
    Map importInfosMap = new HashMap();
    for (Iterator iterator = importsInfos.iterator(); iterator.hasNext();) {
        Map infos = (Map) iterator.next();
        File file = (File) infos.get("importFile");
        importInfosMap.put(file, infos);
    }
    List importsInfosSorted = (List) session.getAttribute("importsInfosSorted");
    List tpls = (List) request.getAttribute("tmplSets");
    ProcessingContext jParams = null;
    if (jData != null) {
        jParams = jData.params();
    }
    final boolean isInstall = session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") != null;
    final String readmefilePath = response.encodeURL(new StringBuffer().append(request.getContextPath()).append("/html/startup/readme.html").toString());
    stretcherToOpen   = 0; %>
<script type="text/javascript">
    <!--

function sendForm(){
    <%if(isInstall){%>
          var openrf = document.getElementById('openReadmeFile');
          if(openrf != null && openrf.checked){
              openReadmeFile();
          }
          <%}%>
          setWaitingCursor();
          document.main.submit();
      }


      function openReadmeFile() {
           var params = "width=1100,height=500,left=0,top=0,resizable=yes,scrollbars=yes,status=no";
           window.open('<%=readmefilePath%>', 'Readme', params);
      }

      function setWaitingCursor() {
          if (typeof workInProgressOverlay != 'undefined') {
             workInProgressOverlay.launch();
          }
      }

      -->
</script>
<div id="topTitle">
    <h1>Jahia</h1>
    <h2 class="edit">
        <fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/>
    </h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
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
            <div class="head">
                <div class="object-title">
                    <fmt:message key="label.virtualSitesManagement.configwizard.variables"/>
                </div>
            </div>
            <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                <thead>
                <tr>
                    <th<%if(importsInfos.size()==1){ %> style="display:none;"<%} %> width="5%">&nbsp;</th>
                    <th width="95%">
                        <fmt:message key="label.name"/>
                    </th>

                </tr>
                </thead>
                <form name="main">
                    <input type="hidden" name="do" value="sites"/>
                    <input type="hidden" name="sub" value="processimport"/>
                    <tbody>
                    <%
                        int lineCounter = 0;
                        for (Iterator iterator = importsInfosSorted.iterator(); iterator.hasNext();) {
                            File file = (File) iterator.next();
                            Map infos = (Map) importInfosMap.get(file);
                            pageContext.setAttribute("infos", infos);
                            String filename = (String) infos.get("importFileName");
                            String fileType = (String) infos.get("type");
                            String siteKey = file.getName();
                            String lineClass = "oddLine";
                            if (lineCounter % 2 == 0) {
                                lineClass = "evenLine";
                            }
                            lineCounter++;
                    %>

                    <tr class="<%=lineClass%>">
                        <td<%if(importsInfos.size()==1){ %> style="display:none;"<%} %> align="center">
                            <input type="checkbox" name="<%=file.getName()%>selected" value="on"${not empty infos.selected ? 'checked' : ''}>
                        </td>
                        <td>
                            <% if ("site".equals(fileType)) { %>
                            <table border="0" cellpadding="0" width="100%">
                                <tr>
                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.siteTitle.label"/>*&nbsp;<c:if test="${infos.siteTitleInvalid}">
                                        <div class="error">
                                            <fmt:message key="org.jahia.admin.warningMsg.completeRequestInfo.label"/>
                                        </div></c:if>
                                    </td>
                                    <td>
                                        <input class="input" type="text" name="<%=siteKey+"siteTitle"%>" value="${fn:escapeXml(infos.sitetitle)}" size="<%=inputSize%>" maxlength="100">
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>*&nbsp;<c:if test="${infos.siteServerNameInvalid || infos.siteServerNameExists}">
                                        <div class="error">
                                            <fmt:message key="${empty infos.siteservername ? 'org.jahia.admin.warningMsg.completeRequestInfo.label' : (infos.siteServerNameInvalid ? 'org.jahia.admin.warningMsg.invalidServerName.label' : 'org.jahia.admin.warningMsg.chooseAnotherServerName.label')}"/>
                                        </div></c:if>
                                    </td>
                                    <td>
                                        <input class="input" type="text" name="<%=siteKey+"siteServerName"%>" value="${fn:escapeXml(infos.siteservername)}" size="<%=inputSize%>" maxlength="200">
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>*&nbsp;<c:if test="${infos.siteKeyInvalid || infos.siteKeyExists}">
                                        <div class="error">
                                            <fmt:message key="${empty infos.sitekey ? 'org.jahia.admin.warningMsg.completeRequestInfo.label' : (infos.siteKeyInvalid ? 'org.jahia.admin.warningMsg.onlyLettersDigitsUnderscore.label' : 'org.jahia.admin.warningMsg.chooseAnotherSiteKey.label')}"/>
                                        </div></c:if>
                                    </td>
                                    <td>
                                        <input type="hidden" name="<%=siteKey+"oldSiteKey"%>" value="<%= infos.get("oldsitekey") %>"><input class="input" type="text" name="<%=siteKey+"siteKey"%>" value="${fn:escapeXml(infos.sitekey)}" size="<%=inputSize%>" maxlength="50">
                                    </td>
                                </tr>
                                <tr>

                                    <td>
                                        <fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseTemplateSet.label"/>&nbsp;
                                    </td>
                                    <td>
                                        <select name="<%=siteKey + "templates"%>">

                                            <% if (tpls != null)
                                                for (Iterator iterator1 = tpls.iterator(); iterator1.hasNext();) {
                                                    JCRNodeWrapper pack = (JCRNodeWrapper) iterator1.next();
                                            %>
                                            <option value="<%=pack.getName()%>"<% if (pack.getName().equals(infos.get("templates"))) { %>selected<% } %>><%=pack.getName() %></option>

                                            <%
                                                    } %>
                                        </select>
                                    </td>
                                </tr>
                            </table>
                            <% } else if ("files".equals(fileType))  { %>
                            <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.shared"/>: <%=filename %>
                            <% } else { %>
                            <fmt:message key='<%="org.jahia.admin.site.ManageSites.fileImport."+filename %>'/><% } %>
                        </td>
                    </tr>
                    <%} %>
                    </tbody>
                </form>
            </table>
        </div>
    </div>
</td>
</tr>
</tbody>
</table>
</div>
<div id="actionBar">
    <%
        if (session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") == null) { %>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
            </span>
          </span><%} %>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href='javascript:sendForm();'><fmt:message key="label.doImport"/></a>
            </span>
          </span>
</div>

</div>
<%@include file="/admin/include/footer.inc" %>