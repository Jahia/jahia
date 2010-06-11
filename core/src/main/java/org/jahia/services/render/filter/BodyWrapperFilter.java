package org.jahia.services.render.filter;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.*;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * WrapperFilter
 *
 * Looks for all registered wrappers in the resource and calls the associated scripts around the output.
 * Output is made available to the wrapper script through the "wrappedContent" request attribute.
 *
 */
public class BodyWrapperFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(BodyWrapperFilter.class);

    public String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        Stack<Wrapper> wrappers = (Stack<Wrapper>) renderContext.getRequest().getAttribute("bodyWrapperStack");

        if (wrappers == null) {
            wrappers = new Stack<Wrapper>();
            pushBodyWrappers(resource.getNode(), wrappers);
            renderContext.getRequest().setAttribute("bodyWrapperStack", wrappers);
        }

        if (!wrappers.isEmpty() && !renderContext.isAjaxRequest()) {
            Wrapper wrapper = wrappers.pop();

            try {
                JCRNodeWrapper wrapperNode = wrapper.node;
                renderContext.getRequest().setAttribute("wrappedResource", resource);
                Resource wrapperResource = new Resource(wrapperNode, resource.getTemplateType().equals("edit")?"html":resource.getTemplateType(), null, wrapper.template,
                        Resource.CONFIGURATION_WRAPPER);
                if (service.hasTemplate(wrapperNode.getPrimaryNodeType(), wrapper.template)) {
                    chain.pushAttribute(renderContext.getRequest(), "inWrapper", Boolean.TRUE);
                    String output = RenderService.getInstance().render(wrapperResource, renderContext);
                    if (renderContext.isEditMode()) {
                        output = "<div jahiatype=\"wrappedContentInfo\" wrappedNode=\""+resource.getNode().getIdentifier()+"\"" +

                                " wrapperContent=\""+wrapperNode.getIdentifier()+"\">"+output+"</div>";
                    }
                    return output;
                } else {
                    logger.warn("Cannot get wrapper "+wrapper);
                }
            } catch (TemplateNotFoundException e) {
                logger.debug("Cannot find wrapper "+wrapper,e);
            } catch (RenderException e) {
                logger.error("Cannot execute wrapper "+wrapper,e);
            }
        }
        chain.pushAttribute(renderContext.getRequest(), "inWrapper", (renderContext.isAjaxRequest())?Boolean.TRUE:Boolean.FALSE);
        return chain.doFilter(renderContext, resource);
    }

    public void pushBodyWrappers(JCRNodeWrapper node, Stack<Wrapper> wrappers) {
        JCRNodeWrapper current = node;
        Set<String> foundWrappers = new HashSet<String>();
        try {
            if (node.isNodeType("jnt:wrapper")) {
                foundWrappers.add(node.getProperty("j:key").getString());
            }
            while (true) {
//                if (current.isNodeType("jmix:wrapper")) {
                    Query q = current.getSession().getWorkspace().getQueryManager().createQuery("select * from [jnt:wrapper] as w where ischildnode(w, ['"+current.getPath()+"'])", Query.JCR_SQL2);
                    QueryResult result = q.execute();
                    NodeIterator ni = result.getNodes();
                    while (ni.hasNext()) {
                        JCRNodeWrapper wrapper = (JCRNodeWrapper) ni.next();
                        if (!foundWrappers.contains(wrapper.getProperty("j:key").getString())) {
                            boolean ok = true;
                            if (wrapper.hasProperty("j:applyOn")) {
                                ok = false;
                                Value[] values = wrapper.getProperty("j:applyOn").getValues();
                                for (Value value : values) {
                                    if (node.isNodeType(value.getString())) {
                                        ok = true;
                                        break;
                                    }
                                }
                                if (values.length == 0) {
                                    ok = true;
                                }
                            }
                            if (ok) {
                                wrappers.push(new Wrapper(wrapper.getProperty("j:template").getString(), wrapper));
                                foundWrappers.add(wrapper.getProperty("j:key").getString());
                            }
                        }
                    }
//                }
                current = current.getParent();
            }
        } catch (ItemNotFoundException e) {
            // default
            if (!foundWrappers.contains("bodywrapper")) {
                wrappers.push(new Wrapper("bodywrapper", node));
            }
            return;
        } catch (RepositoryException e) {
            logger.error("Cannot find wrapper",e);
        }
    }

    public class Wrapper {
        public String template;
        public JCRNodeWrapper node;

        Wrapper(String template, JCRNodeWrapper node) {
            this.template = template;
            this.node = node;
        }
    }

}