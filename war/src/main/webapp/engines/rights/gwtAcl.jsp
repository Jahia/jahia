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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.jahia.data.fields.JahiaFieldDefinition" %>
<%@ page import="org.jahia.data.viewhelper.principal.PrincipalViewHelper" %>
<%@ page import="org.jahia.engines.rights.ManageRights"%>
<%@ page import="org.jahia.params.*" %>
<%@ page import="org.jahia.resourcebundle.*" %>
<%@ page import="org.jahia.services.usermanager.JahiaGroup" %>
<%@ page import="org.jahia.services.usermanager.JahiaUser" %>
<%@ page import="org.jahia.utils.JahiaString" %>
<%@ page import="org.jahia.utils.JahiaTools" %>
<%@ page import="java.security.Principal" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper"%>
<%@ page import="org.jahia.engines.JahiaEngine"%>
<%@ page import="java.util.Map" %>
<%@ page import="org.jahia.services.acl.JahiaBaseACL" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.jahia.data.fields.JahiaFieldDefinition" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jahia.engines.EngineLanguageHelper" %>
<%@ page import="org.jahia.engines.JahiaEngine" %>
<%@ page import="org.jahia.params.ParamBean" %>
<%@ taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<%
    final Map engineMap = (Map) request.getAttribute("org.jahia.engines.EngineHashMap");
    final String theScreen = (String) engineMap.get("screen");
    final ParamBean jParams   = (ParamBean) request.getAttribute("org.jahia.params.ParamBean" );

    Boolean showSiteKey = (Boolean) engineMap.get("showSiteKey");
    if(showSiteKey==null) {
        showSiteKey = Boolean.FALSE;
    }
    final Integer userNameWidth = new Integer(showSiteKey.booleanValue()?10:15);
    request.getSession().setAttribute("userNameWidth", userNameWidth);
    EngineLanguageHelper elh = (EngineLanguageHelper) engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
    if (elh != null) {
        jParams.setCurrentLocale(elh.getCurrentLocale());
    }

    JahiaBaseACL acl = (JahiaBaseACL) engineMap.get("acl");
    int aclId = acl.getID();

    boolean newAcl = (Boolean) engineMap.get("newAcl");
    String sessionId = (String) engineMap.get("sessionIdentifier");

    Boolean readOnly = (Boolean) engineMap.get("readOnly");
    if (readOnly ==  null ) {
        readOnly = false;
    }

%>

<div class="dex-TabPanelBottom">
  <div class="tabContent">
    <%@ include file="../menu.inc" %>
    <div id="content" class="fit w2">
      <div class="head">
        <div class="object-title"><fmt:message key="org.jahia.engines.include.actionSelector.RightsMgmt.label"/></div>
      </div>
      <logic:present name="manageRights.warning.engineMessages">
        <p class="errorbold"><fmt:message key="org.jahia.engines.shared.BigText_Field.warning.label"/>:</p>
        <ul>
          <logic:iterate name="manageRights.warning.engineMessages" property="messages" id="curMessage">
            <li><internal:message name="curMessage"/></li>
          </logic:iterate>
        </ul>
        <input type="checkbox" name="ignoreWarnings">&nbsp;<fmt:message key="org.jahia.engines.shared.BigText_Field.ignoreWarning.label"/>
      </logic:present>

             <%
      final Map fieldAcls = (Map) engineMap.get("fieldAcls");
      final Set fieldnames;
      if (fieldAcls != null) {
        fieldnames = fieldAcls.keySet();
      } else {
        fieldnames = null;
      }

      if (theScreen.equals("ctneditview_rights") && (fieldnames != null)) {
        final List fields = (List) engineMap.get("fields");
        final int pageDefID = jParams.getPage().getPageTemplateID();
        final String aclFieldName = (String) engineMap.get("aclfieldname");
      %>
        <table class="formTable" cellpadding="0" cellspacing="1" border="0" width="100%">
          <tr>
            <th width="100">
              <fmt:message key="org.jahia.engines.containerlistproperties.ContainerListProperties_Engine.currentFiled.label"/>:&nbsp;
            </th>
            <td>
              <select name="aclfieldname" onchange="handleActionChanges('<%=theScreen%>','&aclfieldname='+this.options[this.options.selectedIndex].value)">
                <% for (int i = 0; i < fields.size(); i++) { String name = ((JahiaFieldDefinition) fields.get(i)).getName(); %>
                  <option value="<%=name%>" <% if (aclFieldName.equals(name)) { %> selected="selected" <% } %>>
                    <%=((JahiaFieldDefinition)fields.get(i)).getTitle(jParams.getLocale())%>
                  </option>
                <% } %>
              </select>
            </td>
          </tr>
        </table>
      <%}%>

        <script type="text/javascript">
            function sendFormApply() {
                saveAcl("realSendFormApply");
            }
            function sendFormSave() {
                saveAcl("realSendFormSave");
            }
            function realSendFormApply() {
                if (check() && submittedCount == 0) {
                    if (jahia.config.lockResults) {
                        var button = document.getElementById("applyButton");
                        if (button) button.innerHTML = "<div class='clicked'>" + jahia.config.i18n['org.jahia.button.apply'] + "</div>";
                        delete button;
                    }
                    document.mainForm.screen.value = "apply";
                    saveContent();
                    teleportCaptainFlam(document.mainForm);
                }
            }
            function realSendFormSave() {
                if (check() && submittedCount == 0) {
                    var button = document.getElementById("saveButton");
                    if (button) button.innerHTML = "<div class='clicked'>" + jahia.config.i18n['org.jahia.button.ok'] + "</div>";
                    delete button;
                    document.mainForm.screen.value = "save";
                    saveContent();
                    teleportCaptainFlam(document.mainForm);
                }
            }
        </script>
        <internal:gwtImport module="org.jahia.ajax.gwt.module.acleditor.ACLEditor" />
        <internal:aclEditor aclId="<%= aclId %>" newAcl="<%= newAcl %>" sessionIdentifier="<%= sessionId %>" readOnly="<%= readOnly %>"/>

    </div>
  </div>
</div>
