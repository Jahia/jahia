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
package org.jahia.services.deamons.filewatcher;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;

/**
 * The result of the file monitor check with the list of created, changed and deleted files.
 * 
 * @author Sergiy Shyrkov
 */
public class FileMonitorResult implements FileListener {

    private List<File> changed = Collections.emptyList();

    private List<File> created = Collections.emptyList();

    private List<File> deleted = Collections.emptyList();

    /**
     * Initializes an instance of this class.
     * 
     * @param delegate
     *            the delegate listener to notify about file changes
     */
    public FileMonitorResult() {
        super();
    }

    @Override
    public void fileChanged(File file) {
        if (changed.isEmpty()) {
            changed = new LinkedList<File>();
        }

        changed.add(file);
    }

    @Override
    public void fileCreated(File file) {
        if (created.isEmpty()) {
            created = new LinkedList<File>();
        }

        created.add(file);
    }

    @Override
    public void fileDeleted(File file) {
        if (deleted.isEmpty()) {
            deleted = new LinkedList<File>();
        }

        deleted.add(file);
    }

    public Iterable<File> getAll() {
        return new Iterable<File>() {
            @SuppressWarnings("unchecked")
            @Override
            public Iterator<File> iterator() {
                return IteratorUtils.chainedIterator(getDeleted().iterator(),
                        IteratorUtils.chainedIterator(getChanged().iterator(), getCreated().iterator()));
            }
        };
    }

    public List<File> getAllAsList() {
        List<File> all = new LinkedList<File>();
        all.addAll(deleted);
        all.addAll(changed);
        all.addAll(created);

        return all;
    }

    public List<File> getChanged() {
        return changed;
    }

    public List<File> getCreated() {
        return created;
    }

    public List<File> getDeleted() {
        return deleted;
    }

    public String getInfo() {
        StringBuilder b = new StringBuilder(64);
        boolean first = true;
        b.append("Created ").append(created.size()).append(" files");
        if (!created.isEmpty()) {
            b.append(":\n");
            for (File f : created) {
                if (first) {
                    first = false;
                } else {
                    b.append("\n");
                }
                b.append("\t").append(f);
            }
        }
        b.append("\nChanged ").append(changed.size()).append(" files");
        if (!changed.isEmpty()) {
            b.append(":\n");
            first = true;
            for (File f : changed) {
                if (first) {
                    first = false;
                } else {
                    b.append("\n");
                }
                b.append("\t").append(f);
            }
        }
        b.append("\nDeleted ").append(deleted.size()).append(" files");
        if (!deleted.isEmpty()) {
            b.append(":\n");
            first = true;
            for (File f : deleted) {
                if (first) {
                    first = false;
                } else {
                    b.append("\n");
                }
                b.append("\t").append(f);
            }
        }

        return b.toString();
    }

    public boolean isEmpty() {
        return getChanged().isEmpty() && getCreated().isEmpty() && getDeleted().isEmpty();
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("Created ").append(created.size()).append(" files, changed ")
                .append(changed.size()).append(" files, deleted ").append(deleted.size()).append(" files").toString();
    }
}
