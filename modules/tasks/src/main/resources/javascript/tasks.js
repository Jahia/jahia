function completeTask(checkbox) {
    if (checkbox.attr("checked")) {
        checkbox.attr("disabled", "disabled");
        $.post(checkbox.attr('taskPath'), {"jcrMethodToCall":"put","state":"finished"}, function() {
        }, "json");
    }
};
