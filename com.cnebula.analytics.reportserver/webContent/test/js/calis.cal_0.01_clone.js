/*!
 * Piwik - Web Analytics
 *
 * JavaScript tracking client
 *
 * @link http://piwik.org
 * @source http://dev.piwik.org/trac/browser/trunk/js/piwik.js
 * @license http://www.opensource.org/licenses/bsd-license.php Simplified BSD
 */

// Refer to README for build instructions when minifying this file for distribution.

/*
 * Browser [In]Compatibility
 * - minimum required ECMAScript: ECMA-262, edition 3
 *
 * Incompatible with these (and earlier) versions of:
 * - IE4 - try..catch and for..in introduced in IE5
 * - IE5 - named anonymous functions, array.push, encodeURIComponent, and decodeURIComponent introduced in IE5.5
 * - Firefox 1.0 and Netscape 8.x - FF1.5 adds array.indexOf, among other things
 * - Mozilla 1.7 and Netscape 6.x-7.x
 * - Netscape 4.8
 * - Opera 6 - Error object (and Presto) introduced in Opera 7
 * - Opera 7
 */


/**
 * 
 * 嵌入页面中的示例代码如下：
	<!doctype html public "-//w3c//dtd html 4.0 transitional//en">
	<html>
	<head>
		<title>Test Calis Log Track Code</title>
		<meta content="text/html; charset=utf-8" http-equiv=content-type>
		<script type=text/javascript>
			function trackResource(){
				var json = {
					osub:'China Beijing University Book',
					opub:'Hebei Wenyi publisher',
					ort: 'pagedddd',
					ocrt:'ocrt',
					ocor:'ocor',
					odt:'odt',
					oct:'oct',
					ofmt:'ofmt',
					osrc:'osrc',
					oln:'oln',
					orel:'orel',
					orid:'orid'
				};
				_olc.push(json);
			}
		</script>
	</head>
	<body>
		<a onclick="trackResource();" href="javascript:void(0);">查看资源</a>
		<script type=text/javascript>
			var _olc = _olc || [];
			(function (){		
				var json = {
					'oaid' : '237010',
					oln:'oln'
				};
				_olc.push(json);
						
				var d = document,
				_g = d.createElement('script');
				_s = d.getElementsByTagName('script')[0];
				_g.type = 'text/javascript';
				_g.defer = true;
				_g.async = true;
				_g.src = "http://127.0.0.1/piwik/piwik.js";
				_s.parentNode.insertBefore(_g,_s);	
			})();		
		</script>
	</body>
	</html>
 */

/************************************************************
 * JSON - public domain reference implementation by Douglas Crockford
 * @link http://www.JSON.org/json2.js
 ************************************************************/
/*jslint evil: true, strict: true, regexp: false, type: true, bitwise: true */
/*global JSON2 */
/*members "", "\b", "\t", "\n", "\f", "\r", "\"", JSON2, "\\", apply,
    call, charCodeAt, getUTCDate, getUTCFullYear, getUTCHours,
    getUTCMinutes, getUTCMonth, getUTCSeconds, hasOwnProperty, join,
    lastIndex, length, parse, prototype, push, replace, slice, stringify,
    test, toJSON, toString, valueOf,
    objectToJSON
*/

// Create a JSON object only if one does not already exist. We create the
// methods in a closure to avoid creating global variables.

if (!this.JSON2) {
	this.JSON2 = {};
}

(function () {
    "use strict";

    function f(n) {
        // Format integers to have at least two digits.
        return n < 10 ? '0' + n : n;
    }

    function objectToJSON(value, key) {
        var objectType = Object.prototype.toString.apply(value);

        if (objectType === '[object Date]') {
            return isFinite(value.valueOf()) ?
	                value.getUTCFullYear()     + '-' +
	                f(value.getUTCMonth() + 1) + '-' +
	                f(value.getUTCDate())      + 'T' +
	                f(value.getUTCHours())     + ':' +
	                f(value.getUTCMinutes())   + ':' +
	                f(value.getUTCSeconds())   + 'Z' : null;
        }

        if (objectType === '[object String]' ||
                objectType === '[object Number]' ||
                objectType === '[object Boolean]') {
            return value.valueOf();
        }

        if (objectType !== '[object Array]' &&
                typeof value.toJSON === 'function') {
            return value.toJSON(key);
        }

        return value;
    }

    var cx = new RegExp('[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]', 'g'),
	// hack: workaround Snort false positive (sid 8443)
        pattern = '\\\\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]',
        escapable = new RegExp('[' + pattern, 'g'),
        gap,
        indent,
        meta = {    // table of character substitutions
            '\b': '\\b',
            '\t': '\\t',
            '\n': '\\n',
            '\f': '\\f',
            '\r': '\\r',
            '"' : '\\"',
            '\\': '\\\\'
        },
        rep;

    function quote(string) {

// If the string contains no control characters, no quote characters, and no
// backslash characters, then we can safely slap some quotes around it.
// Otherwise we must also replace the offending characters with safe escape
// sequences.

        escapable.lastIndex = 0;
        return escapable.test(string) ? '"' + string.replace(escapable, function (a) {
            var c = meta[a];
            return typeof c === 'string' ? c :
	                '\\u' + ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
        }) + '"' : '"' + string + '"';
    }

    function str(key, holder) {

// Produce a string from holder[key].

        var i,          // The loop counter.
            k,          // The member key.
            v,          // The member value.
            length,
            mind = gap,
            partial,
            value = holder[key];

// If the value has a toJSON method, call it to obtain a replacement value.

        if (value && typeof value === 'object') {
            value = objectToJSON(value, key);
        }

// If we were called with a replacer function, then call the replacer to
// obtain a replacement value.

        if (typeof rep === 'function') {
            value = rep.call(holder, key, value);
        }

// What happens next depends on the value's type.

        switch (typeof value) {
        case 'string':
            return quote(value);

        case 'number':

// JSON numbers must be finite. Encode non-finite numbers as null.

            return isFinite(value) ? String(value) : 'null';

        case 'boolean':
        case 'null':

// If the value is a boolean or null, convert it to a string. Note:
// typeof null does not produce 'null'. The case is included here in
// the remote chance that this gets fixed someday.

            return String(value);

// If the type is 'object', we might be dealing with an object or an array or
// null.

        case 'object':

// Due to a specification blunder in ECMAScript, typeof null is 'object',
// so watch out for that case.

            if (!value) {
                return 'null';
            }

// Make an array to hold the partial results of stringifying this object value.

            gap += indent;
            partial = [];

// Is the value an array?

            if (Object.prototype.toString.apply(value) === '[object Array]') {

// The value is an array. Stringify every element. Use null as a placeholder
// for non-JSON values.

                length = value.length;
                for (i = 0; i < length; i += 1) {
                    partial[i] = str(i, value) || 'null';
                }

// Join all of the elements together, separated with commas, and wrap them in
// brackets.

                v = partial.length === 0 ? '[]' : gap ?
                        '[\n' + gap + partial.join(',\n' + gap) + '\n' + mind + ']' :
                        '[' + partial.join(',') + ']';
                gap = mind;
                return v;
            }

// If the replacer is an array, use it to select the members to be stringified.

            if (rep && typeof rep === 'object') {
                length = rep.length;
                for (i = 0; i < length; i += 1) {
                    if (typeof rep[i] === 'string') {
                        k = rep[i];
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ': ' : ':') + v);
                        }
                    }
                }
            } else {

// Otherwise, iterate through all of the keys in the object.

                for (k in value) {
                    if (Object.prototype.hasOwnProperty.call(value, k)) {
                        v = str(k, value);
                        if (v) {
                            partial.push(quote(k) + (gap ? ': ' : ':') + v);
                        }
                    }
                }
            }

// Join all of the member texts together, separated with commas,
// and wrap them in braces.

            v = partial.length === 0 ? '{}' : gap ?
                    '{\n' + gap + partial.join(',\n' + gap) + '\n' + mind + '}' :
                    '{' + partial.join(',') + '}';
            gap = mind;
            return v;
        }
    }

// If the JSON object does not yet have a stringify method, give it one.

    if (typeof JSON2.stringify !== 'function') {
        JSON2.stringify = function (value, replacer, space) {

// The stringify method takes a value and an optional replacer, and an optional
// space parameter, and returns a JSON text. The replacer can be a function
// that can replace values, or an array of strings that will select the keys.
// A default replacer method can be provided. Use of the space parameter can
// produce text that is more easily readable.

            var i;
            gap = '';
            indent = '';

// If the space parameter is a number, make an indent string containing that
// many spaces.

            if (typeof space === 'number') {
                for (i = 0; i < space; i += 1) {
                    indent += ' ';
                }

// If the space parameter is a string, it will be used as the indent string.

            } else if (typeof space === 'string') {
                indent = space;
            }

// If there is a replacer, it must be a function or an array.
// Otherwise, throw an error.

            rep = replacer;
            if (replacer && typeof replacer !== 'function' &&
                    (typeof replacer !== 'object' ||
                    typeof replacer.length !== 'number')) {
                throw new Error('JSON.stringify');
            }

// Make a fake root object containing our value under the key of ''.
// Return the result of stringifying the value.

            return str('', {'': value});
        };
    }

// If the JSON object does not yet have a parse method, give it one.

    if (typeof JSON2.parse !== 'function') {
        JSON2.parse = function (text, reviver) {

// The parse method takes a text and an optional reviver function, and returns
// a JavaScript value if the text is a valid JSON text.

            var j;

            function walk(holder, key) {

// The walk method is used to recursively walk the resulting structure so
// that modifications can be made.

                var k, v, value = holder[key];
                if (value && typeof value === 'object') {
                    for (k in value) {
                        if (Object.prototype.hasOwnProperty.call(value, k)) {
                            v = walk(value, k);
                            if (v !== undefined) {
                                value[k] = v;
                            } else {
                                delete value[k];
                            }
                        }
                    }
                }
                return reviver.call(holder, key, value);
            }

// Parsing happens in four stages. In the first stage, we replace certain
// Unicode characters with escape sequences. JavaScript handles many characters
// incorrectly, either silently deleting them, or treating them as line endings.

            text = String(text);
            cx.lastIndex = 0;
            if (cx.test(text)) {
                text = text.replace(cx, function (a) {
                    return '\\u' +
                        ('0000' + a.charCodeAt(0).toString(16)).slice(-4);
                });
            }

// In the second stage, we run the text against regular expressions that look
// for non-JSON patterns. We are especially concerned with '()' and 'new'
// because they can cause invocation, and '=' because it can cause mutation.
// But just to be safe, we want to reject all unexpected forms.

// We split the second stage into 4 regexp operations in order to work around
// crippling inefficiencies in IE's and Safari's regexp engines. First we
// replace the JSON backslash pairs with '@' (a non-JSON character). Second, we
// replace all simple value tokens with ']' characters. Third, we delete all
// open brackets that follow a colon or comma or that begin the text. Finally,
// we look to see that the remaining characters are only whitespace or ']' or
// ',' or ':' or '{' or '}'. If that is so, then the text is safe for eval.

            if ((new RegExp('^[\\],:{}\\s]*$'))
                    .test(text.replace(new RegExp('\\\\(?:["\\\\/bfnrt]|u[0-9a-fA-F]{4})', 'g'), '@')
                        .replace(new RegExp('"[^"\\\\\n\r]*"|true|false|null|-?\\d+(?:\\.\\d*)?(?:[eE][+\\-]?\\d+)?', 'g'), ']')
                        .replace(new RegExp('(?:^|:|,)(?:\\s*\\[)+', 'g'), ''))) {

// In the third stage we use the eval function to compile the text into a
// JavaScript structure. The '{' operator is subject to a syntactic ambiguity
// in JavaScript: it can begin a block or an object literal. We wrap the text
// in parens to eliminate the ambiguity.

                j = eval('(' + text + ')');

// In the optional fourth stage, we recursively walk the new structure, passing
// each name/value pair to a reviver function for possible transformation.

                return typeof reviver === 'function' ?
	                    walk({'': j}, '') : j;
            }

// If the text is not JSON parseable, then a SyntaxError is thrown.

            throw new SyntaxError('JSON.parse');
        };
    }
}());
/************************************************************
 * end JSON
 ************************************************************/

