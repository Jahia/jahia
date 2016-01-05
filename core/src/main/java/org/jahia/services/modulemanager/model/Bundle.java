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
package org.jahia.services.modulemanager.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Bean;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Field;
import org.apache.jackrabbit.ocm.mapper.impl.annotation.Node;

/**
 * The data object for the bundle entry.
 * 
 * @author Sergiy Shyrkov
 */
@Node(jcrType = "jnt:moduleManagementBundle", discriminator = false)
public class Bundle extends BasePersistentObject {

    private static final long serialVersionUID = 7399867537701726556L;

    @Field(jcrName = "j:checksum")
    private String checksum;

    @Field(jcrName = "j:displayName")
    private String displayName;

    @Bean(jcrName = "jcr:content", jcrMandatory = true)
    private BinaryFile file;

    @Field(jcrName = "j:fileName")
    private String fileName;

    @Field(jcrName = "j:symbolicName")
    private String symbolicName;

    @Field(jcrName = "j:version")
    private String version;

    /**
     * Initializes an instance of this class.
     */
    public Bundle() {
        super();
    }

    /**
     * Initializes an instance of this class.
     *
     * @param name the bundle entry name
     */
    public Bundle(String name) {
        super(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            Bundle other = (Bundle) obj;
            return StringUtils.equals(getName(), other.getName())
                    && StringUtils.equals(getChecksum(), other.getChecksum());
        }

        return false;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getDisplayName() {
        return displayName;
    }

    public BinaryFile getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).append(getChecksum()).toHashCode();
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setFile(BinaryFile file) {
        this.file = file;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
