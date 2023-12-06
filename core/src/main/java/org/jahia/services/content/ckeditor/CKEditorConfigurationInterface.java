package org.jahia.services.content.ckeditor;

import org.owasp.html.PolicyFactory;

public interface CKEditorConfigurationInterface {

    public String getCKEditor5Config(String siteKey);

    public String getCKEditor4Config(String siteKey);

    public PolicyFactory getOwaspPolicyFactory(String siteKey);

    public boolean configExists(String siteKey);
}
