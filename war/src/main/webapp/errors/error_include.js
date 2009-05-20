
    function EnginePopup(contextPath, engineName )
    {
        var oldurl  = location.href;
        var stringA = "engineName/";
        var posA = oldurl.indexOf( stringA );
        if (posA != -1) {
            posA = posA + stringA.length;
            var posB = oldurl.indexOf( "/", posA );
            if (posB != -1) {
                oldurl = oldurl.substring( 0, posA ) + engineName + oldurl.substring( posB, oldurl.length );
            } else {
                posB = oldurl.indexOf( "?", posA );
                if (posB != -1) {
                    oldurl = oldurl.substring( 0, posA ) + engineName;
                } else {
                    oldurl = oldurl.substring( 0, posA ) + engineName;
                }
            }
        } else {
            var posB = oldurl.indexOf( "?" );
            //added by PAP to check if there is a request path segment parameter
            var posC = oldurl.indexOf( ";" );
            if (posB == -1 || (posC < posB && posC != -1)) {
                posB = posC;
            }
            if (posB != -1) {
                oldurl = oldurl.substring( 0, posB ) +"/engineName/" + engineName + oldurl.substring( posB, oldurl.length );
            } else {
            	if (oldurl.indexOf( contextPath ) != -1) {
                oldurl = oldurl + "/engineName/" + engineName;
            	} else {
            		oldurl = oldurl + contextPath + "/engineName/" + engineName;
            	}
            }
        }

        if (oldurl != location.href)
        {
            var params = "width=" + 450 + ",height=" + 500 + ",resizable=1,scrollbars=0,status=0";
            var myWin = window.open( oldurl, engineName , params );
        }
    }