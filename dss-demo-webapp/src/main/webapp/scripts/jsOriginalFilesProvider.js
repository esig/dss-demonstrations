$('[data-toggle="tooltip"]').tooltip();

$('input[name="defaultPolicy"]:radio').change(function() {
	$('#policyFile').attr("disabled", this.value == 'true');
});


$("#validation-form").submit(function(event) {	
	var originalFilesField = $('#detachedOriginalFiles');
	var originalFiles = originalFilesField[0].files;
	
	var linkedFiles = $('#linked-files');
	linkedFiles.empty();
	
	if (originalFiles.length > 0){
		// prevent default in order to wait until file will be read and base64/digest computed
		event.preventDefault();
		
		var digestAlgorithm = $('input[name=digestToSend]:checked', '#validation-form');
		
		if (digestAlgorithm.val() == '') {
			linkCompleteFiles(originalFiles, linkedFiles, this);
		} else {
			linkDigestFiles(originalFiles, digestAlgorithm, linkedFiles, this);
		}
	}
});

async function linkCompleteFiles(originalFiles, linkedFiles, form) {
	for (var i = 0; i < originalFiles.length; i++) {
		var currentFile = originalFiles[i];
		addFilename(linkedFiles, currentFile, i);
		
		var promise = getBase64(currentFile);
		var base64 = await promise;
		addBase64Content(linkedFiles, base64, i);
	}
	
	form.submit();
}

async function linkDigestFiles(originalFiles, digestAlgorithm, linkedFiles, form) {
	for (var i = 0; i < originalFiles.length; i++) {
		var currentFile = originalFiles[i];
		addFilename(linkedFiles, currentFile, i);
		addDigestAlgorithm(linkedFiles, digestAlgorithm.val(), i);
		
		var algoJavaName = digestAlgorithm.data('javaname');
		var base64Digest = await getDigest(currentFile, algoJavaName);
		addDigestValue(linkedFiles, base64Digest, i);
	}
	
	form.submit();
}

function addFilename(linkedFiles, currentFile, i) {
	var line = "<input type=\"hidden\" id=\"originalFiles" + i + ".filename\" " +
			"name=\"originalFiles[" + i + "].filename\" value=\"" + currentFile.name + "\" />";
	linkedFiles.append(line);
}

function addBase64Content(linkedFiles, base64, i) {
	var line = "<input type=\"hidden\" id=\"originalFiles" + i + ".base64Complete\" " +
			"name=\"originalFiles[" + i + "].base64Complete\" value=\"" + base64 + "\" />";
	linkedFiles.append(line);
}

async function getBase64(file) {
	var arrayBuffer = await readFile(file);
	return arrayBufferToBase64(arrayBuffer);
}

function readFile(file) {
	return new Promise(function(resolve, reject) {
		var reader = new FileReader();
		reader.onloadend = function () {
			var result = reader.result;
			resolve(result);
		};
		reader.onerror = function (event) {
			console.error("File could not be read! Code " + event.target.error.code);
			reject;
		};
		reader.readAsArrayBuffer(file);
	});
}

function addDigestAlgorithm(linkedFiles, digestAlgorithm, i) {
	var line = "<input type=\"hidden\" id=\"originalFiles" + i + ".digestAlgorithm\" " +
			"name=\"originalFiles[" + i + "].digestAlgorithm\" value=\"" + digestAlgorithm + "\" />";
	linkedFiles.append(line);
}

function addDigestValue(linkedFiles, base64Digest, i) {
	var line = "<input type=\"hidden\" id=\"originalFiles" + i + ".base64Digest\" " +
		"name=\"originalFiles[" + i + "].base64Digest\" value=\"" + base64Digest + "\" />";
	linkedFiles.append(line);
}

async function getDigest(file, digestAlgorithm) {
	var contents = await readFile(file);
	const digestValue = await crypto.subtle.digest(digestAlgorithm, contents); // hash the message
	var digestValueBase64 = arrayBufferToBase64(digestValue);
	return digestValueBase64;
}

function arrayBufferToBase64(buffer) {
    var binary = '';
    var bytes = new Uint8Array(buffer);
    var len = bytes.byteLength;
    for (var i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
    }
    return window.btoa(binary);
}