import org.jahia.registries.ServicesRegistry
import org.jahia.services.sites.JahiaSite
import org.jahia.services.pages.ContentPage
import org.jahia.content.ContentObject
import org.jahia.services.acl.JahiaBaseACL
import org.jahia.hibernate.model.JahiaAcl
import org.jahia.hibernate.manager.JahiaSiteLanguageListManager
import org.jahia.hibernate.manager.SpringContextSingleton
import org.jahia.hibernate.manager.JahiaAclManager
import org.jahia.services.workflow.WorkflowService
import org.jahia.content.ContentObjectKey
import org.jahia.services.acl.JahiaACLManagerService
import org.jahia.services.version.EntryLoadRequest
import org.jahia.services.fields.ContentField
import org.jahia.services.containers.ContentContainerList
import org.jahia.services.containers.ContentContainer
import org.jahia.content.*
import org.jahia.version.*
import org.jahia.services.database.ConnectionDispenser
import java.sql.*;

Connection connection = ConnectionDispenser.getConnection();
Map statements = new HashMap()
statements.put(ContentFieldKey.FIELD_TYPE,connection.prepareStatement("update jahia_fields_data set rights_jahia_fields_data=? where id_jahia_fields_data=?"))
statements.put(ContentPageKey.PAGE_TYPE,connection.prepareStatement("update jahia_pages_data set rights_jahia_pages_data=? where id_jahia_pages_data=?"))
statements.put(ContentContainerKey.CONTAINER_TYPE,connection.prepareStatement("update jahia_ctn_entries set rights_jahia_ctn_entries=? where id_jahia_ctn_entries=?"))
statements.put(ContentContainerListKey.CONTAINERLIST_TYPE,connection.prepareStatement("update jahia_ctn_lists set rights_jahia_ctn_lists=? where id_jahia_ctn_lists=?"))
statements.put(ContentMetadataKey.METADATA_TYPE,connection.prepareStatement("update jahia_fields_data set rights_jahia_fields_data=? where id_jahia_obj=? and type_jahia_obj=?"))

private void parseObject (ContentObject object, JahiaBaseACL currentAcl, Set toDelete, Connection connection, Set ctnlistprops, Map statements) throws Exception {
    JahiaAcl currentObjectAcl = object.getACL().getACL();

    print(object.getObjectKey().toString() + " , aclid = "+currentObjectAcl.getAclID());
//    print(" - " + WorkflowService.getInstance().getStagingLanguages((ContentObjectKey) object.getObjectKey(), object.getSiteID()));
    if (currentObjectAcl.getAclID() != currentAcl.getID()) {
        if (!currentObjectAcl.getEntries().isEmpty()) {
            JahiaBaseACL parentAcl = currentAcl;
            currentAcl = object.getACL();
            currentAcl.getACL().setHasEntries(new Integer(1));
            currentAcl.setParentID(parentAcl.getID());
            print(" has entries, set parentacl to "+parentAcl.getID());
        } else if (object.getPickedObject() != null && (
        object.getParent(null).getPickedObject() == null ||
                (!object.getPickedObject().getACL().getACL().getEntries().isEmpty() && !object.getPickedObject().isAclSameAsParent()))) {

            JahiaBaseACL parentAcl = currentAcl;
            currentAcl = object.getACL();
            currentAcl.getACL().setHasEntries(new Integer(0));
            currentAcl.setParentID(parentAcl.getID());

            print(" is a picker, keep it" + object.getParent(null).getPickedObject() + "/ "+object.getPickedObject().getACL().getACL().getEntries());
        } else {
            print(" is empty, set to "+currentAcl.getID());
            int old = object.getACL().getID();
            toDelete.add(new Integer(old));
            updateAcl(object,currentAcl.getID(), connection,statements);
            if (object instanceof ContentContainerList && ctnlistprops.contains(object.getID())) {
                Map properties = ((ContentContainerList)object).getProperties();
                Iterator iterator = new HashSet(properties.keySet()).iterator();
                while(iterator.hasNext()) {
                    String key = (String) iterator.next();
                    if (key.startsWith("view_field_acl_")) {
                        int acl = Integer.parseInt((String) properties.get(key));
                        JahiaBaseACL jacl = new JahiaBaseACL(acl);
                        if (acl == old || jacl.getACL().getEntries().isEmpty()) {
                            ((ContentContainerList)object).setProperty(key, ""+currentAcl.getID());
                            toDelete.add(new Integer(acl));
                        } else {
                            JahiaBaseACL.getACL(acl).setParentID(currentAcl.getID());
                        }
                    }
                }
                ServicesRegistry.getInstance().getJahiaContainersService().setContainerListProperties(object.getID(), object.getSiteID(), properties);
            }
        }
    }

    if (object instanceof ContentPage) {
        MyStatus.pageCount++;
        VersionService.getInstance().setPercentCompleted(MyStatus.getPercent());
    }

    println();

    List objects = object.getChilds(null, EntryLoadRequest.STAGED);
    Iterator iterator = objects.iterator();
    while(iterator.hasNext()) {
        ContentObject child = (ContentObject) iterator.next();
        parseObject(child, currentAcl, toDelete, connection, ctnlistprops, statements);
    }

}

