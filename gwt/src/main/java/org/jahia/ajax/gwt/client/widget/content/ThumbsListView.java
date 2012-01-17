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

package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;

/**
 *
 * User: toto
 * Date: Dec 21, 2009
 * Time: 11:12:37 AM
 *
 */
public class ThumbsListView extends ListView<GWTJahiaNode> {
    public ThumbsListView() {
        setItemSelector("div.thumb-wrap");
        setOverStyle("x-view-over");
    }


    public ThumbsListView(boolean detailed) {
        this();
        if (detailed) {
            setTemplate(getDetailedTemplate());
        } else {
            setTemplate(getSimpleTemplate());
        }
    }

    @Override
    protected GWTJahiaNode prepareData(GWTJahiaNode model) {
        if (model.getNodeTypes().contains("jmix:markedForDeletion")) {
            model.set("markedForDeletion", "true" );
        } else {
            model.set("markedForDeletion", "false" );
        }
        model.set("shortName",  Format.ellipse(model.getName(), 14));
        model.set("nameLabel", Messages.get("label.name", "Name"));
        model.set("titleLabel", Messages.get("label.title", "Title"));
        model.set("authorLabel", Messages.get("versioning_author", "Author"));
        model.set("tagsLabel", Messages.get("org.jahia.jcr.edit.tags.tab", "tags"));
        String width = model.get("j:width");
        if (width != null) {
            if (Integer.parseInt(width) < 80) {
                model.set("nodeImg", "<img src=\"" + URL.appendTimestamp(model.getUrl()) + "\" title=\"" + model.getName() + "\">");
            } else {
                model.set("nodeImg", "<img src=\"" + URL.appendTimestamp(model.getPreview()) + "\" title=\"" + model.getName() + "\">");
            }
            model.set("widthHTML", "<div><b>" + Messages.get("width.label", "Width") + " </b>" + model.get("j:width") + " px</div>");
            model.set("heightHTML", "<div><b>" + Messages.get("height.label", "Height") + " </b>" + model.get("j:height") + " px</div>");
        } else if (model.getPreview() != null) {
            model.set("nodeImg", "<img src=\"" + URL.appendTimestamp(model.getPreview()) + "\" title=\"" + model.getName() + "\">");
        } else {
            model.set("nodeImg", ContentModelIconProvider.getInstance().getIcon(model, true).getHTML());
        }

        if (model.getTags() != null && model.getTags().length() > 0) {
            model.set("tagsHTML", "<div><b>" + model.get("tagsLabel") + ": </b>" + model.getTags() + "</div>");
        }
        return model;
    }

    protected void onMouseDown(ListViewEvent<GWTJahiaNode> e) {
        super.onMouseDown(e);
        if (e.getIndex() == -1) {
            getSelectionModel().select((GWTJahiaNode) null, false);
        }
    }

    public void setContextMenu(Menu menu) {
        super.setContextMenu(menu);
    }

    public native String getSimpleTemplate() /*-{
        return ['<tpl for=".">',
            '<div title="{name}" class="thumb-wrap" id="{name}">',
            '<div class="thumb">{nodeImg}</div>',
            '<tpl if="markedForDeletion == \'true\'">',
            '<span class="markedForDeletion">',
            '</tpl>',
            '<span class="x-editable">{shortName}</span>',
            '<tpl if="markedForDeletion == \'true\'">',
            '</span>',
            '</tpl>',
            '{widthHTML}',
            '{heightHTML}',
            '{tagsHTML}',
            '</div>',
            '</tpl>',
            '<div class="x-clear"></div>'
        ].join("");

    }-*/;


    public native String getDetailedTemplate() /*-{
        return ['<tpl for=".">',
            '<div style="padding: 5px ;border-bottom: 1px solid #D9E2F4;float: left;width: 100%;" class="thumb-wrap" id="{name}">',
            '<div><div style="width: 140px; float: left; text-align: center;" class="thumb">{nodeImg}</div>',
            '<div style="margin-left: 160px; " class="thumbDetails">',
            '<div><tpl if="markedForDeletion == \'true\'">',
            '<span class="markedForDeletion" style="text-align:left;">',
            '</tpl>',
            '<b>{nameLabel}: </b>{name}',
            '<tpl if="markedForDeletion == \'true\'">',
            '</span>',
            '</tpl></div>',
            '<div><b>{titleLabel}: </b>{displayName}</div>',
            '<div><b>{authorLabel}: </b>{createdBy}</div>',
            '{widthHTML}',
            '{heightHTML}',
            '{tagsHTML}',
            '</div>',
            '</div>',
            '<div style="padding-left: 10px; padding-top: 10px; clear: left">{description}</div></div>',
            '</tpl>',
            '<div class="x-clear"></div>'
        ].join("");
    }-*/;

}
