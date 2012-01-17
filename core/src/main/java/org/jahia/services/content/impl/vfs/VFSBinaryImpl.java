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

package org.jahia.services.content.impl.vfs;

import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileSystemException;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * User: loom
 * Date: Aug 12, 2010
 * Time: 3:21:58 PM
 * 
 */
public class VFSBinaryImpl implements Binary {

    FileContent fileContent;
    InputStream inputStream = null;

    public VFSBinaryImpl(InputStream inputStream) {
        // here we should copy the content of the inputstream, but where ??? Keeping it in memory is a bad idea.
        this.inputStream = inputStream;
    }

    public VFSBinaryImpl(FileContent fileContent) {
        this.fileContent = fileContent;
    }

    public InputStream getStream() throws RepositoryException {
        if (fileContent == null) {
            return inputStream;
        }
        try {
            inputStream = fileContent.getInputStream();
        } catch (FileSystemException e) {
            throw new RepositoryException("Error retrieving inputstream to file content", e);
        }
        return inputStream;
    }

    public int read(byte[] b, long position) throws IOException, RepositoryException {
        if (inputStream == null) {
            getStream();
        }
        return inputStream.read(b, (int) position, b.length);
    }

    public long getSize() throws RepositoryException {
        try {
            return fileContent.getSize();
        } catch (FileSystemException e) {
            throw new RepositoryException("Error retrieving file's size", e);
        }
    }

    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
