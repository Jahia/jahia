/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.bin.Edit;
import org.jahia.bin.Render;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RenderModesChoiceListInitializer implements ChoiceListInitializer, ServletContextAware {

    private ServletContext servletContext;

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<ChoiceListValue> list = new ArrayList<ChoiceListValue>();

        boolean excludeEdit = "noGWT".equals(param);
        addMappings(list, (ApplicationContext) servletContext.getAttribute(
                        "org.springframework.web.servlet.FrameworkServlet.CONTEXT.RendererDispatcherServlet"), excludeEdit);

        addMappings(list, SpringContextSingleton.getInstance().getContext(), excludeEdit);

        return list;
    }

    private void addMappings(List<ChoiceListValue> list, ApplicationContext ctx, boolean excludeEdit) {
        if (ctx != null) {
            for (SimpleUrlHandlerMapping mapping : ctx.getBeansOfType(SimpleUrlHandlerMapping.class).values()) {
                for (Map.Entry<String, ?> entry : mapping.getUrlMap().entrySet()) {
                    if (entry.getKey().endsWith("/**") && entry.getValue() instanceof Render && (!excludeEdit || !(entry.getValue() instanceof Edit))) {
                        String key = entry.getKey().substring(1,entry.getKey().lastIndexOf('/'));
                        if (!key.equals("render")) {
                            list.add(new ChoiceListValue(key,key));
                        }
                    }
                }
            }
        }
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
