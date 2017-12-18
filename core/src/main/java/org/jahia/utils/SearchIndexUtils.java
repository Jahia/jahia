/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
        PrintStream infoStream = new PrintStream(os);
        CheckIndex checkIndex = new CheckIndex(FSDirectory.open(indexDir));
        checkIndex.setInfoStream(infoStream);

        Status status = checkIndex.checkIndex();

        if (out != null) {
            infoStream.flush();
            out.append(os.toString());
        }

        return status.clean;
    }

    /**
     * Initializes an instance of this class.
     */
    private SearchIndexUtils() {
        super();
    }
}
