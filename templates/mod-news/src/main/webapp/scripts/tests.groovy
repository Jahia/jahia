import org.jahia.services.content.nodetypes.ValueImpl
import javax.jcr.PropertyType
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue

def values = new ArrayList<ChoiceListValue>();
values.add(new ChoiceListValue("Ca marche",null,new ValueImpl("Ca marche",PropertyType.STRING,false)));
values.add(new ChoiceListValue("Ca marche 2",null,new ValueImpl("Ca marche 2",PropertyType.STRING,false)));
return values;