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
