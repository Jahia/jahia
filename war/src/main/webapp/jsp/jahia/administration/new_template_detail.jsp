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

<%@include file="/jsp/jahia/administration/include/header.inc"%>
<%@page import   = "java.util.*,org.jahia.data.templates.*"%>
<%@page import="org.jahia.bin.*"%>


<%

    String theURL = "";
    JahiaTemplatesPackage aPackage = (JahiaTemplatesPackage)request.getAttribute("aPackage");

    String requestURI = (String)request.getAttribute("requestURI");
    String contextRoot = (String)request.getContextPath();
    Integer templateLimit = (Integer)request.getAttribute("templateLimit");
    Boolean canDeploy = (Boolean)request.getAttribute("canDeploy");

%>


<script language="javascript">

        function sendForm(theAction)
        {
            document.mainForm.subaction.value = theAction;
            document.mainForm.submit();
        }

</script>

<tr>
    <td align="center" class="text"><img name="template" src="<%=URL%>images/icons/admin/briefcase_document.gif" width="48" height="48" border="0" align="middle"></td><td align="left" class="text"><br><h3><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.manageTemplates.label"/><br><% if ( currentSite!= null ){%><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName()%><%}%></h3></td>
</tr>
</table>
<br>

<table cellpadding="2" cellspacing="0" border="0">
<% if ( !canDeploy.booleanValue() ) { %>
<tr>
    <td colspan="2" width="530">&nbsp;</td>
</tr>
<tr>
    <td width="150">&nbsp;&nbsp;&nbsp;</td>
    <td width="100%">
        <table border="0" cellpadding="0" width="84%">
        <tr>
            <td colspan="2" class="text" align="left"><b></b><br><br>&nbsp;</td>
        </tr>

        <tr>
            <td colspan="2" class="text" align="left"><b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.licenseLimitation.label"/>&nbsp;:</b><br><br>&nbsp;</td>
        </tr>
        <tr>
            <td valign="top" align="left" colspan="2" class="text">
                <b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.templateSetCannotBeDeployed.label"/> <%=templateLimit.intValue()%>&nbsp;.</b>
            </td>
        </tr>
        <tr>
            <td colspan="2"><br><br></td>
        </tr>
        </table>
    </td>
</tr>
<% } %>
<tr>
    <td colspan="2" height="10">&nbsp;</td>
