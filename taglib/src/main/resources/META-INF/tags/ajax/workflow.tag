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
<%@ attribute name="sitekey" required="true" rtexprvalue="true" type="java.lang.String" description="text" %>
<%@ attribute name="startpage" required="true" rtexprvalue="true" type="java.lang.String" description="text" %>

<template:gwtJahiaModule id="gwtworkflowmanager" jahiaType="gwtworkflowmanager" sitekey="${sitekey}"
                         startpage="${startpage}">
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.title"
                                     aliasResourceName="wf_title"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.path"
                                     aliasResourceName="wf_path"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.validation"
                                     aliasResourceName="wf_validation"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.history"
                                     aliasResourceName="wf_history"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.page"
                                     aliasResourceName="wf_page"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.depth"
                                     aliasResourceName="wf_depth"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.itemsPerPage"
                                     aliasResourceName="wf_itemsPerPage"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.select"
                                     aliasResourceName="wf_select"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.selectAll"
                                     aliasResourceName="wf_selectAll"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.deselectAll"
                                     aliasResourceName="wf_deselectAll"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.addToBatch"
                                     aliasResourceName="wf_addToBatch"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.showBatch"
                                     aliasResourceName="wf_showBatch"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.ok" aliasResourceName="wf_ok"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.cancel"
                                     aliasResourceName="wf_cancel"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.batch"
                                     aliasResourceName="wf_batch"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.chooseAction"
                                     aliasResourceName="wf_chooseAction"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.removeAction"
                                     aliasResourceName="wf_removeAction"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.action"
                                     aliasResourceName="wf_action"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.language"
                                     aliasResourceName="wf_language"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.objectKey"
                                     aliasResourceName="wf_objectKey"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.execute"
                                     aliasResourceName="wf_execute"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.close"
                                     aliasResourceName="wf_close"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.executeBatch"
                                     aliasResourceName="wf_executeBatch"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.comment"
                                     aliasResourceName="wf_comment"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.user"
                                     aliasResourceName="wf_user"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.nothingChecked"
                                     aliasResourceName="wf_nothingChecked"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.noAction"
                                     aliasResourceName="wf_noAction"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.display.noLanguages"
                                     aliasResourceName="wf_noLanguages"/>

    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.paging.page"
                                     aliasResourceName="wf_pagingPage"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.paging.of"
                                     aliasResourceName="wf_pagingOf"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.paging.display"
                                     aliasResourceName="wf_pagingDisplay"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.paging.nodata"
                                     aliasResourceName="wf_pagingNodata"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.paging.first"
                                     aliasResourceName="wf_pagingFirst"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.paging.last"
                                     aliasResourceName="wf_pagingLast"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.paging.next"
                                     aliasResourceName="wf_pagingNext"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.paging.previous"
                                     aliasResourceName="wf_pagingPrevious"/>
    <utility:gwtEngineResourceBundle resourceName="org.jahia.engines.workflow.paging.refresh"
                                     aliasResourceName="wf_pagingRefresh"/>

</template:gwtJahiaModule>
