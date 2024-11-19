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
package org.jahia.services.templates;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.ExternalEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;


public class JCRModuleListener  extends DefaultEventListener implements ExternalEventListener {

    private static Logger logger = LoggerFactory.getLogger(JCRModuleListener.class);

    private TemplatePackageRegistry packageRegistry;

    private Listener listener;

    public void setPackageRegistry(TemplatePackageRegistry packageRegistry) {
        this.packageRegistry = packageRegistry;
    }

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED;
    }

    @Override
    public String getPath() {
        return "/modules";
    }

    @Override
    public String[] getNodeTypes() {
        return new String[] {"jnt:moduleVersion"};
    }

    public void onEvent(EventIterator events) {
        while (events.hasNext()) {
            try {
                Event e = events.nextEvent();
                String path = e.getPath();
                String[] splitpath = path.split("/");
                JahiaTemplatesPackage p = packageRegistry.lookupByIdAndVersion(splitpath[2], new ModuleVersion(splitpath[3]));
                if (listener != null && p != null) {
                    listener.onModuleImported(p);
                }
            } catch (Exception e1) {
                logger.error("Error handling event", e1);
            }
        }
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public interface Listener {
        void onModuleImported(JahiaTemplatesPackage pack);
    }
}
