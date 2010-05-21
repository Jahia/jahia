package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:03:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkerModule extends SimpleModule {
    private HorizontalPanel panel;

    public LinkerModule(String id, String path, String innerHTML, String template, String scriptInfo, String nodeTypes,
                        String referenceTypes, String templateInfo, MainModule m) {
        super(id, path, template, scriptInfo, nodeTypes, referenceTypes, templateInfo, m);
        this.id = id;
        this.path = path;
        this.mainModule = m;
        this.nodeTypes = nodeTypes;
        setBorders(false);
        panel = new HorizontalPanel();
        panel.setHorizontalAlign(Style.HorizontalAlignment.CENTER);
        panel.addStyleName("x-small-editor");
        panel.addStyleName("x-panel-header");
        panel.addStyleName("x-panel-linker");

//        html = new HTML("<img src=\""+JahiaGWTParameters.getContextPath() + "/templates/default/images/add.png"+"\" /> Add new content here");
        html = new HTML("<p class=\"linkAction\">Drop this to be linked<br/></p>");
        panel.add(html);
        add(panel);
    }

    @Override
    public void onParsed() {
        super.onParsed();
    }

    @Override
    public void setNode(GWTJahiaNode node) {
        super.setNode(node);
        final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();
        async.getProperties(node.getPath(), new BaseAsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onSuccess(GWTJahiaGetPropertiesResult gwtJahiaGetPropertiesResult) {
                if (gwtJahiaGetPropertiesResult.getProperties().containsKey("j:bindedComponent")) {
                    final GWTJahiaNodeProperty o = gwtJahiaGetPropertiesResult.getProperties().get("j:bindedComponent");
                    panel.removeAll();
                    html = new HTML("<p class=\"linkPath\">Linked to: " + o.getValues().get(
                            0).getNode().getPath() + "</p><p class=\"linkAction\">Drop this to be linked</p>");
                    panel.add(html);
                    panel.layout();
                }
            }
        });
    }
}