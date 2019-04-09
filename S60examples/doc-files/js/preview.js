var clipboardAvailable = (window.clipboardData != null);
var req = null;
var reuseBaseURL = "";
var batchErrors = 0;
var libraryId;
var reuseIds;
var checkoutIds;
var async = true;
var previewItems = new Map(true);

var CONDITION_START = "condition-start"
var CONDITION_END = "condition-end"
var CONDITION_HIGHLIGHT = "-highlight"

var PREVIEW_HEADER_ROW = 2;
var PREVIEW_HEADER_WRAP = 1;
var PREVIEW_HEADER_INSERT = 0;

/**
 * Page initialization.
 */
function initPage() {
    window.status = "Initializing page...";
    // condition flags
	var as = document.getElementsByTagName("span");
	for (var i = 0, n = null; i < as.length; i++) {
    	n = as.item(i);
	    if (isClass(n, CONDITION_START) || isClass(n, CONDITION_END)) {
	    	attachEventListener(n, "mouseover", highlightCondition);
	    	attachEventListener(n, "mouseout", downlightCondition);
		}
	}
	// preview styles
	//addPreviewStylesheets();
	// preview header
	addPreviewHeader();
	// preview blocks
	for (var i = 0, k, e, pis, w, buf; k = previewItems.keys[i]; i++) {
	    e = document.getElementById(k);
	    if (e != null) {
    	    pis = previewItems.get(k);
    	    buf = new Array();
    	    for (var j = 0, pi; pi = pis[j]; j++) {
    	        buf.push(pi.getButton(e, pi));
    	    }
    	    addPreviewWrapper(e, buf, pis[0].name);
	    }
	}
	window.status = "";
}

/**
 * Create preview page stylesheets.
 *
 * @xxx for future use
 */
function addPreviewStylesheets() {
	if (stylesheetBaseUri == undefined) {
		alert("ERROR: stylesheetBaseUri not defined");
	} else {
		var head = document.getElementsByTagName("head")[0];
		var style = document.createElement("link");
		style.rel = "stylesheet";
		style.type = "text/css";
		style.href = stylesheetBaseUri + "preview-controls.css";
		style.disabled = true;
		style.id = "previewcontrolstyle";
		head.appendChild(style);
	}
}

/**
 * Create preview page controller header.
 */
function addPreviewHeader() {
    var d = document;
    var we = "div";
    var h = d.createElement(we);
    var ht = d.createElement("span");
    h.appendChild(ht);
    addClass(h, "preview-page-block-header");
    h.style.display = "block";
    addClass(ht, "preview-page-block-title");
    ht.appendChild(d.createTextNode(documentInfo.name));
    h.appendChild(d.createTextNode(" "));
    h.appendChild(documentInfo.getButton(h, documentInfo));
    //if (previewItems.size() > 0) {
        h.appendChild(d.createTextNode(" "));
        h.appendChild(createButton(h,
            "Show preview buttons",
            "Toggle preview button visibility",
            previewItems.size() > 0
            ? function(event) {
	                toggleStylesheet('previewbuttonstyle', getTargetNode(event), 'preview buttons');
	                //toggleStylesheet("previewcontrolstyle", getTargetNode(event), 'preview buttons');
	        }
	        : null));
    //}
    //if (hasConditions) {
        h.appendChild(d.createTextNode(" "));
        h.appendChild(createButton(h,
            "Show condition blocks",
            "Toggle condition block visibility",
            hasConditions
            ? function(event) {
                toggleStylesheet('previewconditionstyle', getTargetNode(event), 'condition blocks');
            }
            : null));
    //}
    if (document.body.firstChild) {
        document.body.insertBefore(h, document.body.firstChild);
    } else {
        document.body.appendChild(h);
    }
}

/**
 * Add preview button wrapper.
 */
