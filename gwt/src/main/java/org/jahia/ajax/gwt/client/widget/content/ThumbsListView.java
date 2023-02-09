/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.core.DomQuery;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Formatter;
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

    private boolean detailed = false;
    private boolean useLargePreview;

    public ThumbsListView() {
        setItemSelector("div.thumb-wrap");
        setOverStyle("x-view-over");
    }


    public ThumbsListView(boolean detailed) {
        this();
        this.detailed = detailed;
        if (detailed) {
            setTemplate(getDetailedTemplate());
        } else {
            setTemplate(getSimpleTemplate());
        }
    }

    public ThumbsListView(boolean detailed, boolean useLargePreview) {
        this(detailed);
        this.useLargePreview = useLargePreview;
    }

    @Override
    protected GWTJahiaNode prepareData(GWTJahiaNode model) {
        if (model.isMarkedForDeletion()) {
            model.set("markedForDeletion", "true" );
        } else {
            model.set("markedForDeletion", "false" );
        }
        model.set("formattedNodeTypes", SafeHtmlUtils.htmlEscape(Formatter.join(model.getNodeTypes(), " ")));
        model.set("shortName",  SafeHtmlUtils.htmlEscape(Format.ellipse(model.getName(), 14)));
        model.set("nameLabel", Messages.get("label.name", "Name"));
        model.set("titleLabel", Messages.get("label.title", "Title"));
        model.set("authorLabel", Messages.get("versioning_author", "Author"));
        model.set("tagsLabel", Messages.get("org.jahia.jcr.edit.tags.tab", "tags"));
        model.set("createdBy", model.get("jcr:createdBy"));
        String width = model.get("j:width");
        if (width != null) {
            if (Integer.parseInt(width) < 80) {
                model.set("nodeImg", "<img src=\"" + URL.appendTimestamp(model.getUrl()) + "\" title=\"" + SafeHtmlUtils.htmlEscape(model.getName()) + "\">");
            } else {
                model.set("nodeImg", "<img src=\"" + URL.appendTimestamp(getPreview(model)) + "\" title=\"" + SafeHtmlUtils.htmlEscape(model.getName()) + "\">");
            }
            if (detailed) {
                model.set("widthHTML", "<div><b>" + Messages.get("width.label", "Width") + " </b>" + model.get("j:width") + " px</div>");
                model.set("heightHTML", "<div><b>" + Messages.get("height.label", "Height") + " </b>" + model.get("j:height") + " px</div>");
            } else {
                model.set("widthAndHeightHTML", model.get("j:width") + " x " + model.get("j:height"));
            }
        } else if (hasPreview(model)) {
            model.set("nodeImg", "<img src=\"" + URL.appendTimestamp(getPreview(model)) + "\" title=\"" + SafeHtmlUtils.htmlEscape(model.getName()) + "\">");
        } else {
            model.set("nodeImg", ContentModelIconProvider.getInstance().getIcon(model, true).getHTML());
        }

        if (model.getTags() != null && model.getTags().length() > 0) {
            model.set("tagsHTML", "<div><b>" + model.get("tagsLabel") + ": </b>" + SafeHtmlUtils.htmlEscape(model.getTags()) + "</div>");
        }
        return model;
    }


    private String getPreview(GWTJahiaNode model) {
        return useLargePreview && model.getPreviewLarge() != null ? model.getPreviewLarge() : model.getPreview();
    }


    private boolean hasPreview(GWTJahiaNode model) {
        return getPreview(model) != null || useLargePreview && model.getPreviewLarge() != null;
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

    public void setSize(int thumbnailSize) {
        NodeList<Element> imageThumbNails = DomQuery.select("#images-view .thumb");
        for (int i=0; i < imageThumbNails.getLength(); i++) {
            Element currentThumbnail = imageThumbNails.getItem(i);
            currentThumbnail.getStyle().setWidth(thumbnailSize, Style.Unit.PX);
            currentThumbnail.getStyle().setHeight(thumbnailSize, Style.Unit.PX);
        }
    }

    public native String getSimpleTemplate() /*-{
        return ['<tpl for=".">',
            '<div title="{name}" class="thumb-wrap" id="{name}">',
            '<div class="thumb" data-nodetypes="{formattedNodeTypes}">{nodeImg}</div>',
            '<tpl if="markedForDeletion == \'true\'">',
            '<span class="markedForDeletion">',
            '</tpl>',
            '<span class="x-editable">{shortName}</span>',
            '<tpl if="markedForDeletion == \'true\'">',
            '</span>',
            '</tpl>',
            '<span class="x-editable">{widthAndHeightHTML}</span>',
            '</div>',
            '</tpl>',
            '<div class="x-clear"></div>'
        ].join("");

    }-*/;


    public native String getDetailedTemplate() /*-{
        return ['<tpl for=".">',
            '<div style="padding: 5px ;border-bottom: 1px solid #D9E2F4;float: left;width: 100%;" class="thumb-wrap" id="{name}">',
            '<div><div style="width: 140px; float: left; text-align: center;" class="thumb" data-nodetypes="{formattedNodeTypes}">{nodeImg}</div>',
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
