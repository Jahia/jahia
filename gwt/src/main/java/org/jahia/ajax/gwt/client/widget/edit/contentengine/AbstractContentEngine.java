package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 7, 2010
 * Time: 1:57:03 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractContentEngine extends Window {
    public static final int BUTTON_HEIGHT = 24;

    protected static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
    protected static JahiaContentDefinitionServiceAsync definitionService = JahiaContentDefinitionService.App.getInstance();
    protected Linker linker = null;
    protected List<GWTJahiaNodeType> nodeTypes;
    protected List<GWTJahiaNodeType> mixin;
    protected Map<String, GWTJahiaNodeProperty> properties;
    protected TabPanel tabs;
    protected boolean existingNode = true;
    protected GWTJahiaNode node;
    protected GWTJahiaNode parentNode;
    protected ButtonBar buttonBar;
    protected String heading;

    protected AbstractContentEngine(Linker linker) {
        this.linker = linker;
    }

    protected void init() {
        setLayout(new FillLayout());
        setBodyBorder(false);
        setSize(950, 750);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.engineLogoJahia());
        setHeading(heading);

        tabs = new TabPanel();

        tabs.setBodyBorder(false);
        tabs.setBorders(true);

        initTabs();

        tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                fillCurrentTab();
            }
        });

        add(tabs);

        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        initFooter();

        buttonsPanel.add(buttonBar);

        // copyrigths
        Text copyright = new Text(Messages.getResource("fm_copyright"));
        ButtonBar container = new ButtonBar();
        container.setAlignment(Style.HorizontalAlignment.CENTER);
        container.add(copyright);
        buttonsPanel.add(container);
        setBottomComponent(buttonsPanel);

        setFooter(true);
    }

    protected abstract void initTabs();

    protected abstract void initFooter();

    /**
     * fill current tab
     */
    protected void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();

        if (currentTab instanceof EditEngineTabItem) {
            EditEngineTabItem engineTabItem = (EditEngineTabItem) currentTab;
            if (!engineTabItem.isProcessed()) {
                engineTabItem.create();
            }
        }
    }

    public Linker getLinker() {
        return linker;
    }

    public List<GWTJahiaNodeType> getNodeTypes() {
        return nodeTypes;
    }

    public List<GWTJahiaNodeType> getMixin() {
        return mixin;
    }

    public Map<String, GWTJahiaNodeProperty> getProperties() {
        return properties;
    }

    public GWTJahiaNode getNode() {
        return node;
    }

    public GWTJahiaNode getParentNode() {
        return parentNode;
    }

    public boolean isExistingNode() {
        return existingNode;
    }
}
