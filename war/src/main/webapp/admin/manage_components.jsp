<%@ page import="org.jahia.bin.JahiaAdministration" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ include file="/admin/include/header.inc" %>
<% stretcherToOpen = 0; %>
<script type="text/javascript">
    var portletDeployment =  {
        formActionUrl: "<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=prepareDeployPortlet")%>",
        autoDeploySupported: "${autoDeploySupported}",
        appserverDeployerUrl: "${appserverDeployerUrl}"
    }
</script>
<internal:gwtGenerateDictionary/>        
<internal:gwtImport module="org.jahia.ajax.gwt.module.contentmanager.ContentManager" /><div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.manageComponents.label"/></h2>
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
            <td style="vertical-align: top;" align="left">
                <div class="dex-TabPanelBottom">
                    <div class="tabContent">
                        <jsp:include page="/admin/include/left_menu.jsp">
                            <jsp:param name="mode" value="server"/>
                        </jsp:include>
                        <div id="content" class="fit">
                            <div class="head headtop">
                                <div class="object-title">
                                    <fmt:message key="org.jahia.admin.manageComponents.label"/>
                                </div>
                            </div>
                            <div class="content-item-noborder">
                                <internal:contentManager embedded="true" conf="portletdefinitionmanager"/>
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
              <a class="ico-back"
                 href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
            </span>
          </span>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>