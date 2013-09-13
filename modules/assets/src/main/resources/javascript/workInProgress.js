function workInProgress(waitingMessage) {
    if (window.parent.waitingMask) {
        window.parent.waitingMask(waitingMessage);
    } else {
        $.blockUI({ css: {
            border: 'none',
            padding: '15px',
            backgroundColor: '#000',
            '-webkit-border-radius': '10px',
            '-moz-border-radius': '10px',
            opacity: .5,
            color: '#fff'
        }, message: waitingMessage });
    }
}
