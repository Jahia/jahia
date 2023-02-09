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
package org.jahia.ajax.gwt.client.widget.contentengine;

import org.jahia.ajax.gwt.client.data.GWTChoiceListInitializer;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * User: toto
 * Date: May 10, 2010
 * Time: 4:51:14 PM
 *
 */
public interface NodeHolder {
    List<GWTJahiaNodeType> getNodeTypes();

    List<GWTJahiaNodeType> getMixin();

    Map<String, GWTChoiceListInitializer> getChoiceListInitializersValues();

    Map<String, Map<String, List<GWTJahiaNodePropertyValue>>> getDefaultValues();

    GWTJahiaNode getNode();

    List<GWTJahiaNode> getNodes();

    String getNodeName();

    String getDefaultLanguageCode();

    GWTJahiaNodeACL getAcl();

    Map<String, Set<String>> getReferencesWarnings();

    GWTJahiaNode getTargetNode();

    boolean isExistingNode();

    boolean isMultipleSelection();

    Map<String, GWTJahiaNodeProperty> getProperties();

    Map<String, GWTJahiaNodeProperty> getPresetProperties();

    Linker getLinker();

    void close();
}
