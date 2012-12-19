package org.jahia.services.content.impl.external;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import java.util.List;
import java.util.Map;

public interface ExternalDataSource {
    boolean isSupportsUuid();

    List<String> getSupportedNodeTypes();

    ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException;

    ExternalData getItemByPath(String path) throws PathNotFoundException;

    List<String> getChildren(String path);

    public interface LazyPropery {
        String[] getPropertyValues(String path, String propertyName) throws PathNotFoundException;
    }

    public interface Writable {
        void saveItem(ExternalData data) throws PathNotFoundException;

        void move(String oldPath, String newPath) throws PathNotFoundException;

        void removeItemByPath(String path) throws PathNotFoundException;

        void order(String path, List<String> children) throws PathNotFoundException;
    }

    public interface Searchable {
        List<String> search(String basePath, String type, Map<String, String> constraints, String orderBy, int limit);
    }


}
