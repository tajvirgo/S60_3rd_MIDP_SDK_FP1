/* Constants */

var STATIC_STRING_TOC_SYNC = "TOC Sync";
var STATIC_STRING_TOC_SYNC_HELP = "Refresh / Show current topic";

/* Variables */

var initialCollapse = true;
var automaticSync = true;

/**
 * Add TOC controllers.
 */
function addTocControllers() {
    var nl = getElementsByClassName(document, "nav3", "div");
    for (var i = 0; i < nl.length; i++) {
        var lis = nl[i].getElementsByTagName("li");
        for (var j = 0; j < lis.length; j++) {
            var li = lis[j];
            var a = li.getElementsByTagName("a")[0];
            if (a != null) {
                  var uls = li.getElementsByTagName("ul");        
                  if (uls.length != 0) {
                      // hide kids
                      if (initialCollapse) {
                          for (var k = li.firstChild;; k = k.nextSibling) {
                              if (k.nodeType == 1 && k.nodeName.toLowerCase() == "ul") {
                                  k.style.display = "none";
                              }
                              if (k.nextSibling == null) {
                                  break;
                              }
                          }
                      }
                      // add controller
                      var c = document.createElement("span");
                      addClass(c, "toc-controller"); //c.className = "toc-controller";
                      if (initialCollapse) {
                        addClass(c, "toc-controller-closed");
                          c.appendChild(document.createTextNode("+"));
                      }  else {
                          addClass(c, "toc-controller-open");
                          c.appendChild(document.createTextNode("-"));
                      }
                      attachEventListener(c, "click", toggleHandler);
                      li.insertBefore(c, li.firstChild);
                  } else {
                      // add space
                      var sp = document.createElement("span");
                      addClass(sp, "toc-space"); //sp.className = "toc-space";
                      var pls = document.createTextNode("x");
                      sp.appendChild(pls);
                      li.insertBefore(sp, li.firstChild);                        
                  }
                  // add state
                  if (!automaticSync) {
                      attachEventListener(a, "click", tocItemClickHandler);
                  }
            }
        }
    }
}

/**
 * Toggle child node display.
 */
function toggleHandler(event) {
    toggle(getTargetNode(event));
}

/**
 * Set current node as current.
 */
function tocItemClickHandler(event) {
    highlightTocItem(getTargetNode(event));
}

/**
 * Initialize TOC.
 */
function initToc() {
    if (tocSupported) {
        addTocControllers();
        if (!automaticSync) {
            //var b = document.createElement("img");
            //b.src = "images/xhtml/e_synch_nav.gif";
            var b = document.createElement("button");
            b.appendChild(document.createTextNode(STATIC_STRING_TOC_SYNC));
            b.title = STATIC_STRING_TOC_SYNC_HELP;
            addClass(b, "button-manual_sync");
            attachEventListener(b, "click", manualSyncTocHandler);
            var bc = document.createElement("div");
            addClass(bc, "button-manual_sync-container");
            bc.appendChild(b);
            var nav3 = getElementsByClassName(document, "nav3", "div");
            if (nav3.length > 0) {
              nav3[0].insertBefore(bc, nav3[0].firstChild);
            }
        }
    }    
}

/**
 * Button listener for manual TOC sync.
 */
function manualSyncTocHandler(event) {
    var b = getTargetNode(event);
    var href = new String(window.top.frames["main"].location);
    var i = href.lastIndexOf("/");
    if (i != -1) {
        href = href.substring(i + 1);
    }
    findTocItem(href);
}
