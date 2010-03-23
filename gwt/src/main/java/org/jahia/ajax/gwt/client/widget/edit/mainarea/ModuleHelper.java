package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;
import org.jahia.ajax.gwt.client.widget.form.CalendarField;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:04:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModuleHelper {
    private static List<Module> modules;
    private static Map<String, List<Module>> modulesByPath;
    private static Map<String, Module> modulesById;

    private static Map<String, List<String>> children;

    public static void initAllModules(final MainModule m, HTML html) {
        modules = new ArrayList<Module>();
        modulesById = new HashMap<String, Module>();
        modulesByPath = new HashMap<String, List<Module>>();

        modulesByPath.put(m.getPath(), new ArrayList<Module>());
        modules.add(m);
        modulesByPath.get(m.getPath()).add(m);
        modulesById.put(m.getModuleId(), m);

        List<Element> el = TemplatesDOMUtil.getAllJahiaTypedElementsRec(html.getElement());
        for (Element divElement : el) {
            String jahiatype = DOM.getElementAttribute(divElement, JahiaType.JAHIA_TYPE);
            if ("module".equals(jahiatype)) {
                String id = DOM.getElementAttribute(divElement, "id");
                String type = DOM.getElementAttribute(divElement, "type");
                String path = DOM.getElementAttribute(divElement, "path");
                String template = DOM.getElementAttribute(divElement, "template");
                String nodetypes = DOM.getElementAttribute(divElement, "nodetypes");
                String scriptInfo = DOM.getElementAttribute(divElement, "scriptInfo");
                Module module = null;
                if (type.equals("area")) {
                    module = new AreaModule(id, path, divElement.getInnerHTML(), template, scriptInfo, nodetypes, m);
                }
                else if (type.equals("list")) {
                    module = new ListModule(id, path, divElement.getInnerHTML(), template, scriptInfo, m);
                } else if (type.equals("existingNode")) {
                    module = new SimpleModule(id, path, divElement.getInnerHTML(), template, scriptInfo, nodetypes, m);
                } else if (type.equals("placeholder")) {
                    module = new PlaceholderModule(id, path, nodetypes, m);
//                } else if (type.equals("text")) {
//                    module = new TextModule(path, divElement.getInnerHTML(), editManager);
                }
                if (module != null) {
                    if (!modulesByPath.containsKey(path)) {
                        modulesByPath.put(path, new ArrayList<Module>());
                    }
                    modules.add(module);
                    modulesByPath.get(path).add(module);
                    modulesById.put(id, module);
                }
            }
        }

        ArrayList<String> list = new ArrayList<String>();
        for (String s : modulesByPath.keySet()) {
            if (!s.endsWith("*")) {
                list.add(s);
            }
        }
        if (Log.isDebugEnabled()) {
            Log.debug("all pathes "+list);
        }
        JahiaContentManagementService.App.getInstance().getNodesWithPublicationInfo(list,new AsyncCallback<List<GWTJahiaNode>>() {
            public void onSuccess(List<GWTJahiaNode> result) {
                for (GWTJahiaNode gwtJahiaNode : result) {
                    for (Module module : modulesByPath.get(gwtJahiaNode.getPath())) {
                        if (Log.isDebugEnabled()) {
                            Log.debug("set object for "+module.getModuleId());
                        }
                        module.setNode(gwtJahiaNode);
                    }
                }
                m.getEditLinker().handleNewModuleSelection();
            }

            public void onFailure(Throwable caught) {
                Log.error("Unable to get node with publication info due to:",caught);

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
                    Module subModule = modulesById.get(id);

                    if (subModule != null) {
                        subModule.setDepth(module.getDepth() + 1);
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

    public static List<Module> getModules() {
        return modules;
    }

    public static Map<String, List<Module>> getModulesByPath() {
        return modulesByPath;
    }

    public static void tranformLinks(final HTML html) {
        String baseUrl = JahiaGWTParameters.getParam(JahiaGWTParameters.BASE_URL);
        List<Element> el = getAllLinks(html.getElement());
        for (Element element : el) {
            String link = DOM.getElementAttribute(element, "href");
            if (link.startsWith(baseUrl)) {
                String path = link.substring(baseUrl.length());
                String template = path.substring(path.indexOf('.')+1);
                if (template.contains(".")) {
                    template = template.substring(0, template.lastIndexOf('.'));
                } else {
                    template = null;
                }
                path = path.substring(0,path.indexOf('.'));
                DOM.setElementAttribute(element,"href","#");
                if (template == null) {
                    DOM.setElementAttribute(element,"onclick","window.goTo('"+path+"',null)");
                } else {
                    DOM.setElementAttribute(element,"onclick","window.goTo('"+path+"','"+template+"')");
                }
            }
        }
    }

    public static List<Element> getAllLinks(Element parent) {
        List<Element> list = new ArrayList<Element>();
        int nb = DOM.getChildCount(parent);

        if (parent.getNodeName().toUpperCase().equals("A")) {
            String link = DOM.getElementAttribute(parent, "href");
            if (link != null && link.length() > 0) {
                list.add(parent);
            }
        }
        for (int i = 0; i < nb; i++) {
            list.addAll(getAllLinks(DOM.getChild(parent, i)));
        }
        return list;
    }

}
