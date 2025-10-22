
$('#selectSignatureLevel').empty();

loadSignatureLevels($('input[name=jwsSerializationType]:checked').val());

disableBase64UrlEncodedEtsiU(true);

$('input[name="jwsSerializationType"]:radio').change(
    function() {
    	loadSignatureLevels(this.value);
    });

$('#selectSignatureLevel').change(
    function() {
        loadBase64UrlEncodedEtsiU(this.value);
    });

function loadSignatureLevels(serializationType) {
	$('#selectSignatureLevel').empty();

    $.ajax({
        type : "GET",
        url : "data/levelsBySerialization?serializationType=" + serializationType,
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
                loadBase64UrlEncodedEtsiU($('#selectSignatureLevel').val());
            });
        }
    });
}

function loadBase64UrlEncodedEtsiU(signatureLevel) {
    switch(signatureLevel) {
      case "JAdES_BASELINE_B":
      case "JAdES_BASELINE_LTA":
        disableBase64UrlEncodedEtsiU(true);
        break;
      case "JAdES_BASELINE_T":
      case "JAdES_BASELINE_LT":
        disableBase64UrlEncodedEtsiU(false);
        break;
      default:
        break;
    }
}

function disableBase64UrlEncodedEtsiU(disable) {
    var $base64UrlEncodedEtsiU = $('input[name="base64UrlEncodedEtsiU"][type="checkbox"]');
    var $base64UrlEncodedEtsiUHidden = $('input[name="base64UrlEncodedEtsiU"][type="hidden"]');
    if (disable) {
        $base64UrlEncodedEtsiU.prop('checked', true);
        $base64UrlEncodedEtsiU.attr("disabled", true);
        $base64UrlEncodedEtsiUHidden.attr("disabled", false);
    } else {
        $base64UrlEncodedEtsiU.attr("disabled", false);
        $base64UrlEncodedEtsiUHidden.attr("disabled", true);
    }
}