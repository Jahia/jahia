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
    private boolean workInProgressChild = false;
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

    public boolean isWorkInProgressChild() {
        return workInProgressChild;
    }

    public void setWorkInProgressChild(boolean workInProgressChild) {
        this.workInProgressChild = workInProgressChild;
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
