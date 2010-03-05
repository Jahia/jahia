package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.settings.SettingsBean;
import org.outerj.daisy.diff.HtmlCleaner;
import org.outerj.daisy.diff.XslFilter;
import org.outerj.daisy.diff.html.HTMLDiffer;
import org.outerj.daisy.diff.html.HtmlSaxDiffOutput;
import org.outerj.daisy.diff.html.TextNodeComparator;
import org.outerj.daisy.diff.html.dom.DomTreeBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Mar 4, 2010
 * Time: 3:29:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class DiffHelper {
    private static final transient Logger logger = Logger.getLogger(DiffHelper.class);
    private static final String DIFF_CONFIG_FILE = "/WEB-INF/etc/config/diffhtmlheader.xsl";

    public String getHighlighted(String original, String amendment) {
        final StringWriter sw = new StringWriter();

        try {

            final SAXTransformerFactory tf = (SAXTransformerFactory) TransformerFactory.newInstance();
            final TransformerHandler result = tf.newTransformerHandler();
            result.setResult(new StreamResult(sw));

            final XslFilter filter = new XslFilter();

            // replace /live/ by /default/ in href and src attributes as it represents same image
            original = original.replaceAll("/"+ Constants.LIVE_WORKSPACE+"/","/"+Constants.EDIT_WORKSPACE+"/");
            final InputStream oldStream = new StringBufferInputStream(original);
            final InputStream newStream = new StringBufferInputStream(amendment);

            //String xlsFilePath = Jahia.getStaticServletConfig().getServletContext().getRealPath(DIFF_CONFIG_FILE);
            //logger.debug("******** "+Jahia.getStaticServletConfig().getServletContext().getRealPath(DIFF_CONFIG_FILE));
            final ContentHandler postProcess = filter.xsl(result, "org/outerj/daisy/diff/htmlheader.xsl");

            final Locale locale = Locale.getDefault();
            final String prefix = "diff";

            final HtmlCleaner cleaner = new HtmlCleaner();

            final InputSource oldSource = new InputSource(oldStream);
            final InputSource newSource = new InputSource(newStream);

            final DomTreeBuilder oldHandler = new DomTreeBuilder();
            cleaner.cleanAndParse(oldSource, oldHandler);

            final TextNodeComparator leftComparator = new TextNodeComparator(oldHandler, locale);

            final DomTreeBuilder newHandler = new DomTreeBuilder();
            cleaner.cleanAndParse(newSource, newHandler);

            final TextNodeComparator rightComparator = new TextNodeComparator(newHandler, locale);

            postProcess.startDocument();
            postProcess.startElement("", "diffreport", "diffreport",new AttributesImpl());
            addDiffCss(postProcess);
            postProcess.startElement("", "diff", "diff", new AttributesImpl());


            final HtmlSaxDiffOutput output = new HtmlSaxDiffOutput(postProcess,prefix);

            final HTMLDiffer differ = new HTMLDiffer(output);
            differ.diff(leftComparator, rightComparator);


            postProcess.endElement("", "diff", "diff");
            postProcess.endElement("", "diffreport", "diffreport");
            postProcess.endDocument();

        } catch (Throwable e) {
            logger.error(e, e);
        }

        return sw.toString();
    }

    /**
     * add css
     * @param handler
     * @throws org.xml.sax.SAXException
     */
    private void addDiffCss(ContentHandler handler) throws SAXException {
        handler.startElement("", "css", "css",new AttributesImpl());
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "href", "href", "CDATA", Jahia.getContextPath()+"/gwt/resources/css/diff.css");
        attr.addAttribute("", "type", "type", "CDATA", "text/css");
        attr.addAttribute("", "rel", "rel", "CDATA", "stylesheet");
        handler.startElement("", "link", "link",attr);
        handler.endElement("", "link", "link");
        handler.endElement("", "css", "css");
    }

}
