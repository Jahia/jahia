/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.notification.templates;

import java.io.File;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Part;

/**
 * Represents an e-mail attachment.
 * 
 * @author Sergiy Shyrkov
 */
public class Attachment {

    public static final String ATTACHMENT = Part.ATTACHMENT;

    public static final String INLINE = Part.INLINE;

    private String disposition = Part.ATTACHMENT;

    private DataSource dataSource;

    private String name;

    /**
     * Initializes an instance of this class.
     * 
     * @param name
     * @param file
     */
    public Attachment(String name, File file) {
        super();
        this.name = name;
        this.dataSource = new FileDataSource(file);
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name
     * @param disposition
     * @param dataSource
     */
    public Attachment(String name, DataSource dataSource) {
        super();
        this.name = name;
        this.dataSource = dataSource; 
    }

    /**
     * @return the disposition
     */
    public String getDisposition() {
        return disposition;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param disposition the disposition to set
     */
    public void setDisposition(String disposition) {
        if (disposition == null || !ATTACHMENT.equals(disposition) && !INLINE.equals(disposition)) {
            throw new IllegalArgumentException("Unsupported attachment disposition type '" + disposition + "'");
        }
        this.disposition = disposition;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the dataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

}
