package org.jahia.ajax.gwt.client.widget.edit;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.allen_sauer.gwt.log.client.Log;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:04:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleHelper {
    private static Map<String, List<Module>> modulesByPath;
    private static Map<String, Module> modules;

    private static Map<String, List<String>> children;

    public static void initAllModules(MainModule m, HTML html) {
        modules = new HashMap<String, Module>();
        modulesByPath = new HashMap<String, List<Module>>();

        modules.put(m.getModuleId(), m);
        ArrayList<Module> moduleArrayList = new ArrayList<Module>();
        modulesByPath.put(m.getPath(), moduleArrayList);
        moduleArrayList.add(m);

        List<Element> el = TemplatesDOMUtil.getAllJahiaTypedElementsRec(html.getElement());
        for (Element divElement : el) {
            String jahiatype = DOM.getElementAttribute(divElement, JahiaType.JAHIA_TYPE);
            if ("module".equals(jahiatype)) {
                String id = DOM.getElementAttribute(divElement, "id");
                String type = DOM.getElementAttribute(divElement, "type");
                String path = DOM.getElementAttribute(divElement, "path");
                String template = DOM.getElementAttribute(divElement, "template");
                String nodetypes = DOM.getElementAttribute(divElement, "nodetypes");
                Module module = null;
                if (type.equals("list")) {
                    module = new ListModule(id, path, divElement.getInnerHTML(), template, m);
                } else if (type.equals("existingNode")) {
                    module = new SimpleModule(id, path, divElement.getInnerHTML(), template, nodetypes, m);
                } else if (type.equals("placeholder")) {
                    module = new PlaceholderModule(id, path, nodetypes, m);
//                } else if (type.equals("text")) {
//                    module = new TextModule(path, divElement.getInnerHTML(), editManager);
                }
                if (module != null) {
                    if (!modulesByPath.containsKey(path)) {
                        modulesByPath.put(path, new ArrayList<Module>());
                    }
                    modulesByPath.get(path).add(module);
                    modules.put(id, module);
                }
            }
        }

        ArrayList<String> list = new ArrayList<String>();
        for (String s : modulesByPath.keySet()) {
            if (!s.endsWith("*") && !(modulesByPath.get(s) instanceof TextModule)) {
                list.add(s);
            }
        }
        Log.info("all pathes "+list);
        JahiaContentManagementService.App.getInstance().getNodesWithPublicationInfo(list,new AsyncCallback<List<GWTJahiaNode>>() {
            public void onSuccess(List<GWTJahiaNode> result) {
                for (GWTJahiaNode gwtJahiaNode : result) {
                    for (Module module : modulesByPath.get(gwtJahiaNode.getPath())) {
                        Log.info("set object for "+module.getModuleId());
                        module.setNode(gwtJahiaNode);

                    }
                }
            }

            public void onFailure(Throwable caught) {

            }
        });
    }

    public static void buildTree(Module module) {
        String rootId = module.getModuleId();
        Element element = module.getHtml().getElement();
        children = new HashMap<String, List<String>>();

        List<Element> el = TemplatesDOMUtil.getAllJahiaTypedElementsRec(element);
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
                } else if ("module".equals(currentEl.getAttribute(JahiaType.JAHIA_TYPE))) {
                    String id = currentEl.getAttribute("id");
                    if (!children.containsKey(id)) {
                        children.put(id, new ArrayList<String>());
                    }
                    children.get(id).add(divElement.getAttribute("id"));
                    break;
                }
            }
        }
    }

    public static Map<Element, Module> parse(Module module) {
        Map<Element, Module> m = new HashMap<Element, Module>();
        if (module.getHtml() == null) {
            return m;
        }
        List<Element> el = TemplatesDOMUtil.getAllJahiaTypedElementsRec(module.getHtml().getElement());
        for (Element divElement : el) {
            String jahiatype = DOM.getElementAttribute(divElement, JahiaType.JAHIA_TYPE);
            if ("module".equals(jahiatype)) {
                String id = DOM.getElementAttribute(divElement, "id");
                if (children.get(module.getModuleId()).contains(id)) {
                    Module subModule = modules.get(id);

                    if (subModule != null) {
                        m.putAll(parse(subModule));                        
                        m.put(divElement, subModule);
                        divElement.setInnerHTML("");
                        module.getContainer().add(subModule.getContainer());
                        subModule.setParentModule(module);
                    }
                }
            }
        }
        module.onParsed();
        return m;
    }

    public static void move(Map<Element, Module> m) {
        for (Element divElement : m.keySet()) {
            Element moduleElement = m.get(divElement).getContainer().getElement();
            divElement.setInnerHTML("");
            DOM.appendChild(divElement, moduleElement);
        }
    }

    public static Map<String, List<Module>> getModulesByPath() {
        return modulesByPath;
    }
}
