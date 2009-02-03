<%--
Copyright 2002-2006 Jahia Ltd

Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
Version 1.0 (the "License"), or (at your option) any later version; you may
not use this file except in compliance with the License. You should have
received a copy of the License along with this program; if not, you may obtain
a copy of the License at

 http://www.jahia.org/license/

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>

<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ attribute name="aclId" required="false" rtexprvalue="true" type="java.lang.Integer" description="text" %>
<%@ attribute name="newAcl" required="false" rtexprvalue="true" type="java.lang.Boolean" description="text" %>
<%@ attribute name="sessionIdentifier" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="readOnly" required="false" rtexprvalue="true" type="java.lang.Boolean" description="text" %>

<template:gwtJahiaModule id="gwtacleditor" jahiaType="gwtacleditor" aclid="<%= aclId %>" readOnly="<%= readOnly != null ? readOnly : Boolean.FALSE.toString() %>"
                         newAcl="<%= newAcl %>" sessionIdentifier="<%= sessionIdentifier %>" templateUsage="false">

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.principal.label"
                                     aliasResourceName="ae_principal"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreInheritance.label"
                                     aliasResourceName="ae_restore_inheritance"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.inheritedFrom.label" 
                                     aliasResourceName="ae_inherited_from"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restoreAllInheritance.label"
                                     aliasResourceName="ae_restore_all_inheritance"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.breakAllInheritance.label"
                                     aliasResourceName="ae_break_all_inheritance"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.remove.label"
                                     aliasResourceName="ae_remove"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.save.label"
                                     aliasResourceName="ae_save"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.rights.ManageRights.restore.label"
                                     aliasResourceName="ae_restore"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newUsers.label"
                                     aliasResourceName="um_adduser"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.users.SelectUG_Engine.newGroups.label"
                                     aliasResourceName="um_addgroup"/>

</template:gwtJahiaModule>
