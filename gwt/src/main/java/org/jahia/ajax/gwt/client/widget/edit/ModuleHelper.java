package org.jahia.ajax.gwt.client.widget.edit;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.HTML;
import com.extjs.gxt.ui.client.widget.LayoutContainer;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:04:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleHelper {
    private static Map<String, Module> modules;

    private static Map<String, List<String>> children;

    public static void initAllModules(HTML html, EditManager editManager) {
        modules = new HashMap<String, Module>();
        List<Element> el = TemplatesDOMUtil.getAllJahiaTypedElementsRec(html.getElement());
        for (Element divElement : el) {
            String jahiatype = DOM.getElementAttribute(divElement, JahiaType.JAHIA_TYPE);
            if ("placeholder".equals(jahiatype)) {
                String type = DOM.getElementAttribute(divElement, "type");
                String path = DOM.getElementAttribute(divElement, "path");
                Module module = null;
                if (type.equals("list")) {
                    module = new ListModule(path, divElement.getInnerHTML(), editManager);
                } else if (type.equals("existingNode")) {
                    module = new SimpleModule(path, divElement.getInnerHTML(), editManager);
                } else if (type.equals("placeholder")) {
                    module = new PlaceholderModule(path, editManager);
                }
                if (module != null) {
                    modules.put(path, module);
                }
            }
        }

        ArrayList<String> list = new ArrayList<String>();
        for (String s : modules.keySet()) {
            if (!s.endsWith("*")) {
                list.add(s);
            }
        }
        JahiaContentManagementService.App.getInstance().getNodes(list,new AsyncCallback<List<GWTJahiaNode>>() {
            public void onSuccess(List<GWTJahiaNode> result) {
                for (GWTJahiaNode gwtJahiaNode : result) {
                    modules.get(gwtJahiaNode.getPath()).setNode(gwtJahiaNode);
                }
            }

            public void onFailure(Throwable caught) {

            }
        });
    }

    public static void buildTree(Module module) {
        String rootPath = module.getPath();
        Element element = module.getHtml().getElement();
        children = new HashMap<String, List<String>>();

        List<Element> el = TemplatesDOMUtil.getAllJahiaTypedElementsRec(element);
        for (Element divElement : el) {
            Element currentEl = divElement;
            while (currentEl != null) {
                currentEl = DOM.getParent(currentEl);
                if (currentEl == element) {
                    if (!children.containsKey(rootPath)) {
                        children.put(rootPath, new ArrayList<String>());
                    }
                    children.get(rootPath).add(divElement.getAttribute("path"));

                    break;
                } else if ("placeholder".equals(currentEl.getAttribute(JahiaType.JAHIA_TYPE))) {
                    String path = currentEl.getAttribute("path");
                    if (!children.containsKey(path)) {
                        children.put(path, new ArrayList<String>());
                    }
                    children.get(path).add(divElement.getAttribute("path"));

                    break;
                }
            }
        }
    }

    public static Map<Element, Module> parse(Module module) {
        Map<Element, Module> m = new HashMap<Element, Module>();
        List<Element> el = TemplatesDOMUtil.getAllJahiaTypedElementsRec(module.getHtml().getElement());
        for (Element divElement : el) {
            String jahiatype = DOM.getElementAttribute(divElement, JahiaType.JAHIA_TYPE);
            if ("placeholder".equals(jahiatype)) {
                String path = DOM.getElementAttribute(divElement, "path");
                if (children.get(module.getPath()).contains(path)) {
                    Module subModule = modules.get(path);

                    if (subModule != null) {
                        subModule.parse();
                        m.put(divElement, subModule);
                        divElement.setInnerHTML("");
                        module.getContainer().add(subModule.getContainer());
                        subModule.setParentModule(module);
                    }
                }
            }
        }
        return m;
    }

    public static void move(Map<Element, Module> m) {
        for (Element divElement : m.keySet()) {
            Element moduleElement = m.get(divElement).getContainer().getElement();
            divElement.setInnerHTML("");
            DOM.appendChild(divElement, moduleElement);
        }
    }

}
