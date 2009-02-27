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

<%@include file="/admin/include/header.inc"%>
<%@page import   = "java.util.*"%>
<%@page import="org.jahia.bin.*"%>

<%  Iterator  scriptsListInfos           = (Iterator) request.getAttribute("jahiaScriptsInfos");
    Iterator  scriptsListJavaScript      = (Iterator) request.getAttribute("jahiaScriptsJavaScript");
    Integer      jahiaDBWhichAction         = (Integer)     request.getAttribute("jahiaDBWhichAction");
    String       jahiaDBScript              = (String)      request.getAttribute("jahiaDBScript");
    String       jahiaDBDriver              = (String)      request.getAttribute("jahiaDBDriver");
    String       jahiaDBUrl                 = (String)      request.getAttribute("jahiaDBUrl");
    String       jahiaDBUsername            = (String)      request.getAttribute("jahiaDBUsername");
    String       jahiaDBPassword            = (String)      request.getAttribute("jahiaDBPassword");
    String       jahiaDBMinConnections      = (String)      request.getAttribute("jahiaDBMinConnections");
    String       jahiaDBMaxConnections      = (String)      request.getAttribute("jahiaDBMaxConnections");
    String       jahiaDBWaitIfBusy          = (String)      request.getAttribute("jahiaDBWaitIfBusy");
    String       jahiaDBVerbose             = (String)      request.getAttribute("jahiaDBVerbose");

    int          countJavaScript            = 0;
    String       jahiaWhichActionMessage    = "";
    boolean      displaySpecialSettings     = false;
    switch(jahiaDBWhichAction.intValue())
    {
        case 1 : displaySpecialSettings   = true;
                 break;
        case 2 :
                 break;
    }
%>
<!-- Supprim� du switch mais remplac� par autre switch (cf. N.B.) :
jahiaWhichActionMessage  = <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.message1.label"/>;
jahiaWhichActionMessage  = <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.message2.label"/>;
-->


<tr>
    <td align="center" class="text"><img name="db" src="<%=URL%>images/icons/admin/data.gif" width="48" height="48" border="0" align="middle"></td><td align="left" class="text"><h3><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.databaseSettings.label"/></h3></td>
</tr>
</table>
<br><br>

<script language="javascript">
    function changeDatabaseType()
    {
        <%
            while(scriptsListJavaScript.hasNext()) {
                Map jsHash  = (Map) scriptsListJavaScript.next();
            %>
        if(document.jahiaAdmin.dbtype.options[<%=countJavaScript%>].selected) {
            document.jahiaAdmin.dbdriver.value='<%=jsHash.get("jahia.database.driver")%>';
            document.jahiaAdmin.dburl.value='<%=JahiaTools.replacePattern((String)jsHash.get("jahia.database.url"), "\\", "\\\\")%>';
            document.jahiaAdmin.dbusername.value='<%=jsHash.get("jahia.database.user")%>';
            document.jahiaAdmin.dbpassword.value='<%=jsHash.get("jahia.database.pass")%>';
        }
            <%
                countJavaScript++;
            }
        %>
    /*
        if(document.jahiaAdmin.dbtype.options[<%=countJavaScript%>].selected) {
            document.jahiaAdmin.dbdriver.value='';
            document.jahiaAdmin.dburl.value='';
            document.jahiaAdmin.dbusername.value='';
            document.jahiaAdmin.dbpassword.value='';
        }
    */
    }
</script>

<table cellpadding="2" cellspacing="0" border="0" width="530">
<tr>
    <td colspan="2" width="530">&nbsp;</td>
</tr>
<% if(jahiaDBWhichAction.intValue() == 1) { %>
    <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"database","&sub=change")%>' method="post">
<% } else { %>
    <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"database","&sub=transfer")%>' method="post">
