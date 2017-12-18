/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.wcag;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * Represents a single WCAG validation error/warning/info item.
 * 
 * @author Sergiy Shyrkov
 */
public class WCAGViolation extends BaseModel {

	private static final long serialVersionUID = 5076304453248072003L;
	
	public WCAGViolation() {
		super();
    }

	public WCAGViolation(String type, String message, String context, String code, String example, Integer line, Integer column) {
		this();
		setType(type);
		setMessage(message);
		setContext(context);
		setCode(code);
		setExample(example);
		setLine(line);
		setColumn(column);
    }

	public String getCode() {
		return get("code");
	}

	public Integer getColumn() {
		return (Integer) get("column");
	}

	public String getContext() {
		return get("context");
	}

	public String getExample() {
		return get("example");
	}

	public Integer getLine() {
		return (Integer) get("line");
	}

	public String getMessage() {
		return get("message");
	}

	public String getType() {
		return get("type");
	}

	public void setCode(String code) {
		set("code", code);
	}

	public void setColumn(Integer column) {
		set("column", column);
	}

	public void setContext(String context) {
		set("context", context);
	}

	public void setExample(String example) {
		set("example", example);
	}

	public void setLine(Integer line) {
		set("line", line);
	}
	
	public void setMessage(String message) {
		set("message", message);
	}
	
	public void setType(String type) {
		set("type", type);
	}
}
