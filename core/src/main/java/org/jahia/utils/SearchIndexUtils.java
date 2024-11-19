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
package org.jahia.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.index.CheckIndex.Status;
import org.apache.lucene.store.FSDirectory;

/**
 * Search index related utilities.
 *
 * @author Sergiy Shyrkov
 */
public final class SearchIndexUtils {

    /**
     * Performs the check for the specified Lucene index using {@link CheckIndex} tool.
     *
     * @param indexDir
     *            the Lucene index directory to be checked
     * @return <code>true</code> if the index is clean, <code>false</code> otherwise
     * @throws IOException
     */
    public static boolean checkIndex(File indexDir, StringBuilder out) throws IOException {
        StringOutputStream os = new StringOutputStream();
        try (PrintStream infoStream = new PrintStream(os)) {
            CheckIndex checkIndex = new CheckIndex(FSDirectory.open(indexDir));
            checkIndex.setInfoStream(infoStream);

            Status status = checkIndex.checkIndex();

            if (out != null) {
                infoStream.flush();
                out.append(os.toString());
            }

            return status.clean;
        }
    }

    /**
     * Initializes an instance of this class.
     */
    private SearchIndexUtils() {
        super();
    }
}
