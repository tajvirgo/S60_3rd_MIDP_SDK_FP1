if (window.top.developerMode == undefined) {
    window.top.developerMode = false;
}
if (window.top.developerMode) {
    document.getElementById("developer-style").disabled = true;
}

function switchStylesheetHandler(event) { 
    window.top.developerMode = !(window.top.developerMode)
    document.getElementById("developer-style").disabled = window.top.developerMode;
    var b = getTargetNode(event);
    if (b) {
        b.firstChild.data = (window.top.developerMode ? "hide" : "show") + " advanced";
    }
}

function init() {
    var h = document.getElementById("header-links");
    if (h) {
        var b = document.createElement("button");
        b.appendChild(document.createTextNode((window.top.developerMode ? "hide" : "show") + " advanced"));
    	attachEventListener(b, "click", switchStylesheetHandler);
        h.insertBefore(document.createTextNode(" "), h.firstChild);
        h.insertBefore(b, h.firstChild);
    }
}