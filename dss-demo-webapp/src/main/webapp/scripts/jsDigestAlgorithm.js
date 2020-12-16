
var defaultCheckedDigestAlgorithm = $('input[name=digestAlgorithm]:checked');

function getDigestAlgorithms(value) {
    $.ajax({
        type : "GET",
        url : "data/digestAlgosByForm?form=" + value,
        dataType : "json",
        error : function(msg) {
            alert("Error !: " + msg);
        },
        success : function(data) {
            $('input[name="digestAlgorithm"]').each(function() {
                if ($.inArray($(this)[0].value, data) == -1) {
                    if ($(this).is(':checked')) {
                        defaultCheckedDigestAlgorithm.prop("checked", true);
                    }
                    $(this).attr("disabled", true);
                } else {
                    $(this).attr("disabled", false);
                }
            });
        }
    });
}