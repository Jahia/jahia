/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.modules.visibility.conditions;

import java.util.Calendar;
import java.util.Locale;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.visibility.BaseVisibilityConditionRule;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handle the execution of a start/end date condition for checking a node visibility.
 * If the current date is after the start date or if start date is not defined then the node is visible unless
 * end date is defined and we are after end date.
 *
 * @author rincevent
 * @since JAHIA 6.6
 * Created : 8/29/11
 */
public class StartEndDateConditionRuleImpl extends BaseVisibilityConditionRule {
    
    private transient static Logger logger = LoggerFactory.getLogger(StartEndDateConditionRuleImpl.class);

    /**
     * Return the associated display template that will be used by gwt.
     *
     * @return Return the associated display template that will be used by gwt.
     */
    public String getGWTDisplayTemplate(Locale locale) {
        return JahiaResourceBundle.getString("JahiaVisibility", "label.startEndDateCondition.xtemplate",locale, "Jahia Visibility");
    }

    public boolean matches(JCRNodeWrapper nodeWrapper) {
        Calendar start = null;
        Calendar end = null;
        try {
            start = nodeWrapper.getProperty("start").getValue().getDate();
        } catch (PathNotFoundException e) {
            logger.debug("start is not defined for this rule");
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            end = nodeWrapper.getProperty("end").getValue().getDate();
        } catch (PathNotFoundException e) {
            logger.debug("end is not defined for this rule");
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        Calendar calendar = null;
        try {
            calendar = nodeWrapper.getSession().getPreviewDate();
        } catch (RepositoryException e) {
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        if (start != null) {
            if (!calendar.after(start)) {
                return false;
            }
        }
        if (end != null) {
            if (!calendar.before(end)) {
                return false;
            }
        }
        return true;
    }

}
