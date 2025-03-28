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
package org.jahia.services.importexport;

import org.jahia.commons.Version;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Service used to update import file based on the originating version and build number
 * @deprecated Legacy import code, seems not used
 */
@Deprecated(since = "8.2.1.0", forRemoval = true)
public class ImportUpdateService {

    private Set<ImportFileUpdater> updaters = new LinkedHashSet<ImportFileUpdater>();

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final ImportUpdateService INSTANCE = new ImportUpdateService();
    }

    public static ImportUpdateService getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Internal method to register an import file updater
     * @param updater
     */
    public void registerUpdater(ImportFileUpdater updater) {
        updaters.add(updater);
    }

    /**
     * Internal method to unregister an import file updater
     * @param updater
     */
    public void unregisterUpdater(ImportFileUpdater updater) {
        updaters.remove(updater);
    }

    /**
     * Method used to update if necessary an import zip file based on the originating version and build number
     * @param importFile
     * @param fileName
     * @param fileType @return updated file OR the original file if no updates have been performed
     * @param version
     */
    public File updateImport(File importFile, String fileName, String fileType, Version version) {
        File file = importFile;
        for (ImportFileUpdater updater : updaters) {
            if (updater.mustUpdate(version)) {
                file = updater.updateImport(file, fileName, fileType);
            }
        }
        return file;
    }

}
