package org.jahia.bundles.ckeditor.config;

import org.apache.commons.lang.StringUtils;
import org.jahia.bundles.ckeditor.config.parse.Parser;
import org.jahia.bundles.ckeditor.config.parse.PropsToJsonParser;
import org.jahia.services.content.ckeditor.CKEditorConfigurationInterface;
import org.json.JSONObject;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(service = {CKEditorConfigurationInterface.class, ManagedServiceFactory.class}, property = {
        "service.pid=org.jahia.bundles.ckeditor.config",
        "service.description=CKEditor configuration service",
        "service.vendor=Jahia Solutions Group SA"
}, immediate = true)
public class CKEditorConfiguration implements CKEditorConfigurationInterface, ManagedServiceFactory {

    private static Logger logger = LoggerFactory.getLogger(CKEditorConfiguration.class);
    private Map<String, JSONObject> configs = new HashMap<>();
    private Map<String, String> siteKeyToPid = new HashMap<>();

    @Activate
    public void activate() {
        logger.info("Activate CKEditorConfiguration service");
    }

    @Override
    public String getName() {
        return "CKEditorConfiguration service";
    }

    @Override
    public void updated(String pid, Dictionary<String, ?> dictionary) throws ConfigurationException {
        List<String> keys = Collections.list(dictionary.keys());
        Map<String, Object> dictCopy = keys.stream()
                .filter(key -> key.startsWith("htmlFiltering."))
                .collect(Collectors.toMap(Function.identity(), dictionary::get));

        String siteKey = StringUtils.substringBefore(StringUtils.substringAfter((String) dictionary.get("felix.fileinstall.filename"), "org.jahia.bundles.ckeditor.config-"), ".");

        if (!dictCopy.isEmpty()) {
            configs.put(pid, new PropsToJsonParser().parse(dictCopy));
            siteKeyToPid.put(siteKey, pid);
            logger.info(String.format("Setting htmlFiltering config for site %s: %s", siteKey, configs.get(pid).toString()));
        } else {
            logger.warn(String.format("Could not find htmlFiltering object for site: %s", siteKey));
        }
    }

    @Override
    public void deleted(String pid) {
        configs.remove(pid);
    }

    @Override
    public String getCKEditor5Config(String siteKey) {
        return null;
    }

    @Override
    public String getCKEditor4Config(String siteKey) {
        return null;
    }

    @Override
    public PolicyFactory getOwaspPolicyFactory(String siteKey) {
        if (configExists(siteKey)) {
            JSONObject config = configs.get(siteKeyToPid.get(siteKey));
            return Parser.parseToPolicy(config);
        }
        return null;
    }

    @Override
    public boolean configExists(String siteKey) {
        if (siteKeyToPid.containsKey(siteKey)) {
            String pid = siteKeyToPid.get(siteKey);
            return configs.containsKey(pid);
        }
        return false;
    }
}
