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
package org.jahia.services.render.filter;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.Script;

/**
 * Base filter implementation that is also supports conditional execution, i.e.
 * the filter logic is executed only if all the specified conditions are
 * matched.
 * 
 * @author Thomas Draier
 * @author Sergiy Shyrkov
 */
public abstract class AbstractFilter implements RenderFilter {

    /**
     * Evaluates to <code>true</code> if any of the underlying conditions
     * evaluates to true.
     * 
     * @author Sergiy Shyrkov
     * 
     */
    public static class AnyOfCondition implements ExecutionCondition {

        private List<ExecutionCondition> conditions = new LinkedList<ExecutionCondition>();

        public void add(ExecutionCondition condition) {
            conditions.add(condition);
        }

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
    }

    public interface ExecutionCondition {
        /**
         * Returns <code>true</code> if the condition matches the specified
         * resource.
         * 
         * @param renderContext
         *            Current RenderContext
         * @param resource
         *            Resource being displayed
         * @return <code>true</code> if the condition matches the specified
         *         resource
         */
        boolean matches(RenderContext renderContext, Resource resource);
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

        public String getValue(RenderContext renderContext, Resource resource) {
            return ((Script) renderContext.getRequest().getAttribute("script")).getTemplate().getModule()
                    .getName();
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
            super();
            this.nodeTypeName = nodeTypeName;
        }

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
                logger.warn("Unable to evaluate filter execution condition." + "Node is null for resource "
                        + resource);
            }

            return matches;
        }
    }

    /**
     * Inverts the result of the underlying condition.
     * 
     * @author Sergiy Shyrkov
     * 
     */
    public static class NotCondition implements ExecutionCondition {

        private ExecutionCondition condition;

        public NotCondition(ExecutionCondition condition) {
            super();
            this.condition = condition;
        }

        public boolean matches(RenderContext renderContext, Resource resource) {
            return !condition.matches(renderContext, resource);
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
         * @param pattern
         *            the string to be matched
         * @param isRegExp
         *            do we consider this pattern as a regular expression and
         *            not as an exact string?
         */
        public PatternCondition(String pattern, boolean isRegExp) {
            super();
            if (isRegExp) {
                this.pattern = Pattern.compile(pattern);
            } else {
                exactMatch = pattern;
            }

        }

        protected abstract String getValue(RenderContext renderContext, Resource resource);

        public final boolean matches(RenderContext renderContext, Resource resource) {
            return pattern != null ? pattern.matcher(getValue(renderContext, resource)).matches()
                    : exactMatch.equals(getValue(renderContext, resource));
        }
    }

    /**
     * Evaluates to <code>true</code> if the current resource's template matches
     * the provided one.
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

        public String getValue(RenderContext renderContext, Resource resource) {
            return resource.getResolvedTemplate();
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

        public String getValue(RenderContext renderContext, Resource resource) {
            return resource.getTemplateType();
        }
    }

    private static final Logger logger = Logger.getLogger(AbstractFilter.class);

    private List<ExecutionCondition> conditions = new LinkedList<ExecutionCondition>();

    protected RenderService service;

    /**
     * Initializes an instance of this class.
     */
    public AbstractFilter() {
        super();
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param conditions
     *            the execution conditions to be matched.
     */
    public AbstractFilter(ExecutionCondition... conditions) {
        this();
        for (ExecutionCondition cond : conditions) {
            addCondition(cond);
        }
    }

    public void addCondition(ExecutionCondition condition) {
        conditions.add(condition);
    }

    private boolean areConditionsMatched(RenderContext renderContext, Resource resource) {
        boolean matches = true;
        for (ExecutionCondition condition : conditions) {
            if (!condition.matches(renderContext, resource)) {
                matches = false;
                break;
            }
        }
        return matches;
    }

    public final String doFilter(RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        return areConditionsMatched(renderContext, resource) ? execute(renderContext, resource, chain)
                : chain.doFilter(renderContext, resource);
    }

    protected abstract String execute(RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception;

    public void setApplyOnModules(String modules) {
        if (modules.contains(",")) {
            AnyOfCondition condition = new AnyOfCondition();
            for (String module : modules.split(",")) {
                condition.add(new ModuleCondition(module.trim()));
            }
            addCondition(condition);
        } else {
            addCondition(new ModuleCondition(modules));
        }
    }

    public void setApplyOnNodeTypes(String nodeTypes) {
        if (nodeTypes.contains(",")) {
            AnyOfCondition condition = new AnyOfCondition();
            for (String nodeType : nodeTypes.split(",")) {
                condition.add(new NodeTypeCondition(nodeType.trim()));
            }
            addCondition(condition);
        } else {
            addCondition(new NodeTypeCondition(nodeTypes));
        }
    }

    public void setApplyOnTemplates(String templates) {
        if (templates.contains(",")) {
            AnyOfCondition condition = new AnyOfCondition();
            for (String template : templates.split(",")) {
                condition.add(new TemplateCondition(template.trim()));
            }
            addCondition(condition);
        } else {
            addCondition(new TemplateCondition(templates));
        }
    }

    public void setConditions(Set<ExecutionCondition> conditions) {
        this.conditions.addAll(conditions);
    }

    public final void setRenderService(RenderService service) {
        this.service = service;
    }
}
