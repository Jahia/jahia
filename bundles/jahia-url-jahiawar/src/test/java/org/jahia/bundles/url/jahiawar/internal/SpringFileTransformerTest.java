package org.jahia.bundles.url.jahiawar.internal;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Spring file transformer unit test.
 */
public class SpringFileTransformerTest {

    @Test
    public void testSpringTransform() throws JDOMException, IOException {
        InputStream springFileInputStream = getClass().getResourceAsStream("/mod-remotepublish.xml");
        SAXBuilder saxBuilder = new SAXBuilder();
        Document jdomDocument = saxBuilder.build(springFileInputStream);
        Document transformedDomDocument = SpringFileTransformer.transform(jdomDocument);
        XMLOutputter xmlOutputter = new XMLOutputter();
        xmlOutputter.setFormat(Format.getPrettyFormat());
        xmlOutputter.output(transformedDomDocument, System.out);

        String prefix = "beans";
        List<Element> transformedProperties = getNodes(transformedDomDocument, "//beans:property[@ref='moduleSessionFactory']", prefix);
        assertTrue("Couldn't find transformed properties !", transformedProperties.size() > 0);

        // let's now check that the attribute values with curly braces have the proper values
        List<Element> hibernateDialectEntry = getNodes(transformedDomDocument, "//beans:entry[@value='${hibernate.dialect}']", prefix);
        assertTrue("Couldn't find hibernate dialect entry !", hibernateDialectEntry.size() > 0);
    }

    public List<Element> getNodes(Document jdomDocument, String xPathExpression, String prefix) throws JDOMException {
        Element rootElement = jdomDocument.getRootElement();
        String namespaceURI = rootElement.getNamespaceURI();
        XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null, Namespace.getNamespace(prefix, namespaceURI));
        return xpath.evaluate(jdomDocument);
    }

}
