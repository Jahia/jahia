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
<%@ page import="org.jahia.services.lock.LockKey" %>
<%if (blockingLockKey.getType().startsWith(LockKey.WORKFLOW_ACTION)) {%>
  <fmt:message key="org.jahia.engines.lock.theObject.label"/> <%=blockingLockKey.getName()%> [ID: <b><%=blockingLockKey.getId()%></b>]
  <fmt:message key="org.jahia.engines.lock.lockedByWorkflow.label"/>
<%} else if (LockKey.WAITING_FOR_APPROVAL_TYPE.equals(blockingLockKey.getType())) {
    canDisplayDetails = false; // This is a permanent lock, no needs to display details.
%>
  <fmt:message key="org.jahia.engines.lock.parentPage.label"/> [<fmt:message key="org.jahia.engines.lock.page.label"/> ID : <b><%=blockingLockKey.getId()%></b>]
  <fmt:message key="org.jahia.engines.lock.waitingForApproval.label"/>
<%} else if (LockKey.ADD_CONTAINER_TYPE.equals(blockingLockKey.getType())) {%>
  <fmt:message key="org.jahia.engines.lock.containerTocontentContainer.label"/> [ID : <b><%=blockingLockKey.getId()%></b>].
<%} else if (LockKey.UPDATE_CONTAINERLIST_TYPE.equals(blockingLockKey.getType())) {%>
  <fmt:message key="org.jahia.engines.lock.theContainerList.label"/>
  [<fmt:message key="org.jahia.engines.lock.containerList.label"/> ID : <b><%=blockingLockKey.getId()%></b>]
  <fmt:message key="org.jahia.engines.lock.toBeEdited.label"/>
<%} else if (LockKey.UPDATE_PAGE_TYPE.equals(blockingLockKey.getType())) {%>
  <fmt:message key="org.jahia.engines.lock.objectParentPageProperties.label"/>
  [<fmt:message key="org.jahia.engines.lock.page.label"/> ID : <b><%=blockingLockKey.getId()%></b>]
  <fmt:message key="org.jahia.engines.lock.toBeEdited.label"/> <fmt:message key="org.jahia.engines.lock.pageTemplateCanChange.label"/>
<%} else if (LockKey.UPDATE_CONTAINER_TYPE.equals(blockingLockKey.getType())) {%>
  <fmt:message key="org.jahia.engines.lock.theContainer.label"/> [<fmt:message key="org.jahia.engines.lock.container.label"/> ID : <b><%=blockingLockKey.getId()%></b>]
  <fmt:message key="org.jahia.engines.lock.isEditionMode.label"/>
<%} else if (LockKey.DELETE_CONTAINER_TYPE.equals(blockingLockKey.getType())) {%>
  <fmt:message key="org.jahia.engines.lock.theContainer.label"/> [<fmt:message key="org.jahia.engines.lock.container.label"/> ID : <b><%=blockingLockKey.getId()%></b>]
  <fmt:message key="org.jahia.engines.lock.willBeDeleted.label"/>
<%} else if (LockKey.UPDATE_FIELD_TYPE.equals(blockingLockKey.getType())) {%>
  <fmt:message key="org.jahia.engines.lock.theField.label"/> [<fmt:message key="org.jahia.engines.lock.field.label"/> ID : <b><%=blockingLockKey.getId()%></b>]
  <fmt:message key="org.jahia.engines.lock.isEditionMode.label"/>
<%} else if (blockingLockKey.getType().startsWith(LockKey.IMPORT_ACTION)) {%>
  <fmt:message key="org.jahia.engines.lock.copy.label"/> [ID : <b><%=blockingLockKey.getId()%></b>].
<%} else if (blockingLockKey.getType().startsWith(LockKey.EXPORT_ACTION)) {%>
  <fmt:message key="org.jahia.engines.lock.copy.label"/> [ID : <b><%=blockingLockKey.getId()%></b>].
<%} else if (blockingLockKey.getType().startsWith(LockKey.LIVEEXPORT_ACTION)) {%>
  <fmt:message key="org.jahia.engines.lock.copy.label"/> [ID : <b><%=blockingLockKey.getId()%></b>].
<%}%>