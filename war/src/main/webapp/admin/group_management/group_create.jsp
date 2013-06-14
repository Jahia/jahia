<%@page language="java" %>
<%@page import="java.util.*" %>
<%@page import="org.jahia.bin.*" %>
<%@page import="org.jahia.data.JahiaData" %>
<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>
<jsp:useBean id="groupMessage" class="java.lang.String" scope="session"/>
<%
    JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    String groupName = (String) request.getAttribute("groupName");
    String defaultHomePage = (String) request.getAttribute("defaultHomePage");
    int stretcherToOpen = 1; %>
<script language="javascript">
    function setFocus() {
        document.mainForm.groupName.focus();
    }

    function handleKeyCode(code) {
        if (code == 13) {
            document.mainForm.submit();
        }
    }
</script>
<!-- Adiministration page position -->
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.users.ManageGroups.createNewGroup.label"/></h2>
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
                                        <fmt:message key="org.jahia.admin.users.ManageGroups.createNewGroup.label"/>
                                    </div>
                                </div>
                                <div class="content-item"><%
                                    if (groupMessage.length() > 0) { %>
                                    <p class="errorbold">
                                        <%=groupMessage %>
                                    </p>
                                    <% } %>
                                    <form name="mainForm"
                                          action='<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=processCreate")%>'
                                          method="post">
                                        <!-- Create new group -->
                                        <p>
                                            <fmt:message key="org.jahia.admin.users.ManageGroups.pleaseTypeGroupName.label"/>
                                        </p>

                                        <p>
                                            <fmt:message key="org.jahia.admin.users.ManageGroups.noteThat.label"/>&nbsp;
                                        </p>
                                        <ul>
                                            <li>
                                                <fmt:message key="org.jahia.admin.users.ManageGroups.groupNameUniq.label"/>
                                            </li>
                                            <li>
                                                <fmt:message key="org.jahia.admin.users.ManageGroups.onlyCharacters.label"/>
                                            </li>
                                            <li>
                                                <fmt:message key="org.jahia.admin.users.ManageGroups.inputMaxCharacter.label"/>
                                            </li>
                                        </ul>
                                        <table border="0" style="width:100%">
                                            <tr>
                                                <td align="right">
                                                    <fmt:message key="org.jahia.admin.users.ManageGroups.groupName.label"/>&nbsp;
                                                </td>
                                                <td>
                                                    <input type="text" name="groupName" class="input" size="40"
                                                           maxlength="185" value="<%= StringEscapeUtils.escapeHtml(groupName)%>">&nbsp;
                                                    <font class="text2">
                                                        (<fmt:message key="org.jahia.admin.required.label"/>)
                                                    </font>
                                                </td>
                                            </tr>
                                            <% if (defaultHomePage != null) { %>
                                            <tr>
                                                <td align="right">
                                                    <input type="checkbox" name="setHomePage"
                                                           checked>&nbsp;<fmt:message key="org.jahia.admin.defaultHomePage.label"/>&nbsp;
                                                </td>
                                                <td>
                                                    <b><%=defaultHomePage %>
                                                    </b>
                                                </td>
                                            </tr>
                                            <% } %>
                                        </table>
                                        <br>
                                        <!--  -->
                                    </form>
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
              <a class="ico-cancel"
                 href='<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=display")%>'><fmt:message key="label.cancel"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="javascript:document.mainForm.submit();"><fmt:message key="label.ok"/></a>
            </span>
          </span>
</div>
<script language="javascript">
    setFocus();
</script>
</div>
</div>
