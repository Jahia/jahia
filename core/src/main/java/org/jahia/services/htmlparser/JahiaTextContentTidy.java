/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.services.htmlparser;

import org.jahia.utils.JahiaTools;

/**
 *
 * <p>Title: Jahia text content specific issue</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class JahiaTextContentTidy {

    public static String JAHIA_HTML_TAG_NAME = "jahiahtml";
    public static String JAHIA_HTML_OPEN_TAG = "$$$jahiahtml$$$";
    public static String JAHIA_RESOURCE_MARKER = "jahia$resource$marker";
    public static String JAHIA_EXPRESSION_MARKER = "jahia$expression$marker";
    public static String JAHIA_HTML_CLOSE_TAG = "$$$jahiahtmlclose$$$";
    public static String NEW_LINE = "$$$nl$$$";
    public static String WHITE_SPACE = "$$$ws$$$";

    /**
     * Formats Jahia Text content before tidy processing
     * @param input
     * @return
     */
    static public String tidyPreProcessing(String input){
        if ( input == null ){
            return null;
        }
        String result = input.trim();
        result = JahiaTools.replacePatternIgnoreCase(result,"<html>",JAHIA_HTML_OPEN_TAG);
        result = JahiaTools.replacePatternIgnoreCase(result,"</html>",JAHIA_HTML_CLOSE_TAG);
        result = JahiaTools.replacePatternIgnoreCase(result,"<jahia-resource",JAHIA_RESOURCE_MARKER);
        result = JahiaTools.replacePatternIgnoreCase(result,"<jahia-expression",JAHIA_EXPRESSION_MARKER);
        result = JahiaTools.replacePattern(result,"\r\n","\n");
        //result = encodeLineBreak(result,false);
        //result = encodeTextAreaWhiteSpace(result,true);
        return result;
    }

    /**
     * Formats Jahia Text content after tidy processing.
     * Should be called only if input string has been processed with preTidyProcessing
     *
     * @param input
     * @return
     */
    static public String tidyPostProcessing(String input){
        if ( input == null ){
            return null;
        }
        int pos = input.toLowerCase().indexOf("<body>");
        if ( pos != -1 ){
            input = input.substring(pos+6,input.toLowerCase().indexOf("</body>"));
        }
        String result = JahiaTools.replacePattern(input,NEW_LINE,"\n");
        result = JahiaTools.replacePattern(result,"&#39;","'");
        result = JahiaTools.replacePatternIgnoreCase(result,JAHIA_HTML_OPEN_TAG,"<html>");
        result = JahiaTools.replacePatternIgnoreCase(result,JAHIA_HTML_CLOSE_TAG,"</html>");
        result = JahiaTools.replacePatternIgnoreCase(result,JAHIA_RESOURCE_MARKER,"<jahia-resource");
        result = JahiaTools.replacePatternIgnoreCase(result,JAHIA_EXPRESSION_MARKER,"<jahia-expression");
        //result = encodeTextAreaWhiteSpace(result,false);
        return result.trim();
    }

}
