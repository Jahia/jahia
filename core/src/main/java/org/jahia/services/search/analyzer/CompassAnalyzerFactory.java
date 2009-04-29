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

 import org.apache.lucene.analysis.Analyzer;
 import org.compass.core.config.CompassSettings;
 import org.compass.core.engine.SearchEngineException;
 import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerFactory;

 /**
  * Created by IntelliJ IDEA.
  * User: hollis
  * Date: 21 sept. 2005
  * Time: 15:46:50
  * To change this template use File | Settings | File Templates.
  */
 public class CompassAnalyzerFactory implements LuceneAnalyzerFactory {

     public Analyzer createAnalyzer(String string,
             CompassSettings compassSettings) throws SearchEngineException {
         StandardAnalyzer analyzer = new StandardAnalyzer();
         return analyzer;
     }
 }
