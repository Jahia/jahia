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
package org.jahia.services.content.nodetypes.renderer;

import org.jahia.services.render.RenderContext;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.RepositoryException;

import java.util.Locale;
import java.util.Map;

/**
 * Defines the renderer for a choice list.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 18 nov. 2009
 */
public interface ChoiceListRenderer {
    Map<String,Object> getObjectRendering(RenderContext context, JCRPropertyWrapper propertyWrapper) throws RepositoryException;
    String getStringRendering(RenderContext context, JCRPropertyWrapper propertyWrapper) throws RepositoryException;
    Map<String,Object> getObjectRendering(RenderContext context, ExtendedPropertyDefinition propDef, Object propertyValue) throws RepositoryException;
    String getStringRendering(RenderContext context, ExtendedPropertyDefinition propDef, Object propertyValue) throws RepositoryException;
    Map<String,Object> getObjectRendering(Locale locale, ExtendedPropertyDefinition propDef, Object propertyValue) throws RepositoryException;
    String getStringRendering(Locale locale, ExtendedPropertyDefinition propDef, Object propertyValue) throws RepositoryException;
}
