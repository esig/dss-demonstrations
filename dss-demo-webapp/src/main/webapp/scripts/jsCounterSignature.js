var $spinner = $('#spinner');

disable(true);

$('input[type=file][name=documentToCounterSign]').change(function() {

	$spinner.removeClass('d-none');
	$spinner.addClass('d-block');
	
	$('.text-danger').remove();
	$('#selectSignatureId').empty();
	$('#selectSignatureLevel').empty();
	$('#signatureForm').empty();
	
	var token = $('meta[name=_csrf]').attr('content');
	var header = $('meta[name=_csrf_header]').attr('content');

	var formData = new FormData();
	var files = $('#documentToCounterSign')[0].files;	
	formData.append('documentToCounterSign', files[0], files[0].name)

	$.ajax({
        url : "counter-sign/signatureIds",
        type: 'POST',
        data: formData,
        beforeSend: function(xhr) {
            xhr.setRequestHeader(header, token);
        },
        success: function (data) {
        	var signatureIds = data.signatureIds;
        	$.each(signatureIds, function(idx) {
                $('#selectSignatureId').append($('<option>', {
                    value: signatureIds[idx],
                    text: signatureIds[idx]
                }));
            });
        	
        	var signatureForm = data.signatureForm;
        	$('#signatureForm').val(signatureForm);
        	
        	var signatureLevels = data.signatureLevels;
        	$.each(signatureLevels, function(idx) {
                $('#selectSignatureLevel').append($('<option>', {
                    value: signatureLevels[idx],
                    text: signatureLevels[idx].replace(/_/g, "-")
                }));
            });
        	
            getDigestAlgorithms(signatureForm);

        	disable(false);
        },
        error : function(msg) {
        	disable(true);
        	$('<p class="text-danger">' + msg.responseText + '</p>').appendTo('#documentToCounterSignError');
        },
        contentType: false,
        processData: false
	});
	
});


function disable(disable) {
	$('select[name="signatureIdToCounterSign"]').attr("disabled", disable);
	$('select[name="signatureLevel"]').attr("disabled", disable);
	$('input[name="digestAlgorithm"]').attr("disabled", disable);
	$('input[name="signWithExpiredCertificate"]').attr("disabled", disable);
	$('input[name="detachedOriginalFiles"]').attr("disabled", disable);
	$('input[name="digestToSend"]').attr("disabled", disable);
	
	$spinner.removeClass('d-block');
	$spinner.addClass('d-none');
}