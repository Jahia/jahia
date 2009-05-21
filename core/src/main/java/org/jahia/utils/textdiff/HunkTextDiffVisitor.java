/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.utils.textdiff;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jlibdiff.Diff;
import jlibdiff.Hunk;
import jlibdiff.HunkAdd;
import jlibdiff.HunkChange;
import jlibdiff.HunkDel;
import jlibdiff.HunkVisitor;

import org.apache.log4j.Logger;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.htmlparser.TidyHtmlParser;
import org.springframework.beans.factory.BeanFactory;


/**
 *
 * <p>Title: Text Diff highlighter based on JLibDiff API </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class HunkTextDiffVisitor extends HunkVisitor {
    
    private static final transient Logger logger = Logger
            .getLogger(HunkTextDiffVisitor.class);

    private String oldText = "";
    private String newText = "";

    private List hunks = new ArrayList();

    private StringBuffer highlightedOldText = new StringBuffer();
    private StringBuffer highlightedNewText = new StringBuffer();
    private StringBuffer mergedDiffText     = new StringBuffer();

    public static final String DEFAULT_COMPARE_ADDED_DIFFERENCE_STYLE = "background-color:aqua !important;color:black !important;";
    public static final String DEFAULT_COMPARE_CHANGED_DIFFERENCE_STYLE = "background-color:lime !important;color:black !important;";
    public static final String DEFAULT_COMPARE_DELETED_DIFFERENCE_STYLE = "background-color:red !important; text-decoration:line-through !important; color:black !important;";

    public static String compareAddedDifferenceStyle = null;
    public static String compareChangedDifferenceStyle = null;
    public static String compareDeletedDifferenceStyle = null;

    /**
     *
     * @param oldText
     * @param newText
     */
    public HunkTextDiffVisitor(String oldText, String newText) {

        if ( compareAddedDifferenceStyle == null ){
            try {
                BeanFactory bf = SpringContextSingleton.getInstance().getContext();
                Properties settings = (Properties)bf.getBean("settings");
                if ( settings != null ){
                    compareAddedDifferenceStyle = settings.getProperty("org.jahia.services.versioning.style.compareAddedDifference",
                            DEFAULT_COMPARE_ADDED_DIFFERENCE_STYLE);
                    compareChangedDifferenceStyle = settings.getProperty("org.jahia.services.versioning.style.compareChangedDifference",
                            DEFAULT_COMPARE_CHANGED_DIFFERENCE_STYLE);
                    compareDeletedDifferenceStyle = settings.getProperty("org.jahia.services.versioning.style.compareDeletedDifference",
                            DEFAULT_COMPARE_DELETED_DIFFERENCE_STYLE);
                }
            } catch ( Exception t ){
            }
        }
        if ( oldText != null ){
            this.oldText = oldText;
            this.highlightedOldText = new StringBuffer(oldText);
            this.mergedDiffText = new StringBuffer(oldText);
        }

        if ( newText != null ){
            this.newText = newText;
            this.highlightedNewText = new StringBuffer(newText);
        }

        // Highlight text diff
        try {
            jlibdiff.Diff diff = new Diff();
            diff.diffString(oldText,newText);
            diff.accept(this);
        } catch ( Exception e ){
            logger.error(e.getMessage(), e);
        }
    }

    /**
     *
     * @param hunkadd
     */
    public void visitHunkAdd(HunkAdd hunkadd) {
        this.hunks.add(hunkadd);
    }

    /**
     *
     * @param hunkchange
     */
    public void visitHunkChange(HunkChange hunkchange) {
        this.hunks.add(hunkchange);
    }

    public void visitHunkDel(HunkDel hunkdel) {
        this.hunks.add(hunkdel);
    }

    /**
     * You must call this method to run text diff processing
     *
     */
    public void highLightDiff(){

        if ( this.hunks.size()==0 ){
            return;
        }

        StringBuffer oldValueBuff = new StringBuffer();
        StringBuffer newValueBuff = new StringBuffer();
        StringBuffer mergedValueBuff = new StringBuffer();

        int oldValuePos = 0;
        int newValuePos = 0;

        int size = this.hunks.size();
        Hunk hunk = null;

        String val = null;
        String rawVal = null;
        for ( int i=0; i<size ; i++ ){

            hunk = (Hunk)this.hunks.get(i);
            if ( hunk instanceof HunkAdd ){
                oldValueBuff.append(this.oldText.substring(oldValuePos,hunk.lowLine(0)));
                newValueBuff.append(this.newText.substring(newValuePos,hunk.lowLine(1)-1));
                mergedValueBuff.append(this.oldText.substring(oldValuePos,hunk.lowLine(0)));
                StringBuffer buff = new StringBuffer();
                buff.append("<span class='compareAddedDifference' ");
                if ( compareAddedDifferenceStyle != null && !"".equals(compareAddedDifferenceStyle) ){
                    buff.append("style='").append(compareAddedDifferenceStyle).append("'");
                }
                buff.append(">");
                val = this.newText.substring(hunk.lowLine(1)-1,hunk.highLine(1));
                rawVal = val;
                if ( openTag(val,'<') ){
                    int pos = val.lastIndexOf("<");
                    if ( pos == 0 ){
                        buff.append("</span>");
                        buff.append(val);
                    } else {
                        buff.append(val.substring(0,pos-1));
                        buff.append("</span>");
                        buff.append(val.substring(pos));
                    }
                } else if ( openTag(val,'>') ){
                    int pos = val.lastIndexOf(">");
                    if ( pos == val.length()-1 ){
                        buff.append(val);
                        buff.append("</span>");
                    } else {
                        buff.insert(0,val.substring(0,pos+1));
                        buff.append(val.substring(pos+1));
                        buff.append("</span>");
                    }
                } else {
                    buff.append(val);
                    buff.append("</span>");
                }
                if ( openTag(mergedValueBuff.toString(),'<') ){
                    mergedValueBuff.append(rawVal);
                } else {
                    mergedValueBuff.append(buff.toString());
                }
                if ( openTag(newValueBuff.toString(),'<') ){
                    newValueBuff.append(rawVal);
                } else {
                    newValueBuff.append(buff.toString());
                }

                oldValuePos = hunk.highLine(0);
                newValuePos = hunk.highLine(1);

            } else if ( hunk instanceof HunkChange ){
                oldValueBuff.append(this.oldText.substring(oldValuePos,hunk.lowLine(0)-1));
                newValueBuff.append(this.newText.substring(newValuePos,hunk.lowLine(1)-1));
                mergedValueBuff.append(this.oldText.substring(oldValuePos,hunk.lowLine(0)-1));

                // old
                StringBuffer buff = new StringBuffer();
                buff.append("<span class='compareChangedDifference' ");
                if ( compareChangedDifferenceStyle != null && !"".equals(compareChangedDifferenceStyle) ){
                    buff.append("style='").append(compareChangedDifferenceStyle).append("'");
                }
                buff.append(">");
                val = this.oldText.substring(hunk.lowLine(0)-1,hunk.highLine(0));
                rawVal = val;
                if ( openTag(val,'<') ){
                    int pos = val.lastIndexOf("<");
                    if ( pos == 0 ){
                        buff.append("</span>");
                        buff.append(val);
                    } else {
                        buff.append(val.substring(0,pos-1));
                        buff.append("</span>");
                        buff.append(val.substring(pos));
                    }
                } else if ( openTag(val,'>') ){
                    int pos = val.lastIndexOf(">");
                    if ( pos == val.length()-1 ){
                        buff.append(val);
                        buff.append("</span>");
                    } else {
                        buff.insert(0,val.substring(0,pos+1));
                        buff.append(val.substring(pos+1));
                        buff.append("</span>");
                    }
                } else {
                    buff.append(val);
                    buff.append("</span>");
                }
                if ( openTag(mergedValueBuff.toString(),'<') ){
                    oldValueBuff.append(rawVal);
                } else {
                    oldValueBuff.append(buff.toString());
                }
                // new
                buff = new StringBuffer();
                buff.append("<span class='compareChangedDifference' ");
                if ( compareChangedDifferenceStyle != null && !"".equals(compareChangedDifferenceStyle) ){
                    buff.append("style='").append(compareChangedDifferenceStyle).append("'");
                }
                buff.append(">");
                val = this.newText.substring(hunk.lowLine(1)-1,hunk.highLine(1));
                rawVal = val;
                if ( openTag(val,'<') ){
                    int pos = val.lastIndexOf("<");
                    if ( pos == 0 ){
                        buff.append("</span>");
                        buff.append(val);
                    } else {
                        buff.append(val.substring(0,pos-1));
                        buff.append("</span>");
                        buff.append(val.substring(pos));
                    }
                } else if ( openTag(val,'>') ){
                    int pos = val.lastIndexOf(">");
                    if ( pos == val.length()-1 ){
                        buff.append(val);
                        buff.append("</span>");
                    } else {
                        buff.insert(0,val.substring(0,pos+1));
                        buff.append(val.substring(pos+1));
                        buff.append("</span>");
                    }
                } else {
                    buff.append(val);
                    buff.append("</span>");
                }
                if ( openTag(mergedValueBuff.toString(),'<') ){
                    mergedValueBuff.append(rawVal);
                } else {
                    mergedValueBuff.append(buff.toString());
                }
                if ( openTag(newValueBuff.toString(),'<') ){
                    newValueBuff.append(rawVal);
                } else {
                    newValueBuff.append(buff.toString());
                }
                oldValuePos = hunk.highLine(0);
                newValuePos = hunk.highLine(1);

            } else {
                oldValueBuff.append(this.oldText.substring(oldValuePos,hunk.lowLine(0)-1));
                newValueBuff.append(this.newText.substring(newValuePos,hunk.lowLine(1)));
                mergedValueBuff.append(this.oldText.substring(oldValuePos,hunk.lowLine(0)-1));

                StringBuffer buff = new StringBuffer();
                buff.append("<span class='compareDeletedDifference' ");
                if ( compareDeletedDifferenceStyle != null && !"".equals(compareDeletedDifferenceStyle) ){
                    buff.append("style='").append(compareDeletedDifferenceStyle).append("'");
                }
                buff.append(">");
                val = this.oldText.substring(hunk.lowLine(0)-1,hunk.highLine(0));
                rawVal = val;
                if ( openTag(val,'<') ){
                    int pos = val.lastIndexOf("<");
                    if ( pos == 0 ){
                        buff.append("</span>");
                        buff.append(val);
                    } else {
                        buff.append(val.substring(0,pos-1));
                        buff.append("</span>");
                        buff.append(val.substring(pos));
                    }
                } else if ( openTag(val,'>') ){
                    int pos = val.lastIndexOf(">");
                    if ( pos == val.length()-1 ){
                        buff.append(val);
                        buff.append("</span>");
                    } else {
                        buff.insert(0,val.substring(0,pos+1));
                        buff.append(val.substring(pos+1));
                        buff.append("</span>");
                    }
                } else {
                    buff.append(val);
                    buff.append("</span>");
                }

                // old
                if ( openTag(oldValueBuff.toString(),'<') ){
                    oldValueBuff.append(rawVal);
                } else {
                    oldValueBuff.append(buff.toString());
                }
                if ( openTag(mergedValueBuff.toString(),'<') ){
                    mergedValueBuff.append(rawVal);
                } else {
                    mergedValueBuff.append(buff.toString());
                }
                oldValuePos = hunk.highLine(0);
                newValuePos = hunk.highLine(1);
            }
        }
        oldValueBuff.append(this.oldText.substring(oldValuePos));
        newValueBuff.append(this.newText.substring(newValuePos));
        mergedValueBuff.append(this.oldText.substring(oldValuePos));

        //this.highlightedOldText = oldValueBuff;
        //this.highlightedNewText = newValueBuff;
        //this.mergedDiffText = mergedValueBuff;

        String parsedHtml = ServicesRegistry.getInstance().getHtmlParserService()
            .parse(oldValueBuff.toString(),new ArrayList());
        if ( parsedHtml.indexOf(TidyHtmlParser.TIDYERRORS_TAG) == -1 ){
            this.highlightedOldText = oldValueBuff;
        }

        parsedHtml = ServicesRegistry.getInstance().getHtmlParserService()
            .parse(newValueBuff.toString(),new ArrayList());
        if ( parsedHtml.indexOf(TidyHtmlParser.TIDYERRORS_TAG) == -1 ){
            this.highlightedNewText = newValueBuff;
        }

        parsedHtml = ServicesRegistry.getInstance().getHtmlParserService()
            .parse(mergedValueBuff.toString(),new ArrayList());
        if ( parsedHtml.indexOf(TidyHtmlParser.TIDYERRORS_TAG) == -1 ){
            this.mergedDiffText = mergedValueBuff;
        } else {
            this.mergedDiffText = new StringBuffer("<span class='compareChangedDifference' ");
            if ( compareChangedDifferenceStyle != null && !"".equals(compareChangedDifferenceStyle) ){
                this.mergedDiffText.append("style='").append(compareChangedDifferenceStyle).append("'");
            }
            this.mergedDiffText.append(">").append(this.highlightedNewText.toString()+"</span>");
        }
        this.mergedDiffText = mergedValueBuff;

    }

    protected boolean openTag(String input, char openChar){
        if ( input == null || "".equals(input.trim()) ){
            return false;
        }
        if ( openChar == '<' ){
            int lastOpenTag = input.lastIndexOf('<');
            if ( lastOpenTag == -1 ){
                return false;
            }
            if ( lastOpenTag > input.lastIndexOf('>') ){
                return true;
            }
        } else {
            int lastOpenTag = input.indexOf('>');
            if ( lastOpenTag == -1 ){
                return false;
            }
            if ( lastOpenTag < input.indexOf('<') ){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    public String getHighlightedOldText(){
        return this.highlightedOldText.toString();
    }

    /**
     *
     * @return
     */
    public String getHighlightedNewText(){
        return this.highlightedNewText.toString();
    }

    /**
     *
     * @return
     */
    public String getMergedDiffText(){
        return this.mergedDiffText.toString();
    }

    /**
     * Returns a deleted text style
     *
     * @param value
     * @return
     */
    public static String getDeletedText(String value){
        StringBuffer buff = new StringBuffer();
        buff.append("<span class='compareDeletedDifference' ");
        if ( compareDeletedDifferenceStyle != null && !"".equals(compareDeletedDifferenceStyle) ){
            buff.append("style='").append(compareDeletedDifferenceStyle).append("'");
        }
        buff.append(">");
        buff.append(value);
        buff.append("</span>");
        return buff.toString();
    }

    /**
     * Returns an added text style
     *
     * @param value
     * @return
     */
    public static String getAddedText(String value){
        StringBuffer buff = new StringBuffer();
        buff.append("<span class='compareAddedDifference' ");
        if ( compareAddedDifferenceStyle != null && !"".equals(compareAddedDifferenceStyle) ){
            buff.append("style='").append(compareAddedDifferenceStyle).append("'");
        }
        buff.append(">");
        buff.append(value);
        buff.append("</span>");
        return buff.toString();
    }

    /**
     * Returns a changed text style
     *
     * @param value
     * @return
     */
    public static String getChangedText(String value){
        StringBuffer buff = new StringBuffer();
        buff.append("<span class='compareChangedDifference' ");
        if ( compareChangedDifferenceStyle != null && !"".equals(compareChangedDifferenceStyle) ){
            buff.append("style='").append(compareChangedDifferenceStyle).append("'");
        }
        buff.append(">");
        buff.append(value);
        buff.append("</span>");
        return buff.toString();
    }

    public String getNewText() {
        return newText;
    }

    public void setNewText(String newText) {
        this.newText = newText;
    }

    public String getOldText() {
        return oldText;
    }

    public void setOldText(String oldText) {
        this.oldText = oldText;
    }

}
