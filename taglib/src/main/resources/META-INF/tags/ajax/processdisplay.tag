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

<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ attribute name="state" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="windowHeight" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="windowWidth" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>

<template:gwtJahiaModule id="pdisplay" jahiaType="pdisplay">
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newDir.label"
                                     aliasResourceName="fm_newdir"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.popupinfo.mynextjobposition"
                                     aliasResourceName="pd_popupinfo_mynextjobposition"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.sitekey"
                                     aliasResourceName="pd_column_sitekey"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.popupinfo.mynextjobtype"
                                     aliasResourceName="pd_popupinfo_mynextjobtype"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.popupinfo.waitingjobs"
                                     aliasResourceName="pd_popupinfo_waitingjobs"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.engine.title"
                                     aliasResourceName="pd_engine_title"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.button.apply"
                                     aliasResourceName="pd_button_apply"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.all" aliasResourceName="pd_all"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.filter.time"
                                     aliasResourceName="pd_filter_time"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.filter.type"
                                     aliasResourceName="pd_filter_type"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.filter.owner"
                                     aliasResourceName="pd_filter_owner"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.filter.status"
                                     aliasResourceName="pd_filter_status"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.type.all"
                                     aliasResourceName="pd_type_all"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.type.workflow"
                                     aliasResourceName="pd_type_workflow"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.type.import"
                                     aliasResourceName="pd_type_import"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.type.production"
                                     aliasResourceName="pd_type_production"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.type.activation"
                                     aliasResourceName="pd_type_activation"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.type.propagate"
                                     aliasResourceName="pd_type_propagate"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.op.copypaste.label"
                                     aliasResourceName="pd_type_copypaste"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.type.indexing"
                                     aliasResourceName="pd_type_indexing"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.type.timebasepublishing"
                                     aliasResourceName="pd_type_timebasepublishing"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.type.textextraction"
                                     aliasResourceName="pd_type_textextraction"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.time.lastjobs"
                                     aliasResourceName="pd_time_lastjobs"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.owner.alluser"
                                     aliasResourceName="pd_owner_alluser"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.owner.currentuser"
                                     aliasResourceName="pd_owner_currentuser"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.status.all"
                                     aliasResourceName="pd_status_all"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.status.executing"
                                     aliasResourceName="pd_status_executing"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.status.successful"
                                     aliasResourceName="pd_status_successful"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.status.failed"
                                     aliasResourceName="pd_status_failed"/>
     <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.status.partial"
                                     aliasResourceName="pd_status_partial"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.status.wainting"
                                     aliasResourceName="pd_status_wainting"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.column.created"
                                     aliasResourceName="pd_column_created"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.column.type"
                                     aliasResourceName="pd_column_type"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.column.owner"
                                     aliasResourceName="pd_column_owner"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.column.title"
                                     aliasResourceName="pd_column_title"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.column.start"
                                     aliasResourceName="pd_column_start"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.column.end"
                                     aliasResourceName="pd_column_end"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.column.duration"
                                     aliasResourceName="pd_column_duration"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.column.status"
                                     aliasResourceName="pd_column_status"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.informations"
                                     aliasResourceName="pd_tab_informations"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.warnings"
                                     aliasResourceName="pd_tab_warnings"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.errors"
                                     aliasResourceName="pd_tab_errors"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.owner"
                                     aliasResourceName="pd_tab_owner"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.sitekey"
                                     aliasResourceName="pd_tab_sitekey"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.startdate"
                                     aliasResourceName="pd_tab_startdate"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.enddate"
                                     aliasResourceName="pd_tab_enddate"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.pid"
                                     aliasResourceName="pd_tab_pid"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.locale"
                                     aliasResourceName="pd_tab_locale"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.url"
                                     aliasResourceName="pd_tab_url"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.noinformations"
                                     aliasResourceName="pd_tab_noinformations"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.objecttype"
                                     aliasResourceName="pd_tab_objecttype"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.objectid"
                                     aliasResourceName="pd_tab_objectid"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.languagecode"
                                     aliasResourceName="pd_tab_languagecode"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.comment"
                                     aliasResourceName="pd_tab_comment"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.warning"
                                     aliasResourceName="pd_tab_warning"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.tab.error"
                                     aliasResourceName="pd_tab_error"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.processDisplay.alert.jobdeleted"
                                     aliasResourceName="pd_alert_jobdeleted"/>

</template:gwtJahiaModule>

