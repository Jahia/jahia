package org.jahia.services.content.impl.external.modules;

import org.jahia.api.Constants;
import org.jahia.services.content.impl.external.ExternalDataSource;
import org.jahia.services.content.impl.external.vfs.VFSDataSource;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: david
 * Date: 10/25/12
 * Time: 2:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModulesDataSource extends VFSDataSource {

    @Override
    public List<String> getSupportedNodeTypes() {
        return Arrays.asList(Constants.JAHIANT_NODETYPEFOLDER,
                Constants.JAHIANT_TEMPLATETYPEFOLDER,
                Constants.JAHIANT_CSSFOLDER,
                Constants.JAHIANT_CSSFILE,
                Constants.JAHIANT_JAVASCRIPTFOLDER,
                Constants.JAHIANT_JAVASCRIPTFILE,
                Constants.JAHIANT_VIEWFILE,
                Constants.JAHIANT_DEFINITIONFILE,
                Constants.JAHIANT_RESOURCEBUNDLEFOLDER);
    }


}
