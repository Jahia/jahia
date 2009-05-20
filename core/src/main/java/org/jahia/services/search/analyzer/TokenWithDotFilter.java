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
 import org.jahia.services.search.JahiaSearchConstant;

 /**
  * Created by IntelliJ IDEA.
  * User: hollis
  * Date: 25 mai 2005
  * Time: 21:05:11
  * To change this template use File | Settings | File Templates.
  */
 public class TokenWithDotFilter extends SplitTokenFilter {

     private static final String ACRONYM_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.ACRONYM];
     private static final String EMAIL_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.EMAIL];
     private static final String HOST_TYPE = StandardTokenizer.TOKEN_TYPES[StandardTokenizer.HOST];

     public TokenWithDotFilter(TokenStream in) {
         super(in);
     }

     protected StringTokenizer splitWords(Token t) {
         StringTokenizer st = null;
         if (t.type() == ACRONYM_TYPE || t.type() == EMAIL_TYPE
                 || t.type() == HOST_TYPE) {
             String termText = t.termText();
             if (!termText.startsWith(JahiaSearchConstant.JAHIA_PREFIX)
                     && termText.indexOf(".") != -1) {
                 st = new StringTokenizer(termText, ".");
             }
         }
         return st;
     }
 }
