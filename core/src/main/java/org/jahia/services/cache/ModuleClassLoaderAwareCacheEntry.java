/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.cache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;

/**
 * Wrapper for the cache entry to use the classloader of the specified module.
 *
 * @author Thomas Draier
 */
public class ModuleClassLoaderAwareCacheEntry extends ClassLoaderAwareCacheEntry {
    private static final long serialVersionUID = -4281419239864698107L;

    private transient String moduleName;

    public ModuleClassLoaderAwareCacheEntry(Object value, String moduleName) {
        super(value);
        this.moduleName = moduleName;
    }

    @Override
    protected void beforeReadObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        moduleName = (String) in.readObject();
    }

    @Override
    protected void beforeWriteObject(ObjectOutputStream out) throws IOException {
        out.writeObject(moduleName);
    }

    @Override
    protected ClassLoader getClassLoaderToUse() {
        JahiaTemplatesPackage module = moduleName != null ? ServicesRegistry.getInstance()
                .getJahiaTemplateManagerService().getTemplatePackageById(moduleName) : null;
        return module != null ? module.getClassLoader() : null;
    }

}
