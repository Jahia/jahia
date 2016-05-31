/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.InfoConfig;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.toolbar.GWTEditConfiguration;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.*;

/**
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:04:41 PM
 */
public class ModuleHelper {
    private static final List<String> FIELDS = Arrays.asList(GWTJahiaNode.LOCKS_INFO, GWTJahiaNode.PERMISSIONS, GWTJahiaNode.WORKFLOW_INFO, GWTJahiaNode.VISIBILITY_INFO, GWTJahiaNode.SUBNODES_CONSTRAINTS_INFO);

    private static final List<String> FIELDS_FULL_INFO = Arrays.asList(GWTJahiaNode.LOCKS_INFO, GWTJahiaNode.PERMISSIONS, GWTJahiaNode.PUBLICATION_INFO, GWTJahiaNode.WORKFLOW_INFO, GWTJahiaNode.VISIBILITY_INFO, GWTJahiaNode.SUBNODES_CONSTRAINTS_INFO);

    public final static String JAHIA_TYPE = "jahiatype";
    private static final int AREA_CREATED_NOTIFICATION_DELAY_MS = 10000;

    private static List<Module> modules;
    private static String mainPath;
    private static Map<String, List<Module>> modulesByPath;
    private static Map<String, Module> modulesById;

    private static Map<String, List<String>> children;
    private static Map<String, GWTJahiaNodeType> nodeTypes = new HashMap<String, GWTJahiaNodeType>();
    private static Map<String, List<String>> linkedContentInfo;
    private static Map<String, String> linkedContentInfoType;
    private static Map<String, List<SimpleModule>> simpleModules = new HashMap<String, List<SimpleModule>>();
    private static boolean parsed;
    private static Map<String,List<? extends ModelData>> nodesAndTypes;

    /**
     * Recursive method to retrieve all the jahia typed elements.
     *
     * @param parent the element to search into
     * @return a list of jahia typed elements
     */
    public static List<Element> getAllJahiaTypedElementsRec(Element parent) {
        List<Element> list = new ArrayList<Element>();
        int nb = DOM.getChildCount(parent);
        String type = DOM.getElementAttribute(parent, JAHIA_TYPE);
        if (type != null && type.length() > 0) {
            list.add(parent);
        }
        for (int i = 0; i < nb; i++) {
            list.addAll(getAllJahiaTypedElementsRec(DOM.getChild(parent, i)));
        }
        return list;
    }

