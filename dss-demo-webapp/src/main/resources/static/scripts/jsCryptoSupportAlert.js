$(document).ready(function() {
	if (crypto.subtle == null) {
		
		// disable the file input
		var $fileToCompute = $('#fileToCompute');
		if ($fileToCompute.length) {
			$fileToCompute.prop("disabled", true);
			$fileToCompute.removeClass("cursor-pointer");
			$fileToCompute.addClass("cursor-not-allowed");
			$('#filename').addClass('text-muted');
		}
		
		// disable algos
		var $digestAlgoToSend = $('#digestAlgoToSend');
		if ($digestAlgoToSend.length) {
			var $digestAlgoInputs = $digestAlgoToSend.find('.digest-algo-input');
			$digestAlgoInputs.each(function(i) {
				$(this).prop("disabled", true);
			});
		}
		
		// add a warning alert
		$('#alert-digest').slideDown();
	}
});
