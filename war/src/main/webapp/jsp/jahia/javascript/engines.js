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
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

if (!jahia || !jahia.config) {
    var jahia = new Object();
    jahia.config = new Object();
    jahia.onLoadHandlers = new Array();
}
function check() {
    // override this function if needed in subengine to perform form data check
    // before submit !!!
    return true;
}

function saveContent() {
    // used by Html editors
    // override this for preprocessing before for submission
    if (typeof workInProgressOverlay != 'undefined') workInProgressOverlay.launch();
}

var submittedCount = 0;

function teleportCaptainFlam(what) {
    if (submittedCount == 0) {
        submittedCount++;
        if (! what) {
            document.mainForm.submit();
        } else {
            what.submit();
        }
    }
}

function handleLanguageChange(lang) {
    document.mainForm.screen.value = jahia.config.theScreen;
    document.mainForm.engine_lang.value = lang;
    if (check()) {
        saveContent();
        teleportCaptainFlam(document.mainForm);
    }
}

function handleActionChange(what) {
    submittedCount++;
    saveContent();
    document.mainForm.screen.value = what;
    document.mainForm.submit();
}

function sendFormSave() {
    if (check() && submittedCount == 0) {
        var button = document.getElementById("saveButton");
        if (button) button.innerHTML = "<div class='clicked'>" + jahia.config.i18n['org.jahia.button.ok'] + "</div>";
        delete button;
        document.mainForm.screen.value = "save";
        saveContent();
        teleportCaptainFlam(document.mainForm);
    }
}

function sendFormSaveAndAddNew() {
    if (check() && submittedCount == 0) {
        var button = document.getElementById("saveButtonAdd");
        if (button) button.innerHTML = "<div class='clicked'>" + jahia.config.i18n['org.jahia.button.saveAddNew'] + "</div>";
        delete button;
        document.mainForm.screen.value = "save";
        document.mainForm.addnew.value = "true";
        saveContent();
        teleportCaptainFlam(document.mainForm);
    }
}

function sendFormApply() {
    if (check() && submittedCount == 0) {
        if (jahia.config.lockResults) {
            var button = document.getElementById("applyButton");
            if (button) button.innerHTML = "<div class='clicked'>" + jahia.config.i18n['org.jahia.button.apply'] + "</div>";
            delete button;
        }
        document.mainForm.screen.value = "apply";
        saveContent();
        teleportCaptainFlam(document.mainForm);
    }
}

function sendFormSteal() {
    if (check() && submittedCount == 0) {
        document.mainForm.screen.value = "apply";
        document.mainForm.whichKeyToSteal.value = jahia.config.lockKey;
        saveContent();
        teleportCaptainFlam(document.mainForm);
    }
}

function sendFormCancel() {
    if (submittedCount == 0) {
        var src = jahia.config.jspSource;
        if (src == "lock") {
            CloseJahiaWindow();
            return;
        }
        document.mainForm.screen.value = "cancel";
        teleportCaptainFlam(document.mainForm);
    }
}

function changeField(fieldID) {
    submittedCount++;
    document.mainForm.screen.value = jahia.config.theScreen;
    document.mainForm.editfid.value = fieldID;
    if (check()) {
        saveContent();
        document.mainForm.submit();
    }
}

function setWaitingCursor(showWaitingImage) {
    if (typeof workInProgressOverlay != 'undefined') {
        workInProgressOverlay.launch();
    }
}

var checkParentTimeOut = 500;
function checkParent() {
    if (!window.opener ||
        (!(window.opener.lastLoginTime == null) && window.opener.lastLoginTime == "0"))
        window.close();
    if (checkParentTimeOut > 0) {
        setTimeout("checkParent()", checkParentTimeOut);
    }
}

function sendKeepAliveSuccess() {
    if (jahia.config.sendKeepAliveTimeOut > 0) {
        setTimeout("sendKeepAlive()", jahia.config.sendKeepAliveTimeOut);
    }
}

function sendKeepAlive() {
	jahia.request(jahia.config.contextPath + '/jsp/keepAlive.jsp', {onSuccess: sendKeepAliveSuccess});
}

function engineCustomHandleOnLoad() {
    // should be overridden by sub engine for specific onload code
}

function closeTheWindow() {
    try {
        if (! jahia) return;
        var last = jahia.config.theScreen;
        var src = jahia.config.jspSource;

        if (src == "apply") return;
        if (submittedCount == 0) {
            if (last != "save" && last != "cancel" && last != "showReport" && src != "apply" &&
                src != "close" && src != "lock") {
                releaseLock(jahia.config.lockType, jahia.config.pid, jahia.config.needToRefreshParentPage);
            }
            if (src == "delete_container") return;
            if (src == "close") {
                // Do refresh opener by setting argument to "yes"
                CloseJahiaWindow("yes");
            } else {
                CloseJahiaWindow();
            }

            removeAll();
        }
    } catch(ex) {
        window.close();
    }
}

// resize pageBody
function resizeEnginePagebody() {

    if (jahia.config.skipBodyResize) {
        return;
    }

    var myWidth = 0, myHeight = 0;
    if (typeof( window.innerWidth ) == 'number') {
        //Non-IE
        myWidth = window.innerWidth;
        myHeight = window.innerHeight;
    } else if (document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight )) {
        //IE 6+ in 'standards compliant mode'
        myWidth = document.documentElement.clientWidth;
        myHeight = document.documentElement.clientHeight;
    } else if (document.body && ( document.body.clientWidth || document.body.clientHeight )) {
        //IE 4 compatible
        myWidth = document.body.clientWidth;
        myHeight = document.body.clientHeight;
    }

    // resize
    var pageBodyEle = document.getElementById('pagebody' + (jahia.config.lockResults ? 'ReadOnly' : ''));
    if (pageBodyEle) {
        var pageBodyEleHeight = pageBodyEle.offsetHeight;
        var newHeight = (myHeight - 130);
        if (newHeight > pageBodyEleHeight) {
            pageBodyEle.style.height = (myHeight - 130) + "px";
        }
    }

    // unreference var
    myWidth = null;
    myHeight = null;
    pageBodyEle = null;
}

function handleOnLoad() {
    if (checkParentTimeOut > 0) {
        setTimeout("checkParent()", checkParentTimeOut);
    }
    if (jahia.config.sendKeepAliveTimeOut > 0) {
        setTimeout("sendKeepAlive()", jahia.config.sendKeepAliveTimeOut);
    }
    engineCustomHandleOnLoad();
    resizeEnginePagebody();
}

function releaseLock(lockType, pid, refreshParentWindow) {
    if (!lockType) return;

    if(/Safari/.test(navigator.userAgent)) {
        req = new XMLHttpRequest();
    	req.open("GET", jahia.config.contextPath + '/ajaxaction/ReleaseLock?locktype=' + lockType + "&params=%2Fop%2Fedit%2Fpid%2F" + pid, false);
    	req.send("");
    } else {
    	jahia.releaseLock(lockType);    	
    }

    if (refreshParentWindow) {
        window.opener.location.reload(true);
    }
}

jahia.onLoadHandlers.push(handleOnLoad);