<% } %>
<tr>
    <td width="100">&nbsp;</td>
    <td width="430">
        <table border="0">
        <tr>
            <td nowrap>
                <font class="text"><!-- N.B. : " <%=jahiaWhichActionMessage%> " supprim� en m�me temps que le switch mais remplac� par : -->
                <%    switch(jahiaDBWhichAction.intValue())
                            { case 1 : %><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.message1.label"/><%
                                       break;
                              case 2 : %><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.message2.label"/><%
                                       break;
                            }
                %>
                </font>
                <br><br>
                <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.database.label"/>&nbsp;:</font><br>
                <select class="input" name="dbtype" onChange="changeDatabaseType()">
                    <%
                        boolean already_selected = false;
                        while(scriptsListInfos.hasNext()) {
                            Map scriptHash = (Map) scriptsListInfos.next();
                            if(jahiaDBScript.equals(scriptHash.get("jahia.database.script"))) {
                                already_selected = true;
                            }
                        %>
                    <option value='<%=scriptHash.get("jahia.database.script")%>' <%if(jahiaDBScript.equals(scriptHash.get("jahia.database.script"))){%>selected<%}%>><%=scriptHash.get("jahia.database.name")%></option>
                        <%
                        }
                    %>
                </select>
                <br>&nbsp;<br>
                <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.databaseDriver.label"/>&nbsp;:</font><br>
                <input class="input" type="text" name="dbdriver" size="<%=inputSize%>" maxlength="250" value="<%=jahiaDBDriver%>">
                <br>&nbsp;<br>
                <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.databaseUrl.label"/>&nbsp;:</font><br>
                <input class="input" type="text" name="dburl" size="<%=inputSize%>" maxlength="250" value="<%=jahiaDBUrl%>">
                <br>&nbsp;<br>
                <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.databaseUsername.label"/>&nbsp;:</font><br>
                <input class="input" type="text" name="dbusername" size="<%=inputSize%>" maxlength="250" value="<%=jahiaDBUsername%>">
                <br>&nbsp;<br>
                <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.databasePassword.label"/>&nbsp;:</font><br>
                <input class="input" type="password" name="dbpassword" size="<%=inputSize%>" maxlength="250" value="<%=jahiaDBPassword%>">
                <% if(displaySpecialSettings){ %>
                    <br>&nbsp;<br>
                    <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.databaseWaitIfBusy.label"/><br>
                    <input class="input" type="radio" name="dbwait" value="true" <%if(jahiaDBWaitIfBusy.equals("true")) {%>checked<%}%>> <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.yes.label"/> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <input class="input" type="radio" name="dbwait" value="false" <%if(jahiaDBWaitIfBusy.equals("false")) {%>checked<%}%>> <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.no.label"/>
                    </font>
                    <br>&nbsp;<br>
                    <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.databaseWriteInConsole.label"/>&nbsp;:<br>
                    <input class="input" type="radio" name="dbverbose" value="true" <%if(jahiaDBVerbose.equals("true")) {%>checked<%}%>> <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.yes.label"/> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <input class="input" type="radio" name="dbverbose" value="false" <%if(jahiaDBVerbose.equals("false")) {%>checked<%}%>> <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.no.label"/>
                    </font>
                    <!-- deactivated because we've changed the connection pool settings configuration
                    <br>&nbsp;<br>
                    <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.minimumConnections.label"/></font><br>
                    -->
                    <input class="input" type="hidden" name="dbminconnect" size="<%=inputSize%>" maxlength="250" value="<%=jahiaDBMinConnections%>">
                    <!-- deactivated because we've changed the connection pool settings configuration
                    <br>&nbsp;<br>
                    <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.database.ManageDatabase.maximumConnections.label"/></font><br>
                    -->
                    <input class="input" type="hidden" name="dbmaxconnect" size="<%=inputSize%>" maxlength="250" value="<%=jahiaDBMaxConnections%>">
                <% } %>
            </td>
        </tr>
        <tr>
            <td align="right">
                &nbsp;<br>
                <%if(!isLynx){%>
                    <a href="javascript:document.jahiaAdmin.submit();" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('save','','${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.saveChangeOn.button"/>',1)"><img name="save" src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.saveChangeOff.button"/>" width="114" height="17" border="0" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.saveChanges.label"/>"></a>
                <%}else{%>
                    &nbsp;<br>
                    <input type="submit" name="submit" value="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.saveChanges.label"/> >>">
                <%}%>
            </td>
        </tr>
        <tr>
            <td>
                &nbsp;<br><br>
                <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                    <td nowrap width="145" valign="top"><font class="text"><b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.otherOperations.label"/>&nbsp;:&nbsp;&nbsp;&nbsp;</b></font></td>
                    <td valign="top">
                        <font class="text">
                        <%if(!isLynx){%><li> <%}%><a href='<%=JahiaAdministration.composeActionURL(request,response,"database","&sub=display")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.previousStep.label"/></a><br>
                        <%if(!isLynx){%><li> <%}%><a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/></a><br>
                        </font>
                    </td>
                </tr>
                </table>
            </td>
        </tr>
        </table>
    </td>
</tr>
   </form>
<tr>
    <td colspan="2" align="right">
        <table border="0" width="100%"><tr><td width="48"><img name="logo" src="../css/images/logo/logo-jahia.gif" border="0" width="45" height="34"></td><td><img src="<%=URL%>images/pix.gif" border="0" width="1" height="10">
<div id="copyright"><%=copyright%></div><span class="version">Jahia <%=Jahia.VERSION%>.<%=Jahia.getPatchNumber()%> r<%=Jahia.getBuildNumber()%></span>
</td></tr></table>
    </td>
</tr>

</table>

<%@include file="/admin/include/footer.inc"%>