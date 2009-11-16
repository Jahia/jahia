import org.jahia.services.content.nodetypes.ValueImpl
import javax.jcr.PropertyType
import javax.jcr.Value

def values = new Value[2];
values[0]= new ValueImpl("Ca marche",PropertyType.STRING,false);
values[1]= new ValueImpl("Ca marche 2",PropertyType.STRING,false);
return values;