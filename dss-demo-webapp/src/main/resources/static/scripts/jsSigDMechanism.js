$('[data-toggle="tooltip"]').tooltip();
function isDetached() {
	var value = $('input[name=signaturePackaging]:checked').val();
	return 'DETACHED' == value;
}

function updateSigDMechanism(sigPackaging) {
	$('input[name="sigDMechanism"]:radio').attr("disabled", true);
	if ('DETACHED' == sigPackaging) {
		$('input[name="sigDMechanism"]:radio').filter('[value="OBJECT_ID_BY_URI"]').attr("disabled", false);
		$('input[name="sigDMechanism"]:radio').filter('[value="OBJECT_ID_BY_URI_HASH"]').attr("disabled", false);
	}
}

$('input[name="signaturePackaging"]:radio').change(
        function() {
        	$('input[name="sigDMechanism"]:radio').prop("checked", false);
    	    updateSigDMechanism(this.value);
        });

$(document).ready(function() {
    var checkedPackaging = $('input[name="signaturePackaging"]:checked');
    if (checkedPackaging.length) {
    	updateSigDMechanism(checkedPackaging.val());
    } else {
        $('input[name="sigDMechanism"]:radio').attr("disabled", true);
        $('input[name="sigDMechanism"]:radio').prop("checked", false);
    }
});