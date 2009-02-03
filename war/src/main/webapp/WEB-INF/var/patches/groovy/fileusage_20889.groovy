import org.jahia.services.database.ConnectionDispenser
import org.jahia.version.VersionService
import java.sql.*;
import groovy.sql.Sql

sql = new Sql(ConnectionDispenser.getConnection())

total = sql.firstRow("select count(*) as c from jahia_fields_data where type_jahia_fields_data=6 and workflow_state>0").c + sql.firstRow("select count(*) as c from jahia_bigtext_data").c 
count = 0

sql.eachRow("select * from jahia_fields_data where type_jahia_fields_data=6 and workflow_state>0") {
    VersionService.getInstance().setPercentCompleted((count++) * 100.0 / total);
    if (it.value_jahia_fields_data!="" && it.value_jahia_fields_data != "<empty>") {
        try {
            sql.execute("insert into jahia_fieldreference (fieldId, language, workflow, target, siteId) values (${it.id_jahia_fields_data}, ${it.language_code}, ${it.workflow_state}, ${"file:"+it.value_jahia_fields_data}, ${it.jahiaid_jahia_fields_data})")
        } catch (SQLException e) {
            println("Sql exeption :  ${e.getMessage()}")
        }
    }
}

sql.eachRow("select * from jahia_bigtext_data") {
    VersionService.getInstance().setPercentCompleted((count++) * 100.0 / total);
    key = it.id_bigtext_data.split("\\.")[0].split("-")
    pathes = new HashSet();
    if (key.size() == 4 || key[4] == "s") {
        def pattern = it.raw_value =~ /"###\/webdav\/site\/[^\/]+(\/[^"]+)"/
        pattern.each { match, path ->
            try {
                pathes.add(path)
                if (!pathes.contains(path)) {
                    sql.execute("insert into jahia_fieldreference (fieldId, language, workflow, target, siteId) values (${new Integer(key[2])}, ${key[3]}, ${key.size()-3}, ${"file:"+path}, ${new Integer(key[0])})")
                }
            } catch (SQLException e) {
                println("Sql exeption :  ${e.getMessage()}")
            }
        }
    }
}
