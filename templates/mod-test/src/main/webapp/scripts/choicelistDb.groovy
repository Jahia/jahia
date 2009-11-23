import groovy.sql.Sql
import javax.jcr.PropertyType
import org.jahia.services.content.nodetypes.ValueImpl
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue

def values = new ArrayList<ChoiceListValue>();

sql = Sql.newInstance("jdbc:mysql://localhost/mytest?useUnicode=true&amp;characterEncoding=UTF-8&amp;useServerPrepStmts=false", 
					 "jahia", "jahia", "com.mysql.jdbc.Driver")
sql.eachRow("select * from my_products", { 
	values.add(new ChoiceListValue(it.name, null, new ValueImpl(String.valueOf(it.id), PropertyType.STRING, false)));
} );

return values;