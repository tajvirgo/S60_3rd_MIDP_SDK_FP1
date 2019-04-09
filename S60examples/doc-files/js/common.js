/**
 * Check required support for TOC.
 */
function support() {
	if (document.getElementsByTagName && document.styleSheets) {
		tocSupported = true;
	} else {
		//alert("Dynamic TOC not available.");		
	}
}

/** Flag if dynamic TOC is supported */
var tocSupported = false;
support();

/**
 * Get relative URL.
 *
 * @param url URL string
 */ 
function getRelativeUrl(url) {
    var i = url.lastIndexOf("/");
    if (i != -1) {
        return url.substring(i + 1);
    } else {
        return url;
    }
}

/**
 * Return target node of an event.
 **/
function getTargetNode(event) {
	if (event.target) {
		return event.target;
	} else if (event.srcElement) {
		return event.srcElement;	
	} else {
		return null;	
	}
}

/**
 * Attach event listener to a DOM node.
 *
 * @param node DOM node to attach lister to
 * @param type Type of event to catch
 * @param func Function to act as the handler 
 */
function attachEventListener(node, type, func) {
    if (node.addEventListener) {
        node.addEventListener(type, func, false);
    } else if (node.attachEvent) {
        node.attachEvent("on" + type, function() { func.call(this, event); } );
    }
}

/**
 * Hightlight TOC node.
 *
 * @param node TOC node to highlight
 */
function highlightTocItem(node) {
	if (tocSupported) {
		// turn old off
		var lis = node.ownerDocument.getElementsByTagName("li");
		for (var i = 0; i < lis.length; i++) {
			removeClass(lis.item(i), "on3"); //lis.item(i).className = "";
		}
		// turn this on
		addClass(node.parentNode, "on3");
	}
}

/**
 * Toggle child node display.
 *
 * @param n Target node of the event.
 */
function toggle(n) {
	// toggle controller
	if (isClass(n, "toc-controller-open")) {
		n.firstChild.data = "+";
		switchClass(n, "toc-controller-closed", "toc-controller-open");
	} else {
		n.firstChild.data = "-";
		switchClass(n, "toc-controller-open", "toc-controller-closed");
	}
	// toggle content	
    i = getNextSiblingByTagName(n.parentNode.firstChild, "ul");
    if (i != null) {
		if (i.style.display == "none") {
			i.style.display = "block";
		} else {
			i.style.display = "none";
		}        
    }
}


// CSS CLASS FUNCTIONS


/**
 * Switch class of an element.
 *
 * @param n Element node to change the class of
 * @param f Class to change from
 * @param t Class to change to
 * @param arr Class array (optional)
 */
function switchClass(n, f, t, arr) {
    if (arr == undefined) {
        arr = getClassArray(n.className);
    }
    for (var i = 0; i < arr.length; i++) {
        if (arr[i] == f) {
            arr[i] = t;
        } else if (arr[i] == t) {
            arr[i] = f;
        }
    }
    n.className = arr.join(" "); 
}

/**
 * Test if element is of class.
 *
 * @param n Element node to test
 * @param c Class to test for
 * @param arr Class array (optional)
 * @return boolean value if element is of the tested class
 */
function isClass(n, c, arr) {
    if (arr == undefined) {
        arr = getClassArray(n.className);
    }
    for (var i = 0; i < arr.length; i++) {
        if (arr[i] == c) {
            return true;
        }
    }
    return false;
}

/**
 * Add class to an element.
 *
 * @param n Element node to add the class to
 * @param c Class to add
 * @param arr Class array (optional)
 */
function addClass(n, c, arr) {
    if (arr == undefined) {
        arr = getClassArray(n.className);
    }
    if (!isClass(n, c, arr)) {
        n.className = n.className + " " + c;
    }
}

/**
 * Remove class
 *
 * @param n Element node to remove the class from
 * @param c Class to remove
 * @param arr Class array (optional)
 */
function removeClass(n, c, arr) {
    if (arr == undefined) {
        arr = getClassArray(n.className);
    }
    if (isClass(n, c, arr)) {
        var ret = new Array();
        for (var i = 0; i < arr.length; i++) {
            if (arr[i] != c) {
                ret.push(arr[i]);
            }
        }
        n.className = ret.join(" ");
    }
}

