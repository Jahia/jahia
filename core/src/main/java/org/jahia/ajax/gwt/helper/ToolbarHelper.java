package org.jahia.ajax.gwt.helper;

import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbar;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItemsGroup;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarSet;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.Constants;
import org.jahia.ajax.gwt.client.widget.toolbar.action.WorkflowActionItem;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.toolbar.bean.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.workflow.WorkflowDefinition;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.i18n.JahiaResourceBundle;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Apr 13, 2010
 * Time: 5:25:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ToolbarHelper {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ToolbarHelper.class);

    /**
     * Get gwt toolbar for the current user
     *
     * @return
     */
    public GWTJahiaToolbarSet getGWTToolbars(JCRSiteNode site, JahiaUser jahiaUser, Locale locale,Locale uiLocale, HttpServletRequest request, String toolbarGroup) throws GWTJahiaServiceException {
        try {
            // there is no pref or toolbar are hided
            // get all tool bars
            ToolbarSet toolbarSet = (ToolbarSet) SpringContextSingleton.getBean(toolbarGroup);
            Visibility visibility = toolbarSet.getVisibility();
            if ((visibility != null && visibility.getRealValue(site, jahiaUser, locale, request)) || visibility == null) {
                GWTJahiaToolbarSet gwtJahiaToolbarSet = createGWTToolbarSet(site, jahiaUser, locale,uiLocale, request, toolbarSet);
                return gwtJahiaToolbarSet;
            } else {
                logger.info("Toolbar are not visible.");
                return null;
            }

        } catch (Exception e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException("Error during loading toolbars due to " + e.getMessage());
        }
    }

    /**
     * create gwt toolabr set
     *
     * @param toolbarSet
     * @return
     */
    public GWTJahiaToolbarSet createGWTToolbarSet(JCRSiteNode site, JahiaUser jahiaUser, Locale locale,Locale uiLocale, HttpServletRequest request, ToolbarSet toolbarSet) {
        if (toolbarSet.getToolbars() == null || toolbarSet.getToolbars().isEmpty()) {
            logger.debug("toolbar set list is empty");
            return null;
        }

        // create  a gwtJahiaToolbarSet
        GWTJahiaToolbarSet gwtJahiaToolbarSet = new GWTJahiaToolbarSet();
        for (Toolbar toolbar : toolbarSet.getToolbars()) {
            // add only tool bar that the user can view
            Visibility visibility = toolbar.getVisibility();
            if ((visibility != null && visibility.getRealValue(site, jahiaUser, locale, request)) || visibility == null) {
                GWTJahiaToolbar gwtToolbar = createGWTToolbar(site, jahiaUser, locale,uiLocale, request,toolbar);
                // add toolbar only if not empty
                if (gwtToolbar != null && gwtToolbar.getGwtToolbarItemsGroups() != null && !gwtToolbar.getGwtToolbarItemsGroups().isEmpty()) {
                    gwtJahiaToolbarSet.addGWTToolbar(gwtToolbar);
                } else {
                    logger.debug("[" + (gwtToolbar != null) + "," + (gwtToolbar.getGwtToolbarItemsGroups() != null) + "," + (!gwtToolbar.getGwtToolbarItemsGroups().isEmpty()) + "]" + " toolbar: " + toolbar.getName() + " has no items -->  not visible");
                }
            } else {
                logger.debug("toolbar: " + toolbar.getName() + ":  not visible");
            }
        }
        return gwtJahiaToolbarSet;

    }


    /**
     * Create gwt toolbar
     *
     * @param toolbar
     * @return
     */
    private GWTJahiaToolbar createGWTToolbar(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, Toolbar toolbar) {
        // don't add the tool bar if  has no items group
        if (toolbar.getItems() == null || toolbar.getItems().isEmpty()) {
            logger.debug("toolbar[" + toolbar.getName() + "] itemsgroup list is empty");
            return null;
        }

        // create gwtTollbar
        GWTJahiaToolbar gwtToolbar = new GWTJahiaToolbar();
        gwtToolbar.setName(toolbar.getName());
        gwtToolbar.setTitle(getResources(toolbar.getTitleKey(), uiLocale != null ? uiLocale : locale, site));
        gwtToolbar.setType(toolbar.getType());
        gwtToolbar.setDisplayTitle(toolbar.isDisplayTitle());
        gwtToolbar.setContextMenu(toolbar.isContextMenu());

        // load items-group
        List<GWTJahiaToolbarItemsGroup> gwtToolbarItemsGroupList = new ArrayList<GWTJahiaToolbarItemsGroup>();
        int index = 0;
        for (Item item : toolbar.getItems()) {
            ItemsGroup itemsGroup = null;
            if (item instanceof ItemsGroup) {
                itemsGroup = (ItemsGroup) item;
            } else {
                // create a single item group
                itemsGroup = new ItemsGroup();
                itemsGroup.addItem(item);
                itemsGroup.setLayout("button-label");
                itemsGroup.setVisibility(item.getVisibility());
            }

            // add only itemsgroup that the user can view
            Visibility visibility = itemsGroup.getVisibility();
            if ((visibility != null && visibility.getRealValue(site, jahiaUser, locale, request)) || visibility == null) {
                GWTJahiaToolbarItemsGroup gwtItemsGroup = createGWTItemsGroup(site, jahiaUser, locale,uiLocale, request,gwtToolbar.getName(), index, itemsGroup);

                // add itemsGroup only if not empty
                if (gwtItemsGroup != null && gwtItemsGroup.getGwtToolbarItems() != null && !gwtItemsGroup.getGwtToolbarItems().isEmpty()) {
                    gwtToolbarItemsGroupList.add(gwtItemsGroup);

                }
            } else {
                logger.debug("toolbar[" + gwtToolbar.getName() + "] - itemsGroup [" + itemsGroup.getId() + "," + itemsGroup.getTitleKey() + "]  not visible");
            }

            index++;
        }
        gwtToolbar.setGwtToolbarItemsGroups(gwtToolbarItemsGroupList);

        return gwtToolbar;
    }


    /**
     * Create gwt items group
     *
     * @param toolbarName
     * @param index
     * @param itemsGroup
     * @return
     */
    private GWTJahiaToolbarItemsGroup createGWTItemsGroup(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, String toolbarName, int index, ItemsGroup itemsGroup) {
        // don't add the items group if  has no items group
        List<Item> list = itemsGroup.getRealItems(site, jahiaUser, locale);
        if (list == null || list.isEmpty()) {
            logger.debug("toolbar[" + toolbarName + "] itemlist is empty");
            return null;
        }


        List<GWTJahiaToolbarItem> gwtToolbarItemsList = new ArrayList<GWTJahiaToolbarItem>();
        // create items from definition
        for (Item item : list) {
            addItem(site, jahiaUser, locale,uiLocale, request,gwtToolbarItemsList, item);
        }

        // don't add the items group if  has no items group
        if (gwtToolbarItemsList == null || gwtToolbarItemsList.isEmpty()) {
            logger.debug("toolbar[" + toolbarName + "] itemlist is empty");
            return null;
        }

        // creat items-group
        GWTJahiaToolbarItemsGroup gwtToolbarItemsGroup = new GWTJahiaToolbarItemsGroup();
        gwtToolbarItemsGroup.setId(toolbarName + "_" + index);
        gwtToolbarItemsGroup.setType(itemsGroup.getId());
        gwtToolbarItemsGroup.setLayout(getLayoutAsInt(itemsGroup.getLayout()));

        gwtToolbarItemsGroup.setNeedSeparator(itemsGroup.isSeparator());
        gwtToolbarItemsGroup.setMediumIconStyle(itemsGroup.getMediumIconStyle());
        gwtToolbarItemsGroup.setMinIconStyle(itemsGroup.getMinIconStyle());
        if (itemsGroup.getTitleKey() != null) {
            gwtToolbarItemsGroup.setItemsGroupTitle(getResources(itemsGroup.getTitleKey(), uiLocale != null ? uiLocale : locale, site));
        } else {
            gwtToolbarItemsGroup.setItemsGroupTitle(itemsGroup.getTitle());
        }
        gwtToolbarItemsGroup.setGwtToolbarItems(gwtToolbarItemsList);
        return gwtToolbarItemsGroup;
    }


    /**
     * Get layout as int
     *
     * @param layout
     * @return
     */
    private int getLayoutAsInt(String layout) {
        int layoutInt = -1;
        if (layout != null) {
            if (layout.equalsIgnoreCase("button")) {
                layoutInt = Constants.LAYOUT_BUTTON;
            } else if (layout.equalsIgnoreCase("label")) {
                layoutInt = Constants.LAYOUT_ONLY_LABEL;
            } else if (layout.equalsIgnoreCase("button-label")) {
                layoutInt = Constants.LAYOUT_BUTTON_LABEL;
            } else if (layout.equalsIgnoreCase("menu")) {
                layoutInt = Constants.LAYOUT_ITEMSGROUP_MENU;
            } else if (layout.equalsIgnoreCase("menu-radio")) {
                layoutInt = Constants.LAYOUT_ITEMSGROUP_MENU_RADIO;
            } else if (layout.equalsIgnoreCase("menu-checkbox")) {
                layoutInt = Constants.LAYOUT_ITEMSGROUP_MENU_CHECKBOX;
            } else {
                logger.debug("Warning: layout " + layout + " unknown.");
            }
        }
        return layoutInt;
    }

    /**
     * Add item
     *
     * @param gwtToolbarItemsList
     * @param item
     */
    private void addItem(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, List<GWTJahiaToolbarItem> gwtToolbarItemsList, Item item) {
        if (item instanceof ItemsGroup) {
            for (Item subItem : ((ItemsGroup) item).getRealItems(site, jahiaUser, locale)) {
                addItem(site, jahiaUser, locale,uiLocale, request,gwtToolbarItemsList, subItem);
            }
        } else {
            // add only item that the user can view
            logger.debug("Item: " + item.getId());
            Visibility visibility = item.getVisibility();

            // add only visible items
            if ((visibility != null && visibility.getRealValue(site, jahiaUser, locale, request)) || visibility == null) {
                GWTJahiaToolbarItem gwtToolbarItem = createGWTItem(site, jahiaUser, locale,uiLocale, request,item);
                if (gwtToolbarItem != null) {
                    gwtToolbarItemsList.add(gwtToolbarItem);
                }
            } else {
                logger.debug("Item: " + item.getTitleKey() + ":  not visible");
            }
        }
    }


    /**
     * Create gwt item
     *
     * @param item
     * @return
     */
    private GWTJahiaToolbarItem createGWTItem(JCRSiteNode site, JahiaUser jahiaUser, Locale locale, Locale uiLocale, HttpServletRequest request, Item item) {
        // GWTJahiaToolbarItem
        GWTJahiaToolbarItem gwtToolbarItem = new GWTJahiaToolbarItem();
        if (item.getTitleKey() != null) {
            gwtToolbarItem.setTitle(getResources(item.getTitleKey(), uiLocale != null ? uiLocale : locale, site));
        } else {
            gwtToolbarItem.setTitle(item.getTitle());
        }
        gwtToolbarItem.setType(item.getId());
        gwtToolbarItem.setDisplayTitle(item.isDisplayTitle());
        if (item.getDescriptionKey() != null) {
            gwtToolbarItem.setDescription(getResources(item.getDescriptionKey(), uiLocale != null ? uiLocale : locale, site));
        } else {
            gwtToolbarItem.setDescription(gwtToolbarItem.getTitle());
        }
        gwtToolbarItem.setMediumIconStyle(item.getMediumIconStyle());
        gwtToolbarItem.setMinIconStyle(item.getMinIconStyle());
        if (item.getSelected() != null) {
            gwtToolbarItem.setSelected(item.getSelected().getRealValue(site, jahiaUser, locale));
        } else {
            gwtToolbarItem.setSelected(false);
        }
        Map<String, GWTJahiaProperty> pMap = new HashMap<String, GWTJahiaProperty>();
        for (Property currentProperty : item.getProperties()) {
            GWTJahiaProperty gwtProperty = new GWTJahiaProperty();
            gwtProperty.setName(currentProperty.getName());
            gwtProperty.setValue(currentProperty.getRealValue(site, jahiaUser, locale));
            pMap.put(gwtProperty.getName(), gwtProperty);
        }
        gwtToolbarItem.setLayout(getLayoutAsInt(item.getLayout()));
        gwtToolbarItem.setProperties(pMap);


        if (item.getWorkflowAction() != null) {
            try {
                List<WorkflowDefinition> def = WorkflowService.getInstance().getWorkflowsForAction(item.getWorkflowAction());
                List<String> processes = new ArrayList<String>();
                for (WorkflowDefinition workflowDefinition : def) {
                    processes.add(workflowDefinition.getKey());
                }
                gwtToolbarItem.setProcesses(processes);
                // todo : use the role assigned to the action for bypassing workflow ?
                final WorkflowActionItem workflowActionItem = new WorkflowActionItem(processes, jahiaUser.isAdminMember(0), item.getActionItem());
                gwtToolbarItem.setActionItem(workflowActionItem);
            } catch (RepositoryException e) {
                logger.error("Cannot get workflows", e);
            }
        } else {
            gwtToolbarItem.setActionItem(item.getActionItem());
        }

        return gwtToolbarItem;
    }

    /**
     * Get resources
     * @param key
     * @param locale
     * @param site
     * @return
     */
    private String getResources(String key, Locale locale, JCRSiteNode site) {
        if (logger.isDebugEnabled()) {
            logger.debug("Resources key: " + key);
        }
        if (key == null || key.length() == 0) {
            return key;
        }
        String value = new JahiaResourceBundle(locale, site != null ? site.getTemplatePackageName() : null).get(key, null);
        if (value == null || value.length() == 0) {
            value = JahiaResourceBundle.getJahiaInternalResource(key, locale);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Resources value: " + value);
        }

        return value;
    }


}
