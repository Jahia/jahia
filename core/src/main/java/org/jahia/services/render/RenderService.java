package org.jahia.services.render;

import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.ContainerBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.operations.valves.EngineValve;
import org.jahia.params.ParamBean;
import org.jahia.services.JahiaService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.decorator.JCRJahiaContentNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * Service to render node
 *  
 * @author toto
 *
 */
public class RenderService extends JahiaService {

    private static volatile RenderService instance;
    
    private static final Logger logger = Logger.getLogger(RenderService.class);

    public synchronized static RenderService getInstance() {
        if (instance == null) {
            instance = new RenderService();
        }
        return instance;
    }

    private JCRStoreService storeService;

    private Collection<ScriptResolver> scriptResolvers;

    public JCRStoreService getStoreService() {
        return storeService;
    }

    public void setStoreService(JCRStoreService storeService) {
        this.storeService = storeService;
    }

    public void setScriptResolvers(Collection<ScriptResolver> scriptResolvers) {
        this.scriptResolvers = scriptResolvers;
    }

    public void start() throws JahiaInitializationException {

    }

    public void stop() throws JahiaException {

    }

    /**
     * Render a specific resource and returns it as a StringBuffer.
     *
     * @param resource Resource to display
     * @param context The render context
     * @return The rendered result
     * @throws RepositoryException when jcr issue
     * @throws IOException when input/output issue
     * @throws TemplateNotFoundException when template issue
     */
    public String render(Resource resource, RenderContext context) throws RepositoryException, TemplateNotFoundException, IOException {
        final HttpServletRequest request = context.getRequest();

        Script script = resolveScript(resource, context);

        request.setAttribute("renderContext", context);

        Map<String, Object> old = new HashMap<String, Object>();
        JCRNodeWrapper node = resource.getNode();

        pushAttribute(request, "currentNode", node, old);

        pushAttribute(request, "workspace", node.getSession().getWorkspace().getName(), old);
        pushAttribute(request, "locale", node.getSession().getWorkspace().getName(), old);

        pushAttribute(request, "currentResource", resource, old);
        pushAttribute(request, "scriptInfo", script.getInfo(), old);

        if (node.isNodeType("jnt:contentList")) {
            if (context.getModuleParams().containsKey("forcedSubNodesTemplate")) {
                pushAttribute(request, "subNodesTemplate",  context.getModuleParams().get("forcedSubNodesTemplate"), old);
            } else if (node.hasProperty("j:subNodesTemplate")) {
                pushAttribute(request, "subNodesTemplate", node.getProperty("subNodeTemplate"), old);
            } else if (context.getModuleParams().containsKey("subNodesTemplate")) {
                pushAttribute(request, "subNodesTemplate",  context.getModuleParams().get("subNodesTemplate"), old);
            }
        } else if (node.isNodeType("jnt:nodeReference")) {
            if (context.getModuleParams().containsKey("forcedReferenceTemplate")) {
                pushAttribute(request, "referenceTemplate", context.getModuleParams().get("forcedReferenceTemplate"), old);
            } else if (node.hasProperty("j:referenceTemplate")) {
                pushAttribute(request, "referenceTemplate",  node.getProperty("referenceTemplate"), old);
            } else if (context.getModuleParams().containsKey("referenceTemplate")) {
                pushAttribute(request, "referenceTemplate", context.getModuleParams().get("referenceTemplate"), old);
            }
        }

        pushAttribute(request, "url",new URLGenerator(context, resource, storeService), old);
        if (!context.getModuleParams().containsKey("withoutSkins")) {
            if (context.getModuleParams().containsKey("forcedSkin")) {
                resource.pushWrapper((String) context.getModuleParams().get("forcedSkin"));
            } else if (node.hasProperty("j:skin")) {
                resource.pushWrapper(node.getPropertyAsString("j:skin"));
            } else if (context.getModuleParams().containsKey("skin")) {
                resource.pushWrapper((String) context.getModuleParams().get("skin"));
            }
        }
        ExtendedNodeType[] mixinNodeTypes = null;

        if (!context.getModuleParams().containsKey("withoutOptions")) {
            mixinNodeTypes = node.getMixinNodeTypes();
            if (mixinNodeTypes != null && mixinNodeTypes.length > 0) {
                for (ExtendedNodeType mixinNodeType : mixinNodeTypes) {
                    final String[] supertypeNames = mixinNodeType.getDeclaredSupertypeNames();
                    for (String supertypeName : supertypeNames) {
                        if(supertypeName.equals("jmix:option") && hasTemplate(mixinNodeType, "hidden.options.wrapper"))  {
                            resource.addOption("hidden.options.wrapper",mixinNodeType);
                        }
                    }
                }
            }
        }


        String output;
        try {
            setJahiaAttributes(request, node, (ParamBean) Jahia.getThreadParamBean());
            String render = script.execute();
            String options = renderOptions(resource, context, node, "");
            if(!context.getModuleParams().containsKey("renderOptionsBefore")) {
                output = render+options;
            } else {
                output = options+render;
            }
            while (resource.hasWrapper()) {
                String wrapper = resource.popWrapper();
                try {
                    Resource wrappedResource = new Resource(node, resource.getTemplateType(), null, wrapper);
                    if (hasTemplate(node.getPrimaryNodeType(), wrapper)) {
                        script = resolveScript(wrappedResource, context);
                        request.setAttribute("wrappedContent", output);
                        output = script.execute();
                    } else {
                        logger.warn("Cannot get wrapper "+wrapper);
                    }
                } catch (IOException e) {
                    logger.error("Cannot execute wrapper "+wrapper,e);
                } catch (TemplateNotFoundException e) {
                    logger.debug("Cannot find wrapper "+wrapper,e);
                }
            }
        } finally {
            popAttributes(request, old);
        }

        if (request.getAttribute("currentResource") != null) {
            ((Resource)request.getAttribute("currentResource")).getDependencies().addAll(resource.getDependencies());
        }

        return output;
    }

