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

//

package org.jahia.services.version;

import java.util.Comparator;
import java.util.Locale;

import org.jahia.utils.LanguageCodeConverters;

/**
 *
 * <p>Title: Comparator for Revision Entry</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class RevisionsComparator implements Comparator {

    public static final int ASC_ORDER = 1;
    public static final int DESC_ORDER = 2;

    public static final int SORT_BY_DATE = 1;
    public static final int SORT_BY_STATE = 2;
    public static final int SORT_BY_LANG = 3;
    public static final int SORT_BY_TITLE = 4;
    public static final int SORT_BY_OBJECT = 5;
    public static final int SORT_BY_REVISION_TYPE = 6;
    public static final int SORT_BY_DESCR = 7;

    private Locale displayLocale;
    private int sortOrder;
    private int sortAttribute;

    public RevisionsComparator(String languageCode,
                               int sortAttribute,
                               int sortOrder) {
        this.displayLocale =
                LanguageCodeConverters.languageCodeToLocale(languageCode);
        this.sortOrder = sortOrder;
        this.sortAttribute = sortAttribute;
    }

    /**
     * Compare two Revision Entry objects
     *
     * @param object1
     * @param object2
     * @return
     */
    public int compare(Object object1, Object object2) {

        RevisionEntry rev1 = (RevisionEntry)object1;
        RevisionEntry rev2 = (RevisionEntry)object2;

        if (this.sortAttribute == RevisionsComparator.SORT_BY_DATE) {
            if ( this.sortOrder == RevisionsComparator.ASC_ORDER ){
                return (new Integer(rev1.getVersionID())
                        .compareTo(new Integer(rev2.getVersionID())));
            } else {
                return (new Integer(rev2.getVersionID())
                        .compareTo(new Integer(rev1.getVersionID())));
            }
        } else if (this.sortAttribute == RevisionsComparator.SORT_BY_STATE) {
            if ( this.sortOrder == RevisionsComparator.ASC_ORDER ){
                return (new Integer(rev1.getWorkflowState())
                        .compareTo(new Integer(rev2.getWorkflowState())));
            } else {
                return (new Integer(rev2.getWorkflowState())
                        .compareTo(new Integer(rev1.getWorkflowState())));
            }
        } else if (this.sortAttribute == RevisionsComparator.SORT_BY_LANG) {
            Locale lang1 = LanguageCodeConverters
                         .languageCodeToLocale(rev1.getLanguageCode());
            Locale lang2 = LanguageCodeConverters
                         .languageCodeToLocale(rev2.getLanguageCode());
            if ( this.sortOrder == RevisionsComparator.ASC_ORDER ){
                return (lang1.getDisplayName(this.displayLocale)
                        .compareTo(lang2.getDisplayName(this.displayLocale)));
            } else {
                return (lang2.getDisplayName(this.displayLocale)
                        .compareTo(lang1.getDisplayName(this.displayLocale)));
            }
        } else if (this.sortAttribute == RevisionsComparator.SORT_BY_TITLE ) {
            String title1 = (String)rev1.getProperty(RevisionEntry.REVISION_TITLE);
            String title2 = (String)rev2.getProperty(RevisionEntry.REVISION_TITLE);
            if ( this.sortOrder == RevisionsComparator.ASC_ORDER ){
                return title1.compareTo(title2);
            } else {
                return title2.compareTo(title1);
            }
        } else if (this.sortAttribute == RevisionsComparator.SORT_BY_OBJECT ){
            String obj1 = rev1.getObjectKey().getType();
            String obj2 = rev2.getObjectKey().getType();
            int res = 0;
            if ( this.sortOrder == RevisionsComparator.ASC_ORDER ){
                res = (obj1.compareTo(obj2));
            } else {
                res = (obj2.compareTo(obj1));
            }
            if ( res == 0 ){
                int i1 = rev1.getObjectKey().getIdInType();
                int i2 = rev2.getObjectKey().getIdInType();
                if ( this.sortOrder == RevisionsComparator.ASC_ORDER ){
                    res = i1<i2 ? -1 : (i1==i2 ? 0 : 1);
                } else {
                    res = i2<i1 ? -1 : (i1==i2 ? 0 : 1);
                }
            }
            return res;
        } else if (this.sortAttribute == RevisionsComparator.SORT_BY_REVISION_TYPE ) {
            // @todo
        }
        return 0;
    }

}
