package org.jahia.test.framework;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class DeployResourcesTestExecutionListener extends AbstractTestExecutionListener {
    private static Logger logger = LoggerFactory.getLogger(DeployResourcesTestExecutionListener.class);
    
    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        deployCndFiles("./src/main/resources/META-INF");
        deployCndFiles("./src/test/resources/META-INF");
    }

    private void deployCndFiles(String resourcePath) throws Exception {
        File dir = new File(resourcePath);
        FileFilter fileFilter = new WildcardFileFilter("*.cnd");
        File[] files = dir.listFiles(fileFilter);
        for (File cndFile : files) {
            NodeTypeRegistry.getInstance()
                    .addDefinitionsFile(cndFile, "", null);
        }
    }
}
