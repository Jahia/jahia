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
package org.jahia.services.importexport;

import org.jahia.commons.Version;

import java.io.File;

/**
 * Class used to update import file based on the originating version and build number
 */
public abstract class ImportFileUpdater {

    private ImportUpdateService importUpdateService;

    /**
     * Method used to determine if this import must be updated
     * @param version
     * @param buildNumber
     * @return true if this updater is applicable on an import originating from the specified version and build number
     */
    public abstract boolean mustUpdate(Version version);

    /**
     * Method used to update an import zip file
     * @param importFile
     * @param fileName
     * @param fileType
     * @return the updated zip file
     */
    public abstract File updateImport(File importFile, String fileName, String fileType);

    public void init() {
        importUpdateService.registerUpdater(this);
    }

    public void destroy() {
        importUpdateService.unregisterUpdater(this);
    }

    public void setImportUpdateService(ImportUpdateService importUpdateService) {
        this.importUpdateService = importUpdateService;
    }
}
