import javax.jcr.PropertyType
import org.jahia.services.content.nodetypes.ValueImpl
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue

def newValues = new ArrayList<ChoiceListValue>();

def books = new XmlParser().parse(new File("c:/temp/books.xml"))
books.each { book ->
	newValues.add(new ChoiceListValue(book.'@title', null, new ValueImpl(book.'@isbn', PropertyType.STRING,false)));
}

return newValues
