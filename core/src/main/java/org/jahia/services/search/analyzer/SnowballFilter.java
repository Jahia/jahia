package org.jahia.services.search.analyzer;

import org.apache.lucene.analysis.*;
import net.sf.snowball.SnowballProgram;

import java.io.IOException;
import java.lang.reflect.Method;

/**
 * This snowball filter is different thant the one provided by lucene in that it returns the original unprocessed token too
 *
 *
 */
public class SnowballFilter extends TokenFilter {

    private static final Object [] EMPTY_ARGS = new Object[0];

    private SnowballProgram stemmer;
    private Method stemMethod;
        
    private Token originalToken;

    /** Construct the named stemming filter.
     *
     * @param in the input tokens to stem
     * @param name the name of a stemmer
     */
    public SnowballFilter(TokenStream in, String name) {
        super(in);
        try {
            Class stemClass =
                Class.forName("net.sf.snowball.ext." + name + "Stemmer");
            stemmer = (SnowballProgram) stemClass.newInstance();
            // why doesn't the SnowballProgram class have an (abstract?) stem method?
            stemMethod = stemClass.getMethod("stem", new Class[0]);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

   /** Returns the next input Token, after being stemmed */
    public Token next() throws IOException {
        if ( originalToken != null ){
            Token tmpToken = originalToken;
            originalToken = null;
            return tmpToken;
        }
        Token token = input.next();
        if (token == null)
        return null;
        stemmer.setCurrent(token.termText());
        try {
            stemMethod.invoke(stemmer, EMPTY_ARGS);
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
        if ( stemmer.getCurrent().equals(token.termText()) ){
            return token;
        }
        Token newToken = new Token(stemmer.getCurrent(),
                      token.startOffset(), token.endOffset(), token.type());
        newToken.setPositionIncrement(token.getPositionIncrement());
        originalToken = token;
        return newToken;
    }    
}
