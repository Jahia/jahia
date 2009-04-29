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
 package org.jahia.services.search.analyzer;

 import java.util.StringTokenizer;

 import org.apache.lucene.analysis.Token;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardTokenizer;

 /**
  * Created by IntelliJ IDEA.
  * User: hollis
  * Date: 25 mai 2005
  * Time: 21:05:11
  * To change this template use File | Settings | File Templates.
  */
 public class TokenWithQuoteFilter extends SplitTokenFilter {

     private static final String APOSTROPHE_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.APOSTROPHE];

     public TokenWithQuoteFilter(TokenStream in) {
         super(in);
     }

     protected StringTokenizer splitWords(Token t) {
         StringTokenizer st = null;
         if (t.type() == APOSTROPHE_TYPE) {
             String termText = t.termText();
             if (termText.indexOf("'") != -1) {
                 st = new StringTokenizer(termText, "'");
             }
         }
         return st;
     }

 }

