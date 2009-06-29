<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@tag body-content="empty" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/utilityLib" prefix="utility" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ attribute name="sitekey" required="true" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="startpage" required="true" rtexprvalue="true" type="java.lang.String" description="text" %>

<template:gwtJahiaModule id="gwtworkflowmanager" jahiaType="gwtworkflowmanager" sitekey="${sitekey}"
                         startpage="${startpage}"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.title"
                            aliasResourceName="wf_title"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.path"
                            aliasResourceName="wf_path"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.validation"
                            aliasResourceName="wf_validation"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.history"
                            aliasResourceName="wf_history"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.page"
                            aliasResourceName="wf_page"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.depth"
                            aliasResourceName="wf_depth"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.itemsPerPage"
                            aliasResourceName="wf_itemsPerPage"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.select"
                            aliasResourceName="wf_select"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.selectAll"
                            aliasResourceName="wf_selectAll"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.deselectAll"
                            aliasResourceName="wf_deselectAll"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.addToBatch"
                            aliasResourceName="wf_addToBatch"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.showBatch"
                            aliasResourceName="wf_showBatch"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.ok" aliasResourceName="wf_ok"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.cancel"
                            aliasResourceName="wf_cancel"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.batch"
                            aliasResourceName="wf_batch"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.chooseAction"
                            aliasResourceName="wf_chooseAction"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.removeAction"
                            aliasResourceName="wf_removeAction"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.action"
                            aliasResourceName="wf_action"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.language"
                            aliasResourceName="wf_language"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.objectKey"
                            aliasResourceName="wf_objectKey"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.execute"
                            aliasResourceName="wf_execute"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.close"
                            aliasResourceName="wf_close"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.executeBatch"
                            aliasResourceName="wf_executeBatch"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.comment"
                            aliasResourceName="wf_comment"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.user"
                            aliasResourceName="wf_user"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.nothingChecked"
                            aliasResourceName="wf_nothingChecked"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.noAction"
                            aliasResourceName="wf_noAction"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.display.noLanguages"
                            aliasResourceName="wf_noLanguages"/>

<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.paging.page"
                            aliasResourceName="wf_pagingPage"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.paging.of"
                            aliasResourceName="wf_pagingOf"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.paging.display"
                            aliasResourceName="wf_pagingDisplay"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.paging.nodata"
                            aliasResourceName="wf_pagingNodata"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.paging.first"
                            aliasResourceName="wf_pagingFirst"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.paging.last"
                            aliasResourceName="wf_pagingLast"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.paging.next"
                            aliasResourceName="wf_pagingNext"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.paging.previous"
                            aliasResourceName="wf_pagingPrevious"/>
<internal:gwtResourceBundle resourceName="org.jahia.engines.workflow.paging.refresh"
                            aliasResourceName="wf_pagingRefresh"/>

<internal:gwtResourceBundle resourceName="operationMode.preview"
                            aliasResourceName="wf_preview"/>
<internal:gwtResourceBundle resourceName="operationMode.compare"
                            aliasResourceName="wf_compare"/>


