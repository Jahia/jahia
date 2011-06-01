<%@include file="/admin/include/header.inc" %>
<%@page import="org.jahia.bin.Jahia" %>
<%
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
                  Jahia&nbsp;xCM&nbsp;<%= Jahia.VERSION %>&nbsp;<fmt:message key="org.jahia.admin.build.label"/>&nbsp;<%= Jahia.getBuildNumber() %>
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