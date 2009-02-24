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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ tag body-content="empty" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ attribute name="state" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="windowHeight" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="windowWidth" required="false" rtexprvalue="true" type="java.lang.String" description="text" %>

<template:gwtJahiaModule id="pdisplay" jahiaType="pdisplay"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.filemanager.Filemanager_Engine.newDir.label"
                            aliasResourceName="fm_newdir"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.popupinfo.mynextjobposition"
                            aliasResourceName="pd_popupinfo_mynextjobposition"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.sitekey"
                            aliasResourceName="pd_column_sitekey"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.popupinfo.mynextjobtype"
                            aliasResourceName="pd_popupinfo_mynextjobtype"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.popupinfo.waitingjobs"
                            aliasResourceName="pd_popupinfo_waitingjobs"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.engine.title"
                            aliasResourceName="pd_engine_title"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.button.apply"
                            aliasResourceName="pd_button_apply"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.all" aliasResourceName="pd_all"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.filter.time"
                            aliasResourceName="pd_filter_time"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.filter.type"
                            aliasResourceName="pd_filter_type"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.filter.owner"
                            aliasResourceName="pd_filter_owner"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.filter.status"
                            aliasResourceName="pd_filter_status"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.type.all"
                            aliasResourceName="pd_type_all"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.type.workflow"
                            aliasResourceName="pd_type_workflow"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.type.import"
                            aliasResourceName="pd_type_import"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.type.production"
                            aliasResourceName="pd_type_production"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.type.activation"
                            aliasResourceName="pd_type_activation"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.type.propagate"
                            aliasResourceName="pd_type_propagate"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.op.copypaste.label"
                            aliasResourceName="pd_type_copypaste"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.type.indexing"
                            aliasResourceName="pd_type_indexing"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.type.timebasepublishing"
                            aliasResourceName="pd_type_timebasepublishing"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.type.textextraction"
                            aliasResourceName="pd_type_textextraction"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.time.lastjobs"
                            aliasResourceName="pd_time_lastjobs"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.owner.alluser"
                            aliasResourceName="pd_owner_alluser"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.owner.currentuser"
                            aliasResourceName="pd_owner_currentuser"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.status.all"
                            aliasResourceName="pd_status_all"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.status.executing"
                            aliasResourceName="pd_status_executing"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.status.successful"
                            aliasResourceName="pd_status_successful"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.status.failed"
                            aliasResourceName="pd_status_failed"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.status.partial"
                            aliasResourceName="pd_status_partial"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.status.wainting"
                            aliasResourceName="pd_status_wainting"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.column.created"
                            aliasResourceName="pd_column_created"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.column.type"
                            aliasResourceName="pd_column_type"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.column.owner"
                            aliasResourceName="pd_column_owner"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.column.title"
                            aliasResourceName="pd_column_title"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.column.start"
                            aliasResourceName="pd_column_start"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.column.end"
                            aliasResourceName="pd_column_end"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.column.duration"
                            aliasResourceName="pd_column_duration"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.column.status"
                            aliasResourceName="pd_column_status"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.informations"
                            aliasResourceName="pd_tab_informations"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.warnings"
                            aliasResourceName="pd_tab_warnings"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.errors"
                            aliasResourceName="pd_tab_errors"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.owner"
                            aliasResourceName="pd_tab_owner"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.sitekey"
                            aliasResourceName="pd_tab_sitekey"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.startdate"
                            aliasResourceName="pd_tab_startdate"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.enddate"
                            aliasResourceName="pd_tab_enddate"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.pid"
                            aliasResourceName="pd_tab_pid"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.locale"
                            aliasResourceName="pd_tab_locale"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.url"
                            aliasResourceName="pd_tab_url"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.noinformations"
                            aliasResourceName="pd_tab_noinformations"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.objecttype"
                            aliasResourceName="pd_tab_objecttype"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.objectid"
                            aliasResourceName="pd_tab_objectid"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.languagecode"
                            aliasResourceName="pd_tab_languagecode"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.comment"
                            aliasResourceName="pd_tab_comment"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.warning"
                            aliasResourceName="pd_tab_warning"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.tab.error"
                            aliasResourceName="pd_tab_error"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.processDisplay.alert.jobdeleted"
                            aliasResourceName="pd_alert_jobdeleted"/>


