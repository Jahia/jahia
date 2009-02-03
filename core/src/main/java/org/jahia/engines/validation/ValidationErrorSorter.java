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

package org.jahia.engines.validation;

import java.util.Comparator;

import org.jahia.data.containers.ContainerEditViewFieldGroup;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;

/**
*
* <p>Title: ValidationErrorSorter</p>
* <p>Description: This class sorts the validation messages. The messages, which are on the currently
*                 processed page are the first items. Then the messages are sorted according to the
*                 appearance on the screen (first tab, first field to last tab, last field).</p>
* <p>Copyright: Copyright (c) 2004</p>
* <p>Company: Jahia Ltd</p>
* @author not attributable
* @version 1.0
*/

public class ValidationErrorSorter implements Comparator<ValidationError> {
  ContainerEditViewFieldGroup fieldGroup = null;
  public ValidationErrorSorter(ContainerEditViewFieldGroup currentFieldGroup) {
    super();
    fieldGroup = currentFieldGroup;
  }

  public int compare(ValidationError o1, ValidationError o2) {
    Object leftObj = o1.getSource();
    Object rightObj = o2.getSource();

    if (leftObj != null
      && rightObj != null
      && leftObj instanceof JahiaField
      && rightObj instanceof JahiaField
      && fieldGroup != null) {

      JahiaField leftField = (JahiaField)leftObj;
      JahiaField rightField = (JahiaField)rightObj;

      try {
        boolean leftExists =
          fieldGroup.fieldExists(leftField.getDefinition().getName());
        boolean rightExists =
          fieldGroup.fieldExists(
            rightField.getDefinition().getName());

        if (leftExists != rightExists)
          return leftExists == true ? -1 : 1;
        else {
          return Math.abs(leftField.getID())
            < Math.abs(rightField.getID())
            ? -1
            : 1;
        }
      } catch (JahiaException e) {}
    }
    if (leftObj != null
      && leftObj instanceof JahiaField
      && (rightObj == null || !(rightObj instanceof JahiaField)))
      return -1;

    if (rightObj != null
      && rightObj instanceof JahiaField
      && (leftObj == null || !(leftObj instanceof JahiaField)))
      return 1;

    return leftObj.hashCode() < rightObj.hashCode()
      ? -1
      : (leftObj.equals(rightObj) ? 0 : 1);
  }

  public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            return true;
        }
        return false;
  }
}
