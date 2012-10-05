package org.jahia.services.content.impl.external;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import java.util.List;
import java.util.Map;

public interface ExternalDataSource {
    boolean isSupportsUuid();

    boolean isSupportsSearch();

    List<String> getSupportedNodeTypes();

    ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException;

    ExternalData getItemByPath(String path) throws PathNotFoundException;

    List<String> getChildren(String path);

    List<String> search(String basePath, String type, Map<String, String> constraints, String orderBy, int limit);

}
