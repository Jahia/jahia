/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.toolbar.xml;

import org.jahia.ajax.gwt.client.util.ToolbarConstants;
import org.jahia.services.toolbar.bean.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: jahia
 * Date: 2 avr. 2008
 * Time: 16:44:47
 */
public class ToolbarXMLParser {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ToolbarXMLParser.class);
    private Document[] xmlDescriptors;


    public ToolbarXMLParser(List<String> filePaths) {
        loalXMLToolbarDescriptor(filePaths);
    }

    private void loalXMLToolbarDescriptor(List<String> filePaths) {
        xmlDescriptors = new Document[filePaths.size()];
        SAXBuilder sxb = new SAXBuilder();
        for (int i = 0; i < filePaths.size(); i++) {
            try {
                xmlDescriptors[i] = sxb.build(new File(filePaths.get(i)));
            } catch (JDOMException e) {
                logger.error(e, e);
            } catch (IOException e) {
                logger.error(e, e);
            }
        }

    }

    private List<Element> getElementListByXPath(XPath xpath) throws JDOMException {
        // look for the elements in all xml descriptors
        List<Element> elements = null;
        for (Document xmlDescriptor : xmlDescriptors) {
            elements = xpath.selectNodes(xmlDescriptor);
            if (elements != null && !elements.isEmpty()) {
                return elements;
            }
        }
        return elements;
    }

    private Element getElementByXpath(XPath xpath) throws JDOMException {
        // look for the element in all xml descriptors                
        Element element = null;
        for (Document xmlDescriptor : xmlDescriptors) {
            element = (Element) xpath.selectSingleNode(xmlDescriptor);
            if (element != null) {
                return element;
            }
        }
        return element;
    }

    public ToolbarSet getToolbars() {
        try {
            // tool bar set
            ToolbarSet toolbarSet = new ToolbarSet();

            // set visibility of the toolbar set
            Visibility visibility = getVisibility(getElementByXpath(XPath.newInstance("/toolbars")));
            toolbarSet.setVisibility(visibility);

            List<Toolbar> toolbarList = new ArrayList();
            XPath xpath = XPath.newInstance("/toolbars/display//toolbar");
            List<Element> toolbarElements = getElementListByXPath(xpath);
            logger.debug("Nb. toolbars: " + toolbarElements.size());
            int index = 0;
            for (Element currentToolbarElement : toolbarElements) {
                Toolbar toolbar = new Toolbar();
                toolbar.setIndex(index);

                // set name
                String name = currentToolbarElement.getAttributeValue("name");
                toolbar.setName(name);

                // set type
                String type = currentToolbarElement.getAttributeValue("ref-type");
                toolbar.setType(type);

                // set state
                String state = currentToolbarElement.getAttributeValue("state");
                toolbar.setState(state != null && state.length() > 0 ? state : "top");

                // set displayed
                String displayed = currentToolbarElement.getAttributeValue("displayed");
                try {
                    if (displayed != null) {
                        toolbar.setDisplayed(Boolean.parseBoolean(displayed));
                    } else {
                        toolbar.setDisplayed(true);
                    }
                } catch (Exception e) {
                    logger.error("displayed attribute,'" + displayed + "', is not a boolean");
                    toolbar.setDisplayed(true);
                }


                // set mandatory
                String mandatory = currentToolbarElement.getAttributeValue("mandatory");
                try {
                    if (mandatory != null) {
                        toolbar.setMandatory(Boolean.parseBoolean(mandatory));
                    } else {
                        toolbar.setMandatory(true);
                    }
                } catch (Exception e) {
                    logger.error("mandatory attribute,'" + mandatory + "', is not a boolean");
                    toolbar.setMandatory(true);
                }

                // set display-title
                String displayTitle = currentToolbarElement.getAttributeValue("display-title");
                try {
                    if (displayTitle != null) {
                        toolbar.setDisplayTitle(Boolean.parseBoolean(displayTitle));
                    } else {
                        toolbar.setDisplayTitle(false);
                    }
                } catch (Exception e) {
                    logger.error("display-title attribute,'" + displayed + "', is not a boolean");
                    toolbar.setDisplayTitle(false);
                }

                // set draggable
                String draggable = currentToolbarElement.getAttributeValue("draggable");
                if (draggable != null) {
                    toolbar.setDraggable(Boolean.parseBoolean(draggable));
                } else {
                    toolbar.setDraggable(true);
                }

                // laod rest of toolbar
                loadToolbarDefinition(toolbar);

                // add it to toolbarSet
                toolbarList.add(toolbar);
                index++;
            }
            toolbarSet.setToolbarList(toolbarList);


            return toolbarSet;
        } catch (JDOMException e) {
            logger.error(e, e);
        }

        return null;
    }

    private Visibility getVisibility(Element currentToolbarElement) {
        Visibility visibility = null;
        Element visibilityEle = currentToolbarElement.getChild("visibility");
        if (visibilityEle != null) {
            logger.debug("visibility element found.");
            visibility = new Visibility();
            visibility.setMode(visibilityEle.getAttributeValue("mode"));
            visibility.setPageACL(visibilityEle.getAttributeValue("permission"));
            visibility.setServerActionPermission(visibilityEle.getAttributeValue("server-action-permission"));
            visibility.setSiteActionPermission(visibilityEle.getAttributeValue("site-action-permission"));
            String authentication = visibilityEle.getAttributeValue("authentication");
            visibility.setNeedAuthentication(authentication);
            String value = visibilityEle.getAttributeValue("value");
            visibility.setValue(value);
            Element valueResolverEle = visibilityEle.getChild("value-resolver");
            if (valueResolverEle != null) {
                visibility.setClassResolver(valueResolverEle.getAttributeValue("class"));
                visibility.setInputResolver(valueResolverEle.getAttributeValue("input"));
            }
        } else {
            logger.debug("visibility element not found.");
        }
        return visibility;
    }


    /**
     * Load toolbar definition
     *
     * @param toolbar
     * @throws JDOMException
     */
    private void loadToolbarDefinition(Toolbar toolbar) throws JDOMException {
        String toolbarXpath = "/toolbars/custom-toolbar/toolbar[@type='" + toolbar.getType() + "']";
        Element toolbarEle = getElementByXpath(XPath.newInstance(toolbarXpath));

        // deal with exends feature
        if (toolbarEle != null) {
            String extendsAttr = toolbarEle.getAttributeValue("extends");
            if (extendsAttr != null) {
                toolbar.setType(extendsAttr);
                if (toolbar.getType() != null) {
                    if (extendsAttr.equalsIgnoreCase(toolbar.getType())) {
                        // avoid cycle
                        logger.warn("Toolbar[" + toolbarXpath + "] can't extends itself.");
                    } else {
                        // udpate type
                        loadToolbarDefinition(toolbar);
                    }
                } else {
                    // case real type not yet loaded
                    loadToolbarDefinition(toolbar);
                }
            }

            // set title key
            Element titleEle = toolbarEle.getChild("title-resourcebundle-key");
            if (titleEle != null) {
                String title = titleEle.getAttributeValue("key");
                if (title != null) {
                    toolbar.setTitleKey(title);
                }
            }


            Visibility visibility = getVisibility(toolbarEle);
            if (visibility == null) {
                visibility = new Visibility();
            }
            visibility.setSiteActionPermission("toolbar." + toolbar.getName());
            toolbar.setVisibility(visibility);

            // add itemsgroup
            List<Element> itemGroupsElements = toolbarEle.getChildren("itemsgroup");
            logger.debug("size: " + itemGroupsElements.size());
            for (Element currentElement : itemGroupsElements) {
                String type = currentElement.getAttributeValue("ref-type");
                ItemsGroup itemsGroup = new ItemsGroup();

                // set separator
                itemsGroup.setSeparator(true);
                String separator = currentElement.getAttributeValue("separator");
                if (separator != null) {
                    try {
                        itemsGroup.setSeparator(Boolean.parseBoolean(separator));
                    } catch (Exception e) {
                        logger.error("separator parameter is not a boolean value.");
                    }
                }
                itemsGroup.setType(type);
                toolbar.addItemsGroup(itemsGroup);
                loadItemsGroupDefinition(itemsGroup);
            }
        } else {
            logger.error("Xpath: " + toolbarXpath + " --> empty. 'reftype'[" + toolbar.getType() + "] reference a 'type' that doesn't exist.");
        }
    }

    /**
     * Load items groups definition
     *
     * @param itemsGroup
     * @throws JDOMException
     */
    private void loadItemsGroupDefinition(ItemsGroup itemsGroup) throws JDOMException {
        // init properties
        String itemsgroupXpath = "/toolbars/custom-itemsgroup/itemsgroup[@type='" + itemsGroup.getType() + "']";
        XPath xpath = XPath.newInstance(itemsgroupXpath);
        Element itemGroupsElement = getElementByXpath(xpath);
        if (itemGroupsElement != null) {
            logger.debug("Xpath: " + itemsgroupXpath + "--> element found");

            // deal with exends feature
            String extendsAttr = itemGroupsElement.getAttributeValue("extends");
            if (extendsAttr != null) {
                itemsGroup.setType(extendsAttr);
                loadItemsGroupDefinition(itemsGroup);
            }

            // load title
            Element titleEle = itemGroupsElement.getChild("title-resourcebundle-key");
            if (titleEle != null) {
                String title = titleEle.getAttributeValue("key");
                if (title != null) {
                    itemsGroup.setTitleKey(title);
                }
            }

            // set layout
            String layout = itemGroupsElement.getAttributeValue("layout");
            int layoutInt = 0;
            if (layout != null) {
                if (layout.equalsIgnoreCase("button")) {
                    layoutInt = ToolbarConstants.ITEMSGROUP_BUTTON;
                } else if (layout.equalsIgnoreCase("label")) {
                    layoutInt = ToolbarConstants.ITEMSGROUP_LABEL;
                } else if (layout.equalsIgnoreCase("button-label")) {
                    layoutInt = ToolbarConstants.ITEMSGROUP_BUTTON_LABEL;
                } else if (layout.equalsIgnoreCase("menu")) {
                    layoutInt = ToolbarConstants.ITEMSGROUP_MENU;
                } else if (layout.equalsIgnoreCase("menu-radio")) {
                    layoutInt = ToolbarConstants.ITEMSGROUP_MENU_RADIO;
                } else if (layout.equalsIgnoreCase("menu-checkbox")) {
                    layoutInt = ToolbarConstants.ITEMSGROUP_MENU_CHECKBOX;
                } else if (layout.equalsIgnoreCase("select")) {
                    layoutInt = ToolbarConstants.ITEMSGROUP_SELECT;
                } else if (layout.equalsIgnoreCase("box")) {
                    layoutInt = ToolbarConstants.ITEMSGROUP_BOX;
                } else if (layout.equalsIgnoreCase("tab")) {
                    layoutInt = ToolbarConstants.ITEMSGROUP_TABS;
                } else {
                    logger.debug("Warning: layout " + itemsGroup.getLayout() + " unknown.");
                }
            } else {
                layoutInt = ToolbarConstants.ITEMSGROUP_BUTTON;
            }
            itemsGroup.setLayout(layoutInt);

            // set icon-style
            Element iconStyleEle = itemGroupsElement.getChild("icon-style");
            if (iconStyleEle != null) {
                String iconStyleMin = iconStyleEle.getAttributeValue("min");
                if (iconStyleMin != null) {
                    itemsGroup.setMinIconStyle(iconStyleMin);
                }
                String iconStyleMax = iconStyleEle.getAttributeValue("max");
                if (iconStyleMax != null) {
                    itemsGroup.setMediumIconStyle(iconStyleMax);
                }
            }

            // set visibility
            Visibility visibility = getVisibility(itemGroupsElement);
            if (visibility != null) {
                itemsGroup.setVisibility(visibility);
            }

            // load items from xml
            List<Element> itemElements = itemGroupsElement.getChildren();
            logger.debug("Xpath: " + itemsgroupXpath + ", Nb. items (not inherited)=" + itemElements.size());
            for (Element currentElement : itemElements) {
                // case of "<item>"
                if (currentElement.getName().equalsIgnoreCase("item")) {
                    String type = currentElement.getAttributeValue("ref-type");
                    Item item = new Item();
                    item.setType(type);
                    itemsGroup.addItem(item);
                    loadItemDefinition(item);
                    // add display Title
                    String displayTitle = currentElement.getAttributeValue("display-title");
                    if (displayTitle != null) {
                        try {
                            item.setDisplayTitle(Boolean.valueOf(displayTitle));
                        } catch (Exception e) {
                            logger.error(currentElement.toString() + "--> display-title is not a boolean value.");
                        }
                    } else {
                        item.setDisplayTitle(true);
                    }
                }
                // case of "<itesm-provider>"
                else if (currentElement.getName().equalsIgnoreCase("items-provider")) {
                    ItemsProvider itemsProvider = new ItemsProvider();

                    //load mandatory prop
                    String classValue = currentElement.getAttributeValue("class");
                    String inputValue = currentElement.getAttributeValue("input");
                    itemsProvider.setClassProvider(classValue);
                    itemsProvider.setInputProvider(inputValue);

                    //load custom properties
                    List<Element> propertyElement = currentElement.getChildren("property");
                    if (propertyElement != null) {
                        for (Element currentPropElement : propertyElement) {
                            Property property = createProperty(currentPropElement);
                            itemsProvider.addProperty(property);
                        }
                    }

                    itemsGroup.addItemsProvider(itemsProvider);
                }


            }
            logger.debug("Xpath: " + itemsgroupXpath + ", Total items=" + itemsGroup.getItemList().size());
        } else {
            logger.error("Xpath: " + itemsgroupXpath + " --> empty. 'reftype'[" + itemsGroup.getType() + "] reference a 'type' that doesn't exist.");
        }
    }


    private void loadItemDefinition(Item item) throws JDOMException {
        // init properties
        String itemXpath = "/toolbars/custom-item/item[@type='" + item.getType() + "']";
        XPath xpath = XPath.newInstance(itemXpath);
        Element itemElement = getElementByXpath(xpath);
        if (itemElement != null) {
            logger.debug("Xpath: " + itemXpath + " --> element found");

            // deal with exends feature
            String extendsAttr = itemElement.getAttributeValue("extends");
            if (extendsAttr != null) {
                item.setType(extendsAttr);
                loadItemDefinition(item);
            }

            // set title
            Element titleEle = itemElement.getChild("title-resourcebundle-key");
            if (titleEle != null) {
                String title = titleEle.getAttributeValue("key");
                if (title != null) {
                    item.setTitleResourceBundleKey(title);
                }
            }

            // set description
            Element descriptionEle = itemElement.getChild("description-resourcebundle-key");
            if (descriptionEle != null) {
                String description = descriptionEle.getAttributeValue("key");
                if (description != null) {
                    item.setDescriptionResourceBundleKey(description);
                }
            }

            //set icon style
            Element iconStyleEle = itemElement.getChild("icon-style");
            if (iconStyleEle != null) {
                String iconStyleMin = iconStyleEle.getAttributeValue("min");
                if (iconStyleMin != null) {
                    item.setMinIconStyle(iconStyleMin);
                }
                String iconStyleMax = iconStyleEle.getAttributeValue("max");
                if (iconStyleMax != null) {
                    item.setMediumIconStyle(iconStyleMax);
                }
            }

            // get selected value
            Element selectedResolveEle = itemElement.getChild("selected-resolver");
            if (selectedResolveEle != null) {
                String classResover = selectedResolveEle.getAttributeValue("class");
                String inputReslover = selectedResolveEle.getAttributeValue("input");
                Selected selected = new Selected();
                selected.setClassResolver(classResover);
                selected.setInputResolver(inputReslover);
                item.setSelected(selected);
            }

            // set visibility
            Visibility visibility = getVisibility(itemElement);
            if (visibility != null) {
                item.setVisibility(visibility);
            }

            // load property
            String itemXpathProperty = itemXpath + "//property";
            xpath = XPath.newInstance(itemXpathProperty);
            List<Element> propertyElement = getElementListByXPath(xpath);
            if (propertyElement != null) {
                for (Element currentElement : propertyElement) {
                    Property property = createProperty(currentElement);
                    item.addPropertyList(property);
                }
            }
        } else {
            logger.error("Xpath: " + itemXpath + " --> empty. 'reftype'[" + item.getType() + "] reference a 'type' that doesn't exist.");
        }
    }

    /**
     * Create a property element
     *
     * @param propertyElement
     * @return
     */
    private Property createProperty(Element propertyElement) {
        Property property = new Property();
        // set name and value
        property.setName(propertyElement.getAttributeValue("name"));
        property.setValue(propertyElement.getAttributeValue("value"));

        // add value provider
        Element valueProviderEle = propertyElement.getChild("value-resolver");
        if (valueProviderEle != null) {
            String valueProviderClass = valueProviderEle.getAttributeValue("class");
            String valueProviderInput = valueProviderEle.getAttributeValue("input");
            property.setClassProvider(valueProviderClass);
            property.setInputProvider(valueProviderInput);
        }
        return property;
    }

}
