/*
 * © Nowina Solutions, 2015-2017
 *
 * Concédée sous licence EUPL, version 1.1 ou – dès leur approbation par la Commission européenne - versions ultérieures de l’EUPL (la «Licence»).
 * Vous ne pouvez utiliser la présente œuvre que conformément à la Licence.
 * Vous pouvez obtenir une copie de la Licence à l’adresse suivante:
 *
 * http://ec.europa.eu/idabc/eupl5
 *
 * Sauf obligation légale ou contractuelle écrite, le logiciel distribué sous la Licence est distribué «en l’état»,
 * SANS GARANTIES OU CONDITIONS QUELLES QU’ELLES SOIENT, expresses ou implicites.
 * Consultez la Licence pour les autorisations et les restrictions linguistiques spécifiques relevant de la Licence.
 */

var nexuVersion = "${nexuVersion}";

// IE
if (!String.prototype.startsWith) {
  String.prototype.startsWith = function(searchString, position){
    return this.substr(position || 0, searchString.length) === searchString;
  };
}

$.get("${nexuUrl}/nexu-info", function(data) {
	// something responded
	if(data.version.startsWith(nexuVersion)) {
		// valid version
		// load nexu script 
		console.log("Loading script...");

		var onSuccess = function() {
            $("#nexu_ready_alert").slideDown();
            $("#submit-button").prop('disabled', false);
        };
		loadScript(onSuccess);

	} else {
		// need update
		$("#submit-button").html("Update NexU");
		$("#submit-button").on("click", function() {
			console.log("Update NexU");
			return false;
		});
		
	}
}).fail(function() {
	// no response, NexU not installed or not started
	$("#submit-button").html("Install NexU");
	$("#submit-button").on("click", function() {
		console.log("Install NexU");
		window.location = "${nexuDownloadUrl}";
		return false;
	});
	
    $("#warning-text").html("NexU not detected or not started !");
    $("#nexu_missing_alert").slideDown();
});

function loadScript(onSuccess) {
	var xhrObj = new XMLHttpRequest();
	xhrObj.open('GET', "${nexuUrl}/nexu.js");

    xhrObj.onload = function() {
      if (xhrObj.status != 200) {
	    console.log("Unable to load Nexu : " + xhrObj.status + ": " + xhrObj.statusText);
      } else { // show the result
        var se = document.createElement('script');
        se.type = "text/javascript";
        se.text = xhrObj.responseText;
        document.getElementsByTagName('head')[0].appendChild(se);
        console.log("Nexuscript loaded");
	    onSuccess();
      }
    };

	xhrObj.send();
}
