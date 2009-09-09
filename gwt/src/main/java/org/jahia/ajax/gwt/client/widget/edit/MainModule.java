package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.Events;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.core.client.GWT;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * First module of any rendered element.
 * Sub content will be created as ListModule or SimpleModule.
 */
public class MainModule extends ContentPanel implements Module {

    private GWTJahiaNode node;
    private HTML html;
    private String path;
    private String template;

    private EditLinker editLinker;

    Map<Element, Module> m;

    public MainModule(final String path, final String template) {
        super(new FlowLayout());
        setHeading("Page : "+path);
        setScrollMode(Style.Scroll.AUTO);

        this.path = path;
        this.template = template;

        Selection.getInstance().setMainModule(this);

    }

    public void initWithLinker(EditLinker linker) {
        this.editLinker = linker;
        refresh();

        getHeader().sinkEvents(Event.ONCLICK + Event.ONDBLCLICK);
        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Log.info("click" + path);
                editLinker.onModuleSelection(MainModule.this);
            }
        };
        getHeader().addListener(Events.OnClick, listener);
        getHeader().addListener(Events.OnDoubleClick, new EditContentEnginePopupListener(this));                
    }

    public EditLinker getEditLinker() {
        return editLinker;
    }

    public void refresh() {
        JahiaContentManagementService.App.getInstance().getRenderedContent(path, null, editLinker.getLocale(), template, true, new AsyncCallback<String>() {
            public void onSuccess(String result) {
                int i = getVScrollPosition();

                removeAll();

                html = new HTML(result);
                add(html);

                ModuleHelper.initAllModules(MainModule.this, html);
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
//            editManager.getEditLinker().getCreatePageButton().setEnabled(true);
        }
    }

    public Module getParentModule() {
        return null;
    }

    public void setParentModule(Module module) {
    }

    public String getTemplate() {
        return null;
    }

    public void handleNewModuleSelection(Module selectedModule) {
        Selection l = Selection.getInstance();
        l.hide();
        if (selectedModule != null) {
            l.setCurrentContainer(selectedModule.getContainer());
            l.show();
        }
        l.layout();
    }

    public void handleNewSidePanelSelection(GWTJahiaNode node) {

    }

    public boolean isDraggable() {
        return false;
    }

    public void setDraggable(boolean isDraggable) {
    }
}