</tr>
<tr>
    <td width="100">&nbsp;&nbsp;&nbsp;</td>
    <td width="100%">
        <form name="mainForm" action="<%=requestURI%>?do=templates&sub=details&package_name=<%=aPackage.getFileName()%>" method="post">
        <table border="0"  cellpadding="0">

        <tr>
            <td nowrap valign="top" align="left">
                <font class="text">
                <b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.templatesDetails.label"/>&nbsp;:</b><br>
                </font>
            </td>
            <td>
            </td>
        </tr>
        <tr>
            <td><br></td>
            <td></td>
        </tr>
        <tr>
            <td colspan="2" width="100%">
                <table border="0" cellpadding="0" cellspacing="0" width="100%">
                <tr>
                    <td valign="top" align="left" nowrap>
                        <font class="text"><b><% if (!aPackage.isDirectory()) { %><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.file.label"/><% } else { %><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.directoryName.label"/><% } %></b></font>
                    </td>
                    <td valign="top" align="left">
                        <font class="text"><b>&nbsp;:&nbsp;<% if ( aPackage.isDirectory() ) {%>/<%} %><%=aPackage.getFileName()%></b></font>
                    </td>
                </tr>
                <tr>
                    <td height="10"></td>
                    <td></td>
                </tr>
                <tr>
                    <td valign="top" align="left" nowrap>
                        <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.templateRootFolder.label"/></font>
                    </td>
                    <td valign="top" align="left">
                        <font class="text">&nbsp;:&nbsp;/<%=aPackage.getRootFolder()%></font>
                    </td>
                </tr>
                <tr>
                    <td><br><br></td>
                    <td></td>
                </tr>
                <tr>
                    <td class="text"align="right" nowrap><b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.listOfTemplates.label"/></b></td>
                    <td><b>&nbsp;:</b></td>
                </tr>
                <tr>
                    <td><br></td>
                    <td></td>
                </tr>

                <%
                    JahiaTemplateDef def = null;
                    List templates = aPackage.getTemplates();
                    int size = templates.size();
                    int count = 0;

                    if ( size>0 ){

                        for ( int i=0 ; i<size ; i++ ) {

                            def = (JahiaTemplateDef)templates.get(i);
                            if ( def != null ){
                                count +=1;

                    %>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.templateName.label"/></font>
                        </td>
                        <td width="90%">
                            <font class="text">&nbsp;:&nbsp;<b><%=def.getName()%></b></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.displayName.label"/></font>
                        </td>
                        <td >
                            <font class="text">&nbsp;:&nbsp;<%=def.getDisplayName()%></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.templateFile.label"/></font>
                        </td>
                        <td >
                            <font class="text">&nbsp;:&nbsp;<%=def.getFileName()%></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" height="10"></td>
                    </tr>
                    <tr>
                        <td colspan="2" height="10"></td>
                    </tr>
                    <%

                            }
                        }
                    }

                    if ( count==0 ){

                    %>
                    <tr>
                        <td valign="top" align="right" nowrap colspan="2">
                            <font class="text"><b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.noTemplateFoundWithinPackage.label"/></b></font>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2"><br></td>
                    </tr>
                    <% }

                    %>
                    </table>
            </td>
        </tr>
        <tr>
            <td><br><br><br></td>
            <td></td>
        </tr>
        <tr>
            <td></td>
            <td align="right">
                &nbsp;<br>
                <input type="hidden" name="subaction" value="">
                <a href="javascript:sendForm('delete');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('delete','','${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.deleteOn.button"/>',1)"><img name="delete" src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.deleteOff.button"/>" width="69" height="17" border="0" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.delete.label"/>"></a>

                <% if ( canDeploy.booleanValue() ) { %>
                <a href="javascript:sendForm('deploy');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('deploy','','${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.deployOn.button"/>',1)"><img name="deploy" src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.deployOff.button"/>" width="69" height="17" border="0" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.deploy.label"/>"></a>
                <% } %>
                <br>
            </td>
        </tr>
        <tr>
            <td><br></td>
            <td></td>
        </tr>
        <tr>
            <td colspan="2">
                &nbsp;<br><br>
                <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td nowrap width="145" valign="top"><font class="text"><b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.otherOperations.label"/>&nbsp;:&nbsp;&nbsp;&nbsp;</b></font></td>
                    <td valign="top">
                        <font class="text">
                        <%if(!isLynx){%><li> <%}%><a href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=displaynewlist")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.backToNewTemplatesList.label"/></a><br>
                        <%if(!isLynx){%><li> <%}%><a href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=display")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.backToTemplatesList.label"/></a><br>
                        <%if(!isLynx){%><li> <%}%><a href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=add")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.manuallyAddTemplate.label"/></a><br>
                        <%if(!isLynx){%><li> <%}%><a href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=options")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.templates.ManageTemplates.templatesManagementOptions.label"/></a><br>
                        <%if(!isLynx){%><li> <%}%><a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/></a><br>
                        </font>
                    </td>
                </tr>
                </table>
            </td>
        </tr>

        </table>
        </form>
    </td>
</tr>
<tr>
    <td colspan="2" align="right">
        <table border="0" width="100%"><tr><td width="48"><img name="logo" src="../jsp/jahia/css/images/logo/logo-jahia.gif" border="0" width="45" height="34"></td><td><img src="<%=URL%>images/pix.gif" border="0" width="1" height="10">
<div id="copyright"><%=copyright%></div><span class="version">Jahia <%=Jahia.VERSION%>.<%=Jahia.getPatchNumber()%> r<%=Jahia.getBuildNumber()%></span>
</td></tr></table>
    </td>
</tr>

</table>

<%@include file="/jsp/jahia/administration/include/footer.inc"%>