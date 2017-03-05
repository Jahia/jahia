/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.importexport;

import org.jahia.commons.Version;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Service used to update import file based on the originating version and build number
 */
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
     * @param buildNumber
     */
    public File updateImport(File importFile, String fileName, String fileType, Version version, int buildNumber) {
        File file = importFile;
        for (ImportFileUpdater updater : updaters) {
            if (updater.mustUpdate(version, buildNumber)) {
                file = updater.updateImport(file, fileName, fileType);
            }
        }
        return file;
    }

}
