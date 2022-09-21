/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit.sidepanel;

import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.data.toolbar.GWTSidePanelTab;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeTree;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Side panel tab that allows creation of new content items using drag and drop.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 3:14:11 PM
 */
class CreateContentTabItem extends SidePanelTabItem {

    private transient ContentTypeTree contentTypeTree;
    private transient CreateGridDragSource gridDragSource;
    private List<String> baseTypes = null;
    private List<String> excludedNodeTypes = null;
    private List<String> paths;

    @Override
    public TabItem create(GWTSidePanelTab config) {
        super.create(config);
        tab.setLayout(new FitLayout());

        contentTypeTree = new ContentTypeTree(config.getTreeColumns());

        tab.add(contentTypeTree);
        tab.setId("JahiaGxtCreateContentTab");
        return tab;
    }

    @Override
    public void initWithLinker(EditLinker linker) {
        super.initWithLinker(linker);

        initExcludedNodeTypes(linker);

        contentTypeTree.fillStore(baseTypes, excludedNodeTypes, true);
        if (linker.getConfig().isDragAndDropEnabled()) {
            gridDragSource = new CreateGridDragSource(contentTypeTree.getTreeGrid());
            gridDragSource.addDNDListener(linker.getDndListener());
        }
    }

    private void initExcludedNodeTypes(EditLinker linker) {
        excludedNodeTypes = new ArrayList<String>();
        if (linker.getConfig().getNonEditableTypes() != null) {
            excludedNodeTypes.addAll(linker.getConfig().getNonEditableTypes());
        }
        if (linker.getConfig().getNonVisibleTypes() != null) {
            excludedNodeTypes.addAll(linker.getConfig().getNonVisibleTypes());
        }
        if (linker.getConfig().getExcludedNodeTypes() != null) {
            excludedNodeTypes.addAll(linker.getConfig().getExcludedNodeTypes());
        }
    }

    public void setBaseType(String baseType) {
        this.baseTypes = Arrays.asList(baseType);
    }

    public void setBaseTypes(List<String> baseTypes) {
        this.baseTypes = baseTypes;
    }

    public void setExcludedNodeTypes(List<String> excludedNodeTypes) {
        this.excludedNodeTypes = excludedNodeTypes;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    @Override
    public boolean needRefresh(Map<String, Object> data) {
        return data.containsKey("event") && "nonEditableTypesChanged".equals(data.get("event"));
    }

    @Override
    public void doRefresh() {
        initExcludedNodeTypes(editLinker);
        contentTypeTree.fillStore(baseTypes, excludedNodeTypes, true);
    }

}