function getClassArray(c) {
    return new String(c).split(" ");
}


// COLLECTION CLASSES


/**
 * Map.
 *
 * Usage:
 *
 * var m = new Map();
 * m.put("a", "first");
 * m.put("b", "second");
 * m.size();
 * m.get("a");
 *
 * @constructor
 * @param {Boolean} multi Boolean to define whether map will store dublicate values into an array
 */
function Map(multi) {
    this.keys = new Array();
    this.values = new Array();
    this.multi = (multi == undefined ? false : multi); 
}
/**
 * @param {String} key
 * @param {Object} value
 */
Map.prototype.put = function(key, value) {
    var index = this._find(key);
    var a;
    if (index != -1 ) {
        a = this.values[index];   
    } else {
        index = this.keys.length;
        this.keys[index] = key;
        a = new Array();
    }
    if (this.multi) {
        a.push(value);
    } else {
        a[0] = value;
    }
    this.values[index] = a;
}
/**
 * @param {String} key
 * @type Object
 */
Map.prototype.get = function(key) {
    var index = this._find(key);
    if (index != -1) {
        if (this.multi) {
            return this.values[index];
        } else {
            return this.values[index][0];
        }   
    } else {
        return null;
    }
}
/**
 * @type Number
 */
Map.prototype.size = function() {
	return this.keys.length;
}
/**
 * @param {String} key
 * @type Number
 */
Map.prototype._find = function(key) {
    var index = -1;
    for (var i = 0; i < this.keys.length; i++) {
        if (this.keys[i] == key) {
            index = i;
            break;
        }
    }
    return index;
}

/**
 * Array Iterator
 *
 * Usage:
 *
 * var arr = new Array("a", "b", "c");
 * for (var itr = new ArrayIterator(arr); itr.hasNext();) {
 *     alert(itr.next());
 * }
 *
 * @constructor
 * @param {Array} arrayLike array to iterate through
 */
function ArrayIterator(arrayLike) {
    this.arrayLike = arrayLike;
    this.i = 0;
    NoSuchElementException = function () {};
    NoSuchElementException.prototype = new Error();
    NoSuchElementException.prototype.name = 'NoSuchElementException';
    NoSuchElementException.prototype.message = 'Iteration has no more elements';
}
/**
 * @type Object
 */
ArrayIterator.prototype.next = function() {
    if (!hasNext()) {
        throw new NoSuchElementException();
    }
    return this.arrayLike[this.i++];
}
/*/
 * @type Boolean
 */
ArrayIterator.prototype.hasNext = function() {
    return this.i < this.arrayLike.length;
}


// DOM FUNCTIONS


function getNextSiblingByTagName(n, t) {
    if (n != null) {
        var tag = t.toLowerCase();
    	var i = n;
    	while (true) {
    		if (i.nodeType == 1 && i.nodeName.toLowerCase() == t) {
    		    return i;
    		}
    		if (i.nextSibling != null) {
    			i = i.nextSibling;
    		} else {
    			return null;
    		}
    	}
	}
	return null;
}

function getChildElementsByTagName(node, name) {
	var c = new Array();
	for (var n = node.firstChild; n != null; n = n.nextSibling) {
		if (n.nodeType == 1) {
			if (name == undefined || name == n.nodeName.toLowerCase()) {
				c.push(n);
			}
		}
	}
	return c;
}

function wrapChildElements(parent, wrapper) {
	for (var c = parent.firstChild, n = null; c != null; c = parent.firstChild) {
		n = parent.removeChild(c);
		wrapper.appendChild(n);
	}
	parent.appendChild(wrapper);
}

/**
 * Get elements by class name.
 *
 * @param node DOM node whose descendants select
 * @param cls Class name
 * @param elem Element name (optional)
 * @return Array of DOM nodes
 */
function getElementsByClassName(node, cls, elem) {
	if (elem == undefined) {
		elem = "*"
	}
	var res = new Array();
	for (var el = document.getElementsByTagName(elem), i = 0, j = 0; i < el.length; i++) {
	  	if (isClass(el[i], cls)) {
	    	res[j++] = el[i];
		}
	}
	return res;
}