    public static void initAllModules(final MainModule mainModule, Element htmlElement, List<Element> el, List<Element> elBody, GWTEditConfiguration editModeConfig) {
        long start = System.currentTimeMillis();
        modules = new ArrayList<Module>();
        modulesById = new HashMap<String, Module>();
        modulesByPath = new HashMap<String, List<Module>>();

        modules.add(mainModule);
        modulesById.put(mainModule.getModuleId(), mainModule);

        linkedContentInfo = new HashMap<String, List<String>>();
        linkedContentInfoType = new HashMap<String, String>();

        mainPath = null;
        boolean areaEnabled = false;
        String mainTemplate = null;

        Set<String> allNodetypes = new HashSet<String>();
        for (Element divElement : elBody) {
            String jahiatype = DOM.getElementAttribute(divElement, JAHIA_TYPE);
            if ("module".equals(jahiatype)) {
                String id = DOM.getElementAttribute(divElement, "id");
                String type = DOM.getElementAttribute(divElement, "type");
                String path = DOM.getElementAttribute(divElement, "path");
                String nodetypes = DOM.getElementAttribute(divElement, "nodetypes");
                areaEnabled = areaEnabled || "true".equals(DOM.getElementAttribute(divElement, "areaAutoEnabled"));
                Module module = null;
                if (type.equals("main")) {
                } else if (type.equals("area") || type.equals("absoluteArea")) {
                    module = new AreaModule(id, path, divElement, type, mainModule);
                } else if (type.equals("list")) {
                    module = new ListModule(id, path, divElement, mainModule);
                } else if (type.equals("existingNode")) {
                    module = new SimpleModule(id, path, divElement, mainModule, false);
                    addSimpleModule(simpleModules, path, (SimpleModule) module);
                } else if (type.equals("existingNodeWithHeader")) {
                    module = new SimpleModule(id, path, divElement, mainModule, true);
                    addSimpleModule(simpleModules, path, (SimpleModule) module);
                } else if (type.equals("placeholder")) {
                    module = new PlaceholderModule(id, path, divElement, mainModule);
                }
                allNodetypes.addAll(Arrays.asList(nodetypes.split(" ")));
                if (module != null) {
                    if (!modulesByPath.containsKey(path)) {
                        modulesByPath.put(path, new ArrayList<Module>());
                    }
                    modules.add(module);
                    modulesByPath.get(path).add(module);
                    modulesById.put(id, module);
                }
            } else if ("mainmodule".equals(jahiatype)) {
                mainPath = divElement.getAttribute("path");
                mainTemplate = divElement.getAttribute("template");
                modulesByPath.put(mainPath, new ArrayList<Module>());
                modulesByPath.get(mainPath).add(mainModule);
                // In case of a switch of main module nodetypes need to be injected to have the last ones too
                String nodetypes = DOM.getElementAttribute(divElement, "nodetypes");
                allNodetypes.addAll(Arrays.asList(nodetypes.split(" ")));
                mainModule.setNodeTypes(nodetypes);
                mainModule.setReferenceTypes(DOM.getElementAttribute(divElement, "referencetypes"));
                mainModule.setMainModuleElement(divElement);
            } else if ("linkedContentInfo".equals(jahiatype)) {
                String linkedNode = DOM.getElementAttribute(divElement, "linkedNode");
                String node = DOM.getElementAttribute(divElement, "node");
                String type = DOM.getElementAttribute(divElement, "type");
                if (!linkedContentInfo.containsKey(linkedNode)) {
                    linkedContentInfo.put(linkedNode, new ArrayList<String>());
                }
                linkedContentInfo.get(linkedNode).add(node);
                linkedContentInfoType.put(node, type);
            }
        }

        if (areaEnabled) {
            InfoConfig ic = new InfoConfig(Messages.get("created.area.title", "Area(s) created"), Messages.get("created.area.desc", "One or more areas have been enabled, the publication status of the page has changed"));
            ic.display = AREA_CREATED_NOTIFICATION_DELAY_MS;
            Info.display(ic);
        }

        ArrayList<String> list = new ArrayList<String>();
        for (String s : modulesByPath.keySet()) {
            if (!s.endsWith("*")) {
                list.add(s);
            }
        }
        if (Log.isDebugEnabled()) {
            GWT.log("all pathes " + list);
        }

        final String fmainPath = mainPath;
        final String fmainTemplate = mainTemplate;

        List<ModelData> params = new ArrayList<ModelData>();
        if (editModeConfig.isUseFullPublicationInfoInMainAreaModules()) {
            BaseModelData modelData1 = new BaseModelData();
            modelData1.set("paths", list);
            modelData1.set("fields", FIELDS_FULL_INFO);
            params.add(modelData1);
        } else {
            list.remove(mainPath);
    
            BaseModelData modelData1 = new BaseModelData();
            modelData1.set("paths", list);
            modelData1.set("fields", FIELDS);
            params.add(modelData1);
            BaseModelData modelData2 = new BaseModelData();
            modelData2.set("paths", Arrays.asList(mainPath));
            modelData2.set("fields", FIELDS_FULL_INFO);
            params.add(modelData2);
        }
        parsed = false;
        nodesAndTypes = null;
        JahiaContentManagementService.App.getInstance()
                .getNodesAndTypes(params, new ArrayList<String>(allNodetypes),
                        new BaseAsyncCallback<Map<String, List<? extends ModelData>>>() {
                            public void onSuccess(Map<String, List<? extends ModelData>> result) {
                                if (mainPath.equals(fmainPath)) {
                                    nodesAndTypes = result;
                                    if (parsed) {
                                        handleNodesAndTypesResult(result, mainModule, fmainPath, fmainTemplate);
                                    }
                                }
                            }
                            public void onApplicationFailure(Throwable caught) {
                                Log.error("Unable to get node with publication info due to:", caught);
                            }
                        });

        buildTree(mainModule, el);
        mainModule.parse(el);
        if (nodesAndTypes != null) {
            handleNodesAndTypesResult(nodesAndTypes, mainModule, fmainPath, fmainTemplate);
        } else {
            parsed = true;
        }


        GWT.log("Parsing : " + (System.currentTimeMillis() - start) + " ms");
    }

    private static void handleNodesAndTypesResult(Map<String,List<? extends ModelData>> nodesAndTypes, MainModule mainModule, String fmainPath, String fmainTemplate) {
        List<GWTJahiaNodeType> types = (List<GWTJahiaNodeType>) nodesAndTypes.get("types");
        for (GWTJahiaNodeType type : types) {
            if (type != null) {
                ModuleHelper.nodeTypes.put(type.getName(), type);
            }
        }

        List<GWTJahiaNode> nodes = (List<GWTJahiaNode>) nodesAndTypes.get("nodes");
        for (GWTJahiaNode gwtJahiaNode : nodes) {
            setNodeForModule(gwtJahiaNode);
        }

        for (Map.Entry<String, List<SimpleModule>> entry : simpleModules.entrySet()) {
            for (SimpleModule simpleModule : entry.getValue()) {
                simpleModule.removeAll();
            }
        }

        for (Module module : modules) {
            module.onNodeTypesLoaded();
        }
        mainModule.getEditLinker().onMainSelection(fmainPath, fmainTemplate);
        mainModule.getEditLinker().handleNewMainSelection();
        mainModule.refreshInfoLayer();
    }

    public static void setNodeForModule(GWTJahiaNode gwtJahiaNode) {
        final List<Module> moduleList = modulesByPath.get(gwtJahiaNode.getPath());
        simpleModules.remove(gwtJahiaNode.getPath());
        if (moduleList != null) {
            for (Module module : moduleList) {
                module.setNode(gwtJahiaNode);
            }
        }
    }

