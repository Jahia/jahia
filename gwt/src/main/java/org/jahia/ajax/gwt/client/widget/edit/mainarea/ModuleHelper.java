package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;

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
    private static Map<String, GWTJahiaNodeType> nodeTypes;
    private static Map<String, List<String>> wrappedContentInfo;

    public static void initAllModules(final MainModule m, HTML html) {
        long start = System.currentTimeMillis();
        modules = new ArrayList<Module>();
        modulesById = new HashMap<String, Module>();
        modulesByPath = new HashMap<String, List<Module>>();

        modulesByPath.put(m.getPath(), new ArrayList<Module>());
        modules.add(m);
        modulesByPath.get(m.getPath()).add(m);
        modulesById.put(m.getModuleId(), m);

        wrappedContentInfo = new HashMap<String, List<String>>();

        List<Element> el = TemplatesDOMUtil.getAllJahiaTypedElementsRec(html.getElement());

        Set<String> allNodetypes = new HashSet<String>();

        for (Element divElement : el) {
            String jahiatype = DOM.getElementAttribute(divElement, JahiaType.JAHIA_TYPE);
            if ("module".equals(jahiatype)) {
                String id = DOM.getElementAttribute(divElement, "id");
                String type = DOM.getElementAttribute(divElement, "type");
                String templateInfo = DOM.getElementAttribute(divElement, "templateInfo");
                String path = DOM.getElementAttribute(divElement, "path");
                String template = DOM.getElementAttribute(divElement, "template");
                String nodetypes = DOM.getElementAttribute(divElement, "nodetypes");
                String referenceTypes = DOM.getElementAttribute(divElement, "referenceTypes");
                String scriptInfo = DOM.getElementAttribute(divElement, "scriptInfo");
                Module module = null;
                if (type.equals("area")) {
                    module = new AreaModule(id, path, divElement.getInnerHTML(), template, scriptInfo, nodetypes, referenceTypes, templateInfo, m);
                } else if (type.equals("list")) {
                    module = new ListModule(id, path, divElement.getInnerHTML(), template, scriptInfo, nodetypes, referenceTypes, templateInfo, m);
                } else if (type.equals("existingNode")) {
                    module = new SimpleModule(id, path, divElement.getInnerHTML(), template, scriptInfo, nodetypes, referenceTypes, templateInfo, m);
                } else if (type.equals("placeholder")) {
                    module = new PlaceholderModule(id, path, nodetypes, referenceTypes, m);
                } else if (type.equals("linker")) {
                    module = new LinkerModule(id, path, divElement.getInnerHTML(), template, scriptInfo, nodetypes, referenceTypes, templateInfo, m);
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
            } if ("wrappedContentInfo".equals(jahiatype)) {
                String wrappedNode = DOM.getElementAttribute(divElement, "wrappedNode");
                String wrapperContent = DOM.getElementAttribute(divElement, "wrapperContent");
                if (!wrappedContentInfo.containsKey(wrappedNode)) {
                    wrappedContentInfo.put(wrappedNode, new ArrayList<String>());
                }
                wrappedContentInfo.get(wrappedNode).add(wrapperContent);
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
        JahiaContentManagementService.App.getInstance().getNodesWithPublicationInfo(list,new BaseAsyncCallback<List<GWTJahiaNode>>() {
            public void onSuccess(List<GWTJahiaNode> result) {
                for (GWTJahiaNode gwtJahiaNode : result) {
                    final List<Module> moduleList = modulesByPath.get(gwtJahiaNode.getPath());
                    if (moduleList != null) {
                        for (Module module : moduleList) {
                            module.setNode(gwtJahiaNode);
                        }
                    }
                }
                m.getEditLinker().handleNewModuleSelection();
            }

            public void onApplicationFailure(Throwable caught) {
                Log.error("Unable to get node with publication info due to:",caught);
            }
        });
        JahiaContentDefinitionService.App.getInstance().getNodeTypes(new ArrayList<String>(allNodetypes), new BaseAsyncCallback<List<GWTJahiaNodeType>>() {
            public void onSuccess(List<GWTJahiaNodeType> result) {
                nodeTypes = new HashMap<String, GWTJahiaNodeType>();
                for (GWTJahiaNodeType type : result) {
                    nodeTypes.put(type.getName(), type);
                }
            }

            public void onApplicationFailure(Throwable caught) {
                Log.error("Unable to get nodetypes :",caught);
            }
        });
        Log.info("Parsing : "+(System.currentTimeMillis() - start));
    }

    public static void buildTree(Module module) {
        long start = System.currentTimeMillis();
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
        Log.info("Build tree : "+(System.currentTimeMillis() - start));
    }

    public static Map<Element, Module> parse(Module module, Module parent) {
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
                        m.putAll(parse(subModule, module));
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
            DOM.appendChild(divElement, moduleElement);
        }
        Log.info("Move : "+(System.currentTimeMillis() - start));
    }

    public static List<Module> getModules() {
        return modules;
    }

    public static void tranformLinks(final HTML html) {
        long start = System.currentTimeMillis();
        String baseUrl = JahiaGWTParameters.getBaseUrl();
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
                String params = "";
                if (path.indexOf('?') > 0) {
                    params = path.substring(path.indexOf('?')+1);
                    path = path.substring(0,path.indexOf('?')); 
                }
                if (path.indexOf('.') > -1) {
                    path = path.substring(0,path.indexOf('.'));
                }
                DOM.setElementAttribute(element,"href","#");
                if (template == null) {
                    DOM.setElementAttribute(element,"onclick","window.goTo('"+path+"',null,'"+params+"')");
                } else {
                    DOM.setElementAttribute(element,"onclick","window.goTo('"+path+"','"+template+"','"+params+"')");
                }
            }
        }
        Log.info("Transform links : "+(System.currentTimeMillis() - start));
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

    public static Map<String, List<String>> getWrappedContentInfo() {
        return wrappedContentInfo;
    }
}
