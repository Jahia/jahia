<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<style type="text/css">
    td.title, legend {
        font-weight: bold;
    }

    td.hint, label.hint, input, select {
        font-size: 0.9em;
    }
</style>
<div class="searchform">
<form action="#" name="searchForm" id="searchForm">
<fieldset>
    <legend>Saved search</legend>
    <div>
        <select name="jcr_storedQueryId">
            <option value="">Select a saved search</option>
        </select>
        <button type="button">Save search</button>
    </div>
    <div>
        <input type="checkbox" id="check1" name="jcr_storedQueryPublic"/>&nbsp;<label for="check1">My saved searches
        only</label>
    </div>
</fieldset>

<fieldset>
    <legend>Text search</legend>
    <table cellpadding="3" style="width: 100%;">
        <colgroup>
            <col width="22%"/>
            <col width="25%"/>
            <col width="35%"/>
            <col width="18%"/>
        </colgroup>
        <tbody>
            <tr style="visibility: collapse">
                <td width="22%"/>
                <td width="25%"/>
                <td width="35%"/>
                <td width="18%"/>
            </tr>
            <tr>
                <td colspan="3"><input type="text" name="jcr_textSearches[0].term" style="width: 95%"/></td>
                <td>
                    <a href="#add">Add text</a>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <div>
                    	<span><input type="radio" name="jcr_textSearches[0].matchType" id="check2" value="any_word"
                                     checked="checked"/>&nbsp;<label for="check2">Any word</label></span><span
                            >&nbsp;<input type="radio"
                                          name="jcr_textSearches[0].matchType" id="check3"
                                          value="all_words"/>&nbsp;<label
                            for="check3">All words</label></span>&nbsp;<span><input
                            type="radio" name="jcr_textSearches[0].matchType" id="check4"
                            value="exact_phrase"/>&nbsp;<label for="check4">Exact phrase</label></span><span
                            >&nbsp;<input type="radio"
                                          name="jcr_textSearches[0].matchType" id="check5"
                                          value="without_words"/>&nbsp;<label for="check5">None of these
                        words</label></span></div>
                </td>
            </tr>
            <tr>
                <td colspan="4">
                    <div>
                        search in&nbsp;
                    	<span><input type="checkbox"
                                     id="check6" name="jcr_textSearches[0].searchFields.filename"
                                     value="true" checked=""/>&nbsp;<label for="check6">Content</label></span>
                    	    <span
                                    >&nbsp;<input type="checkbox" id="check7"
                                                  name="jcr_textSearches[0].searchFields.documentTitle" value="true"
                                                  checked=""/>&nbsp;<label for="check7">Metadata</label></span><span
                            >&nbsp;<input type="checkbox" id="check8"
                                          name="jcr_textSearches[0].searchFields.description" value="true"
                                          checked=""/>&nbsp;<label for="check8">File name</label></span><span
                            >&nbsp;<input type="checkbox" id="check10"
                                          name="jcr_textSearches[0].searchFields.content" value="true"
                                          checked=""/>&nbsp;<label for="check10">File metadata</label></span>
                    	    <span>&nbsp;<input type="checkbox" id="check11"
                                               name="jcr_textSearches[0].searchFields.content" value="true"
                                               checked=""/>&nbsp;<label for="check11">File content</label></span></div>
                </td>
            </tr>
        </tbody>
    </table>
</fieldset>

<fieldset>
    <legend>Author and date</legend>
    <table cellpadding="3" style="width: 100%;">
        <colgroup>
            <col width="22%"/>
            <col width="25%"/>
            <col width="35%"/>
            <col width="18%"/>
        </colgroup>
        <tbody>
            <tr style="visibility: collapse">
                <td width="22%"/>
                <td width="25%"/>
                <td width="35%"/>
                <td width="18%"/>
            </tr>
            <tr>
                <td>Author</td>
                <td class="hint">return documents created by</td>
                <td colspan="2"><input type="text"
                                       name="jcr_author"/></td>
            </tr>
            <tr>
                <td>Creation date</td>
                <td class="hint">return documents created in</td>
                <td colspan="2">
                    <div><select name="jcr_creationDate.type">
                        <option value="anytime">anytime</option>
                        <option value="today">today</option>
                        <option value="last_week">last week</option>
                        <option value="last_month">last month</option>
                        <option value="last_three_months">last 3 months</option>
                        <option value="last_six_months">last 6 months</option>
                        <option value="range">date range...</option>
                    </select></div>
                </td>
            </tr>
            <tr>
                <td>Last editor</td>
                <td class="hint">return documents edited by</td>
                <td colspan="2"><input type="text"
                                       name="jcr_lastEditor"/></td>
            </tr>
            <tr>
                <td>Last modification date</td>
                <td class="hint">return documents edited in</td>
                <td colspan="2">
                    <div><select
                            name="jcr_lastModificationDate.type">
                        <option value="anytime">anytime</option>
                        <option value="today">today</option>
                        <option value="last_week">last week</option>
                        <option value="last_month">last month</option>
                        <option value="last_three_months">last 3 months</option>
                        <option value="last_six_months">last 6 months</option>
                        <option value="range">date range...</option>
                    </select></div>
                </td>
            </tr>
        </tbody>
    </table>
</fieldset>

