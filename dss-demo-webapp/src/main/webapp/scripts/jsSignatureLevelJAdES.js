
$('#selectSignatureLevel').empty();

loadSignatureLevels($('input[name=jwsSerializationType]:checked').val());

$('input[name="jwsSerializationType"]:radio').change(
    function() {
    	loadSignatureLevels(this.value);
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
            });
        }
    });
}
