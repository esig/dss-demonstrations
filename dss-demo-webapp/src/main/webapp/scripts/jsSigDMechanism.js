$('[data-toggle="tooltip"]').tooltip();

$('input[name="sigDMechanism"]:radio').attr("disabled", true);

function isDetached() {
	var value = $('input[name=signaturePackaging]:checked').val();
	return 'DETACHED' == value;
}

$('input[name="signaturePackaging"]:radio').change(
        function() {
        	$('input[name="sigDMechanism"]:radio').prop("checked", false);

        	var packaging = this.value;
        	if ('DETACHED' == packaging) {
        		$('input[name="sigDMechanism"]:radio').filter('[value="OBJECT_ID_BY_URI"]').attr("disabled", false);
        		$('input[name="sigDMechanism"]:radio').filter('[value="OBJECT_ID_BY_URI_HASH"]').attr("disabled", false);
        	} else {
        		$('input[name="sigDMechanism"]:radio').attr("disabled", true);
        	}
        });
