package org.jahia.bundles.richtext.config;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.settings.SettingsBean;
import org.jahia.bundles.richtext.config.parse.Parser;
import org.jahia.bundles.richtext.config.parse.PropsToJsonParser;
import org.jahia.services.content.richtext.RichTextConfigurationInterface;
import org.json.JSONArray;
import org.json.JSONObject;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.owasp.html.PolicyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(service = {RichTextConfigurationInterface.class, ManagedServiceFactory.class}, property = {
        "service.pid=org.jahia.bundles.richtext.config",
        "service.description=RichText configuration service",
        "service.vendor=Jahia Solutions Group SA"
}, immediate = true)
public class RichTextConfiguration implements RichTextConfigurationInterface, ManagedServiceFactory {

    private static Logger logger = LoggerFactory.getLogger(RichTextConfiguration.class);
    private static String CONFIG_FILE_NAME_BASE = "org.jahia.bundles.richtext.config-";
    private static String DEFAULT_CONFIG_FILE_NAME = CONFIG_FILE_NAME_BASE + DEFAULT_POLICY_KEY + ".yml";
    private Map<String, JSONObject> configs = new HashMap<>();
    private Map<String, String> siteKeyToPid = new HashMap<>();
    private BundleContext context;
    private SettingsBean settingsBean = org.jahia.settings.SettingsBean.getInstance();

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
        deployDefaultConfig();
        logger.info("Activate RichTextConfiguration service");
    }

    @Override
    public String getName() {
        return "RichTextConfiguration service";
    }

    @Override
    public void updated(String pid, Dictionary<String, ?> dictionary) throws ConfigurationException {
        List<String> keys = Collections.list(dictionary.keys());
        Map<String, Object> dictCopy = keys.stream()
                .filter(key -> key.startsWith("htmlFiltering."))
                .collect(Collectors.toMap(Function.identity(), dictionary::get));

        String siteKey = StringUtils.substringBefore(StringUtils.substringAfter((String) dictionary.get("felix.fileinstall.filename"), CONFIG_FILE_NAME_BASE), ".");

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
    public PolicyFactory getDefaultOwaspPolicyFactory() {
        return getOwaspPolicyFactory(DEFAULT_POLICY_KEY);
    }

    @Override
    public PolicyFactory getMergedOwaspPolicyFactory(String... siteKeys) {
        JSONObject mergedPolicy = new JSONObject();

        for (String key : siteKeys) {
            if (configExists(key)) {
                mergeJsonObject(mergedPolicy, configs.get(siteKeyToPid.get(key)));
            }
        }

        if (!mergedPolicy.isEmpty()) {
            return Parser.parseToPolicy(mergedPolicy);
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

    private void mergeJsonObject(JSONObject target, JSONObject source) {
        for (String key : source.keySet()) {
            if (source.get(key) instanceof JSONObject) {
                if (target.has(key) && target.get(key) instanceof JSONObject) {
                    mergeJsonObject(target.getJSONObject(key), source.getJSONObject(key));
                } else {
                    target.put(key, new JSONObject(source.getJSONObject(key).toString()));
                }
            } else if (source.get(key) instanceof JSONArray) {
                if (target.has(key) && target.get(key) instanceof JSONArray) {
                    JSONArray targetArray = target.getJSONArray(key);
                    JSONArray sourceArray = source.getJSONArray(key);
                    for (int i = 0; i < sourceArray.length(); i++) {
                        targetArray.put(sourceArray.get(i));
                    }
                } else {
                    target.put(key, new JSONArray(source.getJSONArray(key).toString()));
                }
            } else {
                target.put(key, source.get(key));
            }
        }
    }

    private void deployDefaultConfig() {
        URL url = context.getBundle().getResource("META-INF/configuration-default/" + DEFAULT_CONFIG_FILE_NAME);
        if (url != null) {
            Path path = Paths.get(settingsBean.getJahiaVarDiskPath(), "karaf", "etc", DEFAULT_CONFIG_FILE_NAME);
            if (!Files.exists(path)) {
                try (InputStream input = url.openStream()) {
                    List<String> lines = IOUtils.readLines(input, StandardCharsets.UTF_8);
                    lines.add(0, "# This is default configuration provided by Jahia");
                    try (Writer w = new FileWriter(path.toFile())) {
                        IOUtils.writeLines(lines, null, w);
                    }
                    logger.info("Copied default richtext configuration to {}", path);
                } catch (IOException e) {
                    logger.error("Unable to copy richtext configuration", e);
                }
            }
        }
    }
}
