package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.*;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.definition.LangPropertiesEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Jan 20, 2010
 * Time: 1:53:30 PM
 * To change this template use File | Settings | File Templates.
 */
public class TranslateContentEngine extends Window {
    public static final int BUTTON_HEIGHT = 24;
    private static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();
    private static JahiaContentDefinitionServiceAsync definitionService = JahiaContentDefinitionService.App.getInstance();
    private GWTJahiaNode node;
    private Linker linker = null;

    private Button ok;
    private LayoutContainer mainComponent;
    private LangPropertiesEditor sourceLangPropertiesEditor;
    private LangPropertiesEditor targetLangPropertiesEditor;
    protected ButtonBar buttonBar;


    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     */
    public TranslateContentEngine(GWTJahiaNode node, Linker linker) {
        this.linker = linker;
        this.node = node;

        init();
    }

    protected void init() {
        setLayout(new FitLayout());
        setBodyBorder(false);
        setSize(1300, 750);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.engineLogoJahia());
        setHeading(Messages.get("cm_translate " + node.getName(), "Translate " + node.getName()));
        ContentPanel panel = new ContentPanel();
        panel.setLayout(new RowLayout(Style.Orientation.HORIZONTAL));
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.setFrame(true);
        panel.setCollapsible(false);
        panel.setHeaderVisible(false);

        sourceLangPropertiesEditor = new LangPropertiesEditor(node, GWTJahiaItemDefinition.CONTENT,false);
        sourceLangPropertiesEditor.setSize(650,750);
        targetLangPropertiesEditor = new LangPropertiesEditor(node, GWTJahiaItemDefinition.CONTENT,true);
        targetLangPropertiesEditor.setSize(650,750);

        panel.add(sourceLangPropertiesEditor, new RowData(1, 1, new Margins(4, 0, 4, 0)));
        panel.add(targetLangPropertiesEditor, new RowData(-1, 1, new Margins(4)));

        add(panel);



        // add the properties editors
        mainComponent = new LayoutContainer();
        mainComponent.setBorders(false);
        mainComponent.setLayout(new BorderLayout());
        BorderLayoutData centerData = new BorderLayoutData(Style.LayoutRegion.CENTER);
        BorderLayoutData eastData = new BorderLayoutData(Style.LayoutRegion.EAST);
        mainComponent.add(sourceLangPropertiesEditor, eastData);
        mainComponent.add(targetLangPropertiesEditor, centerData);
        //add(mainComponent);

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


    /**
     * init buttons
     */
    protected void initFooter() {
        ok = new Button(Messages.getResource("fm_save"));
        ok.setHeight(BUTTON_HEIGHT);
        ok.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonOK());
        ok.addSelectionListener(new SaveSelectionListener());
        buttonBar.add(ok);

        Button cancel = new Button(Messages.getResource("fm_cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                TranslateContentEngine.this.hide();
            }
        });
        buttonBar.add(cancel);
    }


    /**
     * Save selection listener
     */
    private class SaveSelectionListener extends SelectionListener<ButtonEvent> {
        public void componentSelected(ButtonEvent event) {
            // node
            final List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
            nodes.add(node);


            // Ajax call to update values
            JahiaContentManagementService.App.getInstance().savePropertiesAndACL(nodes, null, targetLangPropertiesEditor.getLangPropertiesMap(), null, new AsyncCallback() {
                public void onFailure(Throwable throwable) {
                    com.google.gwt.user.client.Window.alert(Messages.get("saved_prop_failed", "Properties save failed\n\n") + throwable.getLocalizedMessage());
                    Log.error("failed", throwable);
                }

                public void onSuccess(Object o) {
                    Info.display("", Messages.get("saved_prop", "Properties saved\n\n"));
                    TranslateContentEngine.this.hide();
                    linker.refreshMainComponent();
                }
            });
        }

    }
}

