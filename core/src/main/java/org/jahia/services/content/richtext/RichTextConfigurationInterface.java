package org.jahia.services.content.richtext;

import org.owasp.html.PolicyFactory;

public interface RichTextConfigurationInterface {

    public static String DEFAULT_POLICY_KEY = "default";

    public String getCKEditor5Config(String siteKey);

    public String getCKEditor4Config(String siteKey);

    public PolicyFactory getOwaspPolicyFactory(String siteKey);

    public PolicyFactory getDefaultOwaspPolicyFactory();

    public PolicyFactory getMergedOwaspPolicyFactory(String... siteKeys);

    public boolean configExists(String siteKey);
}
