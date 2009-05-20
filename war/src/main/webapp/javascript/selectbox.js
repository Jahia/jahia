

/* cutPasteSelectBox **********************************************************/
//
// This function cut and paste selected option on a source select box to a target
// select box. Note that if the original selectbox does not contain any otions
// it has to be disabled and the first option (#0) has to contain an <<empty>>
// string (i.e. "-- no option --").
//
// Parameters : 
//      sourceSelectBox : the select box source
//      targetSelectBox : the select box target
// Return :
//      nothing; the result is instantaneously displayed on the browser.
// Testing on :
//      IE 5.0
//      Netscape 6.2
// Version : 
//      v.1.0 MAP
// Known bugs :
// 
function cutPasteSelectBox(sourceSelectBox, targetSelectBox, emptyString)
{
    // This test verify if the select box has already element or not.
    // The user can define an <<empty>> element at position 0 containing a 
    // string i.e. "-- empty --". This test will find 2 consecutive "-"
    // chars and remove the string.
    if (targetSelectBox.options[0] != null && sourceSelectBox.selectedIndex != -1 &&
            targetSelectBox.options[0].text.indexOf("--") != -1)
    {
        targetSelectBox.disabled = false;
        targetSelectBox.options[0] = null;    
    }
    // Start cut and paste operation
    var targetSize; 
    targetSize = targetSelectBox.length;
    var i;      // source index of the select
    // Start parsing source select box
    for (i = 0; i < sourceSelectBox.length; i++)  
    {
        if (sourceSelectBox.options[i].selected)
        {
            // Create a new option in the target select box
            targetSelectBox.options[targetSize] = new Option(sourceSelectBox.options[i].text, 
                                                             sourceSelectBox.options[i].value);
            // Remove the selected option in the source box, but the indexes have
            // to be adjusted
            sourceSelectBox.options[i] = null;
            // Because of deletion of the option, the rest of the select box has
            // jumped up. The index has to be the same.
            i--; 
            targetSize++; // The target select box has one option more
        }
    }
    // If the source select box is empty, the first option can be recreated by
    // adding the "emptyString" parameter.
    if (sourceSelectBox.options[0] == null)
    {
        sourceSelectBox.options[0] = new Option(emptyString, "null");
        sourceSelectBox.disabled = true;
    }
}

/* removeSelectBox **********************************************************/
//
// This function remove selected option on a source select box.
// Note that if the original selectbox does not contain any otions
// it has to be disabled and the first option (#0) has to contain an <<empty>>
// string (i.e. "-- no option --").
//
// Parameters : 
//      sourceSelectBox : the select box source
// Return :
//      nothing; the result is instantaneously displayed on the browser.
// Testing on :
//      IE 5.0
//      Netscape 6.2
// Version : 
//      v.1.0 MAP
// Known bugs :
// 
function removeSelectBox(sourceSelectBox, emptyString)
{
    var i;      // source index of the select
    // Start parsing source select box
    for (i = 0; i < sourceSelectBox.length; i++)  
    {
        if (sourceSelectBox.options[i].selected)
        {
            // Remove the selected option in the source box, but the indexes have
            // to be adjusted
            sourceSelectBox.options[i] = null;
            // Because of deletion of the option, the rest of the select box has
            // jumped up. The index has to be the same.
            i--; 
        }
    }
    // If the source select box is empty, the first option can be recreated by
    // adding the "emptyString" parameter.
    if (sourceSelectBox.options[0] == null)
    {
        sourceSelectBox.options[0] = new Option(emptyString, "null");
        sourceSelectBox.disabled = true;
    }
}

/* sortSelectBox **************************************************************/
//
// Sort the content of a given select box using the Quicksort method. An optional
// parameter regExp can also be used to remove a part of the options[].text 
// string. This parameter format is a regular expression.
//
// Parameters : 
//      sBox : the select box source
//      regExp : the regular expression
// Return :
//      nothing; the result is instantaneously displayed on the browser.
// Testing on :
//      IE 5.0
//      Netscape 6.2
// Version : 
//      v.1.0 MAP
// Known bugs :
//      Does not work correctly with accentueted characters.

