/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.toolbar.provider;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.*;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Util;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.analytics.GWTJahiaAnalyticsParameter;
import org.jahia.ajax.gwt.client.data.toolbar.analytics.GWTJahiaGAprofile;
import org.jahia.ajax.gwt.client.service.toolbar.ToolbarService;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.form.*;
import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.core.XTemplate;
import com.google.gwt.user.client.Element;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.i18n.client.DateTimeFormat;


import java.util.*;

/**
 * User: jahia
 * Date: 4 juil. 2008
 * Time: 09:58:20
 */
public class AjaxActionJahiaToolItemProvider extends AbstractJahiaToolItemProvider {
    public static final String CLASS_ACTION = "classAction";
    public static final String ACTION = "action";
    public static final String ADD_COMMENT = "addComment";
    public static final String COMMENT = "comment";
    public static final String ON_FAILURE_MESSAGE = "onFailureMessage";
    public static final String ON_SUCCESS = "onSuccess";
    public static final String INFO = "info";
    public static final String NOTIFICATION = "notification";
    public static final String WINDOW = "window";
    public static final String REDIRECT = "redirect";
    public static final String REFRESH = "refresh";
    public static final String SELECTED = "selected";
    public static final String TOGGLE = "toggle";


    public static final String SITE_STATS = "siteStats";
    public static final String PAGE_STATS = "pageStats";

    Map<String, String> data = new HashMap<String, String>();