/*jslint browser:true, plusplus:true, vars:true, dangling:true, nomen:true, strict:true, evil:true */
/*global window */
/*global unescape */
/*global ActiveXObject */
/*global _paq:true */
/*members encodeURIComponent, decodeURIComponent,
	shift, unshift,
	addEventListener, attachEvent, removeEventListener, detachEvent,
	cookie, domain, readyState, documentElement, doScroll, title,
	location, top, document, referrer, parent, links, href, protocol, GearsFactory,
	event, which, button, srcElement, type, target,
	parentNode, tagName, hostname, className,
	userAgent, cookieEnabled, platform, mimeTypes, enabledPlugin, javaEnabled,
	XDomainRequest, XMLHttpRequest, ActiveXObject, open, setRequestHeader, onreadystatechange, setRequestHeader, send, readyState, status,
	getTime, setTime, toGMTString, getHours, getMinutes, getSeconds,
	toLowerCase, charAt, indexOf, lastIndexOf, split, slice,
	onLoad, src,
	round, random,
	exec,
	res, width, height,
	pdf, qt, realp, wma, dir, fla, java, gears, ag,
	hook, getHook, getVisitorId, getVisitorInfo, setTrackerUrl, setSiteId,
	getAttributionInfo, getAttributionCampaignName, getAttributionCampaignKeyword,
	getAttributionReferrerTimestamp, getAttributionReferrerUrl,
	setCustomData, getCustomData,
	setCustomVariable, getCustomVariable, deleteCustomVariable,
	setDownloadExtensions, addDownloadExtensions,
	setDomains, setIgnoreClasses, setRequestMethod,
	setReferrerUrl, setCustomUrl, setDocumentTitle,
	setDownloadClasses, setLinkClasses,
	setCampaignNameKey, setCampaignKeywordKey,
	discardHashTag,
	setCookieNamePrefix, setCookieDomain, setCookiePath, setVisitorIdCookie,
	setVisitorCookieTimeout, setSessionCookieTimeout, setReferralCookieTimeout,
	setConversionAttributionFirstReferrer,
	doNotTrack, setDoNotTrack,
	addListener, enableLinkTracking, setLinkTrackingTimer,
	setHeartBeatTimer, killFrame, redirectFile,
	trackGoal, trackLink, trackPageView, setEcommerceView, addEcommerceItem, trackEcommerceOrder, trackEcommerceCartUpdate,
	addPlugin, getTracker, getAsyncTracker
*/
	var calis = calis || {};
	//calis.TRACKER_URL = "http://192.168.52.15:8080/log/param";//TODO:remove
	var _olc = _olc || []; // asynchronous tracker (or proxy)
	
	// Piwik singleton and namespace
	calis.cal =	calis.cal || (function () {
		"use strict";

		/************************************************************
		 * Private data
		 ************************************************************/

		var expireDateTime,

			/* plugins */
			plugins = {},

			/* alias frequently used globals for added minification */
			documentAlias = document,
			navigatorAlias = navigator,
			screenAlias = screen,
			windowAlias = window,

			/* DOM Ready */
			hasLoaded = false,
			registeredOnLoadHandlers = [],

			/* encode */
			encodeWrapper = windowAlias.encodeURIComponent,

			/* decode */
			decodeWrapper = windowAlias.decodeURIComponent,

			/* urldecode */
			urldecode = unescape,

			/* asynchronous tracker */
			asyncTracker,

			/* iterator */
			i;

		/************************************************************
		 * Private methods
		 ************************************************************/

		/*
		 * Is property defined?
		 */
		function isDefined(property) {
			return typeof property !== 'undefined';
		}

		/*
		 * Is property a function?
		 */
		function isFunction(property) {
			return typeof property === 'function';
		}

		/*
		 * Is property an object?
		 *
		 * @return bool Returns true if property is null, an Object, or subclass of Object (i.e., an instanceof String, Date, etc.)
		 */
		function isObject(property) {
			return typeof property === 'object';
		}

		/*
		 * Is property a string?
		 */
		function isString(property) {
			return typeof property === 'string' || property instanceof String;
		}

		/*
		 * apply wrapper
		 *
		 * @param array parameterArray An array comprising either:
		 *      [ 'methodName', optional_parameters ]
		 * or:
		 *      [ functionObject, optional_parameters ]
		 */
		function apply() {
			var i, f, parameterArray;

			for (i = 0; i < arguments.length; i += 1) {
				parameterArray = arguments[i];
				f = parameterArray.shift();

				if (isString(f)) {
					asyncTracker[f].apply(asyncTracker, parameterArray);
				} else {
					f.apply(asyncTracker, parameterArray);
				}
			}
		}

		/*
		 * Cross-browser helper function to add event handler
		 */
		function addEventListener(element, eventType, eventHandler, useCapture) {
			if (element.addEventListener) {
				element.addEventListener(eventType, eventHandler, useCapture);
				return true;
			}
			if (element.attachEvent) {
				return element.attachEvent('on' + eventType, eventHandler);
			}
			element['on' + eventType] = eventHandler;
		}

		/*
		 * Call plugin hook methods
		 */
		function executePluginMethod(methodName, callback) {
			var result = '',
				i,
				pluginMethod;

			for (i in plugins) {
				if (Object.prototype.hasOwnProperty.call(plugins, i)) {
					pluginMethod = plugins[i][methodName];
					if (isFunction(pluginMethod)) {
						result += pluginMethod(callback);
					}
				}
			}

			return result;
		}

		/*
		 * Handle beforeunload event
		 *
		 * Subject to Safari's "Runaway JavaScript Timer" and
		 * Chrome V8 extension that terminates JS that exhibits
		 * "slow unload", i.e., calling getTime() > 1000 times
		 */
		function beforeUnloadHandler() {
			var now;

			executePluginMethod('unload');

			/*
			 * Delay/pause (blocks UI)
			 */
			if (expireDateTime) {
				// the things we do for backwards compatibility...
				// in ECMA-262 5th ed., we could simply use:
				//     while (Date.now() < expireDateTime) { }
				do {
					now = new Date();
				} while (now.getTime() < expireDateTime);
			}
		}

		/*
		 * Handler for onload event
		 */
		function loadHandler() {
			var i;

			if (!hasLoaded) {
				hasLoaded = true;
				executePluginMethod('load');
				for (i = 0; i < registeredOnLoadHandlers.length; i++) {
					registeredOnLoadHandlers[i]();
				}
			}
			return true;
		}

		/*
		 * Add onload or DOM ready handler
		 */
		function addReadyListener() {
			var _timer;

			if (documentAlias.addEventListener) {
				addEventListener(documentAlias, 'DOMContentLoaded', function ready() {
					documentAlias.removeEventListener('DOMContentLoaded', ready, false);
					loadHandler();
				});
			} else if (documentAlias.attachEvent) {
				documentAlias.attachEvent('onreadystatechange', function ready() {
					if (documentAlias.readyState === 'complete') {
						documentAlias.detachEvent('onreadystatechange', ready);
						loadHandler();
					}
				});

				if (documentAlias.documentElement.doScroll && windowAlias === windowAlias.top) {
					(function ready() {
						if (!hasLoaded) {
							try {
								documentAlias.documentElement.doScroll('left');
							} catch (error) {
								setTimeout(ready, 0);
								return;
							}
							loadHandler();
						}
					}());
				}
			}

			// sniff for older WebKit versions
			if ((new RegExp('WebKit')).test(navigatorAlias.userAgent)) {
				_timer = setInterval(function () {
					if (hasLoaded || /loaded|complete/.test(documentAlias.readyState)) {
						clearInterval(_timer);
						loadHandler();
					}
				}, 10);
			}

			// fallback
			addEventListener(windowAlias, 'load', loadHandler, false);
		}

		/*
		 * Get page referrer
		 */
		function getReferrer() {
			var referrer = '';

			try {
				referrer = windowAlias.top.document.referrer;
			} catch (e) {
				if (windowAlias.parent) {
					try {
						referrer = windowAlias.parent.document.referrer;
					} catch (e2) {
						referrer = '';
					}
				}
			}
			if (referrer === '') {
				referrer = documentAlias.referrer;
			}

			return referrer;
		}

		/*
		 * Extract scheme/protocol from URL
		 */
		function getProtocolScheme(url) {
			var e = new RegExp('^([a-z]+):'),
				matches = e.exec(url);

			return matches ? matches[1] : null;
		}

		/*
		 * Extract hostname from URL
		 */
		function getHostName(url) {
			// scheme : // [username [: password] @] hostame [: port] [/ [path] [? query] [# fragment]]
			var e = new RegExp('^(?:(?:https?|ftp):)/*(?:[^@]+@)?([^:/#]+)'),
				matches = e.exec(url);

			return matches ? matches[1] : url;
		}

		/*
		 * Extract parameter from URL
		 */
		function getParameter(url, name) {
			// scheme : // [username [: password] @] hostame [: port] [/ [path] [? query] [# fragment]]
			var e = new RegExp('^(?:https?|ftp)(?::/*(?:[^?]+)[?])([^#]+)'),
				matches = e.exec(url),
				f = new RegExp('(?:^|&)' + name + '=([^&]*)'),
				result = matches ? f.exec(matches[1]) : 0;

			return result ? decodeWrapper(result[1]) : '';
		}

		/*
		 * Set cookie value
		 */
		function setCookie(cookieName, value, msToExpire, path, domain, secure) {
			var expiryDate;

			// relative time to expire in milliseconds
			if (msToExpire) {
				expiryDate = new Date();
				expiryDate.setTime(expiryDate.getTime() + msToExpire);
			}

			documentAlias.cookie = cookieName + '=' + encodeWrapper(value) +
				(msToExpire ? ';expires=' + expiryDate.toGMTString() : '') +
				';path=' + (path || '/') +
				(domain ? ';domain=' + domain : '') +
				(secure ? ';secure' : '');
		}

		/*
		 * Get cookie value
		 */
		function getCookie(cookieName) {
			var cookiePattern = new RegExp('(^|;)[ ]*' + cookieName + '=([^;]*)'),

				cookieMatch = cookiePattern.exec(documentAlias.cookie);

			return cookieMatch ? decodeWrapper(cookieMatch[2]) : 0;
		}

		/*
		 * UTF-8 encoding
		 */
		function utf8_encode(argString) {
			return urldecode(encodeWrapper(argString));
		}

		/************************************************************
		 * sha1
		 * - based on sha1 from http://phpjs.org/functions/sha1:512 (MIT / GPL v2)
		 ************************************************************/
		function sha1(str) {
			// +   original by: Webtoolkit.info (http://www.webtoolkit.info/)
			// + namespaced by: Michael White (http://getsprink.com)
			// +      input by: Brett Zamir (http://brett-zamir.me)
			// +   improved by: Kevin van Zonneveld (http://kevin.vanzonneveld.net)
			// +   jslinted by: Anthon Pang (http://piwik.org)

			var
				rotate_left = function (n, s) {
					return (n << s) | (n >>> (32 - s));
				},

				cvt_hex = function (val) {
					var str = '',
						i,
						v;

					for (i = 7; i >= 0; i--) {
						v = (val >>> (i * 4)) & 0x0f;
						str += v.toString(16);
					}
					return str;
				},

				blockstart,
				i,
				j,
				W = [],
				H0 = 0x67452301,
				H1 = 0xEFCDAB89,
				H2 = 0x98BADCFE,
				H3 = 0x10325476,
				H4 = 0xC3D2E1F0,
				A,
				B,
				C,
				D,
				E,
				temp,
				str_len,
				word_array = [];

			str = utf8_encode(str);
			str_len = str.length;

			for (i = 0; i < str_len - 3; i += 4) {
				j = str.charCodeAt(i) << 24 | str.charCodeAt(i + 1) << 16 |
					str.charCodeAt(i + 2) << 8 | str.charCodeAt(i + 3);
				word_array.push(j);
			}

			switch (str_len & 3) {
			case 0:
				i = 0x080000000;
				break;
			case 1:
				i = str.charCodeAt(str_len - 1) << 24 | 0x0800000;
				break;
			case 2:
				i = str.charCodeAt(str_len - 2) << 24 | str.charCodeAt(str_len - 1) << 16 | 0x08000;
				break;
			case 3:
				i = str.charCodeAt(str_len - 3) << 24 | str.charCodeAt(str_len - 2) << 16 | str.charCodeAt(str_len - 1) << 8 | 0x80;
				break;
			}

			word_array.push(i);

			while ((word_array.length & 15) !== 14) {
				word_array.push(0);
			}

			word_array.push(str_len >>> 29);
			word_array.push((str_len << 3) & 0x0ffffffff);

			for (blockstart = 0; blockstart < word_array.length; blockstart += 16) {
				for (i = 0; i < 16; i++) {
					W[i] = word_array[blockstart + i];
				}

				for (i = 16; i <= 79; i++) {
					W[i] = rotate_left(W[i - 3] ^ W[i - 8] ^ W[i - 14] ^ W[i - 16], 1);
				}

				A = H0;
				B = H1;
				C = H2;
				D = H3;
				E = H4;

				for (i = 0; i <= 19; i++) {
					temp = (rotate_left(A, 5) + ((B & C) | (~B & D)) + E + W[i] + 0x5A827999) & 0x0ffffffff;
					E = D;
					D = C;
					C = rotate_left(B, 30);
					B = A;
					A = temp;
				}

				for (i = 20; i <= 39; i++) {
					temp = (rotate_left(A, 5) + (B ^ C ^ D) + E + W[i] + 0x6ED9EBA1) & 0x0ffffffff;
					E = D;
					D = C;
					C = rotate_left(B, 30);
					B = A;
					A = temp;
				}

				for (i = 40; i <= 59; i++) {
					temp = (rotate_left(A, 5) + ((B & C) | (B & D) | (C & D)) + E + W[i] + 0x8F1BBCDC) & 0x0ffffffff;
					E = D;
					D = C;
					C = rotate_left(B, 30);
					B = A;
					A = temp;
				}

				for (i = 60; i <= 79; i++) {
					temp = (rotate_left(A, 5) + (B ^ C ^ D) + E + W[i] + 0xCA62C1D6) & 0x0ffffffff;
					E = D;
					D = C;
					C = rotate_left(B, 30);
					B = A;
					A = temp;
				}

				H0 = (H0 + A) & 0x0ffffffff;
				H1 = (H1 + B) & 0x0ffffffff;
				H2 = (H2 + C) & 0x0ffffffff;
				H3 = (H3 + D) & 0x0ffffffff;
				H4 = (H4 + E) & 0x0ffffffff;
			}

			temp = cvt_hex(H0) + cvt_hex(H1) + cvt_hex(H2) + cvt_hex(H3) + cvt_hex(H4);
			return temp.toLowerCase();
		}
		/************************************************************
		 * end sha1
		 ************************************************************/

		/*
		 * Fix-up URL when page rendered from search engine cache or translated page
		 */
		function urlFixup(hostName, href, referrer) {
			if (hostName === 'translate.googleusercontent.com') {		// Google
				if (referrer === '') {
					referrer = href;
				}
				href = getParameter(href, 'u');
				hostName = getHostName(href);
			} else if (hostName === 'cc.bingj.com' ||					// Bing
					hostName === 'webcache.googleusercontent.com' ||	// Google
					hostName.slice(0, 5) === '74.6.') {					// Yahoo (via Inktomi 74.6.0.0/16)
				href = documentAlias.links[0].href;
				hostName = getHostName(href);
			}
			return [hostName, href, referrer];
		}

		/*
		 * Fix-up domain
		 */
		function domainFixup(domain) {
			var dl = domain.length;

			// remove trailing '.'
			if (domain.charAt(--dl) === '.') {
				domain = domain.slice(0, dl);
			}
			// remove leading '*'
			if (domain.slice(0, 2) === '*.') {
				domain = domain.slice(1);
			}
			return domain;
		}
		
		function isEmptyObject(O){
			  for (var x in O){
			    return false;
			  }
			  return true;
		}

		/*
		 * Piwik Tracker class
		 *
		 * trackerUrl and trackerSiteId are optional arguments to the constructor
		 *
		 * See: Tracker.setTrackerUrl() and Tracker.setSiteId()
		 */
		function Tracker(trackerUrl, siteId) {

			/************************************************************
			 * Private members
			 ************************************************************/

			var
/*<DEBUG>*/
				/*
				 * registered test hooks
				 */
				registeredHooks = {},
/*</DEBUG>*/

				// Current URL and Referrer URL
				locationArray = urlFixup(documentAlias.domain, windowAlias.location.href, getReferrer()),
				domainAlias = domainFixup(locationArray[0]),
				locationHrefAlias = locationArray[1],
				configReferrerUrl = locationArray[2],

				// Request method (GET or POST)
				configRequestMethod = 'GET',

				// Tracker URL
				configTrackerUrl = trackerUrl || '',

				// Site ID
				configTrackerSiteId = siteId || '',

				// Document URL
				configCustomUrl,

				// Document title
				configTitle = documentAlias.title,

				// Extensions to be treated as download links
				configDownloadExtensions = '7z|aac|ar[cj]|as[fx]|avi|bin|csv|deb|dmg|doc|exe|flv|gif|gz|gzip|hqx|jar|jpe?g|js|mp(2|3|4|e?g)|mov(ie)?|ms[ip]|od[bfgpst]|og[gv]|pdf|phps|png|ppt|qtm?|ra[mr]?|rpm|sea|sit|tar|t?bz2?|tgz|torrent|txt|wav|wm[av]|wpd||xls|xml|z|zip',

				// Hosts or alias(es) to not treat as outlinks
				configHostsAlias = [domainAlias],

				// HTML anchor element classes to not track
				configIgnoreClasses = [],

				// HTML anchor element classes to treat as downloads
				configDownloadClasses = [],

				// HTML anchor element classes to treat at outlinks
				configLinkClasses = [],

				// Maximum delay to wait for web bug image to be fetched (in milliseconds)
				configTrackerPause = 500,

				// Minimum visit time after initial page view (in milliseconds)
				configMinimumVisitTime,

				// Recurring heart beat after initial ping (in milliseconds)
				configHeartBeatTimer,

				// Disallow hash tags in URL
				configDiscardHashTag,

				// Custom data
				configCustomData,

				// Campaign names
				configCampaignNameParameters = [ 'pk_campaign', 'piwik_campaign', 'utm_campaign', 'utm_source', 'utm_medium' ],

				// Campaign keywords
				configCampaignKeywordParameters = [ 'pk_kwd', 'piwik_kwd', 'utm_term' ],

				// First-party cookie name prefix
				configCookieNamePrefix = '_pk_',

				// First-party cookie domain
				// User agent defaults to origin hostname
				configCookieDomain,

				// First-party cookie path
				// Default is user agent defined.
				configCookiePath,

				// Do Not Track
				configDoNotTrack,

				// Do we attribute the conversion to the first referrer or the most recent referrer?
				configConversionAttributionFirstReferrer,

				// Life of the visitor cookie (in milliseconds)
				configVisitorCookieTimeout = 63072000000, // 2 years

				// Life of the session cookie (in milliseconds)
				configSessionCookieTimeout = 1800000, // 30 minutes

				// Life of the referral cookie (in milliseconds)
				configReferralCookieTimeout = 15768000000, // 6 months

				// Should cookies have the secure flag set
				cookieSecure = documentAlias.location.protocol === 'https',

				// Custom Variables read from cookie, scope "visit"
				customVariables = false,

				// Custom Variables, scope "page"
				customVariablesPage = {},

				// Custom Variables names and values are each truncated before being sent in the request or recorded in the cookie
				customVariableMaximumLength = 200,

				// Ecommerce items
				ecommerceItems = {},

				// Browser features via client-side data collection
				browserFeatures = {},

				// Guard against installing the link tracker more than once per Tracker instance
				linkTrackingInstalled = false,

				// Guard against installing the activity tracker more than once per Tracker instance
				activityTrackingInstalled = false,

				// Last activity timestamp
				lastActivityTime,

				// Internal state of the pseudo click handler
				lastButton,
				lastTarget,

				// Hash function
				hash = sha1,

				// Domain hash value
				domainHash,

				// Visitor UUID
				visitorUUID,
			
				//以下为CALIS相关的变量
				_ca_caam_key = "_caam",
				
				_ca_casn_key = "_casn",
				
				_ca_casc_key = "_casc",
				
				_ca_UUID,
				
				ca_casn_SessionCookieTimeout = 1800000,//30分钟
				
				ca_caam_CookieTimeout = 63072000000,//2年，一般要比sessioin cookie时间长
				
				ca_casc_CookieTimeout =  0,
				
				_ca_configAppId;//应用系统ID
			
			/*
			 * Removes hash tag from the URL
			 *
			 * URLs are purified before being recorded in the cookie,
			 * or before being sent as GET parameters
			 */
			function purify(url) {
				var targetPattern;

				if (configDiscardHashTag) {
					targetPattern = new RegExp('#.*');
					return url.replace(targetPattern, '');
				}
				return url;
			}

			/*
			 * Resolve relative reference
			 *
			 * Note: not as described in rfc3986 section 5.2
			 */
			function resolveRelativeReference(baseUrl, url) {
				var protocol = getProtocolScheme(url),
					i;

				if (protocol) {
					return url;
				}

				if (url.slice(0, 1) === '/') {
					return getProtocolScheme(baseUrl) + '://' + getHostName(baseUrl) + url;
				}

				baseUrl = purify(baseUrl);
				if ((i = baseUrl.indexOf('?')) >= 0) {
					baseUrl = baseUrl.slice(0, i);
				}
				if ((i = baseUrl.lastIndexOf('/')) !== baseUrl.length - 1) {
					baseUrl = baseUrl.slice(0, i + 1);
				}

				return baseUrl + url;
			}

			/*
			 * Is the host local? (i.e., not an outlink)
			 */
			function isSiteHostName(hostName) {
				var i,
					alias,
					offset;

				for (i = 0; i < configHostsAlias.length; i++) {
					alias = domainFixup(configHostsAlias[i].toLowerCase());

					if (hostName === alias) {
						return true;
					}

					if (alias.slice(0, 1) === '.') {
						if (hostName === alias.slice(1)) {
							return true;
						}

						offset = hostName.length - alias.length;
						if ((offset > 0) && (hostName.slice(offset) === alias)) {
							return true;
						}
					}
				}
				return false;
			}

			/*
			 * Send image request to Piwik server using GET.
			 * The infamous web bug (or beacon) is a transparent, single pixel (1x1) image
			 */
			function getImage(request) {
				var image = new Image(1, 1);
				image.onLoad = function () { };
				
				image.src = configTrackerUrl + (configTrackerUrl.indexOf('?') < 0 ? '?' : '&') + request;
			}

			/*
			 * POST request to Piwik server using XMLHttpRequest.
			 */
			function sendXmlHttpRequest(request) {
				try {
					// we use the progid Microsoft.XMLHTTP because
					// IE5.5 included MSXML 2.5; the progid MSXML2.XMLHTTP
					// is pinned to MSXML2.XMLHTTP.3.0
					var xhr = windowAlias.XDomainRequest ? new windowAlias.XDomainRequest() :
							windowAlias.XMLHttpRequest ? new windowAlias.XMLHttpRequest() :
									windowAlias.ActiveXObject ? new ActiveXObject('Microsoft.XMLHTTP') :
											null;

					xhr.open('POST', configTrackerUrl, true);

					// fallback on error
					xhr.onreadystatechange = function () {
						if (this.readyState === 4 && this.status !== 200) {
							getImage(request);
						}
					};

					xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');

					// Safari: unsafe headers
//					xhr.setRequestHeader('Content-Length', request.length);
//					xhr.setRequestHeader('Connection', 'close');
					xhr.send(request);
				} catch (e) {
					// fallback
					getImage(request);
				}
			}

			/*
			 * Send request
			 */
			function sendRequest(request, delay) {
				var now = new Date();

				if (!configDoNotTrack) {
					if (configRequestMethod === 'POST') {
						sendXmlHttpRequest(request);
					} else {
						getImage(request);
					}

					expireDateTime = now.getTime() + delay;
				}
			}

			/*
			 * Get cookie name with prefix and domain hash
			 */
			function getCookieName(baseName) {
				// NOTE: If the cookie name is changed, we must also update the PiwikTracker.php which
				// will attempt to discover first party cookies. eg. See the PHP Client method getVisitorId()
				return configCookieNamePrefix + baseName + '.' + configTrackerSiteId + '.' + domainHash;
			}

			/*
			 * Does browser have cookies enabled (for this site)?
			 */
			function hasCookies() {
				var testCookieName = getCookieName('testcookie');

				if (!isDefined(navigatorAlias.cookieEnabled)) {
					setCookie(testCookieName, '1');
					return getCookie(testCookieName) === '1' ? '1' : '0';
				}

				return navigatorAlias.cookieEnabled ? '1' : '0';
			}

			/*
			 * Update domain hash
			 */
			function updateDomainHash() {
				domainHash = hash((configCookieDomain || domainAlias) + (configCookiePath || '/')).slice(0, 4); // 4 hexits = 16 bits
			}

			/*
			 * Inits the custom variables object
			 */
			function getCustomVariablesFromCookie() {
				var cookieName = getCookieName('cvar'),
					cookie = getCookie(cookieName);

				if (cookie.length) {
					cookie = JSON2.parse(cookie);
					if (isObject(cookie)) {
						return cookie;
					}
				}
				return {};
			}

			/*
			 * Lazy loads the custom variables from the cookie, only once during this page view
			 */
			function loadCustomVariables() {
				if (customVariables === false) {
					customVariables = getCustomVariablesFromCookie();
				}
			}

			/*
			 * Process all "activity" events.
			 * For performance, this function must have low overhead.
			 */
			function activityHandler() {
				var now = new Date();

				lastActivityTime = now.getTime();
			}

			/*
			 * Sets the Visitor ID cookie: either the first time loadVisitorIdCookie is called
			 * or when there is a new visit or a new page view
			 */
			function setVisitorIdCookie(uuid, createTs, visitCount, nowTs, lastVisitTs, lastEcommerceOrderTs) {
				setCookie(getCookieName('id'), uuid + '.' + createTs + '.' + visitCount + '.' + nowTs + '.' + lastVisitTs + '.' + lastEcommerceOrderTs, configVisitorCookieTimeout, configCookiePath, configCookieDomain, cookieSecure);
			}
			
			/*
			 * Load visitor ID cookie
			 */
			function loadVisitorIdCookie() {
				var now = new Date(),
					nowTs = Math.round(now.getTime() / 1000),
					id = getCookie(getCookieName('id')),
					tmpContainer;

				if (id) {
					tmpContainer = id.split('.');

					// returning visitor flag
					tmpContainer.unshift('0');
				} else {
					// uuid - generate a pseudo-unique ID to fingerprint this user;
					// note: this isn't a RFC4122-compliant UUID
					if (!visitorUUID) {
						visitorUUID = hash(
							(navigatorAlias.userAgent || '') +
								(navigatorAlias.platform || '') +
								JSON2.stringify(browserFeatures) + nowTs
						).slice(0, 16); // 16 hexits = 64 bits
					}

					tmpContainer = [
						// new visitor
						'1',

						// uuid
						visitorUUID,

						// creation timestamp - seconds since Unix epoch
						nowTs,

						// visitCount - 0 = no previous visit
						0,

						// current visit timestamp
						nowTs,

						// last visit timestamp - blank = no previous visit
						'',

						// last ecommerce order timestamp
						''
					];
				}
				return tmpContainer;
			}
			
			/*
			 * Load CA ID cookie
			 */
			function loadCaIdCookie() {
				var now = new Date(),
					nowTs = Math.round(now.getTime() / 1000),
					id = getCookie(_ca_caam_key),
					tmpContainer;

				if (id) {
					tmpContainer = id.split('.');
					
					
					// returning visitor flag
					tmpContainer.unshift('0');
					
					//tmpContainer[5] = tmpContainer[4];
				} else {
					// uuid - generate a pseudo-unique ID to fingerprint this user;
					// note: this isn't a RFC4122-compliant UUID
					if (!_ca_UUID) {
						_ca_UUID = hash(String(Math.random()).slice(2, 8) + _ca_configAppId).slice(0, 16); // 16 hexits = 64 bits
					}

					tmpContainer = [
						// new visitor
						'1',

						// uuid
						_ca_UUID,

						// creation timestamp - seconds since Unix epoch
						nowTs,

						// last visit timestamp - blank = no previous visit
						'',
						
						// current visit timestamp
						nowTs,
						
						// visitCount - 0 = no previous visit
						0
					];
				}
				return tmpContainer;
			}
			
			//获取session cookie的值，如果没有，根据caam的id标识创建
			function loadCaSessionCookie(caamId){
				var sesCookie = getCookie(_ca_casn_key);
				
				if(sesCookie){
					return sesCookie;
				}else{
					var nowTs = Math.round(new Date().getTime() / 1000);
					return caamId + '.' + hash(String(Math.random()).slice(2, 8) + nowTs).slice(0, 16);
				}
			}

			/*
			 * Loads the referrer attribution information
			 *
			 * @returns array
			 *  0: campaign name
			 *  1: campaign keyword
			 *  2: timestamp
			 *  3: raw URL
			 */
			function loadReferrerAttributionCookie() {
				// NOTE: if the format of the cookie changes,
				// we must also update JS tests, PHP tracker, Integration tests,
				// and notify other tracking clients (eg. Java) of the changes
				var cookie = getCookie(getCookieName('ref'));

				if (cookie.length) {
					try {
						cookie = JSON2.parse(cookie);
						if (isObject(cookie)) {
							return cookie;
						}
					} catch (err) {
						// Pre 1.3, this cookie was not JSON encoded
					}
				}
				return [
					'',
					'',
					0,
					''
				];
			}
			
			function isEmptyObject(o){
				for (var x in o){
					return false;
				}
				return true;
			}
			
			function getOrid(){
				var pathname = window.location.pathname;
				if(!pathname){
					return "/";
				}
				return pathname + (window.location.search ? window.location.search : ""); 
			}

			/*
			 * Returns the URL to call piwik.php,
			 * with the standard parameters (plugins, resolution, url, referrer, etc.).
			 * Sends the pageview and browser settings with every request in case of race conditions.
			 */
			function getRequest(request, customData, pluginMethod) {
				if(!customData){
					customData = {};
				}
				var i,
					now = new Date(),
					nowTs = Math.round(now.getTime() / 1000),
					newVisitor,
					uuid,
					visitCount,
					createTs,
					currentVisitTs,
					lastVisitTs,
					lastEcommerceOrderTs,
					
					ca_newVisitor,//CALIS相关的
					ca_uuid,
					ca_visitCount,//_caam未过期内的访问次数
					ca_createTs,
					ca_currentVisitTs,
					ca_lastVisitTs,
					
					referralTs,
					referralUrl,
					referralUrlMaxLength = 1024,
					currentReferrerHostName,
					originalReferrerHostName,
					customVariablesCopy = customVariables,
					idname = getCookieName('id'),
					sesname = getCookieName('ses'),
					refname = getCookieName('ref'),
					cvarname = getCookieName('cvar'),
					id = loadVisitorIdCookie(),
					ca_id = loadCaIdCookie(),
					ses = getCookie(sesname),
					ca_ses = getCookie(_ca_casn_key),
					attributionCookie = loadReferrerAttributionCookie(),
					currentUrl = configCustomUrl || locationHrefAlias;

				if (configDoNotTrack) {
					setCookie(idname, '', -1, configCookiePath, configCookieDomain);
					setCookie(sesname, '', -1, configCookiePath, configCookieDomain);
					setCookie(cvarname, '', -1, configCookiePath, configCookieDomain);
					setCookie(refname, '', -1, configCookiePath, configCookieDomain);
					return '';
				}
				
				//CALIS相关的
				ca_newVisitor = ca_id[0];
				ca_uuid = ca_id[1];
				ca_createTs = ca_id[2];
				ca_visitCount = ca_id[5];
				ca_currentVisitTs = ca_id[4];
				ca_lastVisitTs = ca_id[3];
				
				ca_visitCount++;//访问计数增加
				
				/**
				 * 如果session cookie不存在，则：
				 * 		更新id cookie的Session startTs为当前时间
				 * 		更新id cookie的Session CurentTs为当前时间
				 * 		更新id cookie的Session LastTs为当前时间
				 * 否则：
				 * 		id cookie的Session startTs不变
				 * 		id cookie的Session CurentTs为当前时间
				 * 		更新id cookie的Session LastTs为上一次访问时间
				 */
				if(!ca_ses){
					ca_createTs = ca_currentVisitTs = ca_lastVisitTs = nowTs;
				}else{
					ca_currentVisitTs = nowTs;
					ca_lastVisitTs = ca_id[4];
				}
				

				/********************* 构建采集的数据json对象、创建cookie ***********************/
				var ca_clientData = {};
				//data model 的"结果"(Result)中计数相关的参数
				ca_clientData.rsc = (getCookie(_ca_casn_key) && getCookie(_ca_casc_key) ? '0' : '1');
				ca_clientData.rnc = (getCookie(_ca_caam_key) ? '0' : '1');
				
				//更新(包括创建)CALIS相关的cookie
				setCookie(_ca_caam_key, 
						ca_uuid + '.' + ca_createTs + '.'  + ca_lastVisitTs + '.' + ca_currentVisitTs + '.' +  ca_visitCount, 
						ca_caam_CookieTimeout, configCookiePath, configCookieDomain, cookieSecure);
				var sesCookieV = loadCaSessionCookie(ca_uuid);
				setCookie(_ca_casn_key, sesCookieV, ca_casn_SessionCookieTimeout, configCookiePath, configCookieDomain, cookieSecure);
				setCookie(_ca_casc_key, ca_uuid, ca_casc_CookieTimeout, configCookiePath, configCookieDomain, cookieSecure);
				
				//与cookie相关的参数
				ca_clientData.ssn = sesCookieV.split('.')[1];
				ca_clientData._caam = getCookie(_ca_caam_key);
				
				//浏览器相关的参数
				for(var p in browserFeatures){
					ca_clientData[p] = browserFeatures[p];
				}
				//用户参数op的不同值情况
				if(!customData.op || customData.op == 'v'){
					ca_clientData.lfrm = document.referrer;
					ca_clientData.op = "v";
					ca_clientData.orid = getOrid();
					ca_clientData.otil = document.title;
					ca_clientData.ort = "p";
					ca_clientData.rvc = "1";
				}
				if(customData.op && customData.op != 'v'){
					if(!customData.otil || !customData.ort || !customData.orid){
						alert('op值不为v时,必须同时提供3个参数：otil、ort、orid!');
						throw "paras config error";
					}
					ca_clientData.lfrm = window.location.href;
					ca_clientData.rvc = "1";
				}
				for(var p in customData){
					ca_clientData[p] = customData[p];
				}
				/********************* 构建采集的数据json对象、创建cookie结束 ***********************/
				
				request = getRequestString(ca_clientData);
				_ca_configAppId = null;//用户每次log时都需要重新设置oaid
				
				return request;
			}
			
			/**
			 * 拼接url字符串，同时截取各个参数的长度
			 * @param ca_clientData
			 * @returns
			 */
			function getRequestString(ca_clientData){
				var limit = {
					sorg:	20,
					sten:	20,
					sid:	20,
					st:		20,
					op:		20,
					oaid:	32,
					ocrt: 	20,
					osub: 	20,
					opub: 	50,
					ocor:	20,
					odt:	20,
					ofmt: 	20,
					osrc: 	20,
					oln: 	20,
					orel:	50,
					rrs: 	20,
					orid: 	1024
				};
				var rt = "";
				for (var p in ca_clientData) {
					if(limit[p] && ca_clientData[p].length > limit[p]){
						ca_clientData[p] = ca_clientData[p].substring(0,limit[p]);
					}
					rt += '&' + p + '=' + encodeURIComponent(ca_clientData[p]);
				}
				rt += "&v=" + calis.cal.ver;
				
				return rt.substring(1);
			}
			
			/*
			 * Log the page view / visit
			 */
			function logPageView(customTitle, customData) {
				var now = new Date(),
					request = getRequest('', customData, 'log');

				sendRequest(request, configTrackerPause);

				// send ping
				if (configMinimumVisitTime && configHeartBeatTimer && !activityTrackingInstalled) {
					activityTrackingInstalled = true;

					// add event handlers; cross-browser compatibility here varies significantly
					// @see http://quirksmode.org/dom/events
					addEventListener(documentAlias, 'click', activityHandler);
					addEventListener(documentAlias, 'mouseup', activityHandler);
					addEventListener(documentAlias, 'mousedown', activityHandler);
					addEventListener(documentAlias, 'mousemove', activityHandler);
					addEventListener(documentAlias, 'mousewheel', activityHandler);
					addEventListener(windowAlias, 'DOMMouseScroll', activityHandler);
					addEventListener(windowAlias, 'scroll', activityHandler);
					addEventListener(documentAlias, 'keypress', activityHandler);
					addEventListener(documentAlias, 'keydown', activityHandler);
					addEventListener(documentAlias, 'keyup', activityHandler);
					addEventListener(windowAlias, 'resize', activityHandler);
					addEventListener(windowAlias, 'focus', activityHandler);
					addEventListener(windowAlias, 'blur', activityHandler);

					// periodic check for activity
					lastActivityTime = now.getTime();
					setTimeout(function heartBeat() {
						var now = new Date(),
							request;

						// there was activity during the heart beat period;
						// on average, this is going to overstate the visitDuration by configHeartBeatTimer/2
						if ((lastActivityTime + configHeartBeatTimer) > now.getTime()) {
							// send ping if minimum visit time has elapsed
							if (configMinimumVisitTime < now.getTime()) {
								request = getRequest('ping=1', customData, 'ping');

								sendRequest(request, configTrackerPause);
							}

							// resume heart beat
							setTimeout(heartBeat, configHeartBeatTimer);
						}
						// else heart beat cancelled due to inactivity
					}, configHeartBeatTimer);
				}
			}

			/*
			 * Log the goal with the server
			 */
			function logGoal(idGoal, customRevenue, customData) {
				var request = getRequest('idgoal=' + idGoal + (customRevenue ? '&revenue=' + customRevenue : ''), customData, 'goal');

				sendRequest(request, configTrackerPause);
			}

			/*
			 * Log the link or click with the server
			 */
			function logLink(url, linkType, customData) {
				var request = getRequest(linkType + '=' + encodeWrapper(purify(url)), customData, 'link');

				sendRequest(request, configTrackerPause);
			}

			/*
			 * Construct regular expression of classes
			 */
			function getClassesRegExp(configClasses, defaultClass) {
				var i,
					classesRegExp = '(^| )(piwik[_-]' + defaultClass;

				if (configClasses) {
					for (i = 0; i < configClasses.length; i++) {
						classesRegExp += '|' + configClasses[i];
					}
				}
				classesRegExp += ')( |$)';

				return new RegExp(classesRegExp);
			}

			/*
			 * Link or Download?
			 */
			function getLinkType(className, href, isInLink) {
				// outlinks
				if (!isInLink) {
					return 'link';
				}

				// does class indicate whether it is an (explicit/forced) outlink or a download?
				var downloadPattern = getClassesRegExp(configDownloadClasses, 'download'),
					linkPattern = getClassesRegExp(configLinkClasses, 'link'),

					// does file extension indicate that it is a download?
					downloadExtensionsPattern = new RegExp('\\.(' + configDownloadExtensions + ')([?&#]|$)', 'i');

				// optimization of the if..elseif..else construct below
				return linkPattern.test(className) ? 'link' : (downloadPattern.test(className) || downloadExtensionsPattern.test(href) ? 'download' : 0);

/*
				var linkType;

				if (linkPattern.test(className)) {
					// class attribute contains 'piwik_link' (or user's override)
					linkType = 'link';
				} else if (downloadPattern.test(className)) {
					// class attribute contains 'piwik_download' (or user's override)
					linkType = 'download';
				} else if (downloadExtensionsPattern.test(sourceHref)) {
					// file extension matches a defined download extension
					linkType = 'download';
				} else {
					// otherwise none of the above
					linkType = 0;
				}

				return linkType;
 */
			}

			/*
			 * Process clicks
			 */
			function processClick(sourceElement) {
				var parentElement,
					tag,
					linkType;

				while ((parentElement = sourceElement.parentNode) !== null &&
						isDefined(parentElement) && // buggy IE5.5
						((tag = sourceElement.tagName.toUpperCase()) !== 'A' && tag !== 'AREA')) {
					sourceElement = parentElement;
				}

				if (isDefined(sourceElement.href)) {
					// browsers, such as Safari, don't downcase hostname and href
					var originalSourceHostName = sourceElement.hostname || getHostName(sourceElement.href),
						sourceHostName = originalSourceHostName.toLowerCase(),
						sourceHref = sourceElement.href.replace(originalSourceHostName, sourceHostName),
						scriptProtocol = new RegExp('^(javascript|vbscript|jscript|mocha|livescript|ecmascript):', 'i');

					// ignore script pseudo-protocol links
					if (!scriptProtocol.test(sourceHref)) {
						// track outlinks and all downloads
						linkType = getLinkType(sourceElement.className, sourceHref, isSiteHostName(sourceHostName));
						if (linkType) {
							// urldecode %xx
							sourceHref = urldecode(sourceHref);
							logLink(sourceHref, linkType);
						}
					}
				}
			}

			/*
			 * Handle click event
			 */
			function clickHandler(evt) {
				var button,
					target;

				evt = evt || windowAlias.event;
				button = evt.which || evt.button;
				target = evt.target || evt.srcElement;

				// Using evt.type (added in IE4), we avoid defining separate handlers for mouseup and mousedown.
				if (evt.type === 'click') {
					if (target) {
						processClick(target);
					}
				} else if (evt.type === 'mousedown') {
					if ((button === 1 || button === 2) && target) {
						lastButton = button;
						lastTarget = target;
					} else {
						lastButton = lastTarget = null;
					}
				} else if (evt.type === 'mouseup') {
					if (button === lastButton && target === lastTarget) {
						processClick(target);
					}
					lastButton = lastTarget = null;
				}
			}

			/*
			 * Add click listener to a DOM element
			 */
			function addClickListener(element, enable) {
				if (enable) {
					// for simplicity and performance, we ignore drag events
					addEventListener(element, 'mouseup', clickHandler, false);
					addEventListener(element, 'mousedown', clickHandler, false);
				} else {
					addEventListener(element, 'click', clickHandler, false);
				}
			}

			/*
			 * Add click handlers to anchor and AREA elements, except those to be ignored
			 */
			function addClickListeners(enable) {
				if (!linkTrackingInstalled) {
					linkTrackingInstalled = true;

					// iterate through anchor elements with href and AREA elements

					var i,
						ignorePattern = getClassesRegExp(configIgnoreClasses, 'ignore'),
						linkElements = documentAlias.links;

					if (linkElements) {
						for (i = 0; i < linkElements.length; i++) {
							if (!ignorePattern.test(linkElements[i].className)) {
								addClickListener(linkElements[i], enable);
							}
						}
					}
				}
			}

			/*
			 * Browser features (plugins, resolution, cookies)
			 */
			function detectBrowserFeatures() {
				var i,
					mimeType,
					pluginMap = {
						// document types
						lbpp: 'application/pdf',

						// media players
						lbpq: 'video/quicktime',
						lbpm: 'video/x-ms-wm',//windows media

						// interactive multimedia
						lbpd: 'application/x-director',

						// RIA
						//java: 'application/x-java-vm',//去除"是否支持Java"
						lbpg: 'application/x-googlegears',
						lbps: 'application/x-silverlight'
					};

				// general plugin detection
				if (navigatorAlias.mimeTypes && navigatorAlias.mimeTypes.length) {
					for (i in pluginMap) {
						if (Object.prototype.hasOwnProperty.call(pluginMap, i)) {
							mimeType = navigatorAlias.mimeTypes[pluginMap[i]];
							browserFeatures[i] = (mimeType && mimeType.enabledPlugin) ? '1' : '0';
						}
					}
				}

				// Safari and Opera
				// IE6/IE7 navigator.javaEnabled can't be aliased, so test directly
				/*if (typeof navigator.javaEnabled !== 'unknown' &&
						isDefined(navigatorAlias.javaEnabled) &&
						navigatorAlias.javaEnabled()) {
					browserFeatures.java = '1';
				}*/

				// Firefox
				if (isFunction(windowAlias.GearsFactory)) {
					browserFeatures.lbpg = '1';
				}

				// other browser features
				browserFeatures.lsr = screenAlias.width + 'x' + screenAlias.height;
				browserFeatures.oadm = window.location.host ? window.location.host : "";
				browserFeatures.ldev = paraDev;
				
				//other Calis-needed featurs
				setOtherFeaturs(browserFeatures);
			}
			
			var paraDev = (function(){
				var deviceIphone = "iphone";
				var deviceIpod = "ipod";
				var devicePalm = "palm";
				var deviceS60 = "series60";
				var deviceSymbian = "symbian";
				var engineWebKit = "webkit";
				var deviceAndroid = "android";
				var deviceWinMob = "windows ce";
				var deviceWinPhone = "windows phone";
				var deviceBB = "blackberry";
				//Initialize our user agent string to lower case.
				var uagent = navigator.userAgent.toLowerCase();
				//**************************
				// Detects if the current device is an iPhone.
				function DetectIphone(){
					if (uagent.search(deviceIphone) > -1)
						return true;
					else
						return false;
				}
				//**************************
				// Detects if the current device is an iPod Touch.
				function DetectIpod(){
					if (uagent.search(deviceIpod) > -1)
						return true;
					else
						return false;
				}
				//**************************
				// Detects if the current device is an iPhone or iPod Touch.
				function DetectIphoneOrIpod(){
					if (DetectIphone())
						return true;
					else if (DetectIpod())
						return true;
					else
						return false;
				}
				//**************************
				// Detects if the current browser is the S60 Open Source Browser.
				// Screen out older devices and the old WML browser.
				function DetectS60OssBrowser(){
					if (uagent.search(engineWebKit) > -1)
					{
						if ((uagent.search(deviceS60) > -1 ||
						uagent.search(deviceSymbian) > -1))
							return true;
						else
							return false;
					}
					else
						return false;
				}
				//**************************
				// Detects if the current device is an Android OS-based device.
				function DetectAndroid(){
					if (uagent.search(deviceAndroid) > -1)
						return true;
					else
						return false;
				}
				//**************************
				// Detects if the current device is an Android OS-based device and
				//   the browser is based on WebKit.
				function DetectAndroidWebKit(){
					if (DetectAndroid())
					{
						if (DetectWebkit())
							return true;
						else
							return false;
					}
					else
						return false;
				}
				//**************************
				// Detects if the current browser is a Windows Mobile device.
				function DetectWindowsMobile(){
					if (uagent.search(deviceWinMob) > -1)
						return true;
					else
						return false;
				}
				//**************************
				// Detects if the current browser is a Windows Phone OS 7+ Mobile device.
				function DetectWindowsPhone(){
					if (uagent.search(deviceWinPhone) > -1)
						return true;
					else
						return false;
				}
				//**************************
				// Detects if the current browser is a BlackBerry of some sort.
				function DetectBlackBerry(){
					if (uagent.search(deviceBB) > -1)
						return true;
					else
						return false;
				}
				//**************************
				// Detects if the current browser is on a PalmOS device.
				function DetectPalmOS(){
					if (uagent.search(devicePalm) > -1)
						return true;
					else
						return false;
				}
				function DetectMobileOS(){
					if(DetectIphone()){
						return deviceIphone;
					}else if(DetectIpod()){
						return deviceIpod;
					}else if(DetectS60OssBrowser()){
						return deviceSymbian;
					}else if(DetectAndroid()){
						return deviceAndroid;
					}else if(DetectWindowsMobile()){
						return deviceWinMob;
					}else if(DetectBlackBerry()){
						return deviceBB;
					}else if(DetectPalmOS()){
						return devicePalm;
					}else{
						return "pc";
					}
				}		
				return DetectMobileOS();
			}());
			
			var deployJava = function () {
		        var rv = {
		            debug: null,
		            firefoxJavaVersion: null,
		            myInterval: null,
		            preInstallJREList: null,
		            returnPage: null,
		            brand: null,
		            locale: null,
		            installType: null,
		            EAInstallEnabled: false,
		            EarlyAccessURL: null,
		            // GetJava page
		            getJavaURL: 'http://java.sun.com/webapps/getjava/BrowserRedirect?host=java.com',
		            // Apple redirect page
		            appleRedirectPage: 'http://www.apple.com/support/downloads/',
		            // mime-type of the DeployToolkit plugin object
		            oldMimeType: 'application/npruntime-scriptable-plugin;DeploymentToolkit',
		            mimeType: 'application/java-deployment-toolkit',
		            browserName: null,
		            browserName2: null,

		            getJREs: function () {
		                var list = new Array();
		                if (this.isPluginInstalled()) {
		                    var plugin = this.getPlugin();
		                    var VMs = plugin.jvms;
		                    for (var i = 0; i < VMs.getLength(); i++) {
		                        list[i] = VMs.get(i).version;
		                    }
		                } else {
		                    var browser = this.getBrowser();

		                    if (browser == 'MSIE') {
		                        if (this.testUsingActiveX('1.7.0')) {
		                            list[0] = '1.7.0';
		                        } else if (this.testUsingActiveX('1.6.0')) {
		                            list[0] = '1.6.0';
		                        } else if (this.testUsingActiveX('1.5.0')) {
		                            list[0] = '1.5.0';
		                        } else if (this.testUsingActiveX('1.4.2')) {
		                            list[0] = '1.4.2';
		                        } else if (this.testForMSVM()) {
		                            list[0] = '1.1';
		                        }
		                    } else if (browser == 'Netscape Family') {
		                        this.getJPIVersionUsingMimeType();
		                        if (this.firefoxJavaVersion != null) {
		                            list[0] = this.firefoxJavaVersion;
		                        } else if (this.testUsingMimeTypes('1.7')) {
		                            list[0] = '1.7.0';
		                        } else if (this.testUsingMimeTypes('1.6')) {
		                            list[0] = '1.6.0';
		                        } else if (this.testUsingMimeTypes('1.5')) {
		                            list[0] = '1.5.0';
		                        } else if (this.testUsingMimeTypes('1.4.2')) {
		                            list[0] = '1.4.2';
		                        } else if (this.browserName2 == 'Safari') {
		                            if (this.testUsingPluginsArray('1.7.0')) {
		                                list[0] = '1.7.0';
		                            } else if (this.testUsingPluginsArray('1.6')) {
		                                list[0] = '1.6.0';
		                            } else if (this.testUsingPluginsArray('1.5')) {
		                                list[0] = '1.5.0';
		                            } else if (this.testUsingPluginsArray('1.4.2')) {
		                                list[0] = '1.4.2';
		                            }
		                        }
		                    }
		                }
		                if (this.debug) {
		                    for (var i = 0; i < list.length; ++i) {
		                        alert('We claim to have detected Java SE ' + list[i]);
		                    }
		                }
		                return list.length > 0 ? list[0] : "0";
		            },
		            
		            getJPIVersionUsingMimeType: function() {
		                // Walk through the full list of mime types.
		                for (var i = 0; i < navigator.mimeTypes.length; ++i) {
		                    var s = navigator.mimeTypes[i].type;
		                    // The jpi-version is the plug-in version.  This is the best
		                    // version to use.
		                    var m = s.match(/^application\/x-java-applet;jpi-version=(.*)$/);
		                    if (m != null) {
		                        this.firefoxJavaVersion = m[1];
		                        // Opera puts the latest sun JRE last not first
		                        if ('Opera' != this.browserName2) {
		                            break;
		                        }
		                    }
		                }
		            },

		            /*
		             * returns true if the ActiveX or XPI plugin is installed
		             */
		            isPluginInstalled: function () {
		                var plugin = this.getPlugin();
		                if (plugin && plugin.jvms) {
		                    return true;
		                } else {
		                    return false;
		                }
		            },

		            allowPlugin: function () {
		                this.getBrowser();

		                // Safari and Opera browsers find the plugin but it
		                // doesn't work, so until we can get it to work - don't use it.
		                var ret = ('Safari' != this.browserName2 && 'Opera' != this.browserName2);

		                return ret;
		            },

		            getPlugin: function () {
		                this.refresh();

		                var ret = null;
		                if (this.allowPlugin()) {
		                    ret = document.getElementById('deployJavaPlugin');
		                }
		                return ret;
		            },

		            getBrowser: function () {
		                if (this.browserName == null) {
		                    var browser = navigator.userAgent.toLowerCase();

		                    if (this.debug) {
		                        alert('userAgent -> ' + browser);
		                    }
		                    // order is important here.  Safari userAgent contains mozilla,
		                    // and Chrome userAgent contains both mozilla and safari.
		                    if (browser.indexOf('msie') != -1) {
		                        this.browserName = 'MSIE';
		                        this.browserName2 = 'MSIE';
		                    } else if (browser.indexOf('iphone') != -1) {
		                        // this included both iPhone and iPad
		                        this.browserName = 'Netscape Family';
		                        this.browserName2 = 'iPhone';
		                    } else if (browser.indexOf('firefox') != -1) {
		                        this.browserName = 'Netscape Family';
		                        this.browserName2 = 'Firefox';
		                    } else if (browser.indexOf('chrome') != -1) {
		                        this.browserName = 'Netscape Family';
		                        this.browserName2 = 'Chrome';
		                    } else if (browser.indexOf('safari') != -1) {
		                        this.browserName = 'Netscape Family';
		                        this.browserName2 = 'Safari';
		                    } else if (browser.indexOf('mozilla') != -1) {
		                        this.browserName = 'Netscape Family';
		                        this.browserName2 = 'Other';
		                    } else if (browser.indexOf('opera') != -1) {
		                        this.browserName = 'Netscape Family';
		                        this.browserName2 = 'Opera';
		                    } else {
		                        this.browserName = '?';
		                        this.browserName2 = 'unknown';
		                    }

		                    if (this.debug) {
		                        alert('Detected browser name:' + this.browserName + ', ' + this.browserName2);
		                    }
		                }
		                return this.browserName;
		            },

		            IEInstall: function () {
		                location.href = this.getJavaURL + ((this.returnPage != null) ? ('&returnPage=' + this.returnPage) : '') + ((this.locale != null) ? ('&locale=' + this.locale) : '') + ((this.brand != null) ? ('&brand=' + this.brand) : '');
		                // should not actually get here
		                return false;
		            },

		            done: function (name, result) {},

		            FFInstall: function () {
		                location.href = this.getJavaURL + ((this.returnPage != null) ? ('&returnPage=' + this.returnPage) : '') + ((this.locale != null) ? ('&locale=' + this.locale) : '') + ((this.brand != null) ? ('&brand=' + this.brand) : '') + ((this.installType != null) ? ('&type=' + this.installType) : '');
		                // should not actually get here
		                return false;
		            },

		            enableAlerts: function () {
		                // reset this so we can show the browser detection
		                this.browserName = null;
		                this.debug = true;
		            },

		            writePluginTag: function () {
		                var browser = this.getBrowser();

		                if (browser == 'MSIE') {
		                    document.write('<' + 'object classid="clsid:CAFEEFAC-DEC7-0000-0000-ABCDEFFEDCBA" ' + 'id="deployJavaPlugin" width="0" height="0">' + '<' + '/' + 'object' + '>');
		                } else if (browser == 'Netscape Family' && this.allowPlugin()) {
		                    this.writeEmbedTag();
		                }
		            },

		            refresh: function () {
		                navigator.plugins.refresh(false);

		                var browser = this.getBrowser();
		                if (browser == 'Netscape Family' && this.allowPlugin()) {
		                    var plugin = document.getElementById('deployJavaPlugin');
		                    // only do this again if no plugin
		                    if (plugin == null) {
		                        this.writeEmbedTag();
		                    }
		                }
		            },

		            writeEmbedTag: function () {
		                var written = false;
		                if (navigator.mimeTypes != null) {
		                    for (var i = 0; i < navigator.mimeTypes.length; i++) {
		                        if (navigator.mimeTypes[i].type == this.mimeType) {
		                            if (navigator.mimeTypes[i].enabledPlugin) {
		                               // document.write('<' + 'embed id="deployJavaPlugin" type="' + this.mimeType + '" hidden="true" />');
		                                var embed = document.createElement('embed');
		                                embed.setAttribute('id','deployJavaPlugin');
		                                embed.setAttribute('type',this.mimeType);
		                                embed.setAttribute('hidden',true);
		                                document.body.appendChild(embed);
		                                written = true;
		                            }
		                        }
		                    }
		                    // if we ddn't find new mimeType, look for old mimeType
		                    if (!written) for (var i = 0; i < navigator.mimeTypes.length; i++) {
		                        if (navigator.mimeTypes[i].type == this.oldMimeType) {
		                            if (navigator.mimeTypes[i].enabledPlugin) {
		                                //document.write('<' + 'embed id="deployJavaPlugin" type="' + this.oldMimeType + '" hidden="true" />');
		                                var embed = document.createElement('embed');
		                                embed.setAttribute('id','deployJavaPlugin');
		                                embed.setAttribute('type',this.oldMimeType);
		                                embed.setAttribute('hidden',true);
		                                document.body.appendChild(embed);
		                            }
		                        }
		                    }
		                }
		            }
		        }; // deployJava object
		        rv.writePluginTag();
		        if (rv.locale == null) {
		            var loc = null;

		            if (loc == null) try {
		                loc = navigator.userLanguage;
		            } catch (err) {}

		            if (loc == null) try {
		                loc = navigator.systemLanguage;
		            } catch (err) {}

		            if (loc == null) try {
		                loc = navigator.language;
		            } catch (err) {}

		            if (loc != null) {
		                loc.replace("-", "_")
		                rv.locale = loc;
		            }
		        }

		        return rv;
		    }();
			
			function setOtherFeaturs(browserFeatures){
				var BrowserDetect = {
					init: function () {
						this.browser = this.searchString(this.dataBrowser) || "An unknown browser";
						this.version = this.searchVersion(navigator.userAgent)
							|| this.searchVersion(navigator.appVersion)
							|| "an unknown version";
						//this.OS = this.searchString(this.dataOS) || "an unknown OS",
						this.OS = this.osInfo(),
						this.flv = this.flashVersion();
					},
					flashVersion:function getFlashVersion(){
						  // ie
						  try {
						    try {
						      // avoid fp6 minor version lookup issues
						      // see: http://blog.deconcept.com/2006/01/11/getvariable-setvariable-crash-internet-explorer-flash-6/
						      var axo = new ActiveXObject('ShockwaveFlash.ShockwaveFlash.6');
						      try { 
						    	  axo.AllowScriptAccess = 'always'; 
						      }catch(e) { 
						    	  return '6.0.0'; 
						      }
						    } catch(e) {}
						    return new ActiveXObject('ShockwaveFlash.ShockwaveFlash').GetVariable('$version').replace(/\D+/g, '.').match(/^.?(.+).?$/)[1];
						  // other browsers
						  } catch(e) {
						    try {
						    	if(navigator.mimeTypes["application/x-shockwave-flash"].enabledPlugin){
						    		return (navigator.plugins["Shockwave Flash 2.0"] || navigator.plugins["Shockwave Flash"]).description.replace(/\D+/g, ".").match(/^.?(.+).?$/)[1];
						    	}
						    } catch(e) {}
						  }
					  	return '0';
					},
					osInfo:function(){
						var OSName="";
						//The below few line of code will find the OS name
						if (navigator.appVersion.indexOf("Win")!=-1) OSName="Windows";
						if (navigator.appVersion.indexOf("Mac")!=-1) OSName="MacOS";
						if (navigator.appVersion.indexOf("X11")!=-1) OSName="UNIX";
						if (navigator.appVersion.indexOf("Linux")!=-1) OSName="Linux";
						
						var sUA=navigator.userAgent.toLowerCase();
						var OSVer="";
						if (sUA.indexOf("Mac OS X 10.4")!=-1) OSVer="Tiger";
						if (sUA.indexOf("Mac OS X 10.5")!=-1) OSVer="Leopard";
						if (sUA.indexOf("Mac OS X 10.6")!=-1) OSVer="Snow Leopard";
						if((sUA.indexOf("win98")!=-1) ||(sUA.indexOf("windows 98")!=-1)) OSVer = "98";
						if((sUA.indexOf('nt 5.0')!=-1)||(sUA.indexOf('2000')!=-1)) OSVer = "2000";
						if (sUA.indexOf("nt 5.1")!=-1 ||(sUA.indexOf("XP")!=-1)) OSVer="XP";
						if (sUA.indexOf("nt 6.0")!=-1) OSVer="Vista";
						if (sUA.indexOf("nt 6.1")!=-1) OSVer="7";
						
						return {'os' : OSName, 'ver': OSVer};
					},
					searchString: function (data) {
						for (var i=0;i<data.length;i++)	{
							var dataString = data[i].string;
							var dataProp = data[i].prop;
							this.versionSearchString = data[i].versionSearch || data[i].identity;
							if (dataString) {
								if (dataString.indexOf(data[i].subString) != -1)
									return data[i].identity;
							}
							else if (dataProp)
								return data[i].identity;
						}
					},
					searchVersion: function (dataString) {
						var index = dataString.indexOf(this.versionSearchString);
						if (index == -1) return;
						return parseFloat(dataString.substring(index+this.versionSearchString.length+1));
					},
					dataBrowser: [
						{
							string: navigator.userAgent,
							subString: "Chrome",
							identity: "Chrome"
						},
						{ 	string: navigator.userAgent,
							subString: "OmniWeb",
							versionSearch: "OmniWeb/",
							identity: "OmniWeb"
						},
						{
							string: navigator.vendor,
							subString: "Apple",
							identity: "Safari",
							versionSearch: "Version"
						},
						{
							prop: window.opera,
							identity: "Opera",
							versionSearch: "Version"
						},
						{
							string: navigator.vendor,
							subString: "iCab",
							identity: "iCab"
						},
						{
							string: navigator.vendor,
							subString: "KDE",
							identity: "Konqueror"
						},
						{
							string: navigator.userAgent,
							subString: "Firefox",
							identity: "Firefox"
						},
						{
							string: navigator.vendor,
							subString: "Camino",
							identity: "Camino"
						},
						{		// for newer Netscapes (6+)
							string: navigator.userAgent,
							subString: "Netscape",
							identity: "Netscape"
						},
						{
							string: navigator.userAgent,
							subString: "MSIE",
							identity: "MSIE",
							versionSearch: "MSIE"
						},
						{
							string: navigator.userAgent,
							subString: "Gecko",
							identity: "Mozilla",
							versionSearch: "rv"
						},
						{ 		// for older Netscapes (4-)
							string: navigator.userAgent,
							subString: "Mozilla",
							identity: "Netscape",
							versionSearch: "Mozilla"
						}
					]
				};
				BrowserDetect.init();
				
				var javaVer;
				try{
					javaVer = deployJava.getJREs();
				}catch(e){
					javaVer = "";
				};
				browserFeatures.los = BrowserDetect.OS.os;
				browserFeatures.losv = BrowserDetect.OS.ver;
				browserFeatures.lbt = BrowserDetect.browser;
				browserFeatures.lbv = BrowserDetect.version;
				browserFeatures.lbl = navigator.language || navigator.browserLanguage;
				browserFeatures.lbpj = javaVer;
				browserFeatures.lbpf = BrowserDetect.flv;
				
//				for(var p in browserFeatures){//去除不支持（值为零）的插件参数
//					if(parseInt(browserFeatures[p]) == 0){
//						delete browserFeatures[p];
//					}
//				}
			}

/*<DEBUG>*/
			/*
			 * Register a test hook. Using eval() permits access to otherwise
			 * privileged members.
			 */
			function registerHook(hookName, userHook) {
				var hookObj = null;

				if (isString(hookName) && !isDefined(registeredHooks[hookName]) && userHook) {
					if (isObject(userHook)) {
						hookObj = userHook;
					} else if (isString(userHook)) {
						try {
							eval('hookObj =' + userHook);
						} catch (e) { }
					}

					registeredHooks[hookName] = hookObj;
				}
				return hookObj;
			}
/*</DEBUG>*/

			/************************************************************
			 * Constructor
			 ************************************************************/

			/*
			 * initialize tracker
			 */
			detectBrowserFeatures();
			updateDomainHash();

/*<DEBUG>*/
			/*
			 * initialize test plugin
			 */
			executePluginMethod('run', registerHook);
/*</DEBUG>*/

			/************************************************************
			 * Public data and methods
			 ************************************************************/

			return {
/*<DEBUG>*/
				/*
				 * Test hook accessors
				 */
				hook: registeredHooks,
				getHook: function (hookName) {
					return registeredHooks[hookName];
				},
/*</DEBUG>*/
				
				/**
				 * Get visitor ID (from first party cookie)
				 *
				 * @return string Visitor ID in hexits (or null, if not yet known)
				 */
				getVisitorId: function () {
					return (loadVisitorIdCookie())[1];
				},

				/**
				 * Get the visitor information (from first party cookie)
				 *
				 * @return array
				 */
				getVisitorInfo: function () {
					return loadVisitorIdCookie();
				},

				/**
				 * Get the Attribution information, which is an array that contains
				 * the Referrer used to reach the site as well as the campaign name and keyword
				 * It is useful only when used in conjunction with Tracker API function setAttributionInfo()
				 * To access specific data point, you should use the other functions getAttributionReferrer* and getAttributionCampaign*
				 *
				 * @return array Attribution array, Example use:
				 *   1) Call JSON2.stringify(piwikTracker.getAttributionInfo())
				 *   2) Pass this json encoded string to the Tracking API (php or java client): setAttributionInfo()
				 */
				getAttributionInfo: function () {
					return loadReferrerAttributionCookie();
				},

				/**
				 * Get the Campaign name that was parsed from the landing page URL when the visitor
				 * landed on the site originally
				 *
				 * @return string
				 */
				getAttributionCampaignName: function () {
					return loadReferrerAttributionCookie()[0];
				},

				/**
				 * Get the Campaign keyword that was parsed from the landing page URL when the visitor
				 * landed on the site originally
				 *
				 * @return string
				 */
				getAttributionCampaignKeyword: function () {
					return loadReferrerAttributionCookie()[1];
				},

				/**
				 * Get the time at which the referrer (used for Goal Attribution) was detected
				 *
				 * @return int Timestamp or 0 if no referrer currently set
				 */
				getAttributionReferrerTimestamp: function () {
					return loadReferrerAttributionCookie()[2];
				},

				/**
				 * Get the full referrer URL that will be used for Goal Attribution
				 *
				 * @return string Raw URL, or empty string '' if no referrer currently set
				 */
				getAttributionReferrerUrl: function () {
					return loadReferrerAttributionCookie()[3];
				},

				/**
				 * Specify the Piwik server URL
				 *
				 * @param string trackerUrl
				 */
				setTrackerUrl: function (trackerUrl) {
					configTrackerUrl = trackerUrl;
				},
				
				
				setAppId: function (appId) {
					_ca_configAppId = appId;
				},
				
				getAppId: function () {
					return _ca_configAppId;
				},

				/**
				 * Specify the site ID
				 *
				 * @param int|string siteId
				 */
				setSiteId: function (siteId) {
					configTrackerSiteId = siteId;
				},

				/**
				 * Pass custom data to the server
				 *
				 * Examples:
				 *   tracker.setCustomData(object);
				 *   tracker.setCustomData(key, value);
				 *
				 * @param mixed key_or_obj
				 * @param mixed opt_value
				 */
				setCustomData: function (key_or_obj, opt_value) {
					if (isObject(key_or_obj)) {
						configCustomData = key_or_obj;
					} else {
						if (!configCustomData) {
							configCustomData = [];
						}
						configCustomData[key_or_obj] = opt_value;
					}
				},

				/**
				 * Get custom data
				 *
				 * @return mixed
				 */
				getCustomData: function () {
					return configCustomData;
				},
				
				/**
				 * 将用户数据进行log
				 * @param obj 用户json值
				 * @returns
				 */
				logUserData: function(obj){
					this.trackPageView('',obj);
				},

				/**
				 * Set custom variable within this visit
				 *
				 * @param int index
				 * @param string name
				 * @param string value
				 * @param string scope Scope of Custom Variable:
				 *                     - "visit" will store the name/value in the visit and will persist it in the cookie for the duration of the visit,
				 *                     - "page" will store the name/value in the page view.
				 */
				setCustomVariable: function (index, name, value, scope) {
					var toRecord;
					if (!isDefined(scope)) {
						scope = 'visit';
					}
					if (index > 0) {
						toRecord = [name.slice(0, customVariableMaximumLength), value.slice(0, customVariableMaximumLength)];
						if (scope === 'visit' || scope === 2) { /* GA compatibility/misuse */
							loadCustomVariables();
							customVariables[index] = toRecord;
						} else if (scope === 'page' || scope === 3) { /* GA compatibility/misuse */
							customVariablesPage[index] = toRecord;
						}
					}
				},

				/**
				 * Get custom variable
				 *
				 * @param int index
				 * @param string scope Scope of Custom Variable: "visit" or "page"
				 */
				getCustomVariable: function (index, scope) {
					var cvar;
					if (!isDefined(scope)) {
						scope = "visit";
					}
					if (scope === "page" || scope === 3) {
						cvar = customVariablesPage[index];
					} else if (scope === "visit" || scope === 2) {
						loadCustomVariables();
						cvar = customVariables[index];
					}
					if (!isDefined(cvar)
							|| (cvar && cvar[0] === '')) {
						return false;
					}
					return cvar;
				},

				/**
				 * Delete custom variable
				 *
				 * @param int index
				 */
				deleteCustomVariable: function (index, scope) {
					// Only delete if it was there already
					if (this.getCustomVariable(index, scope)) {
						this.setCustomVariable(index, '', '', scope);
					}
				},

				/**
				 * Set delay for link tracking (in milliseconds)
				 *
				 * @param int delay
				 */
				setLinkTrackingTimer: function (delay) {
					configTrackerPause = delay;
				},

				/**
				 * Set list of file extensions to be recognized as downloads
				 *
				 * @param string extensions
				 */
				setDownloadExtensions: function (extensions) {
					configDownloadExtensions = extensions;
				},

				/**
				 * Specify additional file extensions to be recognized as downloads
				 *
				 * @param string extensions
				 */
				addDownloadExtensions: function (extensions) {
					configDownloadExtensions += '|' + extensions;
				},

				/**
				 * Set array of domains to be treated as local
				 *
				 * @param string|array hostsAlias
				 */
				setDomains: function (hostsAlias) {
					configHostsAlias = isString(hostsAlias) ? [hostsAlias] : hostsAlias;
					configHostsAlias.push(domainAlias);
				},

				/**
				 * Set array of classes to be ignored if present in link
				 *
				 * @param string|array ignoreClasses
				 */
				setIgnoreClasses: function (ignoreClasses) {
					configIgnoreClasses = isString(ignoreClasses) ? [ignoreClasses] : ignoreClasses;
				},

				/**
				 * Set request method
				 *
				 * @param string method GET or POST; default is GET
				 */
				setRequestMethod: function (method) {
					configRequestMethod = method || 'GET';
				},

				/**
				 * Override referrer
				 *
				 * @param string url
				 */
				setReferrerUrl: function (url) {
					configReferrerUrl = url;
				},

				/**
				 * Override url
				 *
				 * @param string url
				 */
				setCustomUrl: function (url) {
					configCustomUrl = resolveRelativeReference(locationHrefAlias, url);
				},

				/**
				 * Override document.title
				 *
				 * @param string title
				 */
				setDocumentTitle: function (title) {
					configTitle = title;
				},

				/**
				 * Set array of classes to be treated as downloads
				 *
				 * @param string|array downloadClasses
				 */
				setDownloadClasses: function (downloadClasses) {
					configDownloadClasses = isString(downloadClasses) ? [downloadClasses] : downloadClasses;
				},

				/**
				 * Set array of classes to be treated as outlinks
				 *
				 * @param string|array linkClasses
				 */
				setLinkClasses: function (linkClasses) {
					configLinkClasses = isString(linkClasses) ? [linkClasses] : linkClasses;
				},

				/**
				 * Set array of campaign name parameters
				 *
				 * @see http://piwik.org/faq/how-to/#faq_120
				 * @param string|array campaignNames
				 */
				setCampaignNameKey: function (campaignNames) {
					configCampaignNameParameters = isString(campaignNames) ? [campaignNames] : campaignNames;
				},

				/**
				 * Set array of campaign keyword parameters
				 *
				 * @see http://piwik.org/faq/how-to/#faq_120
				 * @param string|array campaignKeywords
				 */
				setCampaignKeywordKey: function (campaignKeywords) {
					configCampaignKeywordParameters = isString(campaignKeywords) ? [campaignKeywords] : campaignKeywords;
				},

				/**
				 * Strip hash tag (or anchor) from URL
				 *
				 * @param bool enableFilter
				 */
				discardHashTag: function (enableFilter) {
					configDiscardHashTag = enableFilter;
				},

				/**
				 * Set first-party cookie name prefix
				 *
				 * @param string cookieNamePrefix
				 */
				setCookieNamePrefix: function (cookieNamePrefix) {
					configCookieNamePrefix = cookieNamePrefix;
					// Re-init the Custom Variables cookie
					customVariables = getCustomVariablesFromCookie();
				},

				/**
				 * Set first-party cookie domain
				 *
				 * @param string domain
				 */
				setCookieDomain: function (domain) {
					configCookieDomain = domainFixup(domain);
					updateDomainHash();
				},

				/**
				 * Set first-party cookie path
				 *
				 * @param string domain
				 */
				setCookiePath: function (path) {
					configCookiePath = path;
					updateDomainHash();
				},

				/**
				 * Set visitor cookie timeout (in seconds)
				 *
				 * @param int timeout
				 */
				setVisitorCookieTimeout: function (timeout) {
					configVisitorCookieTimeout = timeout * 1000;
				},

				/**
				 * Set session cookie timeout (in seconds)
				 *
				 * @param int timeout
				 */
				setSessionCookieTimeout: function (timeout) {
					configSessionCookieTimeout = timeout * 1000;
				},

				/**
				 * Set referral cookie timeout (in seconds)
				 *
				 * @param int timeout
				 */
				setReferralCookieTimeout: function (timeout) {
					configReferralCookieTimeout = timeout * 1000;
				},

				/**
				 * Set conversion attribution to first referrer and campaign
				 *
				 * @param bool if true, use first referrer (and first campaign)
				 *             if false, use the last referrer (or campaign)
				 */
				setConversionAttributionFirstReferrer: function (enable) {
					configConversionAttributionFirstReferrer = enable;
				},

				/**
				 * Handle do-not-track requests
				 *
				 * @param bool enable If true, don't track if user agent sends 'do-not-track' header
				 */
				setDoNotTrack: function (enable) {
					configDoNotTrack = enable && navigatorAlias.doNotTrack;
				},

				/**
				 * Add click listener to a specific link element.
				 * When clicked, Piwik will log the click automatically.
				 *
				 * @param DOMElement element
				 * @param bool enable If true, use pseudo click-handler (mousedown+mouseup)
				 */
				addListener: function (element, enable) {
					addClickListener(element, enable);
				},

				/**
				 * Install link tracker
				 *
				 * The default behaviour is to use actual click events. However, some browsers
				 * (e.g., Firefox, Opera, and Konqueror) don't generate click events for the middle mouse button.
				 *
				 * To capture more "clicks", the pseudo click-handler uses mousedown + mouseup events.
				 * This is not industry standard and is vulnerable to false positives (e.g., drag events).
				 *
				 * There is a Safari/Chrome/Webkit bug that prevents tracking requests from being sent
				 * by either click handler.  The workaround is to set a target attribute (which can't
				 * be "_self", "_top", or "_parent").
				 *
				 * @see https://bugs.webkit.org/show_bug.cgi?id=54783
				 *
				 * @param bool enable If true, use pseudo click-handler (mousedown+mouseup)
				 */
				enableLinkTracking: function (enable) {
					if (hasLoaded) {
						// the load event has already fired, add the click listeners now
						addClickListeners(enable);
					} else {
						// defer until page has loaded
						registeredOnLoadHandlers.push(function () {
							addClickListeners(enable);
						});
					}
				},

				/**
				 * Set heartbeat (in seconds)
				 *
				 * @param int minimumVisitLength
				 * @param int heartBeatDelay
				 */
				setHeartBeatTimer: function (minimumVisitLength, heartBeatDelay) {
					var now = new Date();

					configMinimumVisitTime = now.getTime() + minimumVisitLength * 1000;
					configHeartBeatTimer = heartBeatDelay * 1000;
				},

				/**
				 * Frame buster
				 */
				killFrame: function () {
					if (windowAlias.location !== windowAlias.top.location) {
						windowAlias.top.location = windowAlias.location;
					}
				},

				/**
				 * Redirect if browsing offline (aka file: buster)
				 *
				 * @param string url Redirect to this URL
				 */
				redirectFile: function (url) {
					if (windowAlias.location.protocol === 'file:') {
						windowAlias.location = url;
					}
				},

				/**
				 * Trigger a goal
				 *
				 * @param int|string idGoal
				 * @param int|float customRevenue
				 * @param mixed customData
				 */
				trackGoal: function (idGoal, customRevenue, customData) {
					logGoal(idGoal, customRevenue, customData);
				},

				/**
				 * Manually log a click from your own code
				 *
				 * @param string sourceUrl
				 * @param string linkType
				 * @param mixed customData
				 */
				trackLink: function (sourceUrl, linkType, customData) {
					logLink(sourceUrl, linkType, customData);
				},

				/**
				 * Log visit to this page
				 *
				 * @param string customTitle
				 */
				trackPageView: function (customTitle, customData) {
					logPageView(customTitle, customData);
				},


				/**
				 * Used to record that the current page view is an item (product) page view, or a Ecommerce Category page view.
				 * This must be called before trackPageView() on the product/category page.
				 * It will set 3 custom variables of scope "page" with the SKU, Name and Category for this page view.
				 * Note: Custom Variables of scope "page" slots 3, 4 and 5 will be used.
				 *
				 * On a category page, you can set the parameter category, and set the other parameters to empty string or false
				 *
				 * Tracking Product/Category page views will allow Piwik to report on Product & Categories
				 * conversion rates (Conversion rate = Ecommerce orders containing this product or category / Visits to the product or category)
				 *
				 * @param string sku Item's SKU code being viewed
				 * @param string name Item's Name being viewed
				 * @param string category Category page being viewed. On an Item's page, this is the item's category
				 * @param float price Item's display price, not use in standard Piwik reports, but output in API product reports. 
				 */
				setEcommerceView: function (sku, name, category, price) {
					if (!isDefined(category) || !category.length) {
						category = "";
					}
					customVariablesPage[5] = ['_pkc', category];
					if (isDefined(price) && String(price).length) {
						customVariablesPage[2] = ['_pkp', price];
					}
					// On a category page, do not track Product name not defined
					if ((!isDefined(sku) || !sku.length)
							&& (!isDefined(name) || !name.length)) {
						return;
					}

					if (isDefined(sku) && sku.length) {
						customVariablesPage[3] = ['_pks', sku];
					}
					if (!isDefined(name) || !name.length) {
						name = "";
					}
					customVariablesPage[4] = ['_pkn', name];
				},

				/**
				 * Adds an item (product) that is in the current Cart or in the Ecommerce order.
				 * This function is called for every item (product) in the Cart or the Order.
				 * The only required parameter is sku.
				 *
				 * @param string sku (required) Item's SKU Code. This is the unique identifier for the product.
				 * @param string name (optional) Item's name
				 * @param string name (optional) Item's category, or array of up to 5 categories
				 * @param float price (optional) Item's price. If not specified, will default to 0
				 * @param float quantity (optional) Item's quantity. If not specified, will default to 1
				 */
				addEcommerceItem: function (sku, name, category, price, quantity) {
					if (sku.length) {
						ecommerceItems[sku] = [ sku, name, category, price, quantity ];
					}
				},
				
				/**
				 * 供测试用例调用
				 */
				getCollectionInfo: function(reqStr,customData){
					return getRequest(reqStr,customData);
				}

			};
		}

		/************************************************************
		 * Proxy object
		 * - this allows the caller to continue push()'ing to _paq
		 *   after the Tracker has been initialized and loaded
		 ************************************************************/
		function caTrackerProxy(){
			this.push = function(userJson){
				_logUserData(userJson);
			};
		}
		
		function _logUserData(userJson){
			if(!userJson.oaid){
				alert('应用系统id为必备参数!详见API文档说明!');
				throw "paras config error";
			}
			
			var userJsonClone = {};
			for(var p in userJson){
				if (userJson[p] != null){
					userJsonClone[p] = userJson[p];
				}
				if(p == "oaid"){
					asyncTracker.setAppId(userJson[p]);
				}
			}
			asyncTracker.setTrackerUrl(calis.TRACKER_URL);
			asyncTracker.logUserData(userJsonClone);
		}
		
		/************************************************************
		 * Constructor
		 ************************************************************/

		// initialize the Piwik singleton
		addEventListener(windowAlias, 'beforeunload', beforeUnloadHandler, false);
		addReadyListener();

		asyncTracker = new Tracker();
		function _trackCurrentPage(){
			for(var k = 0; k  < _olc.length; k++){
				_logUserData(_olc[k]);
			}
			_olc = new caTrackerProxy();
		}
		
		/************************************************************
		 * Public data and methods
		 ************************************************************/

		return {
			/**
			 * Add plugin
			 *
			 * @param string pluginName
			 * @param Object pluginObj
			 */
			addPlugin: function (pluginName, pluginObj) {
				plugins[pluginName] = pluginObj;
			},

			/**
			 * Get Tracker (factory method)
			 *
			 * @param string piwikUrl
			 * @param int|string siteId
			 * @return Tracker
			 */
			getTracker: function (piwikUrl, siteId) {
				return new Tracker(piwikUrl, siteId);
			},

			/**
			 * Get internal asynchronous tracker object
			 *
			 * @return Tracker
			 */
			getAsyncTracker: function () {
				return asyncTracker;
			},
			log:function(json){
				this.getAsyncTracker().logUserData(json);
			},
			initTrack:function(){
				return _trackCurrentPage();
			}
		};
	}());
	calis.cal.ver = "114";
	calis.cal.initTrack();