package org.jahia.bundles.url.jahiawar.internal;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.transform.XSLTransformException;
import org.jdom2.transform.XSLTransformer;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.InputStream;
import java.util.List;

/**
 * A class to perform needed transformations on Spring configuration files.
 */
public class SpringFileTransformer {

    public static Document transform(Document document) {

        InputStream stylesheetInputStream = SpringFileTransformer.class.getResourceAsStream("/org/jahia/bundles/url/jahiawar/internal/spring.xslt");
        try {
            String prefix = "beans";
            List<Element> transformedProperties = getNodes(document, "//beans:property[@ref='sessionFactory']", prefix);

            if (transformedProperties.size() == 0) {
                return document;
            }

            XSLTransformer transformer = new XSLTransformer(stylesheetInputStream);
            Document resultingDocument = transformer.transform(document);
            if (resultingDocument != null) {
                return resultingDocument;
            }
            return document;
        } catch (XSLTransformException e) {
            e.printStackTrace();
            return document;
        } catch (JDOMException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return document;
        }
    }

    public static List<Element> getNodes(Document jdomDocument, String xPathExpression, String prefix) throws JDOMException {
        Element rootElement = jdomDocument.getRootElement();
        String namespaceURI = rootElement.getNamespaceURI();
        XPathExpression<Element> xpath = XPathFactory.instance().compile(xPathExpression, Filters.element(), null, Namespace.getNamespace(prefix, namespaceURI));
        return xpath.evaluate(jdomDocument);
    }

}
