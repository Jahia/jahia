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

<%@ page language="java" %>
<%@ page import="java.util.*" %>

<%@include file="/views/engines/common/taglibs.jsp" %>
<!-- actionBar (start) -->
<div id="actionBar">
  <span class="dex-PushButton">
    <span class="first-child">
      <a class="ico-back" href="javascript:sendForm('showContainersList','')">
        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.backToStep"/>&nbsp;1&nbsp;:&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.engines.version.showContainersList" defaultValue="Show containers list" /></a>
    </span>
  </span>
  <span class="dex-PushButton">
    <span class="first-child">
      <a href="javascript:sendForm('restoreSave','');" class="ico-ok" title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.altApplyAndClose.label"/>" onclick="setWaitingCursor(1);">
              <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.button.ok"/></a>
    </span>
  </span>
  <span class="dex-PushButton">
    <span class="first-child">
      <a href="javascript:sendForm('restoreApply','');" class="ico-apply" title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.altApplyWithoutClose.label"/>" onclick="setWaitingCursor(1);">
        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.button.apply"/></a>
    </span>
  </span>
  <span class="dex-PushButton">
    <span class="first-child">
      <a href="javascript:window.close();" class="ico-cancel" title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.altCloseWithoutSave.label"/>">
        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.button.cancel"/></a>
    </span>
  </span>
</div>
<!-- actionBar (end) -->