private void updateAcl(ContentObject object, int aclId, Connection connection, Map statements) {
    PreparedStatement ps = (PreparedStatement) statements.get(object.getObjectKey().getType())

    ps.setInt(1,aclId);
    ps.setInt(2,object.getID());
    ps.execute();

    if (!(object instanceof ContentField)) {
        ps = (PreparedStatement) statements.get(ContentMetadataKey.METADATA_TYPE);
        ps.setInt(1,aclId);
        ps.setInt(2,object.getID());
        ps.setString(3,object.getObjectKey().getType());
        ps.execute();
    }
}

private void parseObjectForPickers(Connection connection) throws Exception {

    Statement st = connection.createStatement();
    ResultSet rs = st.executeQuery("select left_oid, right_oid from jahia_link where type like '%_picker_relationship' order by id");
    while (rs.next()) {
        ContentObjectKey source = (ContentObjectKey) ContentObjectKey.getInstance(rs.getString(1));
        ContentObjectKey dest = (ContentObjectKey) ContentObjectKey.getInstance(rs.getString(2));
        ContentObject object = (ContentObject)ContentObject.getInstance(source);
        ContentObject picker = (ContentObject)ContentObject.getInstance(dest);
if(picker!=null) {
        JahiaAcl pickerObjectAcl = picker.getACL()!=null ? picker.getACL().getACL():null;

        if (!picker.isAclSameAsParent() && pickerObjectAcl!=null) {
                print(object.getObjectKey().toString() + " , aclid = "+picker.getAclID());
                print(" is a picker, update acl link to "+object.getAclID());
                pickerObjectAcl.setPickedAclId(new Integer(object.getAclID()));
                ServicesRegistry.getInstance().getJahiaACLManagerService().updateCache(pickerObjectAcl);
                println();
        }
}
        MyStatus.pageCount++;
        VersionService.getInstance().setPercentCompleted(MyStatus.getPercent());
    }
}

Set toDelete = new HashSet();

Statement st = connection.createStatement();
ResultSet rs = st.executeQuery("select distinct ctnlistid_ctnlists_prop from jahia_ctnlists_prop where name_ctnlists_prop like 'view_field_acl_%'")
Set ctnlistprops = new HashSet();
while (rs.next()) {
    int id = rs.getInt(1);
     ctnlistprops.add(id);
}
println ("list :"+ctnlistprops)

rs = st.executeQuery("SELECT count( DISTINCT id_jahia_pages_data ) FROM jahia_pages_data");
rs.next();

MyStatus.total = rs.getInt(1);
MyStatus.pageCount = 0;

VersionService.getInstance().setSubStatus("org.jahia.admin.patchmanagement.aclpatch_17886.updateAclIds");

Iterator en = ServicesRegistry.getInstance().getJahiaSitesService().getSites();
while (en.hasNext()) {
    JahiaSite jahiaSite = (JahiaSite) en.next()
    ContentPage homePage = jahiaSite.getHomeContentPage()
    parseObject(homePage, homePage.getACL(), toDelete, connection,ctnlistprops, statements)
}

rs = st.executeQuery("SELECT count(*) from jahia_link where type like '%_picker_relationship'");
rs.next();

MyStatus.total = rs.getInt(1);
MyStatus.pageCount = 0;

VersionService.getInstance().setSubStatus("org.jahia.admin.patchmanagement.aclpatch_17886.pickersAcl");

parseObjectForPickers(connection)

VersionService.getInstance().setSubStatus("org.jahia.admin.patchmanagement.aclpatch_17886.deleteAcl");
MyStatus.total = toDelete.size();
MyStatus.pageCount = 0;
PreparedStatement delete = connection.prepareStatement("delete from jahia_acl where id_jahia_acl=?")

Iterator iterator = toDelete.iterator();
while (iterator.hasNext()) {
    Integer acl = (Integer) iterator.next();
    try {
        delete.setInt(1, acl.intValue())
        delete.execute();
        MyStatus.pageCount++;
        VersionService.getInstance().setPercentCompleted(MyStatus.getPercent());
    } catch (Exception e) {
        println("Cannot delete acl "+acl+ ", "+e.getMessage())
    }
}
connection.close();

class MyStatus
{
    public static int total;
    public static int pageCount = 0;
    public static double getPercent() {
        return pageCount * 100.0 / total;
    }
}

