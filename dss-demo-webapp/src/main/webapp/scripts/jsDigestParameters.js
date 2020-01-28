$('[data-toggle="tooltip"]').tooltip();

var spinner = document.getElementById("spinner");

async function digestMessage(message) {
	const digestElement = document.getElementById('digestToSign');
	const inputRadio = document.querySelector('input[name="digestAlgorithm"]:checked');
	var digestAlgo = "SHA-" + inputRadio.value.substring(3);
	const digestValue = await crypto.subtle.digest(digestAlgo, message);           // hash the message
	digestElement.value = arrayBufferToBase64(digestValue);
	spinner.style.visibility = "hidden";
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
		spinner.style.visibility = "visible";
		var currentFile = e.files[0];
		reader.readAsArrayBuffer(currentFile);
		const documentNameElement = document.getElementById('documentName');
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