    private String renderOptions(Resource resource, RenderContext context, JCRNodeWrapper node,
                                 String output) throws RepositoryException {
        Script script;
        if (resource.hasOptions()) {
            List<Resource.Option> options = resource.getOptions();
            Collections.sort(options);
            for (Resource.Option option : options) {
                String wrapper = option.getWrapper();
                try {
                    Resource wrappedResource = new Resource(node, resource.getTemplateType(), null, wrapper);
                    wrappedResource.setWrappedMixinType(option.getNodeType());
                    script = resolveScript(wrappedResource, context);
                    output += script.execute();
                } catch (IOException e) {
                    logger.error("Cannot execute wrapper " + wrapper, e);
                } catch (TemplateNotFoundException e) {
                    logger.debug("Cannot find wrapper " + wrapper, e);
                }
            }
        }
        return output;
    }

    private void pushAttribute(HttpServletRequest request, String key, Object value, Map<String,Object> oldMap) {
        oldMap.put(key, request.getAttribute(key));
        request.setAttribute(key, value);
    }

    private void popAttributes(HttpServletRequest request, Map<String,Object> oldMap) {
        for (Map.Entry<String,Object> entry : oldMap.entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }
    }

    /**
     * This resolves the executable script from the resource object. This should be able to find the proper script
     * depending of the template / template type. Currently resolves only simple RequestDispatcherScript.
     *
     * If template cannot be resolved, fall back on default template
     *
     * @param resource The resource to display
     * @param context
     * @return An executable script
     * @throws RepositoryException
     * @throws IOException
     */
    public Script resolveScript(Resource resource, RenderContext context) throws RepositoryException, TemplateNotFoundException {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            Script s = scriptResolver.resolveScript(resource,  context);
            if (s != null) {
                return s;
            }
        }
        return null;
    }

    /**
     * This set Jahia context attributes, so that legacy jahia tags can still be used in the templates
     * @param request Request where the attributes will be set
     * @param node Node to display
     * @param threadParamBean The "param bean"
     */
    private void setJahiaAttributes(HttpServletRequest request, JCRNodeWrapper node, ParamBean threadParamBean) {
        try {
            if (node instanceof JCRJahiaContentNode) {
                ContentObject obj = ((JCRJahiaContentNode)node).getContentObject();
                if (obj instanceof ContentContainer) {
                    ContentContainer c = (ContentContainer) obj;

                    ContainerBean bean = new ContainerBean(c.getJahiaContainer(threadParamBean, threadParamBean.getEntryLoadRequest()), threadParamBean);
                    request.setAttribute("container", bean);
                }
            }
            if (request.getAttribute(JahiaData.JAHIA_DATA) == null) {
                request.setAttribute(JahiaData.JAHIA_DATA,new JahiaData(threadParamBean, false));
            }
            if (request.getAttribute("jahia") == null) {
                // expose beans into the request scope  
                EngineValve.setContentAccessBeans(threadParamBean);
            }
            
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private boolean hasTemplate(ExtendedNodeType nt, String key) {
        for (ScriptResolver scriptResolver : scriptResolvers) {
            if (scriptResolver.hasTemplate(nt,key)) {
                return true;
            }
        }
        return false;
    }

    public SortedSet<Template> getAllTemplatesSet() {
        SortedSet<Template> set = new TreeSet<Template>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getAllTemplatesSet());
        }
        return set;
    }

    public SortedSet<Template> getTemplatesSet(ExtendedNodeType nt) {
        SortedSet<Template> set = new TreeSet<Template>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getTemplatesSet(nt));
        }
        return set;
    }


}
