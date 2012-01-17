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

package org.jahia.services.content;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private Map<String,Boolean> canPublish;
    private List<PublicationInfoNode> child = new LinkedList<PublicationInfoNode>();
    private List<PublicationInfo> references = new LinkedList<PublicationInfo>();
    private List<String> pruned;
    
    private boolean subtreeProcessed;

    public PublicationInfoNode() {
        super();
        canPublish = new HashMap<String, Boolean>(3);
    }

    public PublicationInfoNode(String uuid, String path) {
        this();
        this.uuid = uuid;
        this.path = path;
    }

    public PublicationInfoNode(String uuid, String path, int status) {
        this(uuid, path);
        this.status = status;
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

    public boolean isCanPublish(String language) {
        Boolean aBoolean = canPublish.get(language);
        return aBoolean !=null?aBoolean:false;
    }

    public void setCanPublish(boolean canPublish,String language) {
        this.canPublish.put(language,canPublish);
    }

    public List<PublicationInfoNode> getChildren() {
        return child;
    }

    public List<PublicationInfo> getReferences() {
        return references;
    }

    public List<String> getPruned() {
        return pruned;
    }

    public void addChild(PublicationInfoNode node) {
        child.add(node);
    }

    public PublicationInfoNode addChild(String uuid, String path) {
        final PublicationInfoNode node = new PublicationInfoNode(uuid, path);
        child.add(node);
        return node;
    }

    public void addReference(PublicationInfo ref) {
        references.add(ref);
    }

    public PublicationInfo addReference(String uuid, String path) {
        final PublicationInfo ref = new PublicationInfo(uuid, path);
        references.add(ref);
        return ref;
    }

    public boolean isSubtreeProcessed() {
        return subtreeProcessed;
    }

    public void setSubtreeProcessed(boolean subtreeProcessed) {
        this.subtreeProcessed = subtreeProcessed;
    }
}
