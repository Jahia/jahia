package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: Dec 21, 2009
* Time: 11:12:37 AM
* To change this template use File | Settings | File Templates.
*/
public class ThumbsListView extends ListView<GWTJahiaNode> {
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
        '<div style="width: 140px; float: left; text-align: center;" class="thumb"><img src="{preview}" title="{name}"></div>',
        '<div style="margin-left: 160px; " class="thumbDetails"><div><b>Name:</b></div><div style="padding-left: 10px">{name}</div><div><b>Description</b></div><div style="padding-left: 10px">{description}</div><div><b>Author</b></div><div style="padding-left: 10px">{createdBy}</div></div></div>',
        '</tpl>',
        '<div class="x-clear"></div>'].join("");
    }-*/;

}
