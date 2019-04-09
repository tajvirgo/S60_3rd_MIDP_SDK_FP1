/**
 * Initialize links
 */
function initPage() {
	if (automaticSync) { 
	    var href = new String(window.location);
	    var i = href.lastIndexOf("/");
	    if (i != -1) {
	        href = href.substring(i + 1);
	    }
	    findTocItem(href);
    }
    /*
	if (tocSupported) {
		var as = document.getElementsByTagName("a");
		for (var i = 0; i < as.length; i++) {
			attachEventListener(as[i], "click", syncTocHandler);
			if (as[i].addEventListener) {
				as[i].addEventListener("mousedown", syncTocHandler, false);
			} else if (as[i].attachEvent) {
				as[i].setAttribute("onmousedown", syncTocHandlerIE);	
			}
		}
	}
	*/	
}

/**
 * TOC sync
 */
function syncTocHandler(event) {
	if (tocSupported) {
		var n = getTargetNode(event);
		var href = n.getAttribute("href");
		/*
		if (isClass(n, "javadoc_ref")) {
    		if (href.indexOf('#') != -1) {
    			href = href.substring(0, url.indexOf('#'));
    		}
            var a = href.split("/");
            var last = a.length - 1;
            while (a[last].indexOf(".") != -1) {
                last--;
            }
            a = a.slice(0, last + 1);
            href = a.join("/") + "/package-summary.html";
            alert(n.getAttribute("href") + "\n" + href);
		}
		*/
        findTocItem(href);
	}
}

/**
 * Find current TOC node.
 */
function findTocItem(url) {
	if (tocSupported) {
		var u = url;
		if (url.indexOf('#') != -1) {
			u = u.substring(0, url.indexOf('#'));
		}
		if (!window.parent.frames["toc"]) return;
		var d = window.parent.frames["toc"].document;
		var as = d.getElementsByTagName("a");
		for (var i = 0; i < as.length; i++) {
			if (getRelativeUrl(as[i].href) == u) {
				highlightTocItem(as[i]);
				// make sure TOC is expanded if needed
				var n = as[i].parentNode;
				while (n != null) {
					if (n.firstChild && n.firstChild.firstChild && n.firstChild.firstChild.data == "+") {
						toggle(n.firstChild);
					}
					if (n.parentNode && n.parentNode.nodeName.toLowerCase() == "ul" &&
							n.parentNode.parentNode && n.parentNode.parentNode.nodeName.toLowerCase() == "li") {
						n = n.parentNode.parentNode;
					} else {
						n = null;	
					}
				}
				break;
			}
		}
	}
}