function addPreviewWrapper(e, buf, title) {
    var d = e.ownerDocument;
    var blockElement;
    var headerElement;
    var wrap;
    var n = e.nodeName.toLowerCase();
    switch (n) {
	case "tr":
		blockElement = "tr";
		headerElement = "th";
		wrap = PREVIEW_HEADER_ROW;
		break;
	case "td":
	case "th":
	case "li":
		blockElement = "div";
		headerElement = "div";
		wrap = PREVIEW_HEADER_INSERT;
		break;
	case "div":
	case "p":
	case "pre":
	case "ol":
	case "ul":
	case "dl":
	case "table":
	case "h1":
	case "h2":
	case "h3":
	case "h4":
	case "h5":
	case "h6":
        blockElement = "div";
        headerElement = "div";
        wrap = PREVIEW_HEADER_WRAP;
        break;
    default:
    	blockElement = "span";
    	headerElement = "span";
    	wrap = PREVIEW_HEADER_WRAP;
    }    
    
    var block = d.createElement(blockElement);
    var header = d.createElement(headerElement);
    var headerTitle = d.createElement("span");
    header.appendChild(headerTitle);
    var content = d.createElement(blockElement);
    addClass(block, "preview-block");
    addClass(header, "preview-block-header");
    addClass(headerTitle, "preview-block-title");
    addClass(content, "preview-block-content");
    headerTitle.appendChild(d.createTextNode(title));
    for (var i = 0; i < buf.length; i++) {
        header.appendChild(d.createTextNode(" "));
        header.appendChild(buf[i]);
    }
    if (wrap == PREVIEW_HEADER_ROW) {
    	if (header.colSpan) {
	    	var colspan = getChildElementsByTagName(e).length;
	    	if (colspan > 1) {
	    		header.colSpan = colspan;
	    	}
    	}
        block.appendChild(header);
        e.parentNode.insertBefore(block, e);
        addClass(e, "preview-block-content");
    } else if (wrap == PREVIEW_HEADER_WRAP) {
		block.appendChild(header);
		block.appendChild(content);
	    var e = e.parentNode.replaceChild(block, e);
	    content.appendChild(e);
    } else { // PREVIEW_HEADER_INSERT
    	addClass(e, "preview-block");
    	wrapChildElements(e, content);
    	if (e.firstChild) {
    		e.insertBefore(header, e.firstChild);
    	} else {
    		e.appendChild(header);
    	}
    }
}

function conditionEvent(event, cls) {
    var n = getTargetNode(event);
    var id = new String(n.id);
    id = id.substring(0, id.lastIndexOf("-"));
    if (id != "") {
    	var start = document.getElementById(id + "-start");
    	var end = document.getElementById(id + "-end");
    	if (start && end) {
        	start.className = CONDITION_START + cls;
        	end.className = CONDITION_END + cls;
    	}
	}
}

function highlightCondition(event) {
    return conditionEvent(event, CONDITION_HIGHLIGHT);
}

function downlightCondition(event) {
    return conditionEvent(event, "");
}

/**
 * Stylesheet toggle.
 */
function toggleStylesheet(s, b, l) {
    window.status = "Toggling view...";
    var elem = document.getElementById(s);
    if (elem == null) {
        window.alert("Failed to find stylesheet '" + s + "'");
    } else {
        elem.disabled = !elem.disabled;
        b.firstChild.data = (elem.disabled ? "Hide" : "Show") + " " + l;
    }
        window.status = "";    
}

function createButton(elem, title, helpText, handler) {
    var n = elem.ownerDocument.createElement("button");
    addClass(n, "preview-block-button");
    n.title = helpText;
    if (handler == null || handler == undefined) {
    	n.disabled = true;
    } else {
    	attachEventListener(n, "click", handler);
    }
    n.appendChild(elem.ownerDocument.createTextNode(title));
    return n;
}

/**
 * Conreffed section preview button.
 *
 * @constructor
 * @param id ID of the element
 * @param name Source element name
 * @param conref Conref URL of the conref source
 */
function ConrefItem(id, name, conref) {
    this.id = id;
    this.name = name;
    this.conref = conref;
}
ConrefItem.prototype.getButton = function(elem, i) {
    var n = elem.ownerDocument.createElement("button");
    addClass(n, "preview-block-button");
    n.title = "Preview reuse source";
    var c = function(event) {
        var s = i.conref;
        window.location = "showHyperlinksApprovedDoc.asp?Card="
            + i.conref.substring(0, i.conref.indexOf("#"))
            + "#"
            + i.conref.substring(i.conref.indexOf("/") + 1);
    }
    attachEventListener(n, "click", c);
    n.appendChild(elem.ownerDocument.createTextNode("Reuse source"));
    return n;
}

/**
 * Conref button preview button.
 *
 * @constructor 
 * @param id ID of the element
 * @param name Source element name
 * @param rootId ID of the source root element
 * @param elementId ID of the source element
 */
function ConrefReuseItem(id, name, rootId, elementId) {
    this.id = id;
    this.name = name;
    this.rootId = rootId;
    this.elementId = elementId;
}
ConrefReuseItem.prototype.getButton = function(elem, i) {
    var n = elem.ownerDocument.createElement("button");
    addClass(n, "preview-block-button");
    n.title = "Copy this " + i.name + " element to clipboard for conref reuse";
    var c = function(event) {
        StartConrefReuse(i.rootId, i.elementId);
    }
    attachEventListener(n, "click", c);
    n.appendChild(elem.ownerDocument.createTextNode("Conref Reuse"));
    return n;
}

/**
 * Reuse button preview button.
 *
 * @constructor 
 * @param id ID of the element
 * @param name Source element name
 * @param rootId ID of the source root element
 * @param elementId ID of the source element
 */
