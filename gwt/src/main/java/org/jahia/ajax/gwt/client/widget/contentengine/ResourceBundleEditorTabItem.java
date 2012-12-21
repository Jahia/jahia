/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.contentengine;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.resourcebundle.ResourceBundleEditor;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;

/**
 * Edit engine tab that embeds resource bundle editor.
 * 
 * @author Sergiy Shyrkov
 */
public class ResourceBundleEditorTabItem extends EditEngineTabItem {

    private static final long serialVersionUID = -1527005357999548838L;

    private transient ResourceBundleEditor resourceBundleEditor;

    public ResourceBundleEditorTabItem() {
        setHandleCreate(false);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties,
                       Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes,
                       Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {
        if (resourceBundleEditor != null) {
            node.set(GWTJahiaNode.RESOURCE_BUNDLE, resourceBundleEditor.getResourceBundle());
        }
    }

    @Override
    public void init(final NodeHolder engine, final AsyncTabItem tab, String locale) {
        if (engine.getNode() == null) {
            return;
        }

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            public void execute() {
                resourceBundleEditor = new ResourceBundleEditor(engine);
                tab.add(resourceBundleEditor);
                tab.layout();
            }
        });
    }

    public void setProcessed(boolean processed) {
        if (!processed) {
            resourceBundleEditor = null;
        }

        super.setProcessed(processed);
    }

}
