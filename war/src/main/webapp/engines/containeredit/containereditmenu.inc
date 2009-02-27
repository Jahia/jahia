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
--%><%@ page import="org.jahia.data.containers.ContainerEditView" %>
<%@ page import="org.jahia.data.containers.ContainerEditViewFieldGroup" %>
<%@ page import="org.jahia.data.fields.JahiaField" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="org.jahia.engines.validation.EngineValidationHelper"%>
<%@ page import="org.jahia.registries.ServicesRegistry"%>
<%@ page import="org.jahia.params.ParamBean"%>
<%@ page import="org.jahia.services.acl.JahiaACLManagerService"%>
<%@ page import="org.jahia.services.acl.JahiaBaseACL"%>
<%@ page import="org.jahia.services.lock.LockPrerequisitesResult"%>
<%@ page import="org.jahia.services.lock.LockKey"%>
<%@ page import="org.jahia.services.lock.LockPrerequisites"%>

<%
    final ContainerEditView editView;
    int pageDefinitionID = jParams.getPage().getPageTemplateID();

    if (feh != null) {
      editView = feh.getContainerEditView();
    } else {
      editView = null;
    }

    JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
    final ParamBean paramBean = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult((LockKey) engineMap.get("LockKey"));
    final boolean readOnlyMode;
    if (results == null) {
      results = new LockPrerequisitesResult();
      readOnlyMode = false;
    } else {
      readOnlyMode = true;
    }

    Boolean adminAccess = (Boolean) engineMap.get("adminAccess");
    if (adminAccess == null) {
      adminAccess = Boolean.FALSE;
    }

    Boolean enableAuthoring = (Boolean) engineMap.get("enableAuthoring");
    if (enableAuthoring == null) {
      enableAuthoring = Boolean.FALSE;
    }
    if (aclService.getSiteActionPermission(LockPrerequisites.EDIT, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 || results.getDisabledTabs().contains(LockPrerequisites.EDIT)) {
      enableAuthoring = Boolean.FALSE;
    }

    Boolean enableMetadata = (Boolean) engineMap.get("enableMetadata");
    if (enableMetadata == null) {
      enableMetadata = Boolean.FALSE;
    }
    if (aclService.getSiteActionPermission(LockPrerequisites.METADATA, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 || results.getDisabledTabs().contains(LockPrerequisites.METADATA)) {
      enableMetadata = Boolean.FALSE;
    }

    Boolean enableRightView = (Boolean) engineMap.get("enableRightView");
    if (enableRightView == null) {
      enableRightView = Boolean.FALSE;
    }
    if (aclService.getSiteActionPermission(LockPrerequisites.RIGHTS, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 || results.getDisabledTabs().contains(LockPrerequisites.RIGHTS)) {
      enableRightView = Boolean.FALSE;
    }

    Boolean enableTimeBasedPublishing = (Boolean) engineMap.get("enableTimeBasedPublishing");
    if (enableTimeBasedPublishing == null) {
      enableTimeBasedPublishing = Boolean.FALSE;
    }
    if (aclService.getSiteActionPermission(LockPrerequisites.TIME_BASED_PUBLISHING, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 || results.getDisabledTabs().contains(LockPrerequisites.TIME_BASED_PUBLISHING)) {
      enableTimeBasedPublishing = Boolean.FALSE;
    }

    Boolean doAddEditViewRight = (Boolean) engineMap.get("doAddEditViewRight");
    if (doAddEditViewRight == null) {
      doAddEditViewRight = Boolean.FALSE;
    }
    if (aclService.getSiteActionPermission(LockPrerequisites.FIELD_RIGHTS, paramBean.getUser(), JahiaBaseACL.READ_RIGHTS, paramBean.getSiteID()) <= 0 || results.getDisabledTabs().contains(LockPrerequisites.FIELD_RIGHTS)) {
      doAddEditViewRight = Boolean.FALSE;
    }
%>
<!-- menu containeredit (start) -->
<div class="leftMenu">
  <ul>
  <% if (editView != null && enableAuthoring.booleanValue()) { %>
    <% if (theScreen.equals("edit")) { %>
    <li class="section first">
      <a href="#" onclick="return false;" class="open">
        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.Authoring.label"/>
        <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.EDIT) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %></a>
    </li>
    <%
      ContainerEditViewFieldGroup fieldGroup;
      JahiaField firstField;
      if (theField != null) {
        final ContainerEditViewFieldGroup currentFieldGroup = editView.getFieldGroupByFieldName(theField.getDefinition().getName());
        final List v = new ArrayList(editView.getViews().values());
        final ContainerEditViewFieldGroup dummyFieldGroup = new ContainerEditViewFieldGroup("", "", "");
        Collections.sort(v, dummyFieldGroup);

        final Iterator it = v.iterator();
        while (it.hasNext()) {
          fieldGroup = (ContainerEditViewFieldGroup) it.next();
          firstField = feh.getField((String) fieldGroup.getFieldNames().get(0));
          final Iterator fieldNames = fieldGroup.getFieldNames().iterator();
          boolean isCurrentFieldGroup = (fieldGroup.getName().equals(currentFieldGroup.getName()));

          while (fieldNames.hasNext()) {
            final JahiaField currField = feh.getField((String) fieldNames.next());
            final int type = currField.getType();
            final String className = getIconClassName(type, true);
            final boolean isMandatory = EngineValidationHelper.isFieldMandatory(theContainer,  currField, jParams);
            final String name = currField.getDefinition().getTitle(elh.getCurrentLocale());
            if (isCurrentFieldGroup) {%>
              <li class="item">
                <div class="<%=className%>">
                  <a href="#" onclick="return false;" class="selected"><%=name%>
                    <% if (isMandatory) { %><span class="error">(<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.required.label"/>)</span><% } %>
                  </a>
                </div>
              </li>
            <% } else { %>
              <li class="item">
                <div class="<%=className%>">
                  <a href="javascript:changeField('<%=firstField.getID()%>');">
                    <%=name != null ? name.trim() : ""%><% if (isMandatory) { %><span class="error">(<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.required.label"/>)</span><% } %></a>
                </div>
              </li>
            <%}
          }
        }
      } %>
    <% } else { %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('edit'); return false;" class="closed">
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.Authoring.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.EDIT) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        </a>
      </li>
    <% } %>
  <% } %>
  <% if (enableMetadata.booleanValue()) { %>
    <% if (theScreen.equals("metadata")) { %>
    <li class="section first">
      <a href="#" onclick="return false;" class="open">
        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.Metadata.label"/>
        <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.METADATA) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
      </a>
    </li>
    <%
      ContainerEditViewFieldGroup fieldGroup;
      JahiaField firstField;
      final ContainerEditViewFieldGroup currentFieldGroup = editView.getFieldGroupByFieldName(theField.getDefinition().getName());

      final List v = new ArrayList(editView.getViews().values());
      final ContainerEditViewFieldGroup dummyFieldGroup = new ContainerEditViewFieldGroup("", "", "");
      Collections.sort(v, dummyFieldGroup);

      final Iterator it = v.iterator();
      while (it.hasNext()) {
        fieldGroup = (ContainerEditViewFieldGroup) it.next();
        firstField = feh.getField((String) fieldGroup.getFieldNames().get(0));
        final Iterator fieldNames = fieldGroup.getFieldNames().iterator();
        boolean isCurrentFieldGroup = (fieldGroup.getName().equals(currentFieldGroup.getName()));

        while (fieldNames.hasNext()) {
          final JahiaField currField = feh.getField((String) fieldNames.next());
          final int type = currField.getType();
          final String className = getIconClassName(type, true);
          final String name = currField.getDefinition().getTitle(
                  elh.getCurrentLocale());
          if (isCurrentFieldGroup) {
          %>

          <li class="item"><div class="<%=className%>"><a href="#" onclick="return false;" class="selected"><%=name%></a></div></li>
          <% } else { %>

          <li class="item"><div class="<%=className%>">
              <a href="javascript:changeField('<%=firstField.getID()%>');"><%=name%></a></div>
          </li>
          <%
          }
        }
      } %>
    <% } else { %>
      <li class="section first">
        <a href="#" onclick="handleActionChange('metadata'); return false;" class="closed">
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.Metadata.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.METADATA) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %></a>
      </li>
    <% } %>
  <% } %>
  <% if (enableRightView.booleanValue() && adminAccess.booleanValue()) { %>
    <li class="section first">
      <a href="#" onclick="handleActionChange('rightsMgmt'); return false;" class="single">
        <% if (theScreen.equals("rightsMgmt")) { %>
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.RightsMgmt.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.RIGHTS) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        <% } else { %>
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.RightsMgmt.label"/>
            <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.RIGHTS) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        <% } %>
      </a>
    </li>
  <% } %>
  <% if (doAddEditViewRight.booleanValue() && adminAccess.booleanValue()) { %>
    <li class="section first">
      <a href="#" onclick="handleActionChange('ctneditview_rights'); return false;" class="single">
        <% if (theScreen.equals("ctneditview_rights")) { %>
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.FieldsRightsMgmt.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.FIELD_RIGHTS) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        <% } else { %>
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.FieldsRightsMgmt.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.FIELD_RIGHTS) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        <% } %>
      </a>
    </li>
  <% } %>
  <% if (enableTimeBasedPublishing.booleanValue()) { %>
    <li class="section first">
      <a href="#" onclick="handleActionChange('timeBasedPublishing'); return false;" class="single">
        <% if (theScreen.equals("timeBasedPublishing")) { %>
          <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.TimeBasedPublishing.label"/>
          <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.TIME_BASED_PUBLISHING) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div><% } %>
        <% } else { %>
            <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.include.actionSelector.TimeBasedPublishing.label"/>
            <% if (readOnlyMode && results.getReadOnlyTabs().contains(LockPrerequisites.TIME_BASED_PUBLISHING) || results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT)) { %> <div class="transparentLock">&nbsp;</div> <% } %>
        <% } %>
      </a>
    </li>
  <% } %>
  </ul>
</div>
<!-- menu containeredit (end) -->