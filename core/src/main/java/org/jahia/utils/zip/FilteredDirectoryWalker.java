/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils.zip;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;

/**
 * Utility class for zipping a directory tree content considering path
 * inclusion/exclusion filters. Exclusing always takes precedence over
 * inclusion.
 * 
 * @author Sergiy Shyrkov
 */
public class FilteredDirectoryWalker extends DirectoryWalker {

    private static class PathFilter implements FileFilter {

        private String[] includeWildcards;

        private String[] excludeWildcards;

        public PathFilter(String[] includeWildcards, String[] excludeWildcards) {
            super();
            this.includeWildcards = includeWildcards;
            this.excludeWildcards = excludeWildcards;
        }

        private static boolean matches(String path, String wildcard) {
            return FilenameUtils
                    .wildcardMatch(path, wildcard, IOCase.SENSITIVE);
        }

        public boolean accept(File file) {
            if (file.isDirectory())
                return true;

            boolean accept = false;
            String path = file.getPath();
            if (includeWildcards.length > 0) {
                for (String wildcard : includeWildcards) {
                    if (matches(path, wildcard)) {
                        accept = true;
                        break;
                    }
                }
            } else {
                accept = true;
            }

            if (accept && excludeWildcards.length > 0) {
                for (String wildcard : excludeWildcards) {
                    if (matches(path, wildcard)) {
                        accept = false;
                        break;
                    }
                }
            }
            return accept;
        }
    }

    private static final String[] EMPTY_ARRAY = {};

    private static String[] convertPatterns(File baseDir, String[] pathPatterns) {
        String[] convertedPatterns = new String[pathPatterns.length];
        for (int i = 0; i < pathPatterns.length; i++) {
            convertedPatterns[i] = new File(baseDir,
                    convertSeparator(pathPatterns[i])).getPath();
        }

        return convertedPatterns;
    }

    private static String convertSeparator(String pathPattern) {
        return File.separatorChar != '/' ? pathPattern.replace('/',
                File.separatorChar) : pathPattern;
    }

    private File startDirectory;

    /**
     * Initializes an instance of this class to include all resources under
     * <code>startDirectory</code>.
     * 
     * @param startDirectory
     */
    public FilteredDirectoryWalker(File startDirectory) {
        this(startDirectory, null, null);
    }

    public FilteredDirectoryWalker(File startDirectory,
            String[] includePathPatterns, String[] excludePathPatterns) {
        super(new PathFilter(
                convertPatterns(startDirectory,
                        includePathPatterns != null ? includePathPatterns
                                : EMPTY_ARRAY), convertPatterns(startDirectory,
                        excludePathPatterns != null ? excludePathPatterns
                                : EMPTY_ARRAY)), -1);
        this.startDirectory = startDirectory;
    }

    @Override
    protected void handleFile(File file, int depth, Collection results)
            throws IOException {
        results.add(file);
    }

    public void zip(ZipOutputStream zout) throws IOException {
        List<File> results = new LinkedList<File>();
        walk(startDirectory, results);
        for (File file : results) {
            if (file.getPath().length() <= startDirectory.getPath().length())
                continue;

            if (zout != null) {
                zout.putNextEntry(new ZipEntry(file.getPath().substring(
                        startDirectory.getPath().length() + 1)));
                zout.write(FileUtils.readFileToByteArray(file));
            } else {
                System.out.println(file);
            }
        }
    }
}
