/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Render;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.scripting.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Base filter implementation that is also supports conditional execution, i.e.
 * the filter logic is executed only if all the specified conditions are
 * matched.
 *
 * @author Thomas Draier
 * @author Sergiy Shyrkov
 */
public abstract class AbstractFilter extends ConditionalExecution implements RenderFilter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractFilter.class);

    private String description;
    private boolean disabled;
    private float priority = 99;
    protected RenderService service;

    public interface ExecutionCondition {

        /**
         * Returns <code>true</code> if the condition matches the specified
         * resource.
         *
         * @param renderContext Current RenderContext
         * @param resource      Resource being displayed
         * @return <code>true</code> if the condition matches the specified
         *         resource
         */
        boolean matches(RenderContext renderContext, Resource resource);
    }

    public static class AjaxRequestCondition implements ExecutionCondition {

        /**
         * Returns <code>true</code> if the condition matches the specified
         * resource.
         *
         * @param renderContext Current RenderContext
         * @param resource      Resource being displayed
         * @return <code>true</code> if the condition matches the specified resource
         */
        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            return renderContext.isAjaxRequest();
        }

        @Override
        public String toString() {
            return "is Ajax request";
        }
    }

    /**
     * Evaluates to <code>true</code> if any of the underlying conditions
     * evaluates to true.
     *
     * @author Sergiy Shyrkov
     */
    public static class AnyOfCondition implements ExecutionCondition {

        private List<ExecutionCondition> conditions = new LinkedList<ExecutionCondition>();

        public void add(ExecutionCondition condition) {
            conditions.add(condition);
        }

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            boolean matches = false;
            for (ExecutionCondition condition : conditions) {
                if (condition.matches(renderContext, resource)) {
                    matches = true;
                    break;
                }
            }
            return matches;
        }

        @Override
        public String toString() {
            StringBuilder out = new StringBuilder();
            for (ExecutionCondition cond : conditions) {
                if (out.length() > 0) {
                    out.append(" || ");
                }
                out.append("(").append(cond).append(")");
            }
            return out.toString();
        }
    }

    public static class ConfigurationCondition implements ExecutionCondition {

        private String conf;

        public ConfigurationCondition(String conf) {
            this.conf = conf;
        }

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            return (resource.getContextConfiguration().equals(conf));
        }

        @Override
        public String toString() {
            return "configuration == "  + conf;
        }
    }

    /**
     * Evaluates to <code>true</code> if the current resource is the main resource
     *
     * @author Thomas Draier
     */
    public static class MainResourceCondition implements ExecutionCondition {

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            return (renderContext.getMainResource().getNodePath().equals(resource.getNodePath()));
        }

        @Override
        public String toString() {
            return "is main resource";
        }
    }

    /**
     * Evaluates to <code>true</code> if the current mode equals to the
     * specified one
     *
     * @author Sergiy Shyrkov
     */
    public static class ModeCondition implements ExecutionCondition {

        private String mode;

        /**
         * Initializes an instance of this class.
         *
         * @param mode the target mode to check for
         */
        public ModeCondition(String mode) {
            this.mode = mode;
        }

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            return matches(renderContext, mode);
        }

        @Override
        public String toString() {
            return "mode == " + mode;
        }

        public static boolean matches(RenderContext renderContext, String mode) {
            if (mode.equals("contribution")) {
                mode = "contribute"; // Legacy compatibility
            }
            return renderContext.getMode() != null && renderContext.getMode().equals(mode);
        }
    }

    /**
     * Evaluates to <code>true</code> if the current mode equals to the
     * specified one
     *
     * @author Sergiy Shyrkov
     */
    public static class EditModeCondition implements ExecutionCondition {

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            return renderContext.isEditMode();
        }

        @Override
        public String toString() {
            return "render context is in edit mode";
        }
    }

    /**
     * Evaluates to <code>true</code> if the current resource's template type
     * matches the provided one.
     *
     * @author Sergiy Shyrkov
     */
    public static class ModuleCondition extends PatternCondition {

        public ModuleCondition(String module) {
            super(module, false);
        }

        public ModuleCondition(String module, boolean isRegExp) {
            super(module, isRegExp);
        }

        @Override
        public String getValue(RenderContext renderContext, Resource resource) {
            return ((Script) renderContext.getRequest().getAttribute("script")).getView().getModule().getName();
        }

        @Override
        public String toString() {
            return "module " + super.toString();
        }
    }

    /**
     * Evaluates to <code>true</code> if the current resource's node has the
     * specified node type.
     *
     * @author Sergiy Shyrkov
     */
    public static class NodeTypeCondition implements ExecutionCondition {

        private String nodeTypeName;

        public NodeTypeCondition(String nodeTypeName) {
            this.nodeTypeName = nodeTypeName;
        }

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            boolean matches = false;
            JCRNodeWrapper node = resource.getNode();
            if (node != null) {
                try {
                    matches = node.isNodeType(nodeTypeName);
                } catch (RepositoryException e) {
                    logger.warn("Unable to evaluate filter execution condition for resource " + resource, e);
                }
            } else {
                logger.warn("Unable to evaluate filter execution condition." + "Node is null for resource " + resource);
            }

            return matches;
        }

        @Override
        public String toString() {
            return "nodeTypeName == " + nodeTypeName;
        }
    }

    /**
     * Inverts the result of the underlying condition.
     *
     * @author Sergiy Shyrkov
     */
    public static class NotCondition implements ExecutionCondition {

        private ExecutionCondition condition;

        public NotCondition(ExecutionCondition condition) {
            this.condition = condition;
        }

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            return !condition.matches(renderContext, resource);
        }

        @Override
        public String toString() {
            return "not (" + condition + ")";
        }
    }

    /**
     * Evaluates to <code>true</code> if the current resource's template type
     * matches the provided one.
     *
     * @author Sergiy Shyrkov
     */
    public static abstract class PatternCondition implements ExecutionCondition {

        private String exactMatch;
        private Pattern pattern;

        /**
         * Initializes an instance of this class.
         *
         * @param pattern  the string to be matched
         * @param isRegExp do we consider this pattern as a regular expression and
         *                 not as an exact string?
         */
        public PatternCondition(String pattern, boolean isRegExp) {
            if (isRegExp) {
                this.pattern = Pattern.compile(pattern);
            } else {
                exactMatch = pattern;
            }
        }

        protected abstract String getValue(RenderContext renderContext, Resource resource);

        @Override
        public final boolean matches(RenderContext renderContext, Resource resource) {
            return pattern != null ? pattern.matcher(getValue(renderContext, resource)).matches() : exactMatch.equals(
                    getValue(renderContext, resource));
        }

        @Override
        public String toString() {
            return pattern != null ? ("matches " + pattern.pattern()) :  ("is " + exactMatch);
        }
    }

    /**
     * Filter execution condition that evaluates to true if the specified request attribute matches the specified value.
     *
     * @author Sergiy Shyrkov
     * @since Jahia 6.6
     */
    public static class RequestAttributeCondition extends RequestCondition {

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            return name != null
                    && StringUtils.equals(
                    String.valueOf(renderContext.getRequest().getAttribute(name)), value);
        }
    }

    /**
     * Filter execution condition that evaluates to true if the specified request item matches the specified value.
     *
     * @author Sergiy Shyrkov
     * @since Jahia 6.6
     */
    public static abstract class RequestCondition implements ExecutionCondition {

        protected String name;
        protected String value;

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    /**
     * Filter execution condition that evaluates to true if the specified request header matches the specified value.
     *
     * @author Sergiy Shyrkov
     * @since Jahia 6.6
     */
    public static class RequestHeaderCondition extends RequestCondition {

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            return name != null
                    && StringUtils.equals(renderContext.getRequest().getHeader(name), value);
        }
    }

    /**
     * Filter execution condition that evaluates to true if the specified request parameter matches the specified value.
     *
     * @author Sergiy Shyrkov
     * @since Jahia 6.6
     */
    public static class RequestParameterCondition extends RequestCondition {

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            return name != null
                    && StringUtils.equals(renderContext.getRequest().getParameter(name), value);
        }
    }

    /**
     * Evaluates to <code>true</code> if the current resource's template matches the provided one.
     *
     * @author Sergiy Shyrkov
     */
    public static class TemplateCondition extends PatternCondition {

        public TemplateCondition(String template) {
            super(template, false);
        }

        public TemplateCondition(String template, boolean isRegExp) {
            super(template, isRegExp);
        }

        @Override
        public String getValue(RenderContext renderContext, Resource resource) {
            return resource.getResolvedTemplate();
        }

        @Override
        public String toString() {
            return "template " + super.toString();
        }
    }

    /**
     * Evaluates to <code>true</code> if the current resource's template type
     * matches the provided one.
     *
     * @author Sergiy Shyrkov
     */
    public static class TemplateTypeCondition extends PatternCondition {

        public TemplateTypeCondition(String templateType) {
            super(templateType, false);
        }

        public TemplateTypeCondition(String templateType, boolean isRegExp) {
            super(templateType, isRegExp);
        }

        @Override
        public String getValue(RenderContext renderContext, Resource resource) {
            return resource.getTemplateType();
        }

        @Override
        public String toString() {
            return "template type " + super.toString();
        }
    }

    /**
     * Evaluates to <code>true</code> if the current site's template set matches the specified one
     *
     * @author Sergiy Shyrkov
     * @since 6.6.1.0
     */
    public static class SiteTemplateSetCondition implements ExecutionCondition {

        private String templateSet;

        /**
         * Initializes an instance of this class.
         *
         * @param templateSet the template set name to match
         */
        public SiteTemplateSetCondition(String templateSet) {
            this.templateSet = templateSet;
        }

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            return renderContext.getSite() != null && renderContext.getSite().getInstalledModules().contains(templateSet);
        }

        @Override
        public String toString() {
            return "siteTemplateSet == " + templateSet;
        }
    }

    /**
     * Checks that the current request is processing a Webflow action.
     * 
     * @author Sergiy Shyrkov
     * @since 7.1.2.5 / 7.2.1.0
     */
    public static class WebflowRequestCondition implements ExecutionCondition {
        @Override
        public String toString() {
            return "is Webflow request";
        }

        @Override
        public boolean matches(RenderContext renderContext, Resource resource) {
            return Render.isWebflowRequest(renderContext.getRequest());
        }
    } 

    /**
     * Initializes an instance of this class.
     */
    public AbstractFilter() {
        super();
    }

    /**
     * Initializes an instance of this class.
     *
     * @param conditions the execution conditions to be matched.
     */
    public AbstractFilter(ExecutionCondition... conditions) {
        for (ExecutionCondition cond : conditions) {
            addCondition(cond);
        }
    }

    @Override
    public boolean areConditionsMatched(RenderContext renderContext, Resource resource) {
        if (disabled) {
            return false;
        }
        return super.areConditionsMatched(renderContext, resource);
    }

    @Override
    public int compareTo(RenderFilter filter) {
        int result = Float.valueOf(getPriority()).compareTo(Float.valueOf(filter.getPriority()));
        return result != 0 ? result : getClass().getName().compareTo(filter.getClass().getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() == obj.getClass()) {
            return this.getPriority() == ((AbstractFilter) obj).getPriority();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = this.getClass().getName().hashCode();
        result = 31 * result + Float.floatToIntBits(priority);
        return result;
    }

    /**
     *
     * @param previousOut Result from the previous filter
     * @param renderContext The render context
     * @param resource The resource to render
     * @param chain The render chain
     * @return Filtered content
     */
    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        return previousOut;
    }

    @Override
    public void finalize(RenderContext renderContext, Resource resource, RenderChain renderChain) {
    }

    /**
     * Returns a human-readable description of this filter.
     *
     * @return a human-readable description of this filter
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the priority number of the filter. Filter will be executed in order of priority, lower first.
     */
    @Override
    public float getPriority() {
        return priority;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public String getContentForError(RenderContext renderContext, Resource resource, RenderChain renderChain, Exception e) {
        logger.debug("Handling exception {} in {}", e.getMessage(), resource.getPath());
        return null;
    }

    /**
     *
     * @param renderContext The render context
     * @param resource The resource to render
     * @param chain The render chain
     * @return Content to stop the chain, or null to continue
     */
    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        return null;
    }

    /**
     * Sets a human-readable description of this filter.
     *
     * @param description
     *            a human-readable description of this filter
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public void setPriority(float priority) {
        this.priority = priority;
    }

    @Override
    public final void setRenderService(RenderService service) {
        this.service = service;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        if (description != null) {
            out.append("Description: ").append(description).append("\n");
        }

        out.append("Conditions: ").append(getConditionsSummary());

        return out.toString();
    }
}
