package org.jahia.ajax.gwt.client.widget.content.compare;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.*;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 2, 2010
 * Time: 9:27:04 AM
 * To change this template use File | Settings | File Templates.
 */
public class CompareEngine extends Window {
    public static final int BUTTON_HEIGHT = 24;
    private GWTJahiaNode node;
    private Linker linker = null;
    private String locale;

    //private LayoutContainer mainComponent;
    private VersionViewer liveVersion;
    private VersionViewer stagingVersion;
    protected ButtonBar buttonBar;


    /**
     * Initializes an instance of this class.
     *
     * @param node   the content object to be edited
     * @param linker the edit linker for refresh purpose
     */
    public CompareEngine(GWTJahiaNode node, String locale, Linker linker) {
        this.linker = linker;
        this.node = node;
        this.locale = locale;
        init();
    }

    protected void init() {
        setLayout(new BorderLayout());
        setBodyBorder(false);
        setSize(1300, 750);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.engineLogoJahia());
        setHeading(Messages.get("label_compare " + node.getPath(), "Compare " + node.getPath()));
        ContentPanel panel = new ContentPanel();
        panel.setLayout(new RowLayout(Style.Orientation.HORIZONTAL));
        panel.setWidth("100%");
        panel.setHeight("100%");
        panel.setFrame(true);
        panel.setCollapsible(false);
        panel.setHeaderVisible(false);

        liveVersion = new VersionViewer(node, Constants.MODE_LIVE, locale, linker);
        liveVersion.setSize(650, 750);
        stagingVersion = new VersionViewer(node, Constants.MODE_PREVIEW, locale, linker);
        stagingVersion.setSize(650, 750);

        BorderLayoutData liveLayoutData = new BorderLayoutData(Style.LayoutRegion.WEST, 650);
        liveLayoutData.setCollapsible(true);
        add(liveVersion, liveLayoutData);

        BorderLayoutData stagingLayoutData = new BorderLayoutData(Style.LayoutRegion.CENTER, 650);
        add(stagingVersion, stagingLayoutData);

        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);

        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        //initFooter();

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
        Button cancel = new Button(Messages.getResource("fm_cancel"));
        cancel.setHeight(BUTTON_HEIGHT);
        cancel.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                CompareEngine.this.hide();
            }
        });
        buttonBar.add(cancel);
    }


}


