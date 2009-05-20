
function disableCheckBox(checkboxList)
{
    if (checkboxList.length > 0) {
        for (i = 0; i < checkboxList.length; i++) {
            checkboxList[i].disabled = true;
        }
    } else {
        checkboxList.disabled = true;
    }
}

function enableCheckbox(checkboxList)
{
    if (checkboxList.length > 0) {
        for (i = 0; i < checkboxList.length; i++) {
            checkboxList[i].disabled = false;
        }
    } else {
        checkboxList.disabled = false;
    }
}

function reverseDisabledCheckBox(checkboxList)
{
    if (checkboxList.length > 0) {
        for (i = 0; i < checkboxList.length; i++) {
            checkboxList[i].disabled = !checkboxList[i].disabled;
        }
    } else {
        checkboxList.disabled = !checkboxList.disabled;
    }
}
