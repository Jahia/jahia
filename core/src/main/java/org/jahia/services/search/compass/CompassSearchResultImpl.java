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
 package org.jahia.services.search.compass;

import org.compass.core.CompassDetachedHits;
import org.jahia.services.search.SearchResultImpl;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 17:48:29
 * To change this template use File | Settings | File Templates.
 */
public class CompassSearchResultImpl extends SearchResultImpl {

    private CompassDetachedHits compassHits = null;

    public CompassSearchResultImpl(){
        super();
    }

    public CompassSearchResultImpl(boolean checkAccess){
        super(checkAccess);
    }

    public CompassSearchResultImpl(CompassDetachedHits compassHits){
        this(false);
        this.compassHits = compassHits;
    }

    public CompassDetachedHits getCompassHits() {
        return compassHits;
    }

    public void setCompassHits(CompassDetachedHits compassHits) {
        this.compassHits = compassHits;
    }

}
