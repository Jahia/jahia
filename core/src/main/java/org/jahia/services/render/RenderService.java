package org.jahia.services.render;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.decorator.JCRJahiaContentNode;
import org.jahia.services.JahiaService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.data.beans.ContainerBean;
import org.jahia.data.JahiaData;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaException;
import org.jahia.operations.valves.EngineValve;
import org.jahia.params.ParamBean;
import org.jahia.content.ContentObject;
import org.jahia.registries.ServicesRegistry;
import org.jahia.settings.SettingsBean;

import javax.servlet.http.HttpServletRequest;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.File;
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
     * @throws RepositoryException
     * @throws IOException
     */
    public String render(Resource resource, RenderContext context) throws RepositoryException, IOException {
        final HttpServletRequest request = context.getRequest();

        Script script = resolveScript(resource, context);

        request.setAttribute("renderContext", context);

        Object old = request.getAttribute("currentNode");
        request.setAttribute("currentNode", resource.getNode());

        request.setAttribute("workspace", resource.getNode().getSession().getWorkspace().getName());
        request.setAttribute("locale", resource.getNode().getSession().getWorkspace().getName());

        Resource oldResource = (Resource) request.getAttribute("currentResource");
        request.setAttribute("currentResource", resource);

        URLGenerator oldUrl = (URLGenerator) request.getAttribute("url");
        request.setAttribute("url",new URLGenerator(context, resource, storeService));

        if (resource.getNode().hasProperty("skin")) {
            String skin = resource.getNode().getPropertyAsString("skin");
            resource.pushWrapper(skin);
        }

        String output;
        try {
            setJahiaAttributes(request, resource.getNode(), (ParamBean) Jahia.getThreadParamBean());
            output = script.execute();

            while (resource.hasWrapper()) {
                String wrapper = resource.popWrapper();
                try {
                    Resource wrappedResource = new Resource(resource.getNode(), resource.getTemplateType(), null, wrapper);
                    if (hasTemplate(resource.getNode().getPrimaryNodeType(), wrapper)) {
                        script = resolveScript(wrappedResource, context);
                        request.setAttribute("wrappedContent", output);
                        output = script.execute();
                    } else {
                        logger.warn("Cannot get wrapper "+wrapper);
                    }
                } catch (IOException e) {
                    logger.error("Cannot execute wrapper "+wrapper,e);
                }
            }
        } finally {
            request.setAttribute("currentNode",old);
            request.setAttribute("currentResource",oldResource);
            request.setAttribute("url",oldUrl);
        }

        if (oldResource != null) {
            oldResource.getDependencies().addAll(resource.getDependencies());
        }

        return output;
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
    private Script resolveScript(Resource resource, RenderContext context) throws RepositoryException, IOException {
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

    public SortedSet<Template> getTemplatesSet(ExtendedNodeType nt) {
        SortedSet<Template> set = new TreeSet<Template>();
        for (ScriptResolver scriptResolver : scriptResolvers) {
            set.addAll(scriptResolver.getTemplatesSet(nt));
        }
        return set;
    }


}
