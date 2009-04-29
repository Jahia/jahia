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
package org.jahia.services.search.lucene;

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 avr. 2008
 * Time: 14:51:53
 * To change this template use File | Settings | File Templates.
 */
public class SetNonLazyFieldSelector implements FieldSelector {

    private static final long serialVersionUID = 5689559404564968096L;
    
    private List<String> fieldsToLoad;

    public SetNonLazyFieldSelector(List<String> toLoad) {
      fieldsToLoad = toLoad;
    }

    public FieldSelectorResult accept(String fieldName) {
      if(fieldsToLoad.contains(fieldName))
        return FieldSelectorResult.LOAD;
      else
        return FieldSelectorResult.LAZY_LOAD;
    }

}
