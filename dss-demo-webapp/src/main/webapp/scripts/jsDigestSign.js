$('[data-toggle="tooltip"]').tooltip();

var $spinner = $('#spinner');

disableContentTstField(true);

async function digestMessage(message) {
	const digestElement = document.getElementById('digestToSign');
	const inputRadio = document.querySelector('input[name="digestAlgorithm"]:checked');
	var digestAlgo = "SHA-" + inputRadio.value.substring(3);
	const digestValue = await crypto.subtle.digest(digestAlgo, message);           // hash the message
	digestElement.value = arrayBufferToBase64(digestValue);
	
	$spinner.removeClass('d-block');
    $spinner.addClass('d-none');
}

async function readFile(e){
	var reader = new FileReader();
	reader.onload = function(event) {
	    var contents = event.target.result;
		digestMessage(contents);
	};

	reader.onerror = function(event) {
	    console.error("File could not be read! Code " + event.target.error.code);
	};

	if(e.files.length > 0){
        $spinner.removeClass('d-none');
        $spinner.addClass('d-block');
        
		var currentFile = e.files[0];
		reader.readAsArrayBuffer(currentFile);
		documentName.value = currentFile.name;
	}
}

$('input[type=radio][name=digestAlgorithm]').change(function() {
	readFile(document.getElementById('fileToCompute'));
});

function arrayBufferToBase64(buffer) {
    var binary = '';
    var bytes = new Uint8Array(buffer);
    var len = bytes.byteLength;
    for (var i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
}

$('input[name="signatureForm"]:radio').change(
    function() {
    	checkContentTstPossible(this.value);
    });


function checkContentTstPossible(signatureForm) {
     switch(signatureForm) {
          case "XAdES":
          case "JAdES":
            disableContentTstField(true);
            break;
          default:
            disableContentTstField(false);
            break;
     }
}

function disableContentTstField(disable) {
    var $addContentTimestamp = $('input[name="addContentTimestamp"][type="checkbox"]');
    if (disable) {
        $addContentTimestamp.prop('checked', false);
        $addContentTimestamp.attr("disabled", true);
    } else {
        $addContentTimestamp.attr("disabled", false);
    }
}