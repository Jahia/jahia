package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.Style;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;
import com.google.gwt.core.client.GWT;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:34:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class MainModule extends LayoutContainer implements Module {

    private GWTJahiaNode node;
    private HTML html;
    private String path;
    private String template;

    private EditManager editManager;

    Map<Element, Module> m;

    public MainModule(final String path, final String template, final EditManager editManager) {
        super(new FlowLayout());

        setScrollMode(Style.Scroll.AUTO);

        this.editManager = editManager;
        this.path = path;
        this.template = template;

        Selection.getInstance().setMainModule(this);

        refresh();
    }

    public void refresh() {
        editManager.getEditLinker().getCreatePageButton().setEnabled(false);
        JahiaContentManagementService.App.getInstance().getRenderedContent(path, null, editManager.getLocale(), template, true, new AsyncCallback<String>() {
            public void onSuccess(String result) {
                int i = getVScrollPosition();

                removeAll();

                html = new HTML(result);
                add(html);

                ModuleHelper.initAllModules(MainModule.this, html, editManager);
                ModuleHelper.buildTree(MainModule.this);
                parse();
                layout();

                setVScrollPosition(i);
                List<String> list = new ArrayList<String>(1);
                list.add(path);
            }

            public void onFailure(Throwable caught) {
                GWT.log("error", caught);
            }
        });

    }

    @Override
    protected void onAfterLayout() {
        super.onAfterLayout();
        if (m != null) {
            ModuleHelper.move(m);
        }
    }

    public void parse() {
        m = ModuleHelper.parse(this);
    }

    public void onParsed() {        
    }

    public String getModuleId() {
        return "main";
    }

    public HTML getHtml() {
        return html;
    }

    public LayoutContainer getContainer() {
        return this;
    }

    public String getPath() {
        return path;
    }

    public GWTJahiaNode getNode() {
        return node; 
    }

    public void setNode(GWTJahiaNode node) {
        this.node = node;
        if (node.getNodeTypes().contains("jnt:page") || node.getInheritedNodeTypes().contains("jnt:page")) {
            editManager.getEditLinker().getCreatePageButton().setEnabled(true);
        }
    }

    public Module getParentModule() {
        return null;
    }

    public void setParentModule(Module module) {
    }

    public void setSelected(boolean b) {
    }

    public String getTemplate() {
        return null;
    }
    
}
