/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
        if (linker.getConfig().isEnableDragAndDrop()) {
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
