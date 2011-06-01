<%@page import="org.jahia.bin.*"%>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>   <% // http files path. %>
<%@taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<!-- include file menu -->
<form name="adminMenu" action="" method="post">
    <div align="right">
        <table width="450" border="0" cellspacing="0">
            <tr>
                <td>
                    <!-- <a href='<%=JahiaAdministration.composeActionURL(request,response,"pages","&sub=display")%>' -->
                    <a href="../noGUI.html"
                       onMouseOut="MM_swapImgRestore()"
                       onMouseOver="MM_swapImage('pages','','<%=URL%>/images/icons/admin_pages_on.gif',1)">
                       <img align="left" name="pages"
                            src="<%=URL%>/images/icons/admin_pages_off.gif" width="16" height="16" border="0"
                            alt="<fmt:message key="org.jahia.admin.managePages.label"/>"></a>
                </td>
                <td>
                    <!-- <a href='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=display")%>' -->
                    <a href="../user_management/user_management.html"
                       onMouseOut="MM_swapImgRestore()"
                       onMouseOver="MM_swapImage('users','','<%=URL%>/images/icons/admin_users_on.gif',1)">
                       <img align="left" name="users"
                             src="<%=URL%>/images/icons/admin_users_off.gif" width="16" height="16" border="0"
                             alt="<fmt:message key="label.manageUsers"/>"></a>
                </td>
                <td>
                    <!-- <a href='<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=display")%>' -->
                    <a href="../group_management/group_management.html"
                       onMouseOut="MM_swapImgRestore()"
                       onMouseOver="MM_swapImage('groups','','<%=URL%>/images/icons/admin_groups_on.gif',1)">
                       <img align="left" name="groups"
                             src="<%=URL%>/images/icons/admin_groups_off.gif" width="16" height="16" border="0"
                             alt="<fmt:message key="org.jahia.admin.manageUserGroup.label"/>"></a>
                </td>
                <td>
                    <!-- <a href='<%=JahiaAdministration.composeActionURL(request,response,"components","&sub=display")%>' -->
                    <a href="../noGUI.html"
                       onMouseOut="MM_swapImgRestore()"
                       onMouseOver="MM_swapImage('components','','<%=URL%>/images/icons/admin_components_on.gif',1)">
                       <img align="left" name="components"
                             src="<%=URL%>/images/icons/admin_components_off.gif" width="16" height="16" border="0"
                             alt="<fmt:message key="org.jahia.admin.manageComponents.label"/>"></a>
                </td>
                <td>
                    <!-- <a href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=display")%>' -->
                    <a href="../noGUI.html"
                       onMouseOut="MM_swapImgRestore()"
                       onMouseOver="MM_swapImage('templates','','<%=URL%>/images/icons/admin_templates_on.gif',1)">
                       <img align="left" name="templates"
                             src="<%=URL%>/images/icons/admin_templates_off.gif" width="16" height="16" border="0"
                             alt="<fmt:message key="org.jahia.admin.manageTemplates.label"/>"></a>
                </td>
                <td>
                    <!-- <a href='<%=JahiaAdministration.composeActionURL(request,response,"site","&sub=display")%>' -->
                    <a href="../site_settings/site_settings.html"
                       onMouseOut="MM_swapImgRestore()"
                       onMouseOver="MM_swapImage('site','','<%=URL%>/images/icons/admin_site_on.gif',1)">
                       <img align="left" name="site"
                             src="<%=URL%>/images/icons/admin_site_off.gif" width="16" height="16" border="0"
                             alt="<fmt:message key="org.jahia.admin.siteSettings.label"/>"></a>
                </td>
            </tr>
            <tr>
                <td colspan="6"><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.hr.image"/>" width="400" height="2"></td>
            </tr>
        </table>
    </div>
</form>
<!-- -->