/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
