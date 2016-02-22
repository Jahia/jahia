/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.nodetypes.initializers;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.PropertyType;
import javax.jcr.Value;

import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlHelper;
import org.apache.jackrabbit.util.ISO8601;
import org.slf4j.Logger;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ValueImpl;

/**
 * Initial value provider for the current date.
 * User: toto
 * Date: Oct 10, 2008
 * Time: 11:09:45 AM
 */
public class Now implements ValueInitializer {
    
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Now.class);
    
    public Value[] getValues(ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params) {
        long offset = 0;
        if (params.size() == 1) {
            try {
                offset = (Long) ExpressionFactory.createExpression(params.get(0)).evaluate(JexlHelper.createContext());
            } catch (Exception e) {
                logger.warn("Error evaluating now() initial value with parameter '" + params.get(0) + "'. Cause: " + e.getMessage(), e);
            }
        }
        
        Calendar time = new GregorianCalendar();
        if (offset != 0) {
            time.setTimeInMillis(time.getTimeInMillis() + offset);
        }
        
        return new Value[] { new ValueImpl(ISO8601.format(time), PropertyType.DATE, false) };        
    }
}
