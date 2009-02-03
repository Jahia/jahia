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
