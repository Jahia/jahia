/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
