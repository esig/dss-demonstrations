$('[data-toggle="tooltip"]').tooltip();

$('input[name="defaultPolicy"]:radio').change(function() {
	$('#policyFile').attr("disabled", this.value == 'true');
});