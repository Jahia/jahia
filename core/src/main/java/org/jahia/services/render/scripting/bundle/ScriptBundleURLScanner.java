/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.scripting.bundle;

import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * A {@link BundleURLScanner} that properly implements {@link #hashCode()} and {@link #equals(Object)}
 *
 * @author Christophe Laprun
 */
class ScriptBundleURLScanner extends BundleURLScanner {
    private final int hash;
    private final String extension;

    ScriptBundleURLScanner(String path, String extension, boolean recurse) {
        super(path, BundleScriptResolver.getExtensionPattern(extension), recurse);
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (extension != null ? extension.hashCode() : 0);
        result = 31 * result + (recurse ? 1 : 0);
        hash = result;
        this.extension = extension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScriptBundleURLScanner that = (ScriptBundleURLScanner) o;

        return hash == that.hash;

    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public List<URL> scan(Bundle bundle) {
        if (BundleScriptResolver.shouldBeScannedForViews(bundle, extension)) {
            return super.scan(bundle);
        } else {
            return Collections.emptyList();
        }
    }
}
