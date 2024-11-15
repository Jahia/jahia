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
