/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter.AjaxRequestCondition;
import org.jahia.services.render.filter.AbstractFilter.AnyOfCondition;
import org.jahia.services.render.filter.AbstractFilter.ConfigurationCondition;
import org.jahia.services.render.filter.AbstractFilter.EditModeCondition;
import org.jahia.services.render.filter.AbstractFilter.ExecutionCondition;
import org.jahia.services.render.filter.AbstractFilter.MainResourceCondition;
import org.jahia.services.render.filter.AbstractFilter.ModeCondition;
import org.jahia.services.render.filter.AbstractFilter.ModuleCondition;
import org.jahia.services.render.filter.AbstractFilter.NodeTypeCondition;
import org.jahia.services.render.filter.AbstractFilter.NotCondition;
import org.jahia.services.render.filter.AbstractFilter.SiteTemplateSetCondition;
import org.jahia.services.render.filter.AbstractFilter.TemplateCondition;
import org.jahia.services.render.filter.AbstractFilter.TemplateTypeCondition;
import org.jahia.services.render.filter.AbstractFilter.WebflowRequestCondition;
import org.jahia.utils.Patterns;

/**
 * Base class that supports conditions, allowing to configure when the implementor is executed.
 *
 * @author Thomas Draier
 * @author Sergiy Shyrkov
 */
public abstract class ConditionalExecution {

    private List<ExecutionCondition> conditions = new LinkedList<ExecutionCondition>();

    /**
     * Initializes an instance of this class.
     */
    public ConditionalExecution() {
        super();
    }

    /**
     * Initializes an instance of this class.
     *
     * @param conditions
     *            the execution conditions to be matched.
     */
    public ConditionalExecution(ExecutionCondition... conditions) {
        this();
        for (ExecutionCondition cond : conditions) {
            addCondition(cond);
        }
    }

    /**
     * Adds the specified condition for this filter.
     *
     * @param condition the condition to be added for this filter
     */
    public void addCondition(ExecutionCondition condition) {
        conditions.add(condition);
    }

    /**
     * Adds the specified condition for this filter at the specified position.
     *
     * @param condition the condition to be added for this filter
     */
    public void addCondition(int index, ExecutionCondition condition) {
        conditions.add(index, condition);
    }

    public boolean areConditionsMatched(RenderContext renderContext, Resource resource) {
        boolean matches = true;
        for (ExecutionCondition condition : conditions) {
            if (!condition.matches(renderContext, resource)) {
                matches = false;
                break;
            }
        }
        return matches;
    }

    /**
     * Returns a text-based representation of filter conditions.
     *
     * @return a text-based representation of filter conditions
     */
    public String getConditionsSummary() {
        StringBuilder out = new StringBuilder();
        if (conditions.isEmpty()) {
            out.append("<none>");
        }
        boolean first = true;
        for (ExecutionCondition cond : conditions) {
            if (!first) {
                out.append(" && ");
            } else {
                first = false;
            }
            out.append("(").append(cond).append(")");
        }
        return out.toString();
    }

    /**
     * Comma-separated list of resource configuration this filter will be executed for
     * (all others are skipped).
     *
     * @param configurations comma-separated list of configurations this filter will
     *                       be executed for (all others are skipped)
     */
    public void setApplyOnConfigurations(String configurations) {
        if (configurations.contains(",")) {
            AnyOfCondition condition = new AnyOfCondition();
            for (String conf : Patterns.COMMA.split(configurations)) {
                condition.add(new ConfigurationCondition(conf.trim()));
            }
            addCondition(0, condition);
        } else if (StringUtils.isNotBlank(configurations)) {
            addCondition(0, new ConfigurationCondition(configurations));
        }
    }

    /**
     * If set to <code>true</code>, the current filter will only be executed on
     * the main resource.
     *
     * @param bool set to <code>true</code> to limit filter execution for main
     *             resource only.
     */
    public void setApplyOnMainResource(boolean bool) {
        if (bool) {
            addCondition(new MainResourceCondition());
        }
    }

    /**
     * Comma-separated list of modes this filter will be executed for
     * (all others are skipped).
     *
     * @param modes comma-separated list of modes this filter will
     *              be executed for (all others are skipped)
     */
    public void setApplyOnModes(String modes) {
        if (modes.contains(",")) {
            AnyOfCondition condition = new AnyOfCondition();
            for (String mode : Patterns.COMMA.split(modes)) {
                condition.add(new ModeCondition(mode.trim()));
            }
            addCondition(0, condition);
        } else if (StringUtils.isNotBlank(modes)) {
            addCondition(0, new ModeCondition(modes));
        }
    }

    /**
     * Apply this filter if RenderContext isEditMode method call return true.
     *
     * @param applyOnEditMode true to apply this configuration
     */
    public void setApplyOnEditMode(Boolean applyOnEditMode) {
        if (applyOnEditMode) {
            addCondition(0, new EditModeCondition());
        }
    }

