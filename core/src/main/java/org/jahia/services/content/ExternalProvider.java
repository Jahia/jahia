package org.jahia.services.content;

import org.springframework.beans.factory.InitializingBean;

/**
 *
 * Bean that describes an external provider (like vfs ..)
 *
 */

public class ExternalProvider implements InitializingBean {
    private JCRStoreProvider provider;
    private String key;

    public JCRStoreProvider getProvider() {
        return provider;
    }

    public void setProvider(JCRStoreProvider provider) {
        this.provider = provider;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void afterPropertiesSet() throws Exception {
        JCRStoreService.getInstance().addExternalProvider(this);
    }
}