function ReuseItem(id, name, rootId, elementId) {
    this.id = id;
    this.name = name;
    this.rootId = rootId;
    this.elementId = elementId;
}
ReuseItem.prototype.getButton = function(elem, i) {
    var n = elem.ownerDocument.createElement("button");
    addClass(n, "preview-block-button");
    n.title = "Copy this " + i.name + " element to clipboard for reuse";
    var c = function(event) {
        StartReuse(i.rootId, i.elementId);
    }
    attachEventListener(n, "click", c);
    n.appendChild(elem.ownerDocument.createTextNode("Reuse"));
    return n;
}


/**
 * Anchor preview button.
 *
 * @constructor
 * @param id ID of the element
 * @param name Source element name
 * @param url ISH link for the element
 */
function AnchorItem(id, name, url) {
    this.id = id;
    this.name = name;
    this.url = url;
}
AnchorItem.prototype.getButton = function(elem, i) {
    var n = elem.ownerDocument.createElement("button");
    addClass(n, "preview-block-button");
    n.title = "Copy link to this " + i.name + " element to clipboard";
    var c = function(event) {
        if (ToClipboard(i.url)) {
            window.alert("Done. Link copied into the clipboard");
        }
    }
	attachEventListener(n, "click", c);
    n.appendChild(elem.ownerDocument.createTextNode("Get link"));
    return n;
}


/**
 * Condition preview button.
 */
/*
function ConditionItem(id, name, condition) {
    this.id = id;
    this.name = name;
    this.condition = condition;
}
ConditionItem.prototype.getButton = function(elem, i) {
    var n = elem.ownerDocument.createElement("button");
    n.className = "preview-block-button";
    var c = function(event) {
        if (ToClipboard(i.conref)) {
            window.alert("Done. Link copied into the clipboard");
        }
    }
    if (n.addEventListener) {
		n.addEventListener("click", c, false);
	} else if (n.attachEvent) {
		n.onclick = function() { c(event) };
	}
    n.appendChild(elem.ownerDocument.createTextNode("Condition"));
    return n;
}
*/

/**
 * Add preview item for processing.
 */
function addPreviewItem(i) {
    previewItems.put(i.id, i);
}

/**
 * Start reuse on all documents.
 *
 * @param map ID of the library
 * @param library DOM node of the button
 */
/*
function reuseAll() {
    batchErrors = 0;
    var arr = reuseIds;
    for (i = 0; i < arr.length; i++) {
        window.status = "Processing block " + (1 + i) + " of " + arr.length + "...";
        StartReuse(libraryId, arr[i], true);
    }
    window.status = "";
    window.alert("Done. Processed " + arr.length + " blocks, " + batchErrors + " errors.\n\nRefresh preview.");
}
*/

function getArray(arr, count) {
    if (count > arr.length) {
        count = arr.length;
    }
    var a = new Array();
    for (i = 0; i < count; i++) {
        a.push(arr.pop());
    }
    return a;
}

/**
 * Checkout all documents.
 *
 * @param map ID of the library
 * @param library DOM node of the buttong
 */
/*
function checkoutAll() {
    batchErrors = 0;
    var arr = checkoutIds;
    for (i = 0; i < arr.length; i++) {
        window.status = "Processing block " + (1 + i) + " of " + arr.length + "...";
        var button = document.getElementById(arr[i] + "_checkout_button");
        doCheckOut(arr[i], button, true);
    }
    window.status = "";
    window.alert("Done. Processed " + arr.length + " blocks, " + batchErrors + " errors.\n\nRefresh preview.");
}
*/

/**
 * Retrieve XML document. XMLHTTPRequest implementation.
 */
function makeRequest(url, batch) {
    //window.alert(url);
    if (req != null) {
        window.alert("Request is being processed, please wait...\nState of the request: " + req.readyState);
    }
    req = null;
    /*if (!clipboardAvailable) {
        try {
            loadToFrame(url);
        } catch (e) {
            if (!batch) {
                window.alert("Failed to request module: " + e);
                throw e;
            }
        }
    } else */if (window.XMLHttpRequest) {
        req = new XMLHttpRequest();
        if (batch) {
            req.onreadystatechange = batchProcessReqChangeEvent;
        } else {
            req.onreadystatechange = processReqChangeEvent;
        }
        req.open("GET", url, async);
        if (!batch) {
          window.status = "Loading module...";
        }
        try {
            req.send(null);
        } catch (e) {
            if (!batch) {
                window.alert("Failed to request module: " + e);
                throw e;
            }
        }
    // branch for IE/Windows ActiveX version
    } else if (window.ActiveXObject) {
        req = new ActiveXObject("Microsoft.XMLHTTP");
        if (req) {
            if (batch) {
                req.onreadystatechange = batchProcessReqChangeEvent;
            } else {
                req.onreadystatechange = processReqChangeEvent;
            }
            req.open("GET", url, async);
            if (!batch) {
                window.status = "Loading module...";
            }
            try {
                req.send();
            } catch (e) {
                if (!batch) {
                    //window.alert("Failed to request module");
                    throw e;
                }
            }
        } else {
            try {
                loadToFrame(url);
            } catch (e) {
                if (!batch) {
                    //window.alert("Failed to request module: " + e);
                    throw e;
                }
            }
        }
    }
}