    public SelectionListener getSelectListener(final GWTJahiaToolbarItem gwtToolbarItem) {
        // add listener
        final SelectionListener listener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {
                if (handleAddCommentProperty(gwtToolbarItem)) {
                    return;
                }
                // create a selectedProperty property
                GWTJahiaProperty selectedProperty = new GWTJahiaProperty();
                selectedProperty.setName(SELECTED);
                selectedProperty.setValue(String.valueOf(!gwtToolbarItem.isSelected()));

                gwtToolbarItem.getProperties().put(SELECTED, selectedProperty);

                // execute action withou diplaying a MessageBox
                execute(gwtToolbarItem);

            }
        };
        return listener;
    }

    /**
     * Open dialog box
     *
     * @param gwtToolbarItem
     * @return
     */
    private boolean handleAddCommentProperty(GWTJahiaToolbarItem gwtToolbarItem) {
        Map properties = gwtToolbarItem.getProperties();
        if (properties != null) {
            GWTJahiaProperty property = (GWTJahiaProperty) properties.get(ADD_COMMENT);
            if (property != null) {
                final String value = property.getValue();
                if (value != null) {
                    if (value.endsWith("Stats")) {
                        ToolbarService.App.getInstance().isTracked(new AsyncCallback<Boolean>() {
                            public void onSuccess(Boolean isTracked) {
                                if (isTracked) {
                                    showStatsDialogBox(value);
                                } else {
                                    Info.display("", "Your website is not tracked by google analyitcs");
                                }
                            }

                            public void onFailure(Throwable throwable) {
                            }
                        });
                        return true;
                    }
                    boolean addComment = Boolean.valueOf(value);
                    if (addComment) {
                        try {
                            showDialogBox(gwtToolbarItem);
                            return true;
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }
        return false;
    }


    /**
     * Create a new toolItem
     *
     * @param gwtToolbarItem
     * @return
     */
    public ToolItem createNewToolItem(final GWTJahiaToolbarItem gwtToolbarItem) {
        GWTJahiaProperty prop = gwtToolbarItem.getProperties().get(TOGGLE);
        try {
            if (prop != null && prop.getValue() != null && Boolean.parseBoolean(prop.getValue())) {
                return new ToggleToolItem();
            }
        } catch (Exception e) {
            Log.error("Error when parsing 'toogle' prop.", e);
        }
        return new TextToolItem();
    }


    /**
     * @param gwtToolbarItem
     */
    private void showDialogBox(final GWTJahiaToolbarItem gwtToolbarItem) {
        // display a message box
        final MessageBox box = new MessageBox();
        box.setTitle(gwtToolbarItem.getTitle());
        box.setButtons(MessageBox.OKCANCEL);
        box.setType(MessageBox.MessageBoxType.MULTIPROMPT);
        box.setModal(true);

        // execute action listener
        final Listener executeActionListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Dialog dialog = (Dialog) ce.component;
                Button btn = dialog.getButtonPressed();
                if (btn.getText().equalsIgnoreCase(MessageBox.OK)) {
                    // create a comment property
                    GWTJahiaProperty comment = new GWTJahiaProperty();
                    comment.setName(COMMENT);
                    comment.setValue(box.getMessage());

                    // add comment in the properties map
                    Map<String, GWTJahiaProperty> properties = gwtToolbarItem.getProperties();
                    properties.put(COMMENT, comment);
                    gwtToolbarItem.setProperties(properties);

                    // execute ajax action
                    execute(gwtToolbarItem);
                }
            }
        };
        box.addCallback(executeActionListener);

        // display box
        box.show();
    }

    /**
     * Execute the ajax action
     *
     * @param gwtToolbarItem
     */
    protected void execute(final GWTJahiaToolbarItem gwtToolbarItem) {
        ToolbarService.App.getInstance().execute(getJahiaGWTPageContext(), gwtToolbarItem.getProperties(), new AsyncCallback<GWTJahiaAjaxActionResult>() {
            public void onSuccess(GWTJahiaAjaxActionResult result) {
                // depending on "onSuccess" property , display info, notify, redirect or refresh
                final Map properties = gwtToolbarItem.getProperties();
                if (properties != null) {
                    GWTJahiaProperty actionProperty = (GWTJahiaProperty) properties.get(ON_SUCCESS);
                    if (actionProperty != null) {
                        String action = actionProperty.getValue();
                        if (action != null && action.length() > 0) {
                            // case of info
                            if (action.equalsIgnoreCase(INFO)) {
                                if (result != null && result.getErrors().isEmpty() && result.getValue() != null
                                        && !"".equals(result.getValue().trim())) {
                                    MessageBox box = new MessageBox();
                                    box.setButtons(MessageBox.OK);
                                    box.setIcon(MessageBox.INFO);
                                    box.setTitle(gwtToolbarItem.getTitle());
                                    box.setMessage(result.getValue());
                                    box.show();
                                }
                            }
                            // case of a notification
                            else if (action.equalsIgnoreCase(NOTIFICATION)) {
                                Info.display(gwtToolbarItem.getTitle(), result.getValue());
                            }
                            // case of redirection
                            else if (action.equalsIgnoreCase(REDIRECT)) {
                                if (result != null && result.getErrors().isEmpty() && result.getValue() != null
                                        && !"".equals(result.getValue().trim())) {
                                    Window.Location.replace(result.getValue());
                                }
                            }
                            // case of redirection
                            else if (action.equalsIgnoreCase(REFRESH)) {
                                Window.Location.reload();
                            }
                            // case of redirection
                            else if (action.equalsIgnoreCase(WINDOW)) {
                                com.extjs.gxt.ui.client.widget.Window window = new com.extjs.gxt.ui.client.widget.Window();
                                if (gwtToolbarItem.getTitle() != null) {
                                    String title = gwtToolbarItem.getTitle().replaceAll(" ", "_");
                                    window.setTitle(title);
                                }
                                window.addText(result.getValue());
                                window.setModal(true);
                                window.setResizable(true);
                                window.setClosable(true);
                                window.show();
                            }
                        }
                    }
                }
            }

            public void onFailure(Throwable throwable) {
                // display failure message
                final Map properties = gwtToolbarItem.getProperties();
                if (properties != null) {
                    GWTJahiaProperty messageProp = (GWTJahiaProperty) properties.get(ON_FAILURE_MESSAGE);
                    if (messageProp != null) {
                        String message = messageProp.getValue();
                        if (message != null && message.length() > 0) {
                            MessageBox box = new MessageBox();
                            box.setIcon(MessageBox.ERROR);
                            box.setTitle(gwtToolbarItem.getTitle());
                            box.setMessage(message);
                            box.show();
                        }
                    }
                }
            }
        });
    }

    //##################################################################################################################
    // google analytics
    private void showStatsDialogBox(String siteORpage) { // todo should be adapted to the new version
        // todo add a scroll containing available profiles per site and per page independently

        com.extjs.gxt.ui.client.widget.Window w = new com.extjs.gxt.ui.client.widget.Window();

        final HashMap<String, GWTJahiaGAprofile>[] jahiaGAprofiles = new HashMap[2];
        if (siteORpage.equals("pageStats")) w.setHeading("Statistics for the current page");
        else w.setHeading("Statistics for the current site");


        w.setModal(true);
        w.setActive(true);
        w.setSize(1000, 600);
        w.setMaximizable(true);
        w.setLayout(new BorderLayout());

        // chart display panels
        final ContentPanel annotatedTimeLinePanel = new ContentPanel();
        annotatedTimeLinePanel.setHeading("");
        //todo put content to the center of the panel
        final ContentPanel geoMapPanel = new ContentPanel();
        geoMapPanel.setHeading("");
        //todo put content to the center of the panel
        final ContentPanel piePanel = new ContentPanel();
        piePanel.setHeading("");


        //todo put content to the center of the panel
        BorderLayoutData chartData = new BorderLayoutData(Style.LayoutRegion.CENTER);
        ///  NEW
        final ContentPanel infoPanel = new ContentPanel();
        infoPanel.setHeading("Jahia analytics profiles");
        BorderLayoutData infoData = new BorderLayoutData(Style.LayoutRegion.WEST);
        infoData.setSplit(true);
        infoData.setCollapsible(true);
        infoData.setMargins(new Margins(5));

        StringBuffer sb = new StringBuffer();
        sb.append("<br><br><br>");
        sb.append("<div class=text style='line-height: 1.5em'>");
        sb.append("<table>");
        sb.append("<tr><td><b>User-account</b></td><td>{gaUserAccount}</td></tr>");
        sb.append("<tr><td><b>Profile</b></td><td>{gaProfile}</td></tr>");
        sb.append("<tr><td><b>Login</b></td><td>{gaLogin}</td></tr>");
        sb.append("<tr><td><b>Tracked-urls</b></td><td>{trackedUrls}</td></tr>");
        sb.append("<tr><td><b>State</b></td><td>{trackingEnabled}</td></tr>");
        sb.append("</table>");
        sb.append("</div>");
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("jahiaGAprofileName", "");
        properties.put("gaUserAccount", "");
        properties.put("gaProfile", "");
        properties.put("gaLogin", "");
        properties.put("trackedUrls", "");
        properties.put("trackingEnabled", "");

        final BaseModel bm = new BaseModel(properties);

        final XTemplate template = XTemplate.create(sb.toString());
        final HTML html = new HTML();
        html.setWidth("160px");
        template.overwrite(html.getElement(), Util.getJsObject(bm));
        infoPanel.add(html);
        // update template when model changes
        bm.addChangeListener(new ChangeListener() {
            public void modelChanged(ChangeEvent event) {
                template.overwrite(html.getElement(), Util.getJsObject(bm));
            }
        });
        /*  todo set progress
       new Button("Progress", new SelectionListener<ComponentEvent>() {
       public void componentSelected(ComponentEvent ce) {
         final MessageBox box = MessageBox.progress("Please wait", "Loading items...",
             "Initializing...");
         final ProgressBar bar = box.getProgressBar();
         final Timer t = new Timer() {
           float i;

           @Override
           public void run() {
             bar.updateProgress(i / 100, (int) i + "% Complete");
             i += 5;
             if (i > 105) {
               cancel();
               box.close();
               Info.display("Message", "Items were loaded", "");
             }
           }
         };
         t.scheduleRepeating(500);
       }
     }));
        */
          final String site_or_page = siteORpage;
        final SimpleComboBox<String> profilesComboBox = new SimpleComboBox<String>();
        profilesComboBox.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                //Log.info(be.toString());
                //Log.info(profilesComboBox.getSimpleValue().toString());
                GWTJahiaGAprofile jGAp = new GWTJahiaGAprofile();
                String profileName = profilesComboBox.getSimpleValue().toString();
                if(site_or_page.equals("siteStats")){
                    if(jahiaGAprofiles[0].containsKey(profileName)){
                        jGAp = jahiaGAprofiles[0].get(profileName);
                    }
                    else{
                        jGAp = jahiaGAprofiles[1].get(profileName);
                    }
                }else{
                    jGAp = jahiaGAprofiles[1].get(profileName);
                }

                bm.set("gaUserAccount", jGAp.getGaUserAccount());
                bm.set("gaProfile", jGAp.getGaProfile());
                bm.set("gaLogin", jGAp.getGaLogin());
                bm.set("trackedUrls", jGAp.getTrackedUrls());

                String state = "Disabled";
                if (jGAp.getTrackingEnabled()) {
                    state = "Enabled";
                }
                bm.set("trackingEnabled", state);
            }
        });
        infoPanel.add(profilesComboBox);
        infoPanel.add(html);
        
        // the combobox for stat type selector
        final SimpleComboBox<StatSiteType> siteStats = new SimpleComboBox<StatSiteType>();
        siteStats.setEditable(false);
        siteStats.setTriggerAction(ComboBox.TriggerAction.ALL);
        siteStats.setSimpleValue(StatSiteType.VISITS);
        siteStats.setWidth(12);

        final SimpleComboBox<StatPageType> pageStats = new SimpleComboBox<StatPageType>();
        pageStats.add(Arrays.asList(StatPageType.values()));
        pageStats.setEditable(false);
        pageStats.setTriggerAction(ComboBox.TriggerAction.ALL);
        pageStats.setSimpleValue(StatPageType.PAGEVIEWS);
        pageStats.setWidth(12);

        LabelToolItem languageSelection_Label = new LabelToolItem("Select a language");
        final SimpleComboBox<String> siteLanguages = new SimpleComboBox<String>();
        siteLanguages.add("all");
        siteLanguages.setSimpleValue("all");

        final String[] uuid = new String[]{""};
        final GWTJahiaAnalyticsParameter gaParams = new GWTJahiaAnalyticsParameter();
        GWTJahiaPageContext page = getJahiaGWTPageContext();
        int pid = page.getPid();

        // todo we need here to get the sitekey/siteid/servername... else the returned site in toolbarServiceImpl.java
        // will always be the last registered one

        ToolbarService.App.getInstance().getGAsiteProperties(pid, new AsyncCallback<Map<String, String>>() {
            public void onSuccess(Map<String, String> data) {
                Map<String, String> profileNames = new HashMap<String, String>();
                jahiaGAprofiles[0] = new HashMap();// for real urls
                jahiaGAprofiles[1] = new HashMap();// for virtual urls
                Iterator it = data.keySet().iterator();
                while (it.hasNext()) {
                    String property = (String) it.next();
                    if (property.startsWith("jahiaGAprofileName")) { // its a jahia profile name
                        profileNames.put(data.get(property), data.get(property));
                        Log.info(property + " = " + data.get(property));
                    }
                }
                Iterator itOnProfiles = profileNames.keySet().iterator();
                int tu = 0; // to identify the type of tracked urls and allow to classify profiles
                while (itOnProfiles.hasNext()) {
                    GWTJahiaGAprofile jGAp = new GWTJahiaGAprofile();
                    tu = 0;
                    it = data.keySet().iterator();
                    String currentProfile = (String) itOnProfiles.next();
                    while (it.hasNext()) {
                        String property = (String) it.next();
                        jGAp.setJahiaGAprofile(property);
                        if (property.startsWith(currentProfile+"#")) {
                            if (property.endsWith("trackedUrls")) {
                                if (data.get(property).equals("virtual")) {
                                    tu = 1;
                                } else {
                                    tu = 0;
                                }
                            }
                            jGAp.dispatchSetter(property, data.get(property));
                        }
                    }
                    jahiaGAprofiles[tu].put(currentProfile, jGAp);
                }
                if (site_or_page.equals("siteStats")) {
                    Iterator itOnJgaP = jahiaGAprofiles[0].keySet().iterator();
                    String japName = "";
                    while (itOnJgaP.hasNext()) {
                        japName = (String) itOnJgaP.next();
                        profilesComboBox.add(japName);
                    }
                    itOnJgaP = jahiaGAprofiles[1].keySet().iterator();
                    japName = "";
                    while (itOnJgaP.hasNext()) {
                        japName = (String) itOnJgaP.next();
                        profilesComboBox.add(japName);
                    }
                    profilesComboBox.setSimpleValue(japName);
                    GWTJahiaGAprofile jGAp = jahiaGAprofiles[1].get(japName);
                    bm.set("gaUserAccount", jGAp.getGaUserAccount());
                    bm.set("gaProfile", jGAp.getGaProfile());
                    bm.set("gaLogin", jGAp.getGaLogin());
                    bm.set("trackedUrls", jGAp.getTrackedUrls());
                    String state = "Disabled";
                    if (jGAp.getTrackingEnabled()) {
                        state = "Enabled";
                    }
                    bm.set("trackingEnabled", state);
                    gaParams.setJahiaGAprofile(jGAp.getJahiaGAprofile());
                } else {
                   Iterator itOnJgaP = jahiaGAprofiles[1].keySet().iterator();
                    String japName = "";
                    while (itOnJgaP.hasNext()) {
                        japName = (String) itOnJgaP.next();
                        profilesComboBox.add(japName);
                    }
                    profilesComboBox.setSimpleValue(japName);
                    GWTJahiaGAprofile jGAp = jahiaGAprofiles[1].get(japName);
                    bm.set("gaUserAccount", jGAp.getGaUserAccount());
                    bm.set("gaProfile", jGAp.getGaProfile());
                    bm.set("gaLogin", jGAp.getGaLogin());
                    bm.set("trackedUrls", jGAp.getTrackedUrls());
                    String state = "Disabled";
                    if (jGAp.getTrackingEnabled()) {
                        state = "Enabled";
                    }
                    bm.set("trackingEnabled", state);
                    gaParams.setJahiaGAprofile(jGAp.getJahiaGAprofile());
                }
                uuid[0] = data.get("uuid");
                String[] tab = data.get("siteLanguages").split("#");
                for (int i = 0; i < tab.length; i++) {
                    siteLanguages.add(tab[i]);
                }
            }

            public void onFailure(Throwable throwable) {
                Window.alert("Failure of ga parameters retreiving");
            }
        });

        LabelToolItem begin_date_Label = new LabelToolItem("Select begin date");
        final DateField begin_date_field = new DateField();
        long twentyFourDaysAgoLong = ((new Date()).getTime()) - (24 * 86400000);// 1 day = 86400000 ms
        begin_date_field.setMaxValue(new Date());
        begin_date_field.setValue(new Date(twentyFourDaysAgoLong - 8 * 86400000));
        begin_date_field.setAllowBlank(false);


        LabelToolItem end_date_Label = new LabelToolItem("Select end date");
        final DateField end_date_field = new DateField();
        end_date_field.setMaxValue(new Date());
        end_date_field.setValue(new Date());
        end_date_field.setAllowBlank(false);

        ToolBar toolbar = new ToolBar();

        AdapterToolItem atiSiteStats = new AdapterToolItem(siteStats);
        AdapterToolItem atiBeginDate = new AdapterToolItem(begin_date_field);
        AdapterToolItem atiEndDate = new AdapterToolItem(end_date_field);

        AdapterToolItem atiPageStats = new AdapterToolItem(pageStats);
        AdapterToolItem atiSiteLanguages = new AdapterToolItem(siteLanguages);
        AdapterToolItem atiSiteLanguagesSelectionLabel = new AdapterToolItem(languageSelection_Label);


        if (siteORpage.startsWith("site")) toolbar.add(atiSiteStats);
        else toolbar.add(atiPageStats);
        toolbar.add(new SeparatorToolItem());
        toolbar.add(new SeparatorToolItem());
        if (siteORpage.startsWith("page")) {
            toolbar.add(atiSiteLanguagesSelectionLabel);
            toolbar.add(new SeparatorToolItem());
            toolbar.add(new SeparatorToolItem());
            toolbar.add(atiSiteLanguages);
            toolbar.add(new SeparatorToolItem());
            toolbar.add(new SeparatorToolItem());
        }
        toolbar.add(begin_date_Label);
        toolbar.add(new SeparatorToolItem());
        toolbar.add(new SeparatorToolItem());
        toolbar.add(atiBeginDate);
        toolbar.add(new SeparatorToolItem());
        toolbar.add(new SeparatorToolItem());
        toolbar.add(end_date_Label);
        toolbar.add(new SeparatorToolItem());
        toolbar.add(new SeparatorToolItem());
        toolbar.add(atiEndDate);
        toolbar.add(new SeparatorToolItem());
        toolbar.add(new SeparatorToolItem());


        TabPanel charts = new TabPanel();
        charts.setWidth(1000);

        final TabItem annotatedTimeLineTab = new TabItem("Annotated time line");
        annotatedTimeLineTab.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                siteStats.removeAll();
                siteStats.add(StatSiteType.BOUNCERATE);
                siteStats.add(StatSiteType.DIRECT);
                siteStats.add(StatSiteType.NEWVISITS);
                siteStats.add(StatSiteType.PAGEVIEWS);
                siteStats.add(StatSiteType.REFERRAL);
                siteStats.add(StatSiteType.SEARCH);
                siteStats.add(StatSiteType.TIMEONSITE);
                siteStats.add(StatSiteType.VISITORS);
                siteStats.add(StatSiteType.VISITS);
                siteStats.setSimpleValue(StatSiteType.VISITS);
            }
        });
        annotatedTimeLineTab.setLayout(new BorderLayout());
        annotatedTimeLineTab.add(annotatedTimeLinePanel, chartData);


        final TabItem geoMapTab = new TabItem("Geographic map");
        geoMapTab.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                siteStats.removeAll();
                siteStats.add(StatSiteType.VISITS);
                siteStats.add(StatSiteType.PAGEVIEWS);
                siteStats.add(StatSiteType.NEWVISITS);
                siteStats.add(StatSiteType.PAGESPERVISIT);
                siteStats.add(StatSiteType.BOUNCES);
                siteStats.setSimpleValue(StatSiteType.VISITS);
            }
        });
        geoMapTab.setLayout(new BorderLayout());
        geoMapTab.add(geoMapPanel, chartData);


        final TabItem pieTab = new TabItem("Pie");
        pieTab.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                siteStats.removeAll();
                siteStats.add(StatSiteType.BROWSER);
                siteStats.add(StatSiteType.CONNECTIONSPEED);
                siteStats.add(StatSiteType.KEYWORD);
                siteStats.add(StatSiteType.SOURCE);
                siteStats.setSimpleValue(StatSiteType.BROWSER);

            }
        });
        pieTab.setLayout(new BorderLayout());
        pieTab.add(piePanel, chartData);


        if (siteORpage.startsWith("site")) {
            //charts.add(pieTab);
            charts.add(geoMapTab);
            charts.add(annotatedTimeLineTab);
        } else {
            charts.add(annotatedTimeLineTab);
        }

        final SelectionListener executeActionListener = new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent ce) {

                // get google analytics parameters from the form
                DateTimeFormat fmt = DateTimeFormat.getFormat("yyyyMMdd");
                if (!(fmt.format(begin_date_field.getValue()).length() < 8) && (!(fmt.format(end_date_field.getValue()).length() < 8))) {
                    String dateRange = fmt.format(begin_date_field.getValue()) + "-" + fmt.format(end_date_field.getValue());
                    String statType = "";
                    gaParams.setDateRange(dateRange);
                    // Info.display("date range ",dateRange);
                    String chartType = "";
                    String url = "";
                    if (site_or_page.equals("siteStats")) {
                        url = "site";
                        gaParams.setSiteORpage(url);

                        if (annotatedTimeLineTab.isVisible()) {
                            chartType = "AnnotatedTimeLine";
                        } else if (pieTab.isVisible()) {
                            chartType = "Pie";
                        } else if (geoMapTab.isVisible()) {
                            chartType = "GeoMap";
                        }
                        statType = siteStats.getSimpleValue().toString().replace(" ", "");
                        gaParams.setStatType(statType);
                        gaParams.setChartType(chartType);
                        //Info.display("", statType + " in " + chartType);

                    } else {
                        if (siteLanguages.getSimpleValue().equals("all")) {
                            url = "/Unique_Universal_id/" + uuid[0] + "/";
                        } else {
                            url = "/Unique_Universal_id/" + uuid[0] + "/" + siteLanguages.getSimpleValue() + "/";
                        }
                        gaParams.setSiteORpage(url);
                        statType = pageStats.getSimpleValue().toString();
                        chartType = "AnnotatedTimeLine";
                        gaParams.setStatType(statType);
                        gaParams.setChartType(chartType);

                    }
                    gaParams.setJahiaGAprofile(profilesComboBox.getSimpleValue().toString());
                    if (chartType.equals("AnnotatedTimeLine")) {
                        annotatedTimeLinePanel.getElement().setInnerHTML("Requested report is being downloaded");
                    } else if (chartType.equals("Pie")) {
                        piePanel.getElement().setInnerHTML("Requested report is being downloaded");
                    } else if (chartType.equals("GeoMap")) {
                        geoMapPanel.getElement().setInnerHTML("Requested report is being downloaded");
                    }

                    ToolbarService.App.getInstance().getGAdata(gaParams, new AsyncCallback<Map<String, String>>() {
                        public void onSuccess(Map<String, String> gaData) {
                            data.clear();
                            data.putAll(gaData);
                            boolean error = false;
                            if (data.containsKey("Error")) {
                                error = true;
                                Window.alert(data.get("Error"));
                            }
                            if (!error) {
                                if (annotatedTimeLineTab.isVisible()) {
                                    displayAnnotatedTimeLine(annotatedTimeLinePanel.getElement());
                                } else if (pieTab.isVisible()) {
                                    piePanel.getElement().setInnerHTML("Coming soon");
                                    //  displayPieChart(piePanel.getElement());
                                } else if (geoMapTab.isVisible()) {
                                    displayGeoMap(geoMapPanel.getElement());
                                }
                            }

                        }

                        public void onFailure(Throwable throwable) {
                            Window.alert("Failure of data retreiving");
                        }
                    });
                } else {
                    Window.alert("Please select a begin and an end date");
                }
            }
        };
        Button button = new Button("OK");
        button.addSelectionListener(executeActionListener);
        AdapterToolItem atiButton = new AdapterToolItem(button);
        toolbar.add(atiButton);

        w.setTopComponent(toolbar);
        w.add(charts, chartData);
        w.add(infoPanel, infoData);
        w.show();


    }

    /*
   * Native method used to display a piechart
   * @param Element : the widget in which the chart will be displayed
   * */

    public native void displayPieChart(Element element)
        /*-{

       if(!$wnd.google && !wnd.google.visualisation)
       {
          return;
       }
       var data        = new $wnd.google.visualization.DataTable();
       var size        = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::getSize()();
       var statType    = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::getStatType()();

        data.addColumn('string', statType);
        data.addColumn('number', 'Visits');

        data.addRows(size);

        for( i=0; i < size ; i++)
       {
           var element = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::pickElementFromTable()();
           data.setValue(i, 0, ''+i);
           var value = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::getElementValue(Ljava/lang/String;)(element);
           data.setValue(i, 1, value);
           this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::removePickedElement(Ljava/lang/String;)(element);
       }
       options = {};
       options['width'] = 400;
       options['height'] = 240;
       options['is3D'] = true;
       options['title'] = "'"+statType+"'";

        var chart = new  $wnd.google.visualization.BarChart(element);
        chart.draw(data, options);
        }-*/;

    /*
   * Native method used to display a geomap
   * */

    public native void displayGeoMap(Element element)
        /*-{
            if(!$wnd.google && !wnd.google.visualisation) {
               return;
           }
           var data = new $wnd.google.visualization.DataTable();
           var size        = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::getSize()();
           var statType    = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::getStatType()();

          data.addRows(size);
          data.addColumn('string', 'Country');
          data.addColumn('number', statType);

          for( i=0; i < size ; i++)
          {
                var country = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::pickAcountry()();
                data.setValue(i, 0, country);
                var value = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::getCountryValue(Ljava/lang/String;)(country);
                data.setValue(i, 1, value);
                this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::removePickedCountry(Ljava/lang/String;)(country);
          }

          var options = {};
          options['dataMode'] = 'regions';
          var geomap = new $wnd.google.visualization.GeoMap(element);
          geomap.draw(data, options);
        }-*/;

    /*
   * Native method used to display an annotated time line
   * */
    public native void displayAnnotatedTimeLine(Element element)
        /*-{

        if(!$wnd.google && !wnd.google.visualisation)
           {
               return;
           }
        var data     = new $wnd.google.visualization.DataTable();
        var size     = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::getSize()();
        var statType = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::getStatType()();

        data.addColumn('date', 'Date');
        data.addColumn('number', statType);
        data.addRows(size);
        for( i=0; i < size ; i++)
        {
             var date = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::pickAdate()();
             data.setValue(i, 0, new Date(date));
             var value = this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::getValueSpark(Ljava/lang/String;)(date);
             data.setValue(i, 1, value);
             this.@org.jahia.ajax.gwt.client.widget.toolbar.provider.AjaxActionJahiaToolItemProvider::removePickedDate(Ljava/lang/String;)(date);
        }

         var options={};
         options['displayAnnotations']= true;
         options['annotationsWidth'] = 5;
         options['scaleType'] = 'maximize';
         var chart = new $wnd.google.visualization.AnnotatedTimeLine(element);
         chart.draw(data, options);

        }-*/;

    /*
   * An enumeration used to fill the combobox containing the different types of statistics available for the website
   * */
    private enum StatSiteType {

        BOUNCERATE("Bounce Rate"), BROWSER("Browser"), CONNECTIONSPEED("Connection Speed"), DIRECT("Direct"),
        /*GOAL1("Goal1"), GOAL2("Goal2"), GOAL3("Goal3"), GOAL4("Goal4"),*/ KEYWORD("Keyword"),
        NEWVISITS("New Visits"), PAGEVIEWS("Pageviews"), REFERRAL("Referral"), SEARCH("Search"),
         /**/SOURCE("Source"), TIMEONSITE("Time On Site"), VISITORS("Visitors"), VISITS("Visits"),
        PAGESPERVISIT("Pages Per Visit"), AVGTIMEONSITE("Avgerage Time on Site"), PERCENTAGEOFNEWVISITS("Percentage Of New Visits"), BOUNCES("Bounces");

        //todo unlock goals


        private String name;

        StatSiteType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

    }

    /*
   * An enumeration used to fill the combobox containing the different types of statistics available for individual pages
   * */
    private enum StatPageType {

        BOUNCERATE("Bounce Rate"), DOLLARINDEX("$ Index"), EXITPERCENT("% Exit"),
        PAGEVIEWS("Pageviews"), TIMEONPAGE("Time on Page"), UNIQUEVIEWS("Unique Pageviews");

        private String name;

        StatPageType(String name) {
            this.name = name;
        }

        public String toString() {
            return name;
        }

    }

    /*
   * Method accessible by native methods to get the size of data to display
   * */
    public int getSize() {
        return Integer.parseInt(data.get("size"));
    }

    /*
   * Method accessible by native methods to get the type of statistics received
   * */
    public String getStatType() {
        return data.get("statType");
    }

    /*
   * Method accessible by native methods to pick a date from the received data
   * */
    public String pickAdate() {
        String date = "January 1, 2008";
        Set dates = data.keySet();
        Iterator it = dates.iterator();
        while (it.hasNext()) {
            date = (String) it.next();
            if (!date.equals("statType") && !date.equals("size") && !date.equals("total"))
                return date;
        }
        return date;
    }

    /*
   * Method accessible by native methods to get the value corresponding to the date picked above
   * */
    public double getValueSpark(String date) {
        if (!data.containsKey(date)) return 0;
        else
            return Double.parseDouble(data.get(date).replace(",", ""));
    }

    /*
   * Method accessible by native methods to remove the date picked above
   * */
    public void removePickedDate(String date) {
        data.remove(date);
    }

    /*
   * Method accessible by native methods to pick an element from the received data
   * */
    public String pickElementFromTable() {
        String element = "table";
        Set elements = data.keySet();
        Iterator it = elements.iterator();
        while (it.hasNext()) {
            element = (String) it.next();
            if (!element.equals("statType") && !element.equals("size") && !element.equals("total")) {
                Log.debug(element + " picked");
                return element;
            }
        }
        return element;
    }

    /*
   * Method accessible by native methods to get the value corresponding to the element picked above
   * */
    public double getElementValue(String element) {
        if (!data.containsKey(element)) return 0;
        else
            return Double.parseDouble(data.get(element).replace(",", ""));
    }

    /*
   * Method accessible by native methods to remove the element picked above
   * */
    public void removePickedElement(String element) {
        Log.debug(element + " removed");
        data.remove(element);
    }

    /*
   * Method accessible by native methods to pick a country from the received data
   * */
    public String pickAcountry() {
        String country = "Switzerland";
        Set countries = data.keySet();
        Iterator it = countries.iterator();
        while (it.hasNext()) {
            country = (String) it.next();
            if (!country.equals("statType") && !country.equals("size") && !country.equals("total"))
                return country;
        }
        return country;
    }

    /*
   * Method accessible by native methods to get the value corresponding to the country picked above
   * */
    public double getCountryValue(String country) {
        if (!data.containsKey(country)) return 0;
        return Double.parseDouble((data.get(country)).replace(",", ""));
    }

    /*
   * Method accessible by native methods to remove the country picked above
   * */
    public void removePickedCountry(String country) {
        data.remove(country);
    }

    public void getData(DateField begin_date_field,DateField end_date_field, GWTJahiaAnalyticsParameter gaParams,String site_or_page, final TabItem annotatedTimeLineTab1,final TabItem pieTab1,
                        final TabItem geoMapTab1,SimpleComboBox<StatSiteType> siteStats,SimpleComboBox<StatPageType> pageStats,SimpleComboBox<String> siteLanguages,String uuid,
                        SimpleComboBox<String> profilesComboBox,final ContentPanel annotatedTimeLinePanel1,final ContentPanel piePanel1,final ContentPanel geoMapPanel1){
        {

                // get google analytics parameters from the form
                DateTimeFormat fmt = DateTimeFormat.getFormat("yyyyMMdd");
                if (!(fmt.format(begin_date_field.getValue()).length() < 8) && (!(fmt.format(end_date_field.getValue()).length() < 8))) {
                    String dateRange = fmt.format(begin_date_field.getValue()) + "-" + fmt.format(end_date_field.getValue());
                    String statType = "";
                    gaParams.setDateRange(dateRange);
                    // Info.display("date range ",dateRange);
                    String chartType = "";
                    String url = "";
                    if (site_or_page.equals("siteStats")) {
                        url = "site";
                        gaParams.setSiteORpage(url);

                        if (annotatedTimeLineTab1.isVisible()) {
                            chartType = "AnnotatedTimeLine";
                        } else if (pieTab1.isVisible()) {
                            chartType = "Pie";
                        } else if (geoMapTab1.isVisible()) {
                            chartType = "GeoMap";
                        }
                        statType = siteStats.getSimpleValue().toString().replace(" ", "");
                        gaParams.setStatType(statType);
                        gaParams.setChartType(chartType);
                        //Info.display("", statType + " in " + chartType);

                    } else {
                        if (siteLanguages.getSimpleValue().equals("all")) {
                            url = "/Universal_Unique_Id/" + uuid+ "/";
                        } else {
                            url = "/Universal_Unique_Id/" + uuid + "/" + siteLanguages.getSimpleValue() + "/";
                        }
                        gaParams.setSiteORpage(url);
                        statType = pageStats.getSimpleValue().toString();
                        chartType = "AnnotatedTimeLine";
                        gaParams.setStatType(statType);
                        gaParams.setChartType(chartType);

                    }
                    gaParams.setJahiaGAprofile(profilesComboBox.getSimpleValue().toString());
                    if (chartType.equals("AnnotatedTimeLine")) {
                        annotatedTimeLinePanel1.getElement().setInnerHTML("Requested report is being downloaded");
                    } else if (chartType.equals("Pie")) {
                        piePanel1.getElement().setInnerHTML("Requested report is being downloaded");
                    } else if (chartType.equals("GeoMap")) {
                        geoMapPanel1.getElement().setInnerHTML("Requested report is being downloaded");
                    }

                    ToolbarService.App.getInstance().getGAdata(gaParams, new AsyncCallback<Map<String, String>>() {
                        public void onSuccess(Map<String, String> gaData) {
                            data.clear();
                            data.putAll(gaData);
                            boolean error = false;
                            if (data.containsKey("Error")) {
                                error = true;
                                Window.alert(data.get("Error"));
                            }
                            if (!error) {
                                if (annotatedTimeLineTab1.isVisible()) {
                                    displayAnnotatedTimeLine(annotatedTimeLinePanel1.getElement());
                                } else if (pieTab1.isVisible()) {
                                    //piePanel.getElement().setInnerHTML("Coming soon");
                                    //  displayPieChart(piePanel.getElement());
                                } else if (geoMapTab1.isVisible()) {
                                    displayGeoMap(geoMapPanel1.getElement());
                                }
                            }

                        }

                        public void onFailure(Throwable throwable) {
                            Window.alert("Failure of data retreiving");
                        }
                    });
                } else {
                    Window.alert("Please select a begin and an end date");
                }
            }
    }


}
