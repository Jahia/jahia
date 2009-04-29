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
<%@ include file="/admin/include/header.inc" %>
<%@ page import="java.util.*" %>
<%@ page import="org.jahia.services.pages.*" %>
<%@ page import="org.jahia.bin.*" %>
<%@ page import="org.jahia.data.beans.JahiaBean" %>
<%@ page import="org.jahia.params.*" %>
<%@ page import="org.jahia.services.pages.*" %>
<%@ page import="org.jahia.engines.selectpage.SelectPage_Engine" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%
    final Iterator allTemplatesIterator = (Iterator) request.getAttribute("allTemplatesIterator");
    final int basePageID = ((Integer) request.getAttribute("basePageID")).intValue();
    final Integer baseTemplateID = (Integer) request.getAttribute("baseTemplateID");
    final Integer homePageID = (Integer) request.getAttribute("homePageID");
    final ProcessingContext jParams = jData.getProcessingContext();
    pageContext.setAttribute("jahia", new JahiaBean(jParams));
    String title = "";
    if (basePageID > 0) {
        try {
            title = ContentPage.getPage(basePageID).getTitle(jData.getProcessingContext().getEntryLoadRequest());
        } catch (Exception e) {
            title = "";
        }
    } %>

<script type="text/javascript" language="javascript">
    <!--

    function setPid(pid) {
        handleActionChanges(pid, pid);
    }

    function sendForm(action) {
        document.jahiaAdmin.subaction.value = action;
        document.jahiaAdmin.submit();
    }

    function handleActionChanges(params, pid) {
        document.jahiaAdmin.pageid.value = pid;
        sendForm('changePage');
    }

    //-->
</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.pageSettings.label"/></h2>
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
                                <div class="head headtop">
                                    <div class="object-title">
                                        <fmt:message key="org.jahia.admin.pageSettings.label"/>
                                    </div>
                                </div>
                                <div class="content-item">
                                    <form name="jahiaAdmin"
                                          action='<%=JahiaAdministration.composeActionURL(request,response,"pages","&sub=process")%>'
                                          method="post">
                                        <table cellpadding="5" cellspacing="0" border="0" class="topAlignedTable">
                                            <tr>
                                                <td style="width:25%">
                                                    <fmt:message key="org.jahia.admin.pages.ManagePages.forThisPage.label"/>
                                                </td>
                                                <td>
                                                    <fmt:message key="org.jahia.admin.homepages.ManageHomepages.selectedpage.label"/>: <%=title %>
                                                    [<%=basePageID > 0 ? String.valueOf(basePageID) : "" %>]&nbsp;&nbsp;
                                                </td>
                                                <td>
                      <span class="dex-PushButton">
                        <span class="first-child">
                          <a class="ico-page-select"
                             href="javascript:<%=jData.gui().html().drawSelectPageLauncher(SelectPage_Engine.SELECT_ANY_PAGE_OPERATION, -1, -1, "setPid", jParams.getSiteID(), -1)%>"><fmt:message key="org.jahia.admin.selectAPage.label"/></a>
                        </span>
                      </span>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td>
                                                    <fmt:message key="org.jahia.admin.pages.ManagePages.useThisTemplate.label"/>
                                                </td>
                                                <td colspan="2">
                                                    <select class="input" name="templateid">
                                                        <option value="0">--&nbsp;&nbsp;<fmt:message key="org.jahia.admin.chooseATemplate.label"/>&nbsp;&nbsp;--&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
                                                        <%
                                                            while (allTemplatesIterator.hasNext()) {
                                                                JahiaPageDefinition pageDefinition = (JahiaPageDefinition) allTemplatesIterator.next();
                                                                pageContext.setAttribute("pageTemplate", pageDefinition);
                                                        %>
                                                        <option value="<%=pageDefinition.getID()%>"<%if (pageDefinition.getID() == baseTemplateID.intValue()) { %>
                                                                selected<%} %>
                                                                title="<fmt:message key='${pageTemplate.description}'/>">
                                                            <fmt:message key="${pageTemplate.displayName}"/></option>
                                                        <% } %>
                                                    </select>
                                                </td>
                                            </tr>
                                        </table>
                                        <input type="hidden" name="subaction" value=""><input type="hidden"
                                                                                              name="pageid"
                                                                                              value="<%=basePageID%>">
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
              <a class="ico-back"
                 href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="javascript:sendForm('save');"><fmt:message key="org.jahia.admin.saveChanges.label"/></a>
            </span>
          </span>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>