    public static void buildTree(Module module, List<Element> el) {
        long start = System.currentTimeMillis();
        String rootId = module.getModuleId();
        Element element = module.getInnerElement();
        children = new HashMap<String, List<String>>();

        for (Element divElement : el) {
            Element currentEl = divElement;
            while (currentEl != null) {
                currentEl = DOM.getParent(currentEl);
                if (currentEl == element) {
                    if (!children.containsKey(rootId)) {
                        children.put(rootId, new ArrayList<String>());
                    }
                    children.get(rootId).add(divElement.getAttribute("id"));
                    break;
                } else if ("module".equals(currentEl.getAttribute(JAHIA_TYPE))) {
                    String id = currentEl.getAttribute("id");
                    if (!children.containsKey(id)) {
                        children.put(id, new ArrayList<String>());
                    }
                    children.get(id).add(divElement.getAttribute("id"));
                    break;
                }
            }
        }
        GWT.log("Build tree : " + (System.currentTimeMillis() - start) + " ms");
    }

    public static Map<Element, Module> parse(Module module, Module parent, List<Element> el) {
        Map<Element, Module> m = new HashMap<Element, Module>();
        GWT.log("size : "+el.size());
        for (Element divElement : el) {
            String jahiatype = DOM.getElementAttribute(divElement, JAHIA_TYPE);
            if ("module".equals(jahiatype)) {
                String id = DOM.getElementAttribute(divElement, "id");
                if (children.get(module.getModuleId()).contains(id)) {
                    Module subModule = modulesById.get(id);

                    if (subModule != null) {
                        subModule.setDepth(module.getDepth() + 1);
                        m.putAll(parse(subModule, module, getAllJahiaTypedElementsRec(subModule.getInnerElement())));
                        m.put(divElement, subModule);
                        divElement.setInnerHTML("");
                        module.getContainer().add(subModule.getContainer());
                    }
                }
            }
        }
        module.setParentModule(parent);
        module.onParsed();
        return m;
    }

    public static void move(Map<Element, Module> m) {
        long start = System.currentTimeMillis();
        for (Element divElement : m.keySet()) {
            Element moduleElement = m.get(divElement).getContainer().getElement();
            divElement.setInnerHTML("");
//            Element oldParent = DOM.getParent(divElement);
//            DOM.appendChild(DOM.clone(divElement, true), moduleElement);
//            DOM.removeChild(oldParent, divElement);
            DOM.appendChild(divElement, moduleElement);

        }
        GWT.log("Move : " + (System.currentTimeMillis() - start) + " ms");
    }


    public static void deleteAll(Map<Element, Module> m) {
        for (Element element : m.keySet()) {
            element.removeFromParent();
        }
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static Map<String, List<Module>> getModulesByPath() {
        return modulesByPath;
    }

    public static Map<String, Module> getModulesById() {
        return modulesById;
    }

    public static void tranformLinks(Element htmlElement) {
        long start = System.currentTimeMillis();
        String baseUrl = JahiaGWTParameters.getBaseUrl();

        String mode = baseUrl.substring(baseUrl.indexOf("/cms/")+5);
        String path = mode.substring(mode.indexOf("/"));
        mode = mode.substring(0, mode.indexOf("/"));
        baseUrl = JahiaGWTParameters.getContextPath() + "/cms/" + mode + "frame" + path;

        List<Element> el = getAllLinks(htmlElement);
        for (Element element : el) {
            String link = DOM.getElementAttribute(element, "href");
            if (link.startsWith(baseUrl)) {
                DOM.setElementAttribute(element, "href", "#" + link);
                DOM.setElementAttribute(element, "onclick", "window.parent.goToUrl('"+link+"')");
            }
        }
        GWT.log("Transform links : " + (System.currentTimeMillis() - start) + " ms");
    }

    public static List<Element> getAllLinks(Element parent) {
        List<Element> list = new ArrayList<Element>();
        int nb = DOM.getChildCount(parent);

        if (parent.getNodeName().toUpperCase().equals("A")) {
            try {
                String link = DOM.getElementAttribute(parent, "href");
                if (link != null && link.length() > 0) {
                    list.add(parent);
                }
            } catch (Exception e) {
                if (e != null) {
                    Log.error(e.getMessage(), e);
                }
            }
        }
        for (int i = 0; i < nb; i++) {
            list.addAll(getAllLinks(DOM.getChild(parent, i)));
        }
        return list;
    }

    public static Map<String, List<String>> getLinkedContentInfo() {
        return linkedContentInfo;
    }

    public static Map<String, String> getLinkedContentInfoType() {
        return linkedContentInfoType;
    }

    public static GWTJahiaNodeType getNodeType(String nodeType) {
        return nodeTypes.get(nodeType);
    }

    private static void addSimpleModule(Map<String, List<SimpleModule>> simpleModules, String path, SimpleModule simpleModule) {
        if (!simpleModules.containsKey(path)) {
            simpleModules.put(path,new ArrayList<SimpleModule>());
        }
        simpleModules.get(path).add(simpleModule);
    }
}
