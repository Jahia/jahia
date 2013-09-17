package org.jahia.services.workflow.jbpm.custom.email;

import java.util.ArrayList;
import java.util.List;

public class MailTemplate {

    private AddressTemplate from;
    private AddressTemplate to;
    private AddressTemplate cc;
    private AddressTemplate bcc;
    private String subject;
    private String text;
    private String html;
    private String language;
    private List<AttachmentTemplate> attachmentTemplates = new ArrayList<AttachmentTemplate>();

    public AddressTemplate getFrom() {
        return from;
    }

    public void setFrom(AddressTemplate from) {
        this.from = from;
    }

    public AddressTemplate getTo() {
        return to;
    }

    public void setTo(AddressTemplate to) {
        this.to = to;
    }

    public AddressTemplate getCc() {
        return cc;
    }

    public void setCc(AddressTemplate cc) {
        this.cc = cc;
    }

    public AddressTemplate getBcc() {
        return bcc;
    }

    public void setBcc(AddressTemplate bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<AttachmentTemplate> getAttachmentTemplates() {
        return attachmentTemplates;
    }

    public boolean addAttachmentTemplate(AttachmentTemplate attachmentTemplate) {
        return attachmentTemplates.add(attachmentTemplate);
    }
}
