package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.aws.ext.ds.S3Backend;
import org.apache.jackrabbit.core.data.Backend;
import org.apache.jackrabbit.core.data.CachingDataStore;
import org.jahia.settings.SettingsBean;

import java.util.Properties;
import java.util.stream.Collectors;

public class S3DataStore extends CachingDataStore {
    protected Backend createBackend() {
        S3Backend backend = new S3Backend();

        Properties properties = new Properties();

        properties.putAll(SettingsBean.getInstance().getPropertiesFile().entrySet().stream()
                .filter(e -> e.getKey().toString().startsWith("jahia.jackrabbit.s3datastore."))
                .collect(Collectors.toMap(e -> e.getKey().toString().substring("jahia.jackrabbit.s3datastore.".length()),
                        e -> e.getValue().toString())));

        backend.setProperties(properties);

        return backend;
    }

    protected String getMarkerFile() {
        return "s3.init.done";
    }

}

