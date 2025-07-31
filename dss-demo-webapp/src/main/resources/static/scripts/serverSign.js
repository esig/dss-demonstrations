// Developer note.
// We re-use the NexU-like signing process to be able to support both NexU and server-signing.

var serverSignUrl = document.getElementById("signingProcessScript").getAttribute("data-server-sign-url");

function nexu_get_certificates(success_callback, error_callback) {
	transmitRequest("certificates", {}, success_callback, error_callback);
}

/* function to use if we already know a certificate and its tokenId/keyId */
function nexu_sign_with_token_infos(tokenId, keyId, dataToSign, digestAlgo, success_callback, error_callback) {
	var data = '{ "tokenId":{"id":"' + tokenId + '"}, "keyId":"' + keyId + '", "toBeSigned": { "bytes": "' + dataToSign + '" } , "digestAlgorithm":"' + digestAlgo + '"}';
	transmitRequest("sign", data, success_callback, error_callback);
}

/* function to use without tokenId/keyId */
function nexu_sign(dataToSign, digestAlgo, success_callback, error_callback) {
	var data = { dataToSign:dataToSign, digestAlgo:digestAlgo };
	transmitRequest("sign", data, success_callback, error_callback);
}

function nexu_get_identity_info(success_callback, error_callback) {
	transmitRequest("identityInfo", {}, success_callback, error_callback);
}

function transmitRequest(service, data, success_callback, error_callback) {
	callUrl(serverSignUrl + "/" + service, "POST", data, success_callback, error_callback);
}

function callUrl(url, type, data, success_callback, error_callback) {
	$.ajax({
		  type: type,
		  url: url,
		  data: data,
		  crossDomain: true,
		  contentType: "application/json; charset=utf-8",
		  dataType: "json",
		  success: function (result) {
			  console.log(url + " : OK");
			  success_callback.call(this, result);
		  }
		}).fail(function (error) {
			console.log(url + " : KO");
			error_callback.call(this, error);
		});
}
