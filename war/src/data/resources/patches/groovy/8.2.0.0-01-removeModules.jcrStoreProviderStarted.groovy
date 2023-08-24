import java.util.stream.Collectors
import javax.jcr.*
import javax.jcr.query.*
import org.jahia.services.content.*
import org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper;

BundleInfoJcrHelper.storePersistentStates(
        BundleInfoJcrHelper.getPersistentStates()
                .stream()
                .filter { bpi -> !(bpi.getSymbolicName() in ["content-editor", "jahia-category-manager"]) }
                .collect(Collectors.toList())
);