    /**
     * Comma-separated list of module names this filter will be executed for
     * (all others are skipped).
     *
     * @param modules comma-separated list of module names this filter will be
     *                executed for (all others are skipped)
     */
    public void setApplyOnModules(String modules) {
        if (modules.contains(",")) {
            AnyOfCondition condition = new AnyOfCondition();
            for (String module : Patterns.COMMA.split(modules)) {
                condition.add(new ModuleCondition(module.trim()));
            }
            addCondition(condition);
        } else if (StringUtils.isNotBlank(modules)) {
            addCondition(new ModuleCondition(modules));
        }
    }

    /**
     * Comma-separated list of node type names this filter will be executed for
     * (all others are skipped).
     *
     * @param nodeTypes comma-separated list of node type names this filter will
     *                  be executed for (all others are skipped)
     */
    public void setApplyOnNodeTypes(String nodeTypes) {
        if (nodeTypes.contains(",")) {
            AnyOfCondition condition = new AnyOfCondition();
            for (String nodeType : Patterns.COMMA.split(nodeTypes)) {
                condition.add(new NodeTypeCondition(nodeType.trim()));
            }
            addCondition(condition);
        } else if (StringUtils.isNotBlank(nodeTypes)) {
            addCondition(new NodeTypeCondition(nodeTypes));
        }
    }

    /**
     * Comma-separated list of template names this filter will be executed for
     * (all others are skipped).
     *
     * @param templates comma-separated list of template names this filter will
     *                  be executed for (all others are skipped)
     */
    public void setApplyOnTemplates(String templates) {
        if (templates.contains(",")) {
            AnyOfCondition condition = new AnyOfCondition();
            for (String template : Patterns.COMMA.split(templates)) {
                condition.add(new TemplateCondition(template.trim()));
            }
            addCondition(condition);
        } else if (StringUtils.isNotBlank(templates)) {
            addCondition(new TemplateCondition(templates));
        }
    }

    /**
     * Comma-separated list of template type names this filter will be executed
     * for (all others are skipped).
     *
     * @param templateTypes comma-separated list of template type names this filter
     *                      will be executed for (all others are skipped)
     */
    public void setApplyOnTemplateTypes(String templateTypes) {
        if (templateTypes.contains(",")) {
            AnyOfCondition condition = new AnyOfCondition();
            for (String templateType : Patterns.COMMA.split(templateTypes)) {
                condition.add(new TemplateTypeCondition(templateType.trim(),true));
            }
            addCondition(condition);
        } else if (StringUtils.isNotBlank(templateTypes)) {
            addCondition(new TemplateTypeCondition(templateTypes,true));
        }
    }

    /**
     * Comma-separated list of template set names this filter will be executed for (all others are skipped).
     *
     * @param templateSets
     *            comma-separated list of template type names this filter will be executed for (all others are skipped)
     * @since 6.6.1.0
     */
    public void setApplyOnSiteTemplateSets(String templateSets) {
        if (templateSets.contains(",")) {
            AnyOfCondition condition = new AnyOfCondition();
            for (String templateSet : Patterns.COMMA.split(templateSets)) {
                condition.add(new SiteTemplateSetCondition(templateSet.trim()));
            }
            addCondition(condition);
        } else if (StringUtils.isNotBlank(templateSets)) {
            addCondition(new SiteTemplateSetCondition(templateSets));
        }
    }

    /**
     * Adds specified conditions to the list of filter conditions. N.B. this
     * operation does not reset the list of conditions, but rather appends
     * specified list to the current one.
     *
     * @param conditions list of conditions to be added for this filter
     */
    public void setConditions(Set<ExecutionCondition> conditions) {
        this.conditions.addAll(conditions);
    }

    public void setSkipOnAjaxRequest(Boolean skip) {
        if(skip) {
            addCondition(new NotCondition(new AjaxRequestCondition()));
        }
    }

    @Deprecated
    public void setSkipOnConfiguration(String configurations) {
        setSkipOnConfigurations(configurations);
    }

    /**
     * Comma-separated list of configuration names this filter won't be executed
     * for.
     *
     * @param configurations comma-separated list of node type names this filter
     *                       won't be executed for
     */
    public void setSkipOnConfigurations(String configurations) {
        ExecutionCondition condition = null;
        if (configurations.contains(",")) {
            AnyOfCondition anyOf = new AnyOfCondition();
            for (String configuration : Patterns.COMMA.split(configurations)) {
                anyOf.add(new ConfigurationCondition(configuration.trim()));
            }
            condition = anyOf;
        } else if (StringUtils.isNotBlank(configurations)) {
            condition = new ConfigurationCondition(configurations);
        }
        if (condition != null) {
            addCondition(0, new NotCondition(condition));
        }
    }

