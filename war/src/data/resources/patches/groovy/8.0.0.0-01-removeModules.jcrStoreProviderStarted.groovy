import java.util.stream.Collectors
import javax.jcr.*
import javax.jcr.query.*
import org.jahia.services.content.*
import org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper;

BundleInfoJcrHelper.storePersistentStates(
        BundleInfoJcrHelper.getPersistentStates()
                .stream()
                .filter { bpi -> !(bpi.getSymbolicName() in ["content-media-manager", "dx-commons-webpack", "contribute", "jcr-webdav", "document-management-api", "document-viewer-service", "document-thumbnails"]) }
                .collect(Collectors.toList())
);

JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
    public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
        NodeIterator n = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:moduleManagementBundle] where isdescendantnode('"+BundleInfoJcrHelper.PATH_BUNDLES+"')", Query.JCR_SQL2).execute().getNodes();
        while (n.hasNext()) {
            n.nextNode().setProperty("j:transformationRequired", true);
        }

        session.save();
        return null
    }
})
