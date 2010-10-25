<%@include file="/admin/include/header.inc" %>
<%@page import = "java.util.*" %>
<%@page import="org.jahia.bin.*" %>
<%@page import="org.jahia.resourcebundle.*" %>
<%@page import="org.jahia.params.*" %>
<%@page import="java.text.SimpleDateFormat" %>
<%@ page import="org.jahia.utils.DateUtils" %>
<%
ProcessingContext jParams = null;
if (jData != null) {
jParams = jData.params();
}
List licenses         = (List) request.getAttribute("licenses");
Date expirationDate = null;
SimpleDateFormat dateFormatter = new SimpleDateFormat(DateUtils.DEFAULT_DATETIME_FORMAT);
int maxDays = -1;
if (request.getAttribute("expirationDate") != null) {
expirationDate = (Date) request.getAttribute("expirationDate");
maxDays = ((Integer) request.getAttribute("allowedDays")).intValue();
}
pageContext.setAttribute("jahiaEditionTitle", JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.info.LicenceInfo.jahiaEdition." + request.getAttribute("jahiaEdition") + ".label", jData.getProcessingContext().getUILocale()));
stretcherToOpen   = 0; %>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="label.aboutJahia"/></h2>
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
                <jsp:param name="mode" value="server"/>
            </jsp:include>
            <div id="content" class="fit">
              <div class="head">
                <div class="object-title">
                  <fmt:message key="org.jahia.admin.info.LicenceInfo.jahiaRelease.label"/>&nbsp;${release},&nbsp;${jahiaEditionTitle},&nbsp;<fmt:message key="org.jahia.admin.build.label"/>&nbsp;${build}
                </div>
              </div>
              <div class="content-body">
                <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                    <tr>
						<td>
				<pre><%@include file="../LICENSE"%></pre>
						</td>
					</tr>
				</table>
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
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/admin/include/footer.inc" %>