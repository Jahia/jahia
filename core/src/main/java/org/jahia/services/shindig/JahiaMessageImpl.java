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
