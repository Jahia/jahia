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