// These global variables are used with sortSelectBox
var selectBox;
var regularExpression;
// This variable content is not attached to a select box context. That means if 
// more than one select box are display on a page the sort direction can be
// unpredicable.
var invertSort = true;

// Used from sortSelectBox function.
function selectBoxSplit(low, high)
{
    var left = low;
    var right = high;
    var pivot = selectBox.options[low].text;
    var pivotVal = selectBox.options[low].value;
    
    while (left < right)
    {
        if (!invertSort)
        {
            while (selectBox.options[right].text.toLowerCase().replace(regularExpression, "") >
                                           pivot.toLowerCase().replace(regularExpression, ""))
                right--;
            while ((left < right) && 
                   (selectBox.options[left].text.toLowerCase().replace(regularExpression, "") <=
                                           pivot.toLowerCase().replace(regularExpression, "")))
                left++;
        } else
        {
            while (selectBox.options[right].text.toLowerCase().replace(regularExpression, "") <
                                           pivot.toLowerCase().replace(regularExpression, ""))
                right--;
            while ((left < right) && 
                   (selectBox.options[left].text.toLowerCase().replace(regularExpression, "") >=
                                           pivot.toLowerCase().replace(regularExpression, "")))
                left++;            
        }
        if (left < right)
        {
            var text = selectBox.options[left].text;
            var value = selectBox.options[left].value;
            selectBox.options[left].text = selectBox.options[right].text;
            selectBox.options[left].value = selectBox.options[right].value;
            selectBox.options[right].text = text;
            selectBox.options[right].value = value;
        }
    }
    selectBox.options[low].text = selectBox.options[right].text;
    selectBox.options[low].value = selectBox.options[right].value;
    selectBox.options[right].text = pivot;
    selectBox.options[right].value = pivotVal;

    return right;
}

// Used with sortSelectBox function
function quickSort(low, high)
{
    var mid;
    
    if (low < high)
    {
        mid = selectBoxSplit(low, high);
        quickSort(low, mid - 1);
        quickSort(mid + 1, high);
    }
}
// Irrelevant, only for time comparison
function bubbleSort()
{
    for (i = 0; i < selectBox.length; i++) {
        for (j = 0; j < selectBox.length; j++) {
            if (selectBox.options[i].text < selectBox.options[j].text) {
                var text = selectBox.options[i].text;
                selectBox.options[i].text = selectBox.options[j].text;
                selectBox.options[j].text = text;
            }
        }
    }
}

function sortSelectBox(sBox, invert, regExp) 
{
    selectBox = sBox;
    invertSort = !invertSort;
    regularExpression = regExp;
    quickSort(0, selectBox.length - 1);
    //bubbleSort();
}

/* selectAllOptionsSelectBox **************************************************/
//
// Select all options in a given select box.
//
// Parameters : 
//      sBox : the select box source
// Return :
//      nothing; all options will be selected
// Testing on :
//      IE 5.0
//      Netscape 6.2
// Version : 
//      v.1.0 MAP
// Knowing bugs :
//      
function selectAllOptionsSelectBox(sBox)
{
    var i;
    
    if (!sBox.disabled)
    {
        for (i = 0; i < sBox.length; i++)
        {
            sBox.options[i].selected = true;
        }
    }
}

/* invertSelectionSelectBox ***************************************************/
//
// Invert the selection in a given select box.
//
// Parameters : 
//      sBox : the select box source
// Return :
//      nothing
// Testing on :
//      IE 5.0
//      Netscape 6.2
// Version : 
//      v.1.0 MAP
// Knowing bugs :
//      
function invertSelectionSelectBox(sBox)
{
    var i;
    
    if (!sBox.disabled) {    
        for (i = 0; i < sBox.length; i++)
            sBox.options[i].selected = !sBox.options[i].selected;
    }
}

/* unselectAllSelectBox ***************************************************/
//
// Unselect all options in a given select box.
//
// Parameters : 
//      sBox : the select box source
// Return :
//      nothing
// Testing on :
//      IE 5.0
//      Netscape 6.2
// Version : 
//      v.1.0 MAP
// Knowing bugs :
//      
function unselectAllSelectBox(sBox)
{
    var i;
    
    if (!sBox.disabled) {    
        for (i = 0; i < sBox.length; i++)
            sBox.options[i].selected = false;
    }
}