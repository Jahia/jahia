package org.jahia.services.workflow.jbpm.custom.email;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MailTemplateRegistry {

    private Map<String, MailTemplate> mailTemplates = new ConcurrentHashMap<String, MailTemplate>();

    public Map<String, MailTemplate> getMailTemplates() {
        return mailTemplates;
    }

    public void setMailTemplates(Map<String, MailTemplate> mailTemplates) {
        this.mailTemplates = mailTemplates;
    }

    public MailTemplate addTemplate(String name, MailTemplate mailTemplate) {
        return mailTemplates.put(name, mailTemplate);
    }

    public MailTemplate removeTemplate(String name) {
        return mailTemplates.remove(name);
    }

    public MailTemplate getTemplate(String name) {
        return mailTemplates.get(name);
    }
}
