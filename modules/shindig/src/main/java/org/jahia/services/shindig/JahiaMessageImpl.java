/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.shindig;

import org.apache.shindig.social.opensocial.model.Message;
import org.apache.shindig.social.opensocial.model.Url;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;
import java.util.Date;
import java.util.List;

/**
 * Jahia's implementation of an OpenSocial message.
 *
 * @author loom
 *         Date: Jun 24, 2010
 *         Time: 3:39:12 PM
 */
public class JahiaMessageImpl implements Message {
    private JCRNodeWrapper messageNode;

    public JahiaMessageImpl(JCRNodeWrapper messageNode) throws RepositoryException {
        super();
        this.messageNode = messageNode;
        populateValues();
    }

    private void populateValues() throws RepositoryException {
    }

    public String getAppUrl() {
        return null;
    }

    public void setAppUrl(String url) {
    }

    public String getBody() {
        return null;
    }

    public void setBody(String newBody) {
    }

    public String getBodyId() {
        return null;
    }

    public void setBodyId(String bodyId) {
    }

    public List<String> getCollectionIds() {
        return null;
    }

    public void setCollectionIds(List<String> collectionIds) {
    }

    public String getId() {
        return null;
    }

    public void setId(String id) {
    }

    public String getInReplyTo() {
        return null;
    }

    public void setInReplyTo(String parentId) {
    }

    public List<String> getRecipients() {
        return null;
    }

    public List<String> getReplies() {
        return null;
    }

    public Status getStatus() {
        return null;
    }

    public void setStatus(Status status) {
    }

    public void setRecipients(List<String> recipients) {
    }

    public String getSenderId() {
        return null;
    }

    public void setSenderId(String senderId) {
    }

    public Date getTimeSent() {
        return null;
    }

    public void setTimeSent(Date timeSent) {
    }

    public String getTitle() {
        return null;
    }

    public void setTitle(String newTitle) {
    }

    public String getTitleId() {
        return null;
    }

    public void setTitleId(String titleId) {
    }

    public Type getType() {
        return null;
    }

    public void setType(Type newType) {
    }

    public Date getUpdated() {
        return null;
    }

    public void setUpdated(Date updated) {
    }

    public List<Url> getUrls() {
        return null;
    }

    public void setUrls(List<Url> urls) {
    }

    public String sanitizeHTML(String htmlStr) {
        return null;
    }
}
