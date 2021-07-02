
$('input[name="signaturePackaging"]:radio').attr("disabled", true);

$('input[name="containerType"]:radio').filter('[value="none"]').attr('checked', true);

$('#selectSignatureLevel').empty();

function isAsicContainer(asicValue) {
	return 'none' != asicValue;
}

function updateSignatureForm(asicValue) {
    $('input[name="signatureForm"]:radio').attr("disabled", true);

    $('#selectSignatureLevel').empty();

    if ('none' == asicValue) {
        $("#formCAdES").attr("disabled", false);
        $("#formPAdES").attr("disabled", false);
        $("#formXAdES").attr("disabled", false);
        $("#formJAdES").attr("disabled", false);

    } else {
        $("#formCAdES").attr("disabled", false);
        $("#formXAdES").attr("disabled", false);
    }

    $('input[name="signatureForm"]:radio').each(function() {
        if ($(this).attr('disabled')) {
            $(this).prop("checked", false);
        }
    });
}

function updateSignaturePackaging(asicValue, signatureForm) {
    if (isAsicContainer(asicValue)) {
        $('input[name="signaturePackaging"]:radio').prop("checked", false);
        $('input[name="signaturePackaging"]:radio').attr("disabled", true);

        $("#signaturePackaging-DETACHED").attr("disabled", false);
        $("#signaturePackaging-DETACHED").prop("checked", true);
    } else {
        var currentSignaturePackaging = $('input[name="signaturePackaging"]:checked');
        $('input[name="signaturePackaging"]:radio').attr("disabled", true).prop("checked", false);
        $.ajax({
            type : "GET",
            url : "data/packagingsByForm?form=" + signatureForm,
            dataType : "json",
            error : function(msg) {
                alert("Error !: " + msg);
            },
            success : function(data) {
                if (data.length == 1) {
                    $.each(data, function(idx) {
                        $('#signaturePackaging-' + data[idx]).attr("disabled", false);
                        $('#signaturePackaging-' + data[idx]).prop("checked", true);
                    });
                } else {
                    $.each(data, function(idx) {
                        var sigPackaging = $('#signaturePackaging-' + data[idx]);
                        $('#signaturePackaging-' + data[idx]).attr("disabled", false);
                        if (currentSignaturePackaging != null && currentSignaturePackaging.val() == sigPackaging.val()) {
                            $('#signaturePackaging-' + data[idx]).prop("checked", true);
                        }
                    });
                }
            }
        });
    }
}

function updateSignatureLevel(signatureForm) {
    $('#selectSignatureLevel').empty();

    var process = $('#process').val();

    $.ajax({
        type : "GET",
        url : "data/levelsByForm?form=" + signatureForm + "&process=" + process,
        dataType : "json",
        error : function(msg) {
            alert("Error !: " + msg);
        },
        success : function(data) {
            $.each(data, function(idx) {
                $('#selectSignatureLevel').append($('<option>', {
                    value: data[idx],
                    text: data[idx].replace(/_/g, "-")
                }));
            });
        }
    });
}

$('input[name="containerType"]:radio').change(
    function() {

        updateSignatureForm(this.value);
        var checkedSignatureForm = $('input[name="signatureForm"]:checked');
        if (checkedSignatureForm.length) {
            updateSignaturePackaging(this.value, checkedSignatureForm.val())
            updateSignatureLevel(checkedSignatureForm.val());
        } else {
            $('#selectSignatureLevel').empty();
        }

    });

$('input[name="signatureForm"]:radio').change(
    function() {

        var checkedContainerType = $('input[name="containerType"]:checked');
        if (checkedContainerType.length) {
            var asicValue = checkedContainerType.val();
            updateSignaturePackaging(asicValue, this.value)
        }
        updateSignatureLevel(this.value);
        getDigestAlgorithms(this.value);

    });

$(document).ready(function() {
    var checkedContainerType = $('input[name="containerType"]:checked');
    var checkedSignatureForm = $('input[name="signatureForm"]:checked');
    if (checkedContainerType.length && checkedSignatureForm.length) {
        updateSignaturePackaging(checkedContainerType.val(), checkedSignatureForm.val())
    }
    if (checkedSignatureForm.length) {
        updateSignatureLevel(checkedSignatureForm.val());
    }
});