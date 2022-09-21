/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a WCAG validation result for the HTML text.
 * 
 * @author Sergiy Shyrkov
 */
public class WCAGValidationResult implements Serializable {

	private static final long serialVersionUID = -548535667201816053L;
	
	public static final WCAGValidationResult OK = new WCAGValidationResult(); 

	private List<WCAGViolation> errors = new ArrayList<WCAGViolation>();
	private List<WCAGViolation> infos = new ArrayList<WCAGViolation>();
	private List<WCAGViolation> warnings = new ArrayList<WCAGViolation>();

	public List<WCAGViolation> getErrors() {
		return errors;
	}

	public List<WCAGViolation> getInfos() {
		return infos;
	}

	public List<WCAGViolation> getWarnings() {
		return warnings;
	}

	public boolean isEmpty() {
		return errors.isEmpty() && warnings.isEmpty() && infos.isEmpty();
	}
}
