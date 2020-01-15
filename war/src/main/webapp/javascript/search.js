function searchDateTypeToggle(dateType, range) {
    if (dateType.value == 'range' && range.style.display != 'none' || dateType.value != 'range' && range.style.display == 'none') {
        return;
    }
    range.style.display = dateType.value == 'range' ? '' : 'none';
    for (var i = 0; i < range.childNodes.length; i++) {
        if (range.childNodes[i].nodeName.toLowerCase() == 'input') {
            range.childNodes[i].disabled = dateType.value != 'range';
        }
    }
}

function searchOrderByToggle(orderBy, operandElement) {
    if (orderBy.value == 'score') {
        operandElement.value = 'score'
    } else {
        operandElement.value = 'property'
    }
}
