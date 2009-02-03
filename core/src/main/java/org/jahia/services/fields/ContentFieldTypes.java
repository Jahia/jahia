/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.fields;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>Title: Jahia</p>
 * <p>Description: CMS Enterprise Portal</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia SA</p>
 *
 * @author Dada's
 * @author Khue
 * @author MAP
 * @version 1.0
 * @see org.jahia.data.fieldTypes
 */
public class ContentFieldTypes {

    public static final int INTEGER = 1;
    public static final int SMALLTEXT = 2;
    public static final int BIGTEXT = 3;
    public static final int DATE = 4;
    public static final int PAGE = 5;
    public static final int FILE = 6;
    public static final int IMAGE = 7; // not yet implemented
    public static final int AUDIO = 8; // not yet implemented
    public static final int VIDEO = 9; // not yet implemented
    public static final int APPLICATION = 10;
    public static final int FLOAT = 11;
    public static final int BOOLEAN = 12;
    public static final int COLOR = 13;
    public static final int CATEGORY = 14;
    public static final int SMALLTEXT_SHARED_LANG = 20;
    public static final int METADATA = 30;

    private static ContentFieldTypes theObject;

    private Map<Integer, Class<? extends ContentField>> fieldClassNames;

    private ContentFieldTypes () {
        fieldClassNames = new HashMap<Integer, Class<? extends ContentField>> ();
        fieldClassNames.put (new Integer (INTEGER), ContentIntegerField.class);
        fieldClassNames.put (new Integer (SMALLTEXT), ContentSmallTextField.class);
        fieldClassNames.put (new Integer (BIGTEXT), ContentBigTextField.class);
        fieldClassNames.put (new Integer (PAGE), ContentPageField.class);
        fieldClassNames.put (new Integer (FILE), ContentFileField.class);
        fieldClassNames.put (new Integer (APPLICATION), ContentApplicationField.class);
        fieldClassNames.put (new Integer (FLOAT), ContentFloatField.class);
        fieldClassNames.put (new Integer (BOOLEAN), ContentBooleanField.class);
        fieldClassNames.put (new Integer (DATE), ContentDateField.class);
        fieldClassNames.put (new Integer (COLOR), ContentColorField.class);
        fieldClassNames.put (new Integer (CATEGORY), ContentCategoryField.class);
        fieldClassNames.put (new Integer (SMALLTEXT_SHARED_LANG), ContentSmallTextSharedLangField.class);
    }

    /**
     * @return a single instance of the object
     */
    public static synchronized ContentFieldTypes getInstance () {
        if (theObject == null) {
            theObject = new ContentFieldTypes ();
        }
        return theObject;
    }

    /**
     * @return the field class names initialized by the constructor.
     */
    public Map<Integer, Class<? extends ContentField>> getFieldClassNames () {
        return fieldClassNames;
    }

}
