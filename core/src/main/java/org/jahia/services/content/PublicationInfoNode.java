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
package org.jahia.services.content;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * @author toto
 * Date: Aug 26, 2010
 * Time: 5:31:55 PM
 */
public class PublicationInfoNode implements Serializable {
    
    private static final long serialVersionUID = 8826165087616513109L;
    
    private String uuid;
    private String path;
    private int status;
    private boolean locked;
    private boolean workInProgress = false;
    private List<PublicationInfoNode> child = new LinkedList<PublicationInfoNode>();
    private List<PublicationInfo> references = new LinkedList<PublicationInfo>();

    private boolean subtreeProcessed;

    public PublicationInfoNode() {
        super();
    }

    public PublicationInfoNode(String uuid, String path) {
        this();
        this.uuid = uuid;
        this.path = path;
    }

    public String getUuid() {
        return uuid;
    }

    public String getPath() {
        return path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isWorkInProgress() {
        return workInProgress;
    }

    public void setWorkInProgress(boolean workInProgress) {
        this.workInProgress = workInProgress;
    }

    public List<PublicationInfoNode> getChildren() {
        return child;
    }

    public List<PublicationInfo> getReferences() {
        return references;
    }

    public void addChild(PublicationInfoNode node) {
        child.add(node);
    }

    public void addReference(PublicationInfo ref) {
        references.add(ref);
    }

    public boolean isSubtreeProcessed() {
        return subtreeProcessed;
    }

    public void setSubtreeProcessed(boolean subtreeProcessed) {
        this.subtreeProcessed = subtreeProcessed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PublicationInfoNode that = (PublicationInfoNode) o;

        if (!path.equals(that.path)) {
            return false;
        }
        if (!uuid.equals(that.uuid)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + path.hashCode();
        return result;
    }

    
    @Override
    public String toString() {
        return path != null ? path : super.toString();
    }
}
