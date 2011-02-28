[condition][]A search result hit is present=searchHit : JCRNodeHit ( )
[condition][]- the node is of type {type}=type == "{type}"
[consequence][]Append URL query-parameter "{parameterName}" with {parameterValue}=urlService.addURLQueryParameter(searchHit, "{parameterName}", {parameterValue});