/**
 * Reuse response event.
 */
function processReqChangeEvent() {
    // only if req shows "loaded"
    if (req.readyState == 4) {
        // only if "OK"
        if (req.status == 200) {
            //alert(req.responseXML.xml);
            //alert(req.responseText);
            if (ToClipboard(req.responseText)) {
                window.status = "";
                window.alert("Done. Library module has been copied to the clipboard");
            }
        } else {
            window.status = "";
            window.alert("There was a problem retrieving the XML data:\n\n" + req.statusText);
        }
        req = null;
  }
}

function batchProcessReqChangeEvent() {
    if (req.readyState == 4) {
        if (req.status == 200) {
            // NOOP
        } else {
            batchErrors++;
        }
        req = null;
    }
}

function loadToFrame(url) {
    if (parent && parent.DocumentLoad) {
        if (parent.DocumentLoad.location == url) {
          throw "Failed to load into load frame";
        }
    } else {
        //window.alert("Unable to load document into load frame");
        throw "document load frame not available";
    }
}

/**
 * Copy content to clipboard.
 */
function ToClipboard(data){
    if (clipboardAvailable) {
        if (clipboardData.setData("Text", data)) {
            return true;
        } else {
           window.alert("Failed to copy data into the clipboard");
           return false;
        }
    } else {
        window.alert("Copy to clipboard not available in " + navigator.appCodeName);
    }
    return false;
}

/**
 * Retrieve conref content.
 */
function StartConrefReuse(Map, ReuseId, batch) {
    if (batch == undefined) {
        batch = false;
    }
    var url;
    url = reuseBaseURL + "StartConrefReuse.asp?DocId=" + escape(Map) + "&Id=" + ReuseId;
    try {
        makeRequest(url, batch);
    } catch (e) {}
    return true;
}

/**
 * Start reuse and retrieve content.
 */
function StartReuse(Map, ReuseId, batch) {
    if (batch == undefined) {
        batch = false;
    }
    var url;
    url = reuseBaseURL + "StartReuse.asp?DocId=" + escape(Map) + "&ReuseId=" + ReuseId;
    try {
        makeRequest(url, batch);
    } catch (e) {}
    return true;
}

/**
 * Check out reused content.
 */
function CheckOut(DocId, button) {
    if (button == null || button == undefined) {
        window.alert("Unable to locate button");
        return false;
    } else {
        var val = button.firstChild.data.toLowerCase();
        if (val == "checkout") {
            doCheckOut(DocId, button, false);
        } else if (val == "undo checkout") {
            doUndoCheckOut(DocId, button, false);
        } else {
            window.alert("Unknown button value '" + val + "'");
        }
    }
    return true;
}

/**
 * Do reuse check-out.
 */
function doCheckOut(DocId, button, batch) {
    if (batch == undefined) {
        batch = false;
    }
    if (button == null) {
        return false;
    }
    var value = button.value;
    var url = reuseBaseURL + "WSCheckOut.asp?DocId=" + DocId;
    try {
        makeRequest(url, batch);
        changeValue(button, "Undo checkout");
    } catch (e) {}
    return true;
}

/**
 * Do reuse undo check-out.
 */
function doUndoCheckOut(DocId, button, batch) {
    if (batch == undefined) {
        batch = false;
    }
    if (button == null) {
        return false;
    }
    var value = button.value;
    var url = reuseBaseURL + "WSUndoCheckOut.asp?DocId=" + DocId;
    try {
        makeRequest(url, batch);
        changeValue(button, "Checkout");
    } catch (e) {}
    return true;
}

// Utility functions

function collectRows(button, cls) {
    if (cls == undefined) {
        cls = "preview-block-content";
    }
    var trs = new Array();
    if (button == null) {
        return trs;
    }
    var x = 0;
    var tbody = button.getElementsByTagName("tbody");
    var id;
    for (var i = 0; i < tbody.length; i++) {
        var tr = tbody.item(i).getElementsByTagName("tr");
        for (var j = 0; j < tr.length; j++) {
            if (tr.item(j).className == cls) {
                trs.push(tr.item(j));
            }
        }
    }
    return trs;
}

function changeValue(button, text) {
    button.value = text;
    button.firstChild.data = text;
}

function getParent(start, target) {
    var t = target.toLowerCase();
    while (start.nodeName.toLowerCase() != t) {
        start = start.parentNode;
    }
    return start;
}

