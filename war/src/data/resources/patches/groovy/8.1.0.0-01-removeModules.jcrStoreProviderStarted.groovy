import java.util.stream.Collectors
import javax.jcr.*
import javax.jcr.query.*
import org.jahia.services.content.*
import org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper;

BundleInfoJcrHelper.storePersistentStates(
        BundleInfoJcrHelper.getPersistentStates()
                .stream()
                .filter { bpi -> !(bpi.getSymbolicName() in ["security-filter", "webflow-filter"]) }
                .collect(Collectors.toList())
);