<fieldset>
    <legend>Documents</legend>
    <table cellpadding="3" style="width: 100%;">
        <colgroup>
            <col width="22%"/>
            <col width="25%"/>
            <col width="35%"/>
            <col width="18%"/>
        </colgroup>
        <tbody>
            <tr style="visibility: collapse">
                <td width="22%"/>
                <td width="25%"/>
                <td width="35%"/>
                <td width="18%"/>
            </tr>
            <tr>
                <td>Document type</td>
                <td class="hint">return files of type</td>
                <td colspan="2">
                    <select name="jcr_documentType">
                        <option value="">any</option>
                        <option value="nt:file">File</option>
                        <option value="nt:folder">Folder</option>
                        <option value="jmix:document">Document file</option>
                        <option value="jmix:image">Image file</option>
                        <option value="jmix:photo">Picture file</option>
                        <option value="jmix:test" selected="selected">Test type</option>
                        <option value="jmix:SysDocProto">jmix_SysDocProto</option>
                    </select>
                </td>
            </tr>
            <tr>
                <td>Location</td>
                <td class="hint">return files located in</td>
                <td colspan="2">
                    <input type="text" name="location"/>
                    &nbsp;<a href="#select">select</a>
                    &nbsp;<input type="checkbox"
                                 id="check19" name="jcr_textSearches[0].searchFields.filename"
                                 value="true" checked="checked"/>&nbsp;<label for="check19" class="hint">include
                    subfolders</label></td>
            </tr>
            <tr>
                <td>File format</td>
                <td class="hint">return files of the format</td>
                <td colspan="2">
                    <select name="jcr_properties[0].values">
                        <option value="">any</option>
                        <option value="application/pdf">Adobe Acrobat PDF (*.pdf)</option>
                        <option value="application/msword">Microsoft Word (*.doc)</option>
                        <option value="application/vnd.ms-excel">Microsoft Excel (*.xls)</option>
                        <option value="application/vnd.ms-powerpoint">Microsoft Powerpoint (*.ppt)</option>
                        <option value="application/zip">Archive (*.zip,*.rar,*.tar)</option>
                    </select>
                </td>
            </tr>
        </tbody>
    </table>
</fieldset>

<fieldset>
    <legend>More...</legend>
    <table cellpadding="3" style="width: 100%;">
        <colgroup>
            <col width="22%"/>
            <col width="25%"/>
            <col width="35%"/>
            <col width="18%"/>
        </colgroup>
        <tbody>
            <tr style="visibility: collapse">
                <td width="22%"/>
                <td width="25%"/>
                <td width="35%"/>
                <td width="18%"/>
            </tr>
            <tr>
                <td>Site</td>
                <td class="hint">search within a site</td>
                <td colspan="2"><select name="jcr_creationDate.type">
                    <option value="anytime">any</option>
                    <option value="today">jahia</option>
                    <option value="last_week">mySite</option>
                </select>
                </td>
            </tr>
            <tr>
                <td>Results per page</td>
                <td class="hint">displayed items per page</td>
                <td colspan="2">
                    <div><select name="jcr_creationDate.type">
                        <option value="anytime">5</option>
                        <option value="today" selected="selected">10</option>
                        <option value="last_week">20</option>
                        <option value="last_month">30</option>
                        <option value="last_three_months">50</option>
                        <option value="last_six_months">100</option>
                    </select></div>
                </td>
            </tr>
        </tbody>
    </table>
</fieldset>

<div style="width: 100%; text-align: right;">
    <button type="button" class="gwt-Button" name="doSearch">Search</button>
</div>

</form>
</div>

<h3>Search Results</h3>

<div id="resultslist">
    <ol>
        <li>
            <dl>
                <dt><a href="#">Jahia 5.0 SP3 released</a></dt>
                <dd class="desc">This 3rd Service Pack for the Jahia 5.0 release is mainly a performance pack (please
                    read carefully the new Performance Guide) It also include a few generic development sponsorized by
                    certain customers. We strongly recommend to all Jahia 5 customers to migrate to Jahia 5.0.3 (SP3) as
                    it corrects hundreds of issues.
                </dd>
                <dd class="filetype">HTML</dd>
                <dd class="date">22 October 2007</dd>
            </dl>
        </li>
        <li>
            <dl>
                <dt><span style="font-weight: normal; font-size: 0.9em;">[PDF]</span><a href="#">Jahia 5 Performance
                    Roadmap</a></dt>
                <dd class="desc">Benchmark of Jahia performance. Comparison between 4.2 - Sp1 - Sp2 ... This document
                    presents the roadmap to improve Jahia's performance, along ...
                </dd>
                <dd class="filetype">File Format: Adobe Acrobat PDF</dd>
                <dd>last modified 22 October 2007 by Good Developer</dd>
            </dl>
        </li>
        <li>
            <dl>
                <dt><a href="#">Jahia 5.0 SP3 released</a></dt>
                <dd class="desc">This 3rd Service Pack for the Jahia 5.0 release is mainly a performance pack (please
                    read carefully the new Performance Guide) It also include a few generic development sponsorized by
                    certain customers. We strongly recommend to all Jahia 5 customers to migrate to Jahia 5.0.3 (SP3) as
                    it corrects hundreds of issues.
                </dd>
                <dd class="filetype">HTML</dd>
                <dd class="date">22 October 2007</dd>
            </dl>
        </li>
    </ol>
</div>

<div class="pagination">
    <p><span><strong>Previous</strong></span> <span>1</span> <a href="#">2</a> <a href="#">3</a> <a href="#">4</a> <a
            href="#">5</a> <a href="#"><strong>Next</strong></a></p>
    <h4>Results 1-10 of 119</h4>
</div>