    /**
     * If set to <code>true</code>, the current filter won't be executed on main resources.
     *
     * @param bool set to <code>true</code> to prevent filter from executing on main module
     */
    public void setSkipOnMainResource(boolean bool) {
        if (bool) {
            addCondition(new NotCondition(new MainResourceCondition()));
        }
    }

    /**
     * Comma-separated list of mode names this filter won't be executed
     * for.
     *
     * @param modes comma-separated list of node type names this filter
     *              won't be executed for
     */
    public void setSkipOnModes(String modes) {
        ExecutionCondition condition = null;
        if (modes.contains(",")) {
            AnyOfCondition anyOf = new AnyOfCondition();
            for (String mode : Patterns.COMMA.split(modes)) {
                anyOf.add(new ModeCondition(mode.trim()));
            }
            condition = anyOf;
        } else if (StringUtils.isNotBlank(modes)) {
            condition = new ModeCondition(modes);
        }
        if (condition != null) {
            addCondition(0, new NotCondition(condition));
        }
    }

    /**
     * Skip this filter if RenderContext isEditMode method call return false.
     *
     * @param skipOnEditMode true to apply this configuration
     */
    public void setSkipOnEditMode(Boolean skipOnEditMode) {
        if(skipOnEditMode) {
            addCondition(new NotCondition(new EditModeCondition()));
        }
    }

    /**
     * Comma-separated list of module names this filter won't be executed for.
     *
     * @param modules comma-separated list of module names this filter won't be
     *                executed for
     */
    public void setSkipOnModules(String modules) {
        ExecutionCondition condition = null;
        if (modules.contains(",")) {
            AnyOfCondition anyOf = new AnyOfCondition();
            for (String module : Patterns.COMMA.split(modules)) {
                anyOf.add(new ModuleCondition(module.trim()));
            }
            condition = anyOf;
        } else if (StringUtils.isNotBlank(modules)) {
            condition = new ModuleCondition(modules);
        }
        if (condition != null) {
            addCondition(new NotCondition(condition));
        }
    }

    /**
     * Comma-separated list of node type names this filter won't be executed
     * for.
     *
     * @param nodeTypes comma-separated list of node type names this filter
     *                  won't be executed for
     */
    public void setSkipOnNodeTypes(String nodeTypes) {
        ExecutionCondition condition = null;
        if (nodeTypes.contains(",")) {
            AnyOfCondition anyOf = new AnyOfCondition();
            for (String nodeType : Patterns.COMMA.split(nodeTypes)) {
                anyOf.add(new NodeTypeCondition(nodeType.trim()));
            }
            condition = anyOf;
        } else if (StringUtils.isNotBlank(nodeTypes)) {
            condition = new NodeTypeCondition(nodeTypes);
        }
        if (condition != null) {
            addCondition(new NotCondition(condition));
        }
    }

    /**
     * Comma-separated list of template names this filter won't be executed for.
     *
     * @param templates comma-separated list of template names this filter won't
     *                  be executed for
     */
    public void setSkipOnTemplates(String templates) {
        ExecutionCondition condition = null;
        if (templates.contains(",")) {
            AnyOfCondition anyOf = new AnyOfCondition();
            for (String template : Patterns.COMMA.split(templates)) {
                anyOf.add(new TemplateCondition(template.trim()));
            }
            condition = anyOf;
        } else if (StringUtils.isNotBlank(templates)) {
            condition = new TemplateCondition(templates);
        }
        if (condition != null) {
            addCondition(new NotCondition(condition));
        }
    }

    /**
     * Comma-separated list of template type names this filter won't be executed
     * for.
     *
     * @param templateTypes comma-separated list of template type names this
     *                      filter won't be executed for
     */
    public void setSkipOnTemplateTypes(String templateTypes) {
        ExecutionCondition condition = null;
        if (templateTypes.contains(",")) {
            AnyOfCondition anyOf = new AnyOfCondition();
            for (String templateType : Patterns.COMMA.split(templateTypes)) {
                anyOf.add(new TemplateTypeCondition(templateType.trim(),true));
            }
            condition = anyOf;
        } else if (StringUtils.isNotBlank(templateTypes)) {
            condition = new TemplateTypeCondition(templateTypes,true);
        }
        if (condition != null) {
            addCondition(new NotCondition(condition));
        }
    }

    /**
     * Skip this filter if the render chain is processing a Webflow request.
     *
     * @param skipOnWebflowRequest true to apply this configuration
     */
    public void setSkipOnWebflowRequest(Boolean skipOnWebflowRequest) {
        if (skipOnWebflowRequest) {
            addCondition(new NotCondition(new WebflowRequestCondition()));
        }
    }
}
