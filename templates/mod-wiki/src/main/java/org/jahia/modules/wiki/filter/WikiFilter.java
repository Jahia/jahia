package org.jahia.modules.wiki.filter;

import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.modules.wiki.WikiRenderer;
import org.xwiki.rendering.syntax.SyntaxFactory;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.component.embed.EmbeddableComponentManager;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 2, 2009
 * Time: 5:24:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class WikiFilter extends AbstractFilter {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(WikiFilter.class);

    private SyntaxFactory syntaxFactory;
    private String inputSyntax;
    private String outputSyntax;


    public void setSyntaxFactory(SyntaxFactory syntaxFactory) {
        this.syntaxFactory = syntaxFactory;
    }

    public void setInputSyntax(String inputSyntax) {
        this.inputSyntax = inputSyntax;
    }

    public void setOutputSyntax(String outputSyntax) {
        this.outputSyntax = outputSyntax;
    }

    protected String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        return WikiRenderer.renderWikiSyntaxAsXHTML(renderContext,chain.doFilter(renderContext, resource),syntaxFactory,inputSyntax,outputSyntax);
    }

}
