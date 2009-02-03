import org.jahia.registries.ServicesRegistry
import org.jahia.services.sites.JahiaSite
import org.jahia.services.pages.ContentPage
import org.jahia.hibernate.model.JahiaAcl
import org.jahia.services.workflow.WorkflowService
import org.jahia.services.version.EntryLoadRequest
import org.jahia.content.*
import java.sql.Connection
import java.sql.ResultSet
import org.jahia.services.database.ConnectionDispenser
import java.sql.Statement
import org.jahia.version.VersionService

private void parseObject (ContentObject object) throws Exception {
    JahiaAcl currentObjectAcl = object.getACL().getACL();

    println(object.getObjectKey().toString() + " - " + WorkflowService.getInstance().getStagingLanguages((ContentObjectKey) object.getObjectKey(), object.getSiteID()));

    List objects = object.getChilds(null, EntryLoadRequest.STAGED);
    Iterator iterator = objects.iterator();

    if (object instanceof ContentPage) {
        MyStatus.pageCount++;
        VersionService.getInstance().setPercentCompleted(MyStatus.getPercent());
    }

    while(iterator.hasNext()) {
        ContentObject child = (ContentObject) iterator.next();
        parseObject(child);
    }
}

VersionService.getInstance().setSubStatus("org.jahia.admin.patchmanagement.languagestates_17886");

Connection connection = ConnectionDispenser.getConnection();
Statement st = connection.createStatement();
ResultSet rs = st.executeQuery("SELECT count( DISTINCT id_jahia_pages_data ) FROM jahia_pages_data");
rs.next();
MyStatus.total = rs.getInt(1);
MyStatus.pageCount = 0;
connection.close();

Iterator en = ServicesRegistry.getInstance().getJahiaSitesService().getSites();
while (en.hasNext()) {
    JahiaSite jahiaSite = (JahiaSite) en.next()
    ContentPage homePage = jahiaSite.getHomeContentPage()
    parseObject(homePage)
}

class MyStatus
{
    public static int total;
    public static int pageCount = 0;
    public static double getPercent() {
        return pageCount * 100.0 / total;
    }
}

