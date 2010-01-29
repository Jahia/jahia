package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 21, 2009
 * Time: 11:12:37 AM
 * To change this template use File | Settings | File Templates.
 */
public class ThumbsListView extends ListView<GWTJahiaNode> {
    public ThumbsListView() {
        setItemSelector("div.thumb-wrap");
        setOverStyle("x-view-over");
    }


    public ThumbsListView(boolean detailed) {
        if (detailed) {
            setTemplate(getDetailedTemplate());
        } else {
            setTemplate(getSimpleTemplate());
        }

        setItemSelector("div.thumb-wrap");
        setOverStyle("x-view-over");
    }

    @Override
    protected GWTJahiaNode prepareData(GWTJahiaNode model) {
        String s = model.getName();
        model.set("shortName", Format.ellipse(s, 14));
        model.set("nameLabel", Messages.get("fm_column_name", "Name"));
        model.set("authorLabel", Messages.get("versioning_author", "Auhor"));
        model.set("tagsLabel", Messages.get("ece_tags", "tags"));
        model.set("widthLabel", Messages.get("ece_width", "Width"));
        model.set("heightLabel", Messages.get("ece_height", "Height"));
        int width = model.getWidth();
        Log.debug("width" + width);
        if (width > 0 && width < 80) {
            model.set("pathPreview", model.getUrl());
        } else {
            model.set("pathPreview", model.getPreview());
        }
        model.set("widthPreview", model.getWidth());
        model.set("heightPreview", model.getHeight());

        // ugly due to the fact that if condition doesn't work in tpl.
        if (model.getTags() != null && model.getTags().length() > 0) {
            model.set("tagsHTML", "<div><b>" + model.get("tagsLabel") + ": </b>" + model.getTags() + "</div>");
        }
        return model;
    }

    public void setContextMenu(Menu menu) {
        super.setContextMenu(menu);
    }

    public native String getSimpleTemplate() /*-{
        return ['<tpl for=".">',
                '<div class="thumb-wrap" id="{name}">',
                '<div class="thumb"><img src="{preview}" title="{name}"></div>',
                '<div class="x-editable">{shortName}</span></div>',
                '</tpl>',
                '<div class="x-clear"></div>'].join("");
    }-*/;

    public native String getDetailedTemplate() /*-{
    return ['<tpl for=".">',
        '<div style="padding: 5px ;border-bottom: 1px solid #D9E2F4;float: left;width: 100%;" class="thumb-wrap" id="{name}">',
        '<div><div style="width: 140px; float: left; text-align: center;" class="thumb"><img src="{pathPreview}" title="{name}"></div>',
        '<div style="margin-left: 160px; " class="thumbDetails">',
        '<div><b>{nameLabel}: </b>{name}</div>',
        '<div><b>{authorLabel}: </b>{createdBy}</div>',
        '<div><b>{widthLabel}: </b>{widthPreview} px</div>',
        '<div><b>{heightLabel}: </b>{heightPreview} px</div>',
        '{tagsHTML}',
        '</div>',
        '</div>',
        '<div style="padding-left: 10px; padding-top: 10px; clear: left">{description}</div></div></tpl>',
        '<div class="x-clear"></div>'].join("");
    }-*/;

}
