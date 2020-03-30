/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, {
/******/ 				configurable: false,
/******/ 				enumerable: true,
/******/ 				get: getter
/******/ 			});
/******/ 		}
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = 12);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ (function(module, exports, __webpack_require__) {

;(function (root, factory) {
	if (true) {
		// CommonJS
		module.exports = exports = factory();
	}
	else if (typeof define === "function" && define.amd) {
		// AMD
		define([], factory);
	}
	else {
		// Global (browser)
		root.CryptoJS = factory();
	}
}(this, function () {

	/**
	 * CryptoJS core components.
	 */
	var CryptoJS = CryptoJS || (function (Math, undefined) {
	    /*
	     * Local polyfil of Object.create
	     */
	    var create = Object.create || (function () {
	        function F() {};

	        return function (obj) {
	            var subtype;

	            F.prototype = obj;

	            subtype = new F();

	            F.prototype = null;

	            return subtype;
	        };
	    }())

	    /**
	     * CryptoJS namespace.
	     */
	    var C = {};

	    /**
	     * Library namespace.
	     */
	    var C_lib = C.lib = {};

	    /**
	     * Base object for prototypal inheritance.
	     */
	    var Base = C_lib.Base = (function () {


	        return {
	            /**
	             * Creates a new object that inherits from this object.
	             *
	             * @param {Object} overrides Properties to copy into the new object.
	             *
	             * @return {Object} The new object.
	             *
	             * @static
	             *
	             * @example
	             *
	             *     var MyType = CryptoJS.lib.Base.extend({
	             *         field: 'value',
	             *
	             *         method: function () {
	             *         }
	             *     });
	             */
	            extend: function (overrides) {
	                // Spawn
	                var subtype = create(this);

	                // Augment
	                if (overrides) {
	                    subtype.mixIn(overrides);
	                }

	                // Create default initializer
	                if (!subtype.hasOwnProperty('init') || this.init === subtype.init) {
	                    subtype.init = function () {
	                        subtype.$super.init.apply(this, arguments);
	                    };
	                }

	                // Initializer's prototype is the subtype object
	                subtype.init.prototype = subtype;

	                // Reference supertype
	                subtype.$super = this;

	                return subtype;
	            },

	            /**
	             * Extends this object and runs the init method.
	             * Arguments to create() will be passed to init().
	             *
	             * @return {Object} The new object.
	             *
	             * @static
	             *
	             * @example
	             *
	             *     var instance = MyType.create();
	             */
	            create: function () {
	                var instance = this.extend();
	                instance.init.apply(instance, arguments);

	                return instance;
	            },

	            /**
	             * Initializes a newly created object.
	             * Override this method to add some logic when your objects are created.
	             *
	             * @example
	             *
	             *     var MyType = CryptoJS.lib.Base.extend({
	             *         init: function () {
	             *             // ...
	             *         }
	             *     });
	             */
	            init: function () {
	            },

	            /**
	             * Copies properties into this object.
	             *
	             * @param {Object} properties The properties to mix in.
	             *
	             * @example
	             *
	             *     MyType.mixIn({
	             *         field: 'value'
	             *     });
	             */
	            mixIn: function (properties) {
	                for (var propertyName in properties) {
	                    if (properties.hasOwnProperty(propertyName)) {
	                        this[propertyName] = properties[propertyName];
	                    }
	                }

	                // IE won't copy toString using the loop above
	                if (properties.hasOwnProperty('toString')) {
	                    this.toString = properties.toString;
	                }
	            },

	            /**
	             * Creates a copy of this object.
	             *
	             * @return {Object} The clone.
	             *
	             * @example
	             *
	             *     var clone = instance.clone();
	             */
	            clone: function () {
	                return this.init.prototype.extend(this);
	            }
	        };
	    }());

	    /**
	     * An array of 32-bit words.
	     *
	     * @property {Array} words The array of 32-bit words.
	     * @property {number} sigBytes The number of significant bytes in this word array.
	     */
	    var WordArray = C_lib.WordArray = Base.extend({
	        /**
	         * Initializes a newly created word array.
	         *
	         * @param {Array} words (Optional) An array of 32-bit words.
	         * @param {number} sigBytes (Optional) The number of significant bytes in the words.
	         *
	         * @example
	         *
	         *     var wordArray = CryptoJS.lib.WordArray.create();
	         *     var wordArray = CryptoJS.lib.WordArray.create([0x00010203, 0x04050607]);
	         *     var wordArray = CryptoJS.lib.WordArray.create([0x00010203, 0x04050607], 6);
	         */
	        init: function (words, sigBytes) {
	            words = this.words = words || [];

	            if (sigBytes != undefined) {
	                this.sigBytes = sigBytes;
	            } else {
	                this.sigBytes = words.length * 4;
	            }
	        },

	        /**
	         * Converts this word array to a string.
	         *
	         * @param {Encoder} encoder (Optional) The encoding strategy to use. Default: CryptoJS.enc.Hex
	         *
	         * @return {string} The stringified word array.
	         *
	         * @example
	         *
	         *     var string = wordArray + '';
	         *     var string = wordArray.toString();
	         *     var string = wordArray.toString(CryptoJS.enc.Utf8);
	         */
	        toString: function (encoder) {
	            return (encoder || Hex).stringify(this);
	        },

	        /**
	         * Concatenates a word array to this word array.
	         *
	         * @param {WordArray} wordArray The word array to append.
	         *
	         * @return {WordArray} This word array.
	         *
	         * @example
	         *
	         *     wordArray1.concat(wordArray2);
	         */
	        concat: function (wordArray) {
	            // Shortcuts
	            var thisWords = this.words;
	            var thatWords = wordArray.words;
	            var thisSigBytes = this.sigBytes;
	            var thatSigBytes = wordArray.sigBytes;

	            // Clamp excess bits
	            this.clamp();

	            // Concat
	            if (thisSigBytes % 4) {
	                // Copy one byte at a time
	                for (var i = 0; i < thatSigBytes; i++) {
	                    var thatByte = (thatWords[i >>> 2] >>> (24 - (i % 4) * 8)) & 0xff;
	                    thisWords[(thisSigBytes + i) >>> 2] |= thatByte << (24 - ((thisSigBytes + i) % 4) * 8);
	                }
	            } else {
	                // Copy one word at a time
	                for (var i = 0; i < thatSigBytes; i += 4) {
	                    thisWords[(thisSigBytes + i) >>> 2] = thatWords[i >>> 2];
	                }
	            }
	            this.sigBytes += thatSigBytes;

	            // Chainable
	            return this;
	        },

	        /**
	         * Removes insignificant bits.
	         *
	         * @example
	         *
	         *     wordArray.clamp();
	         */
	        clamp: function () {
	            // Shortcuts
	            var words = this.words;
	            var sigBytes = this.sigBytes;

	            // Clamp
	            words[sigBytes >>> 2] &= 0xffffffff << (32 - (sigBytes % 4) * 8);
	            words.length = Math.ceil(sigBytes / 4);
	        },

	        /**
	         * Creates a copy of this word array.
	         *
	         * @return {WordArray} The clone.
	         *
	         * @example
	         *
	         *     var clone = wordArray.clone();
	         */
	        clone: function () {
	            var clone = Base.clone.call(this);
	            clone.words = this.words.slice(0);

	            return clone;
	        },

	        /**
	         * Creates a word array filled with random bytes.
	         *
	         * @param {number} nBytes The number of random bytes to generate.
	         *
	         * @return {WordArray} The random word array.
	         *
	         * @static
	         *
	         * @example
	         *
	         *     var wordArray = CryptoJS.lib.WordArray.random(16);
	         */
	        random: function (nBytes) {
	            var words = [];

	            var r = (function (m_w) {
	                var m_w = m_w;
	                var m_z = 0x3ade68b1;
	                var mask = 0xffffffff;

	                return function () {
	                    m_z = (0x9069 * (m_z & 0xFFFF) + (m_z >> 0x10)) & mask;
	                    m_w = (0x4650 * (m_w & 0xFFFF) + (m_w >> 0x10)) & mask;
	                    var result = ((m_z << 0x10) + m_w) & mask;
	                    result /= 0x100000000;
	                    result += 0.5;
	                    return result * (Math.random() > .5 ? 1 : -1);
	                }
	            });

	            for (var i = 0, rcache; i < nBytes; i += 4) {
	                var _r = r((rcache || Math.random()) * 0x100000000);

	                rcache = _r() * 0x3ade67b7;
	                words.push((_r() * 0x100000000) | 0);
	            }

	            return new WordArray.init(words, nBytes);
	        }
	    });

	    /**
	     * Encoder namespace.
	     */
	    var C_enc = C.enc = {};

	    /**
	     * Hex encoding strategy.
	     */
	    var Hex = C_enc.Hex = {
	        /**
	         * Converts a word array to a hex string.
	         *
	         * @param {WordArray} wordArray The word array.
	         *
	         * @return {string} The hex string.
	         *
	         * @static
	         *
	         * @example
	         *
	         *     var hexString = CryptoJS.enc.Hex.stringify(wordArray);
	         */
	        stringify: function (wordArray) {
	            // Shortcuts
	            var words = wordArray.words;
	            var sigBytes = wordArray.sigBytes;

	            // Convert
	            var hexChars = [];
	            for (var i = 0; i < sigBytes; i++) {
	                var bite = (words[i >>> 2] >>> (24 - (i % 4) * 8)) & 0xff;
	                hexChars.push((bite >>> 4).toString(16));
	                hexChars.push((bite & 0x0f).toString(16));
	            }

	            return hexChars.join('');
	        },

	        /**
	         * Converts a hex string to a word array.
	         *
	         * @param {string} hexStr The hex string.
	         *
	         * @return {WordArray} The word array.
	         *
	         * @static
	         *
	         * @example
	         *
	         *     var wordArray = CryptoJS.enc.Hex.parse(hexString);
	         */
	        parse: function (hexStr) {
	            // Shortcut
	            var hexStrLength = hexStr.length;

	            // Convert
	            var words = [];
	            for (var i = 0; i < hexStrLength; i += 2) {
	                words[i >>> 3] |= parseInt(hexStr.substr(i, 2), 16) << (24 - (i % 8) * 4);
	            }

	            return new WordArray.init(words, hexStrLength / 2);
	        }
	    };

	    /**
	     * Latin1 encoding strategy.
	     */
	    var Latin1 = C_enc.Latin1 = {
	        /**
	         * Converts a word array to a Latin1 string.
	         *
	         * @param {WordArray} wordArray The word array.
	         *
	         * @return {string} The Latin1 string.
	         *
	         * @static
	         *
	         * @example
	         *
	         *     var latin1String = CryptoJS.enc.Latin1.stringify(wordArray);
	         */
	        stringify: function (wordArray) {
	            // Shortcuts
	            var words = wordArray.words;
	            var sigBytes = wordArray.sigBytes;

	            // Convert
	            var latin1Chars = [];
	            for (var i = 0; i < sigBytes; i++) {
	                var bite = (words[i >>> 2] >>> (24 - (i % 4) * 8)) & 0xff;
	                latin1Chars.push(String.fromCharCode(bite));
	            }

	            return latin1Chars.join('');
	        },

	        /**
	         * Converts a Latin1 string to a word array.
	         *
	         * @param {string} latin1Str The Latin1 string.
	         *
	         * @return {WordArray} The word array.
	         *
	         * @static
	         *
	         * @example
	         *
	         *     var wordArray = CryptoJS.enc.Latin1.parse(latin1String);
	         */
	        parse: function (latin1Str) {
	            // Shortcut
	            var latin1StrLength = latin1Str.length;

	            // Convert
	            var words = [];
	            for (var i = 0; i < latin1StrLength; i++) {
	                words[i >>> 2] |= (latin1Str.charCodeAt(i) & 0xff) << (24 - (i % 4) * 8);
	            }

	            return new WordArray.init(words, latin1StrLength);
	        }
	    };

	    /**
	     * UTF-8 encoding strategy.
	     */
	    var Utf8 = C_enc.Utf8 = {
	        /**
	         * Converts a word array to a UTF-8 string.
	         *
	         * @param {WordArray} wordArray The word array.
	         *
	         * @return {string} The UTF-8 string.
	         *
	         * @static
	         *
	         * @example
	         *
	         *     var utf8String = CryptoJS.enc.Utf8.stringify(wordArray);
	         */
	        stringify: function (wordArray) {
	            try {
	                return decodeURIComponent(escape(Latin1.stringify(wordArray)));
	            } catch (e) {
	                throw new Error('Malformed UTF-8 data');
	            }
	        },

	        /**
	         * Converts a UTF-8 string to a word array.
	         *
	         * @param {string} utf8Str The UTF-8 string.
	         *
	         * @return {WordArray} The word array.
	         *
	         * @static
	         *
	         * @example
	         *
	         *     var wordArray = CryptoJS.enc.Utf8.parse(utf8String);
	         */
	        parse: function (utf8Str) {
	            return Latin1.parse(unescape(encodeURIComponent(utf8Str)));
	        }
	    };

	    /**
	     * Abstract buffered block algorithm template.
	     *
	     * The property blockSize must be implemented in a concrete subtype.
	     *
	     * @property {number} _minBufferSize The number of blocks that should be kept unprocessed in the buffer. Default: 0
	     */
	    var BufferedBlockAlgorithm = C_lib.BufferedBlockAlgorithm = Base.extend({
	        /**
	         * Resets this block algorithm's data buffer to its initial state.
	         *
	         * @example
	         *
	         *     bufferedBlockAlgorithm.reset();
	         */
	        reset: function () {
	            // Initial values
	            this._data = new WordArray.init();
	            this._nDataBytes = 0;
	        },

	        /**
	         * Adds new data to this block algorithm's buffer.
	         *
	         * @param {WordArray|string} data The data to append. Strings are converted to a WordArray using UTF-8.
	         *
	         * @example
	         *
	         *     bufferedBlockAlgorithm._append('data');
	         *     bufferedBlockAlgorithm._append(wordArray);
	         */
	        _append: function (data) {
	            // Convert string to WordArray, else assume WordArray already
	            if (typeof data == 'string') {
	                data = Utf8.parse(data);
	            }

	            // Append
	            this._data.concat(data);
	            this._nDataBytes += data.sigBytes;
	        },

	        /**
	         * Processes available data blocks.
	         *
	         * This method invokes _doProcessBlock(offset), which must be implemented by a concrete subtype.
	         *
	         * @param {boolean} doFlush Whether all blocks and partial blocks should be processed.
	         *
	         * @return {WordArray} The processed data.
	         *
	         * @example
	         *
	         *     var processedData = bufferedBlockAlgorithm._process();
	         *     var processedData = bufferedBlockAlgorithm._process(!!'flush');
	         */
	        _process: function (doFlush) {
	            // Shortcuts
	            var data = this._data;
	            var dataWords = data.words;
	            var dataSigBytes = data.sigBytes;
	            var blockSize = this.blockSize;
	            var blockSizeBytes = blockSize * 4;

	            // Count blocks ready
	            var nBlocksReady = dataSigBytes / blockSizeBytes;
	            if (doFlush) {
	                // Round up to include partial blocks
	                nBlocksReady = Math.ceil(nBlocksReady);
	            } else {
	                // Round down to include only full blocks,
	                // less the number of blocks that must remain in the buffer
	                nBlocksReady = Math.max((nBlocksReady | 0) - this._minBufferSize, 0);
	            }

	            // Count words ready
	            var nWordsReady = nBlocksReady * blockSize;

	            // Count bytes ready
	            var nBytesReady = Math.min(nWordsReady * 4, dataSigBytes);

	            // Process blocks
	            if (nWordsReady) {
	                for (var offset = 0; offset < nWordsReady; offset += blockSize) {
	                    // Perform concrete-algorithm logic
	                    this._doProcessBlock(dataWords, offset);
	                }

	                // Remove processed words
	                var processedWords = dataWords.splice(0, nWordsReady);
	                data.sigBytes -= nBytesReady;
	            }

	            // Return processed words
	            return new WordArray.init(processedWords, nBytesReady);
	        },

	        /**
	         * Creates a copy of this object.
	         *
	         * @return {Object} The clone.
	         *
	         * @example
	         *
	         *     var clone = bufferedBlockAlgorithm.clone();
	         */
	        clone: function () {
	            var clone = Base.clone.call(this);
	            clone._data = this._data.clone();

	            return clone;
	        },

	        _minBufferSize: 0
	    });

	    /**
	     * Abstract hasher template.
	     *
	     * @property {number} blockSize The number of 32-bit words this hasher operates on. Default: 16 (512 bits)
	     */
	    var Hasher = C_lib.Hasher = BufferedBlockAlgorithm.extend({
	        /**
	         * Configuration options.
	         */
	        cfg: Base.extend(),

	        /**
	         * Initializes a newly created hasher.
	         *
	         * @param {Object} cfg (Optional) The configuration options to use for this hash computation.
	         *
	         * @example
	         *
	         *     var hasher = CryptoJS.algo.SHA256.create();
	         */
	        init: function (cfg) {
	            // Apply config defaults
	            this.cfg = this.cfg.extend(cfg);

	            // Set initial values
	            this.reset();
	        },

	        /**
	         * Resets this hasher to its initial state.
	         *
	         * @example
	         *
	         *     hasher.reset();
	         */
	        reset: function () {
	            // Reset data buffer
	            BufferedBlockAlgorithm.reset.call(this);

	            // Perform concrete-hasher logic
	            this._doReset();
	        },

	        /**
	         * Updates this hasher with a message.
	         *
	         * @param {WordArray|string} messageUpdate The message to append.
	         *
	         * @return {Hasher} This hasher.
	         *
	         * @example
	         *
	         *     hasher.update('message');
	         *     hasher.update(wordArray);
	         */
	        update: function (messageUpdate) {
	            // Append
	            this._append(messageUpdate);

	            // Update the hash
	            this._process();

	            // Chainable
	            return this;
	        },

	        /**
	         * Finalizes the hash computation.
	         * Note that the finalize operation is effectively a destructive, read-once operation.
	         *
	         * @param {WordArray|string} messageUpdate (Optional) A final message update.
	         *
	         * @return {WordArray} The hash.
	         *
	         * @example
	         *
	         *     var hash = hasher.finalize();
	         *     var hash = hasher.finalize('message');
	         *     var hash = hasher.finalize(wordArray);
	         */
	        finalize: function (messageUpdate) {
	            // Final message update
	            if (messageUpdate) {
	                this._append(messageUpdate);
	            }

	            // Perform concrete-hasher logic
	            var hash = this._doFinalize();

	            return hash;
	        },

	        blockSize: 512/32,

	        /**
	         * Creates a shortcut function to a hasher's object interface.
	         *
	         * @param {Hasher} hasher The hasher to create a helper for.
	         *
	         * @return {Function} The shortcut function.
	         *
	         * @static
	         *
	         * @example
	         *
	         *     var SHA256 = CryptoJS.lib.Hasher._createHelper(CryptoJS.algo.SHA256);
	         */
	        _createHelper: function (hasher) {
	            return function (message, cfg) {
	                return new hasher.init(cfg).finalize(message);
	            };
	        },

	        /**
	         * Creates a shortcut function to the HMAC's object interface.
	         *
	         * @param {Hasher} hasher The hasher to use in this HMAC helper.
	         *
	         * @return {Function} The shortcut function.
	         *
	         * @static
	         *
	         * @example
	         *
	         *     var HmacSHA256 = CryptoJS.lib.Hasher._createHmacHelper(CryptoJS.algo.SHA256);
	         */
	        _createHmacHelper: function (hasher) {
	            return function (message, key) {
	                return new C_algo.HMAC.init(hasher, key).finalize(message);
	            };
	        }
	    });

	    /**
	     * Algorithm namespace.
	     */
	    var C_algo = C.algo = {};

	    return C;
	}(Math));


	return CryptoJS;

}));

/***/ }),
/* 1 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Util = function () {
	function Util() {
		_classCallCheck(this, Util);
	}

	_createClass(Util, null, [{
		key: 'detectIEVersion',
		value: function detectIEVersion() {
			var v = 4,
			    div = document.createElement('div'),
			    all = div.getElementsByTagName('i');
			while (div.innerHTML = '<!--[if gt IE ' + v + ']><i></i><![endif]-->', all[0]) {
				v++;
			}
			return v > 4 ? v : false;
		}
	}, {
		key: 'extend',
		value: function extend(dst, src) {
			for (var i in src) {
				if (Object.prototype.hasOwnProperty.call(src, i) && src[i]) {
					dst[i] = src[i];
				}
			}
		}
	}, {
		key: 'isArray',
		value: function isArray(arr) {
			return Object.prototype.toString.call(arg) === '[object Array]';
		}
	}, {
		key: 'getFileType',
		value: function getFileType(url) {
			url = url.toLowerCase();
			if (/.mp4|.flv|.m3u8|.avi|.rm|.rmvb|.mpeg|.mpg|.mov|.wmv|.3gp|.asf|.dat|.dv|.f4v|.gif|.m2t|.m4v|.mj2|.mjpeg|.mpe|.mts|.ogg|.qt|.swf|.ts|.vob|.wmv|.webm/.test(url)) {
				return 'video';
			} else if (/.mp3|.wav|.ape|.cda|.au|.midi|.mac|.aac|.ac3|.acm|.amr|.caf|.flac|.m4a|.ra|.wma/.test(url)) {
				return 'audio';
			} else if (/.bmp|.jpg|.jpeg|.png/.test(url)) {
				return 'img';
			} else {
				return 'other';
			}
		}
	}, {
		key: 'isImage',
		value: function isImage(url) {
			url = url.toLowerCase();
			if (/.jpg|.jpeg|.png/.test(url)) return true;

			return false;
		}
	}, {
		key: 'ISODateString',
		value: function ISODateString(d) {
			function pad(n) {
				return n < 10 ? '0' + n : n;
			}
			return d.getUTCFullYear() + '-' + pad(d.getUTCMonth() + 1) + '-' + pad(d.getUTCDate()) + 'T' + pad(d.getUTCHours()) + ':' + pad(d.getUTCMinutes()) + ':' + pad(d.getUTCSeconds()) + 'Z';
		}
	}, {
		key: 'isIntNum',
		value: function isIntNum(val) {
			var regPos = /^\d+$/; // 非负整数
			if (regPos.test(val)) {
				return true;
			} else {
				return false;
			}
		}
	}]);

	return Util;
}();

exports.default = Util;

/***/ }),
/* 2 */
/***/ (function(module, exports, __webpack_require__) {

;(function (root, factory) {
	if (true) {
		// CommonJS
		module.exports = exports = factory(__webpack_require__(0));
	}
	else if (typeof define === "function" && define.amd) {
		// AMD
		define(["./core"], factory);
	}
	else {
		// Global (browser)
		factory(root.CryptoJS);
	}
}(this, function (CryptoJS) {

	(function (Math) {
	    // Shortcuts
	    var C = CryptoJS;
	    var C_lib = C.lib;
	    var WordArray = C_lib.WordArray;
	    var Hasher = C_lib.Hasher;
	    var C_algo = C.algo;

	    // Constants table
	    var T = [];

	    // Compute constants
	    (function () {
	        for (var i = 0; i < 64; i++) {
	            T[i] = (Math.abs(Math.sin(i + 1)) * 0x100000000) | 0;
	        }
	    }());

	    /**
	     * MD5 hash algorithm.
	     */
	    var MD5 = C_algo.MD5 = Hasher.extend({
	        _doReset: function () {
	            this._hash = new WordArray.init([
	                0x67452301, 0xefcdab89,
	                0x98badcfe, 0x10325476
	            ]);
	        },

	        _doProcessBlock: function (M, offset) {
	            // Swap endian
	            for (var i = 0; i < 16; i++) {
	                // Shortcuts
	                var offset_i = offset + i;
	                var M_offset_i = M[offset_i];

	                M[offset_i] = (
	                    (((M_offset_i << 8)  | (M_offset_i >>> 24)) & 0x00ff00ff) |
	                    (((M_offset_i << 24) | (M_offset_i >>> 8))  & 0xff00ff00)
	                );
	            }

	            // Shortcuts
	            var H = this._hash.words;

	            var M_offset_0  = M[offset + 0];
	            var M_offset_1  = M[offset + 1];
	            var M_offset_2  = M[offset + 2];
	            var M_offset_3  = M[offset + 3];
	            var M_offset_4  = M[offset + 4];
	            var M_offset_5  = M[offset + 5];
	            var M_offset_6  = M[offset + 6];
	            var M_offset_7  = M[offset + 7];
	            var M_offset_8  = M[offset + 8];
	            var M_offset_9  = M[offset + 9];
	            var M_offset_10 = M[offset + 10];
	            var M_offset_11 = M[offset + 11];
	            var M_offset_12 = M[offset + 12];
	            var M_offset_13 = M[offset + 13];
	            var M_offset_14 = M[offset + 14];
	            var M_offset_15 = M[offset + 15];

	            // Working varialbes
	            var a = H[0];
	            var b = H[1];
	            var c = H[2];
	            var d = H[3];

	            // Computation
	            a = FF(a, b, c, d, M_offset_0,  7,  T[0]);
	            d = FF(d, a, b, c, M_offset_1,  12, T[1]);
	            c = FF(c, d, a, b, M_offset_2,  17, T[2]);
	            b = FF(b, c, d, a, M_offset_3,  22, T[3]);
	            a = FF(a, b, c, d, M_offset_4,  7,  T[4]);
	            d = FF(d, a, b, c, M_offset_5,  12, T[5]);
	            c = FF(c, d, a, b, M_offset_6,  17, T[6]);
	            b = FF(b, c, d, a, M_offset_7,  22, T[7]);
	            a = FF(a, b, c, d, M_offset_8,  7,  T[8]);
	            d = FF(d, a, b, c, M_offset_9,  12, T[9]);
	            c = FF(c, d, a, b, M_offset_10, 17, T[10]);
	            b = FF(b, c, d, a, M_offset_11, 22, T[11]);
	            a = FF(a, b, c, d, M_offset_12, 7,  T[12]);
	            d = FF(d, a, b, c, M_offset_13, 12, T[13]);
	            c = FF(c, d, a, b, M_offset_14, 17, T[14]);
	            b = FF(b, c, d, a, M_offset_15, 22, T[15]);

	            a = GG(a, b, c, d, M_offset_1,  5,  T[16]);
	            d = GG(d, a, b, c, M_offset_6,  9,  T[17]);
	            c = GG(c, d, a, b, M_offset_11, 14, T[18]);
	            b = GG(b, c, d, a, M_offset_0,  20, T[19]);
	            a = GG(a, b, c, d, M_offset_5,  5,  T[20]);
	            d = GG(d, a, b, c, M_offset_10, 9,  T[21]);
	            c = GG(c, d, a, b, M_offset_15, 14, T[22]);
	            b = GG(b, c, d, a, M_offset_4,  20, T[23]);
	            a = GG(a, b, c, d, M_offset_9,  5,  T[24]);
	            d = GG(d, a, b, c, M_offset_14, 9,  T[25]);
	            c = GG(c, d, a, b, M_offset_3,  14, T[26]);
	            b = GG(b, c, d, a, M_offset_8,  20, T[27]);
	            a = GG(a, b, c, d, M_offset_13, 5,  T[28]);
	            d = GG(d, a, b, c, M_offset_2,  9,  T[29]);
	            c = GG(c, d, a, b, M_offset_7,  14, T[30]);
	            b = GG(b, c, d, a, M_offset_12, 20, T[31]);

	            a = HH(a, b, c, d, M_offset_5,  4,  T[32]);
	            d = HH(d, a, b, c, M_offset_8,  11, T[33]);
	            c = HH(c, d, a, b, M_offset_11, 16, T[34]);
	            b = HH(b, c, d, a, M_offset_14, 23, T[35]);
	            a = HH(a, b, c, d, M_offset_1,  4,  T[36]);
	            d = HH(d, a, b, c, M_offset_4,  11, T[37]);
	            c = HH(c, d, a, b, M_offset_7,  16, T[38]);
	            b = HH(b, c, d, a, M_offset_10, 23, T[39]);
	            a = HH(a, b, c, d, M_offset_13, 4,  T[40]);
	            d = HH(d, a, b, c, M_offset_0,  11, T[41]);
	            c = HH(c, d, a, b, M_offset_3,  16, T[42]);
	            b = HH(b, c, d, a, M_offset_6,  23, T[43]);
	            a = HH(a, b, c, d, M_offset_9,  4,  T[44]);
	            d = HH(d, a, b, c, M_offset_12, 11, T[45]);
	            c = HH(c, d, a, b, M_offset_15, 16, T[46]);
	            b = HH(b, c, d, a, M_offset_2,  23, T[47]);

	            a = II(a, b, c, d, M_offset_0,  6,  T[48]);
	            d = II(d, a, b, c, M_offset_7,  10, T[49]);
	            c = II(c, d, a, b, M_offset_14, 15, T[50]);
	            b = II(b, c, d, a, M_offset_5,  21, T[51]);
	            a = II(a, b, c, d, M_offset_12, 6,  T[52]);
	            d = II(d, a, b, c, M_offset_3,  10, T[53]);
	            c = II(c, d, a, b, M_offset_10, 15, T[54]);
	            b = II(b, c, d, a, M_offset_1,  21, T[55]);
	            a = II(a, b, c, d, M_offset_8,  6,  T[56]);
	            d = II(d, a, b, c, M_offset_15, 10, T[57]);
	            c = II(c, d, a, b, M_offset_6,  15, T[58]);
	            b = II(b, c, d, a, M_offset_13, 21, T[59]);
	            a = II(a, b, c, d, M_offset_4,  6,  T[60]);
	            d = II(d, a, b, c, M_offset_11, 10, T[61]);
	            c = II(c, d, a, b, M_offset_2,  15, T[62]);
	            b = II(b, c, d, a, M_offset_9,  21, T[63]);

	            // Intermediate hash value
	            H[0] = (H[0] + a) | 0;
	            H[1] = (H[1] + b) | 0;
	            H[2] = (H[2] + c) | 0;
	            H[3] = (H[3] + d) | 0;
	        },

	        _doFinalize: function () {
	            // Shortcuts
	            var data = this._data;
	            var dataWords = data.words;

	            var nBitsTotal = this._nDataBytes * 8;
	            var nBitsLeft = data.sigBytes * 8;

	            // Add padding
	            dataWords[nBitsLeft >>> 5] |= 0x80 << (24 - nBitsLeft % 32);

	            var nBitsTotalH = Math.floor(nBitsTotal / 0x100000000);
	            var nBitsTotalL = nBitsTotal;
	            dataWords[(((nBitsLeft + 64) >>> 9) << 4) + 15] = (
	                (((nBitsTotalH << 8)  | (nBitsTotalH >>> 24)) & 0x00ff00ff) |
	                (((nBitsTotalH << 24) | (nBitsTotalH >>> 8))  & 0xff00ff00)
	            );
	            dataWords[(((nBitsLeft + 64) >>> 9) << 4) + 14] = (
	                (((nBitsTotalL << 8)  | (nBitsTotalL >>> 24)) & 0x00ff00ff) |
	                (((nBitsTotalL << 24) | (nBitsTotalL >>> 8))  & 0xff00ff00)
	            );

	            data.sigBytes = (dataWords.length + 1) * 4;

	            // Hash final blocks
	            this._process();

	            // Shortcuts
	            var hash = this._hash;
	            var H = hash.words;

	            // Swap endian
	            for (var i = 0; i < 4; i++) {
	                // Shortcut
	                var H_i = H[i];

	                H[i] = (((H_i << 8)  | (H_i >>> 24)) & 0x00ff00ff) |
	                       (((H_i << 24) | (H_i >>> 8))  & 0xff00ff00);
	            }

	            // Return final computed hash
	            return hash;
	        },

	        clone: function () {
	            var clone = Hasher.clone.call(this);
	            clone._hash = this._hash.clone();

	            return clone;
	        }
	    });

	    function FF(a, b, c, d, x, s, t) {
	        var n = a + ((b & c) | (~b & d)) + x + t;
	        return ((n << s) | (n >>> (32 - s))) + b;
	    }

	    function GG(a, b, c, d, x, s, t) {
	        var n = a + ((b & d) | (c & ~d)) + x + t;
	        return ((n << s) | (n >>> (32 - s))) + b;
	    }

	    function HH(a, b, c, d, x, s, t) {
	        var n = a + (b ^ c ^ d) + x + t;
	        return ((n << s) | (n >>> (32 - s))) + b;
	    }

	    function II(a, b, c, d, x, s, t) {
	        var n = a + (c ^ (b | ~d)) + x + t;
	        return ((n << s) | (n >>> (32 - s))) + b;
	    }

	    /**
	     * Shortcut function to the hasher's object interface.
	     *
	     * @param {WordArray|string} message The message to hash.
	     *
	     * @return {WordArray} The hash.
	     *
	     * @static
	     *
	     * @example
	     *
	     *     var hash = CryptoJS.MD5('message');
	     *     var hash = CryptoJS.MD5(wordArray);
	     */
	    C.MD5 = Hasher._createHelper(MD5);

	    /**
	     * Shortcut function to the HMAC's object interface.
	     *
	     * @param {WordArray|string} message The message to hash.
	     * @param {WordArray|string} key The secret key.
	     *
	     * @return {WordArray} The HMAC.
	     *
	     * @static
	     *
	     * @example
	     *
	     *     var hmac = CryptoJS.HmacMD5(message, key);
	     */
	    C.HmacMD5 = Hasher._createHmacHelper(MD5);
	}(Math));


	return CryptoJS.MD5;

}));

/***/ }),
/* 3 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
        value: true
});
var UPLOADSTATE = exports.UPLOADSTATE = {
        INIT: "init",
        UPLOADING: "uploading",
        COMPLETE: "complete",
        INTERRUPT: "interrupt"
};

var UPLOADSTEP = exports.UPLOADSTEP = {
        INIT: "init",
        PART: "part",
        COMPLETE: "complete"
};

var UPLOADDEFAULT = exports.UPLOADDEFAULT = {
        PARALLEL: 5,
        PARTSIZE: 1048576
};

/***/ }),
/* 4 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _guid = __webpack_require__(5);

var _guid2 = _interopRequireDefault(_guid);

var _cookie = __webpack_require__(25);

var _cookie2 = _interopRequireDefault(_cookie);

var _config = __webpack_require__(6);

var _config2 = _interopRequireDefault(_config);

var _ua = __webpack_require__(7);

var _ua2 = _interopRequireDefault(_ua);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Log = function () {
    function Log(props) {
        _classCallCheck(this, Log);

        var osName = _ua2.default.os.name,
            osVersion = _ua2.default.os.version || "",
            exName = _ua2.default.browser.name,
            exVersion = _ua2.default.browser.version || "";
        var address = window.location.href,
            app_n = "";
        if (address) {
            app_n = _ua2.default.getHost(address);
        }
        var tt = "pc";
        if (_ua2.default.os.ipad) {
            tt = "pad";
        } else if (_ua2.default.os.iphone || _ua2.default.os.android) {
            tt = "phone";
        }
        this._ri = _guid2.default.create();
        this.initParam = {
            APIVersion: '0.6.0',
            lv: '1',
            av: _config2.default.version,
            pd: 'upload',
            sm: 'upload',
            md: 'uploader',
            uuid: Log.getUuid(),
            os: osName,
            ov: osVersion,
            et: exName,
            ev: exVersion,
            uat: navigator.userAgent,
            app_n: app_n,
            tt: tt,
            dm: 'h5',
            ut: ""
        };
    }

    /**
    * 唯一表示播放器的id缓存在cookie中
    */


    _createClass(Log, [{
        key: 'log',


        /**
        * jsonp请求
        */
        value: function log(e, params) {
            if (params && params.ri) {
                this._ri = params.ri;
                delete params.ri;
            } else {
                this._ri = _guid2.default.create();
            }
            if (params && params.ut) {
                this.initParam.ut = params.ut;
                delete params.ut;
            }
            this.initParam.t = new Date().getTime();
            this.initParam.ll = e == '20006' ? 'error' : 'info';
            this.initParam.ri = this._ri;
            this.initParam.e = e;
            var vargs = [];
            if (params) {
                for (var key in params) {
                    vargs.push(key + '=' + params[key]);
                }
            }
            var argsStr = vargs.join('&');
            this.initParam.args = encodeURIComponent(argsStr == "" ? "0" : argsStr);
            var paramsArray = [];
            for (var key in this.initParam) {
                paramsArray.push(key + '=' + this.initParam[key]);
            }
            var paramsString = paramsArray.join('&');
            if (AliyunUpload && AliyunUpload.__logTestCallback__) {
                AliyunUpload.__logTestCallback__(paramsString);
            } else {
                var img = new Image(0, 0);
                img.src = 'https://videocloud.cn-hangzhou.log.aliyuncs.com/logstores/upload/track?' + paramsString;
            }
        }
    }], [{
        key: 'getUuid',
        value: function getUuid() {
            // p_h5_u表示prism_h5_uuid
            var uuid = _cookie2.default.get('p_h5_upload_u');

            if (!uuid) {
                uuid = _guid2.default.create();
                _cookie2.default.set('p_h5_upload_u', uuid, 730);
            }

            return uuid;
        }
    }, {
        key: 'getClientId',
        value: function getClientId() {
            var uuid = _cookie2.default.get('p_h5_upload_clientId');
            return uuid;
        }

        /**
        * 唯一表示播放器的id缓存在cookie中
        */

    }, {
        key: 'setClientId',
        value: function setClientId(id) {
            if (!id) {
                id = _guid2.default.create();
            }
            _cookie2.default.set('p_h5_upload_clientId', id, 730);
            return id;
        }
    }]);

    return Log;
}();

exports.default = Log;

/***/ }),
/* 5 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Guid = function () {
    function Guid() {
        _classCallCheck(this, Guid);
    }

    _createClass(Guid, null, [{
        key: 'create',
        value: function create(len, radix) {
            var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
            var uuid = [],
                i;
            radix = radix || chars.length;

            if (len) {
                for (i = 0; i < len; i++) {
                    uuid[i] = chars[0 | Math.random() * radix];
                }
            } else {
                var r;
                uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
                uuid[14] = '4';
                for (i = 0; i < 36; i++) {
                    if (!uuid[i]) {
                        r = 0 | Math.random() * 16;
                        uuid[i] = chars[i == 19 ? r & 0x3 | 0x8 : r];
                    }
                }
            }

            return uuid.join('');
        }
    }]);

    return Guid;
}();

exports.default = Guid;

/***/ }),
/* 6 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
	value: true
});
var config = {
	version: '1.5.0'
};

exports.default = config;

/***/ }),
/* 7 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

// make available to unit tests
var getOSName = function getOSName(os) {
  var sUserAgent = navigator.userAgent;
  var operator = "other";
  if (!!os.ios) {
    return 'iOS';
  }
  if (!!os.android) {
    return 'android';
  }
  if (sUserAgent.indexOf('Baiduspider') > -1) {
    return 'Baiduspider';
  }
  if (sUserAgent.indexOf('PlayStation') > -1) {
    return 'PS4';
  }
  var isWin = navigator.platform == "Win32" || navigator.platform == "Windows" || sUserAgent.indexOf('Windows') > -1;
  var isMac = navigator.platform == "Mac68K" || navigator.platform == "MacPPC" || navigator.platform == "Macintosh" || navigator.platform == "MacIntel";
  if (isMac) operator = "macOS";
  var isUnix = navigator.platform == "X11" && !isWin && !isMac;
  if (isUnix) operator = "Unix";
  var isLinux = String(navigator.platform).indexOf("Linux") > -1;
  if (isLinux) operator = "Linux";
  if (isWin) {
    return "windows";
  }
  return operator;
};

var getWinVersion = function getWinVersion() {
  var sUserAgent = navigator.userAgent;
  var operator = "";
  var isWin2K = sUserAgent.indexOf("Windows NT 5.0") > -1 || sUserAgent.indexOf("Windows 2000") > -1;
  if (isWin2K) operator = "2000";
  var isWinXP = sUserAgent.indexOf("Windows NT 5.1") > -1 || sUserAgent.indexOf("Windows XP") > -1;
  if (isWinXP) operator = "XP";
  var isWin2003 = sUserAgent.indexOf("Windows NT 5.2") > -1 || sUserAgent.indexOf("Windows 2003") > -1;
  if (isWin2003) operator = "2003";
  var isWinVista = sUserAgent.indexOf("Windows NT 6.0") > -1 || sUserAgent.indexOf("Windows Vista") > -1;
  if (isWinVista) operator = "Vista";
  var isWin7 = sUserAgent.indexOf("Windows NT 6.1") > -1 || sUserAgent.indexOf("Windows 7") > -1;
  if (isWin7) operator = "7";
  var isWin8 = sUserAgent.indexOf("Windows NT 6.2") > -1 || sUserAgent.indexOf("Windows 8") > -1;
  if (isWin8) operator = "8";
  var isWin81 = sUserAgent.indexOf("Windows NT 6.3") > -1 || sUserAgent.indexOf("Windows 8.1") > -1;
  if (isWin81) operator = "8.1";
  var isWin10 = sUserAgent.indexOf("Windows NT 10") > -1 || sUserAgent.indexOf("Windows 10") > -1;
  if (isWin10) operator = "10";

  return operator;
};

var getBrowserType = function getBrowserType(browser) {
  var UserAgent = navigator.userAgent.toLowerCase();
  if (!!browser.chrome) {
    return "Chrome";
  } else if (!!browser.firefox) {
    return "Firefox";
  } else if (!!browser.safari) {
    return "Safari";
  } else if (!!browser.webview) {
    return "webview";
  } else if (!!browser.ie) {
    if (/edge/.test(UserAgent)) return "Edge";
    return "IE";
  } else if (/baiduspider/.test(UserAgent)) {
    return 'Baiduspider';
  } else if (/ucweb/.test(UserAgent) || /UCBrowser/.test(UserAgent)) {
    return 'UC';
  } else if (/opera/.test(UserAgent)) {
    return "Opera";
  } else if (/ucweb/.test(UserAgent)) {
    return 'UC';
  } else if (/360se/.test(UserAgent)) {
    return "360浏览器";
  } else if (/bidubrowser/.test(UserAgent)) {
    return "百度浏览器";
  } else if (/metasr/.test(UserAgent)) {
    return "搜狗浏览器";
  } else if (/lbbrowser/.test(UserAgent)) {
    return "猎豹浏览器";
  } else if (/micromessenger/.test(UserAgent)) {
    return "微信内置浏览器";
  } else if (/qqbrowser/.test(UserAgent)) {
    return "QQ浏览器";
  } else if (/playstation/.test(UserAgent)) {
    return 'PS4浏览器';
  }
};

var sysInfo = function () {
  var os = {},
      browser = {},
      ua = navigator.userAgent,
      platform = navigator.platform,
      webkit = ua.match(/Web[kK]it[\/]{0,1}([\d.]+)/),
      android = ua.match(/(Android);?[\s\/]+([\d.]+)?/),
      osx = !!ua.match(/\(Macintosh\; Intel /),
      ipad = ua.match(/(iPad).*OS\s([\d_]+)/),
      ipod = ua.match(/(iPod)(.*OS\s([\d_]+))?/),
      iphone = !ipad && ua.match(/(iPhone\sOS)\s([\d_]+)/),
      webos = ua.match(/(webOS|hpwOS)[\s\/]([\d.]+)/),
      win = /Win\d{2}|Windows/.test(platform),
      wp = ua.match(/Windows Phone ([\d.]+)/),
      touchpad = webos && ua.match(/TouchPad/),
      kindle = ua.match(/Kindle\/([\d.]+)/),
      silk = ua.match(/Silk\/([\d._]+)/),
      blackberry = ua.match(/(BlackBerry).*Version\/([\d.]+)/),
      bb10 = ua.match(/(BB10).*Version\/([\d.]+)/),
      rimtabletos = ua.match(/(RIM\sTablet\sOS)\s([\d.]+)/),
      playbook = ua.match(/PlayBook/),
      chrome = ua.match(/Chrome\/([\d.]+)/) || ua.match(/CriOS\/([\d.]+)/),
      firefox = ua.match(/Firefox\/([\d.]+)/),
      firefoxos = ua.match(/\((?:Mobile|Tablet); rv:([\d.]+)\).*Firefox\/[\d.]+/),
      ie = ua.match(/MSIE\s([\d.]+)/) || ua.match(/Trident\/[\d](?=[^\?]+).*rv:([0-9.].)/),
      webview = !chrome && ua.match(/(iPhone|iPod|iPad).*AppleWebKit(?!.*Safari)/),
      safari = webview || ua.match(/Version\/([\d.]+)([^S](Safari)|[^M]*(Mobile)[^S]*(Safari))/);

  // Todo: clean this up with a better OS/browser seperation:
  // - discern (more) between multiple browsers on android
  // - decide if kindle fire in silk mode is android or not
  // - Firefox on Android doesn't specify the Android version
  // - possibly devide in os, device and browser hashes

  if (browser.webkit = !!webkit) browser.version = webkit[1];

  if (android) os.android = true, os.version = android[2];
  if (iphone && !ipod) os.ios = os.iphone = true, os.version = iphone[2].replace(/_/g, '.');
  if (ipad) os.ios = os.ipad = true, os.version = ipad[2].replace(/_/g, '.');
  if (ipod) os.ios = os.ipod = true, os.version = ipod[3] ? ipod[3].replace(/_/g, '.') : null;
  if (wp) os.wp = true, os.version = wp[1];
  if (webos) os.webos = true, os.version = webos[2];
  if (touchpad) os.touchpad = true;
  if (blackberry) os.blackberry = true, os.version = blackberry[2];
  if (bb10) os.bb10 = true, os.version = bb10[2];
  if (rimtabletos) os.rimtabletos = true, os.version = rimtabletos[2];
  if (playbook) browser.playbook = true;
  if (kindle) os.kindle = true, os.version = kindle[1];
  if (silk) browser.silk = true, browser.version = silk[1];
  if (!silk && os.android && ua.match(/Kindle Fire/)) browser.silk = true;
  if (chrome) browser.chrome = true, browser.version = chrome[1];
  if (firefox) browser.firefox = true, browser.version = firefox[1];
  if (firefoxos) os.firefoxos = true, os.version = firefoxos[1];
  if (ie) browser.ie = true, browser.version = ie[1];
  if (safari && (osx || os.ios || win || android)) {
    browser.safari = true;
    if (!os.ios) browser.version = safari[1];
  }
  if (webview) browser.webview = true;
  if (osx) {
    var version = ua.match(/[\d]*_[\d]*_[\d]*/);
    if (version && version.length > 0 && version[0]) {
      os.version = version[0].replace(/_/g, '.');
    }
  }

  os.tablet = !!(ipad || playbook || android && !ua.match(/Mobile/) || firefox && ua.match(/Tablet/) || ie && !ua.match(/Phone/) && ua.match(/Touch/));
  os.phone = !!(!os.tablet && !os.ipod && (android || iphone || webos || blackberry || bb10 || chrome && ua.match(/Android/) || chrome && ua.match(/CriOS\/([\d.]+)/) || firefox && ua.match(/Mobile/) || ie && ua.match(/Touch/)));

  os.pc = !os.tablet && !os.phone;

  if (osx) {
    os.name = 'macOS';
  } else if (win) {
    os.name = "windows";
    os.version = getWinVersion();
  } else {
    os.name = getOSName(os);
  }
  browser.name = getBrowserType(browser);

  return { os: os, browser: browser };
}();

var UA = function () {
  function UA() {
    _classCallCheck(this, UA);
  }

  _createClass(UA, null, [{
    key: 'getHost',
    value: function getHost(url) {
      var host = "";
      if (typeof url == 'undefined' || url == null || url == "") {
        return "";
      }
      var index = url.indexOf("//"),
          str = url;
      if (index > -1) {
        str = url.substring(index + 2);
      }
      var host = str;
      var arr = str.split("/");
      if (arr && arr.length > 0) {
        host = arr[0];
      }
      arr = host.split(':');
      if (arr && arr.length > 0) {
        host = arr[0];
      }
      return host;
    }
  }, {
    key: 'os',
    get: function get() {
      return sysInfo.os;
    }
  }, {
    key: 'browser',
    get: function get() {
      var browser = sysInfo.browser;
      if (!browser.name) {
        browser.name = getBrowserType();
      }
      return browser;
    }
  }]);

  return UA;
}();

exports.default = UA;

/***/ }),
/* 8 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var IO = function () {
	function IO() {
		_classCallCheck(this, IO);
	}

	_createClass(IO, null, [{
		key: 'get',

		/**
   * Simple http request for retrieving external files (e.g. text tracks)
   * @param  {String}    url             URL of resource
   * @param  {Function} onSuccess       Success callback
   * @param  {Function=} onError         Error callback
   * @param  {Boolean=}   withCredentials Flag which allow credentials
   * @private
   */
		value: function get(url, onSuccess, onError, asyncValue, withCredentials) {
			var request;

			onError = onError || function () {};

			if (typeof XMLHttpRequest === 'undefined') {
				// Shim XMLHttpRequest for older IEs
				window.XMLHttpRequest = function () {
					try {
						return new window.ActiveXObject('Msxml2.XMLHTTP.6.0');
					} catch (e) {}
					try {
						return new window.ActiveXObject('Msxml2.XMLHTTP.3.0');
					} catch (f) {}
					try {
						return new window.ActiveXObject('Msxml2.XMLHTTP');
					} catch (g) {}
					throw new Error('This browser does not support XMLHttpRequest.');
				};
			}

			request = new XMLHttpRequest();
			request.onreadystatechange = function () {
				if (request.readyState === 4) {
					if (request.status === 200) {
						onSuccess(request.responseText);
					} else {
						onError(request.responseText);
					}
				}
			};

			// open the connection
			try {
				// Third arg is async, or ignored by XDomainRequest
				if (typeof asyncValue == 'undefined') {
					asyncValue = true;
				}
				request.open('GET', url, asyncValue);
				// withCredentials only supported by XMLHttpRequest2
				if (withCredentials) {
					request.withCredentials = true;
				}
			} catch (e) {
				onError(e);
				return;
			}

			// send the request
			try {
				request.send();
			} catch (e) {
				onError(e);
			}
		}
	}]);

	return IO;
}();

exports.default = IO;

/***/ }),
/* 9 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

// var CryptoJS = require("crypto-js");
// var jsrsasign = require('jsrsasign');
var hmacSHA1 = __webpack_require__(27);
var base64 = __webpack_require__(30);
var utf8 = __webpack_require__(10);

var Signature = function () {
  function Signature() {
    _classCallCheck(this, Signature);
  }

  _createClass(Signature, null, [{
    key: 'randomUUID',
    value: function randomUUID() {
      var s = [];
      var hexDigits = "0123456789abcdef";
      for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
      }
      s[14] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
      s[19] = hexDigits.substr(s[19] & 0x3 | 0x8, 1); // bits 6-7 of the clock_seq_hi_and_reserved to 01
      s[8] = s[13] = s[18] = s[23] = "-";

      var uuid = s.join("");
      return uuid;
    }
  }, {
    key: 'aliyunEncodeURI',
    value: function aliyunEncodeURI(input) {
      var output = encodeURIComponent(input);

      output = output.replace(/\+/g, "%20").replace(/\*/g, "%2A").replace(/%7E/g, "~").replace(/!/g, "%21").replace(/\(/g, "%28").replace(/\)/g, "%29").replace(/'/g, "%27");

      return output;
    }
  }, {
    key: 'makeUTF8sort',
    value: function makeUTF8sort(ary, str1, str2) {
      if (!ary) {
        throw new Error('PrismPlayer Error: vid should not be null!');
      };
      var keys = [];
      for (var key in ary) {
        keys.push(key);
      }
      var pbugramsdic = keys.sort();
      var outputPub = "",
          length = pbugramsdic.length;
      for (var key = 0; key < length; key++) {

        var a3 = Signature.aliyunEncodeURI(pbugramsdic[key]);
        var b3 = Signature.aliyunEncodeURI(ary[pbugramsdic[key]]);

        if (outputPub == "") {

          outputPub = a3 + str1 + b3;
        } else {
          outputPub += str2 + a3 + str1 + b3;
        }
      }
      return outputPub;
    }

    //signature

  }, {
    key: 'makeChangeSiga',
    value: function makeChangeSiga(obj, secStr) {
      if (!obj) {
        throw new Error('PrismPlayer Error: vid should not be null!');
      };
      return base64.stringify(hmacSHA1('GET&' + Signature.aliyunEncodeURI('/') + '&' + Signature.aliyunEncodeURI(Signature.makeUTF8sort(obj, '=', '&')), secStr + '&'));
    }
  }]);

  return Signature;
}();

exports.default = Signature;

/***/ }),
/* 10 */
/***/ (function(module, exports, __webpack_require__) {

;(function (root, factory) {
	if (true) {
		// CommonJS
		module.exports = exports = factory(__webpack_require__(0));
	}
	else if (typeof define === "function" && define.amd) {
		// AMD
		define(["./core"], factory);
	}
	else {
		// Global (browser)
		factory(root.CryptoJS);
	}
}(this, function (CryptoJS) {

	return CryptoJS.enc.Utf8;

}));

/***/ }),
/* 11 */
/***/ (function(module, exports, __webpack_require__) {

;(function (root, factory) {
	if (true) {
		// CommonJS
		module.exports = exports = factory(__webpack_require__(0));
	}
	else if (typeof define === "function" && define.amd) {
		// AMD
		define(["./core"], factory);
	}
	else {
		// Global (browser)
		factory(root.CryptoJS);
	}
}(this, function (CryptoJS) {

	return CryptoJS.enc.Hex;

}));

/***/ }),
/* 12 */
/***/ (function(module, exports, __webpack_require__) {

module.exports = __webpack_require__(13);


/***/ }),
/* 13 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
var __WEBPACK_AMD_DEFINE_ARRAY__, __WEBPACK_AMD_DEFINE_RESULT__;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _vod = __webpack_require__(14);

var _vod2 = _interopRequireDefault(_vod);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var aliUpload = {
	Vod: _vod2.default
};
// AMD
if (true) {
	!(__WEBPACK_AMD_DEFINE_ARRAY__ = [], __WEBPACK_AMD_DEFINE_RESULT__ = function () {
		return aliUpload;
	}.apply(exports, __WEBPACK_AMD_DEFINE_ARRAY__),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
	// commonjs, 支持browserify
} else if ((typeof exports === 'undefined' ? 'undefined' : _typeof(exports)) === 'object' && (typeof module === 'undefined' ? 'undefined' : _typeof(module)) === 'object') {
	module['exports'] = aliUpload;
}
window.AliyunUpload = aliUpload;

// export default Vod;

/***/ }),
/* 14 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _vodupload = __webpack_require__(15);

var _ossupload = __webpack_require__(3);

var _oss = __webpack_require__(16);

var _oss2 = _interopRequireDefault(_oss);

var _base = __webpack_require__(18);

var _base2 = _interopRequireDefault(_base);

var _store = __webpack_require__(24);

var _store2 = _interopRequireDefault(_store);

var _log = __webpack_require__(4);

var _log2 = _interopRequireDefault(_log);

var _util = __webpack_require__(1);

var _util2 = _interopRequireDefault(_util);

var _guid = __webpack_require__(5);

var _guid2 = _interopRequireDefault(_guid);

var _data = __webpack_require__(26);

var _data2 = _interopRequireDefault(_data);

var _serverpoint = __webpack_require__(31);

var _serverpoint2 = _interopRequireDefault(_serverpoint);

var _fileService = __webpack_require__(32);

var _fileService2 = _interopRequireDefault(_fileService);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var MD5 = __webpack_require__(2);

var VODUpload = function () {
    function VODUpload(options) {
        _classCallCheck(this, VODUpload);

        this.options = options;
        this.options.partSize = this.options.partSize || _ossupload.UPLOADDEFAULT.PARTSIZE;
        this.options.parallel = this.options.parallel || _ossupload.UPLOADDEFAULT.PARALLEL;
        this.options.region = this.options.region || 'cn-shanghai';
        this.options.localCheckpoint = this.options.localCheckpoint || false;
        this.options.enableUploadProgress = this.options.enableUploadProgress || true;
        this._ossCreditor = new Object();
        this._state = _vodupload.VODSTATE.INIT;
        this._uploadList = [];
        this._curIndex = -1;
        this._ossUpload = null;
        this._log = new _log2.default();
        this._retryCount = 0;
        this._retryTotal = this.options.retryCount || 3;
        this._retryDuration = this.options.retryDuration || 2;
        this._state = _vodupload.VODSTATE.INIT;
        this._uploadWay = 'vod';
        this._onbeforeunload = false;
        this._initEvent();
        if (!_util2.default.isIntNum(this.options.userId)) {
            var msg = 'userId属性(阿里云账号ID)不能为空且只能为数字，如果获取账号ID请求参考：https://help.aliyun.com/knowledge_detail/37196.html?spm=a2c4g.11186631.6.554.3e491c54tWONlT';
            console.warn(msg);
            try {
                this.options.onUploadFailed({ file: {} }, 'InvalideUserId', msg);
            } catch (e) {
                console.log(e);
            }
        }
    }

    // 配置OSS参数


    _createClass(VODUpload, [{
        key: 'init',
        value: function init(accessKeyId, accessKeySecret, securityToken, expireTime) {
            this._retryCount = 0;
            if (securityToken && !expireTime || !securityToken && expireTime) {
                return false;
            }
            if (accessKeyId && !accessKeySecret || !accessKeyId && accessKeySecret) {
                return false;
            }

            this._ossCreditor.accessKeyId = accessKeyId;
            this._ossCreditor.accessKeySecret = accessKeySecret;
            this._ossCreditor.securityToken = securityToken;
            this._ossCreditor.expireTime = expireTime;
            return true;
        }
    }, {
        key: 'addFile',
        value: function addFile(file, endpoint, bucket, object, userData, callback) {
            if (!file) {
                return false;
            }
            if (file.size == 0) {
                try {
                    this.options.onUploadFailed({ file: file }, 'EmptyFile', "文件大小为0，不能上传");
                } catch (e) {
                    console.log(e);
                }
            }
            var options = this.options;
            // 判断重复添加
            for (var i = 0; i < this._uploadList.length; i++) {
                if (this._uploadList[i].file == file) {
                    return false;
                }
            }

            var uploadObject = new Object();
            uploadObject.file = file;
            uploadObject._endpoint = endpoint;
            uploadObject._bucket = bucket;
            uploadObject._object = object;
            uploadObject.state = _vodupload.UPLOADSTATE.INIT;
            uploadObject.isImage = _util2.default.isImage(file.name);
            if (!uploadObject.isImage && this.options.enableUploadProgress) {
                var that = this;
                _fileService2.default.getMd5(file, function (data) {
                    uploadObject.fileHash = data;
                    var cp = that._getCheckoutpoint(uploadObject);
                    if (!that.options.localCheckpoint && !cp) {
                        that._getCheckoutpointFromCloud(uploadObject, function (data) {
                            if (data.UploadPoint) {
                                var checkpoint = JSON.parse(data.UploadPoint);
                                if (checkpoint.loaded != 1) {
                                    uploadObject.checkpoint = checkpoint.checkpoint;
                                    uploadObject.loaded = checkpoint.loaded;
                                    uploadObject.videoId = data.VideoId;
                                    that._saveCheckoutpoint(uploadObject, checkpoint.checkpoint);
                                }
                            }
                            try {
                                if (that.options.addFileSuccess) {
                                    that.options.addFileSuccess(uploadObject);
                                }
                            } catch (e) {
                                console.log(e);
                            }
                        }, function (error) {
                            if (that.options.addFileFailed) {
                                that.options.addFileFailed(error);
                            }
                        });
                    }
                });
            }
            if (userData) {
                uploadObject.videoInfo = userData ? JSON.parse(userData).Vod : {};
                uploadObject.userData = _base2.default.encode(userData);
            }
            uploadObject.ri = _guid2.default.create();

            // 添加文件.
            this._uploadList.push(uploadObject);

            this._reportLog('20001', uploadObject, { ql: this._uploadList.length });

            return true;
        }
    }, {
        key: 'deleteFile',
        value: function deleteFile(index) {
            if (this.cancelFile(index)) {
                this._uploadList.splice(index, 1);
                return true;
            }

            return false;
        }
    }, {
        key: 'cleanList',
        value: function cleanList() {
            this.stopUpload();
            this._uploadList.length = 0;
            this._curIndex = -1;
        }
    }, {
        key: 'cancelFile',
        value: function cancelFile(index) {
            var options = this.options;

            if (index < 0 || index >= this._uploadList.length) {
                return false;
            }
            var item = this._uploadList[index];
            if (index == this._curIndex && item.state == _vodupload.UPLOADSTATE.UPLOADING) {
                item.state = _vodupload.UPLOADSTATE.CANCELED;
                var cp = this._getCheckoutpoint(item);
                if (cp && cp.checkpoint) {
                    cp = cp.checkpoint;
                }
                if (cp) {
                    this._ossUpload.abort(item);
                }
                this._removeCheckoutpoint(item);
                this.nextUpload();
            } else if (item.state != _vodupload.UPLOADSTATE.SUCCESS) {
                item.state = _vodupload.UPLOADSTATE.CANCELED;
            }

            this._reportLog('20008', item);

            return true;
        }
    }, {
        key: 'resumeFile',
        value: function resumeFile(index) {
            var options = this.options;

            if (index < 0 || index >= this._uploadList.length) {
                return false;
            }
            var item = this._uploadList[index];
            if (item.state != _vodupload.UPLOADSTATE.CANCELED) {
                return false;
            }

            item.state = _vodupload.UPLOADSTATE.INIT;

            return true;
        }
    }, {
        key: 'listFiles',
        value: function listFiles() {
            // list all.
            return this._uploadList;
        }
    }, {
        key: 'getCheckpoint',
        value: function getCheckpoint(file) {
            return this._getCheckoutpoint({ file: file });
        }

        // 开始上传

    }, {
        key: 'startUpload',
        value: function startUpload(index) {
            this._retryCount = 0;
            var options = this.options;

            if (this._state == _vodupload.VODSTATE.START || this._state == _vodupload.VODSTATE.EXPIRE) {
                console.log('already started or expired');
                return;
            }
            this._initState();
            this._curIndex = this._findUploadIndex();

            if (-1 == this._curIndex) {
                this._state = _vodupload.VODSTATE.END;
                return;
            }

            var curObject = this._uploadList[this._curIndex];
            this._ossUpload = null; //重新new一下oss上传对象
            this._upload(curObject);

            this._state = _vodupload.VODSTATE.START;
        }
    }, {
        key: 'nextUpload',
        value: function nextUpload() {
            var options = this.options;

            if (this._state != _vodupload.VODSTATE.START) {
                return;
            }

            this._curIndex = this._findUploadIndex();

            // 上传结束。
            if (-1 == this._curIndex) {
                this._state = _vodupload.VODSTATE.END;
                try {
                    if (options.onUploadEnd) {
                        options.onUploadEnd(curObject);
                    }
                } catch (e) {
                    console.log(e);
                }
                return;
            }

            var curObject = this._uploadList[this._curIndex];
            this._ossUpload = null; //重新new一下oss上传对象
            this._upload(curObject);
        }
    }, {
        key: 'clear',
        value: function clear(state) {
            var options = this.options;
            var num = 0;
            for (var i = 0; i < this._uploadList.length; i++) {
                if (options.uploadList[i].state == _vodupload.UPLOADSTATE.SUCCESS) {
                    num++;
                }

                if (this._uploadList[i].state == state) {
                    options.uploadList.splice(i, 1);
                    i--;
                }
            }

            if (options.onClear) {
                options.onClear(options.uploadList.length, num);
            }
        }

        // // 停止上传

    }, {
        key: 'stopUpload',
        value: function stopUpload() {
            if (this._state != _vodupload.VODSTATE.START && this._state != _vodupload.VODSTATE.FAILURE && this._curIndex != -1) {
                return;
            }
            if (this._curIndex != -1) {
                var item = this._uploadList[this._curIndex];
                this._state = _vodupload.VODSTATE.STOP;
                item.state = _vodupload.UPLOADSTATE.STOPED;
                this._changeState(item, _vodupload.UPLOADSTATE.STOPED);
                this._ossUpload.cancel();
            }
        }

        // // 停止上传
        // resumeUpload() {
        //     if (this._state == VODSTATE.STOP &&!this._curIndex) {
        //         var item = this._uploadList[this._curIndex];
        //         if(item.state == UPLOADSTATE.STOPED) {
        //             item.state = UPLOADSTATE.UPLOADING;
        //             this._ossUpload.resumeUpload();
        //             this._state = VODSTATE.START;
        //             return;
        //         }
        //     }
        // }
        // 恢复上传

    }, {
        key: 'resumeUploadWithAuth',
        value: function resumeUploadWithAuth(uploadAuth) {
            var self = this;

            if (!uploadAuth) {
                return false;
            }
            var key = JSON.parse(_base2.default.decode(uploadAuth));
            if (!key.AccessKeyId || !key.AccessKeySecret || !key.SecurityToken || !key.Expiration) {
                return false;
            }

            return self.resumeUploadWithToken(key.AccessKeyId, key.AccessKeySecret, key.SecurityToken, key.Expiration);
        }

        // 恢复上传

    }, {
        key: 'resumeUploadWithToken',
        value: function resumeUploadWithToken(accessKeyId, accessKeySecret, securityToken, expireTime) {
            var options = this.options;

            if (!accessKeyId || !accessKeySecret || !securityToken || !expireTime) {
                return false;
            }

            if (this._state != _vodupload.VODSTATE.EXPIRE) {
                return false;
            }

            if (-1 == this._curIndex) {
                return false;
            }
            var curObject = "";
            if (this._uploadList.length > this._curIndex) {
                curObject = this._uploadList[this._curIndex];
            }
            if (curObject) {
                this.init(accessKeyId, accessKeySecret, securityToken, expireTime);
                this._state = _vodupload.VODSTATE.START;
                this._ossUpload = null;
                this._uploadCore(curObject, curObject.retry);
                curObject.retry = false;
            }
            return true;
        }
    }, {
        key: 'resumeUploadWithSTSToken',
        value: function resumeUploadWithSTSToken(accessKeyId, accessKeySecret, securityToken) {
            if (-1 == this._curIndex) {
                return false;
            }
            if (this._state != _vodupload.VODSTATE.EXPIRE) {
                return false;
            }

            if (this._uploadList.length > this._curIndex) {
                var curObject = this._uploadList[this._curIndex];
                if (curObject.object) {
                    this._refreshSTSTokenUpload(curObject, accessKeyId, accessKeySecret, securityToken);
                } else {
                    this.setSTSToken(curObject, accessKeyId, accessKeySecret, securityToken);
                }
            }
        }
    }, {
        key: 'setSTSTokenDirectlyUpload',
        value: function setSTSTokenDirectlyUpload(uploadInfo, accessKeyId, accessKeySecret, securityToken, expiration) {
            if (!accessKeyId || !accessKeySecret || !securityToken || !expiration) {
                console.log('accessKeyId、ccessKeySecret、securityToken and expiration should not be empty.');
                return false;
            }
            this._ut = "oss";
            var curObject = uploadInfo;
            this.init(accessKeyId, accessKeySecret, securityToken, expiration);
            curObject.endpoint = curObject._endpoint;
            curObject.bucket = curObject._bucket;
            curObject.object = curObject._object;
            this._ossUpload = null;
            this._uploadCore(curObject, uploadInfo.retry);
            uploadInfo.retry = false;
        }

        // 设置上传凭证

    }, {
        key: 'setSTSToken',
        value: function setSTSToken(uploadInfo, accessKeyId, accessKeySecret, securityToken) {
            if (!accessKeyId || !accessKeySecret || !securityToken) {
                console.log('accessKeyId、ccessKeySecret、securityToken should not be empty.');
                return false;
            }
            this._ut = "vod";
            this._uploadWay = 'sts';
            var videoInfo = uploadInfo.videoInfo;
            var params = {
                'accessKeyId': accessKeyId,
                'securityToken': securityToken,
                'accessKeySecret': accessKeySecret,
                'fileName': uploadInfo.file.name,
                'title': videoInfo.Title,
                'requestId': uploadInfo.ri,
                'region': this.options.region
            };

            if (videoInfo.ImageType) {
                params.imageType = videoInfo.ImageType;
            }

            if (videoInfo.ImageExt) {
                params.imageExt = videoInfo.ImageExt;
            }

            if (videoInfo.FileSize) {
                params.fileSize = videoInfo.FileSize;
            }
            if (videoInfo.Description) {
                params.description = videoInfo.Description;
            }

            if (videoInfo.CateId) {
                params.cateId = videoInfo.CateId;
            }

            if (videoInfo.Tags) {
                params.tags = videoInfo.Tags;
            }

            if (videoInfo.TemplateGroupId) {
                params.templateGroupId = videoInfo.TemplateGroupId;
            }
            if (videoInfo.StorageLocation) {
                params.storageLocation = videoInfo.StorageLocation;
            }

            if (videoInfo.CoverURL) {
                params.coverUrl = videoInfo.CoverURL;
            }

            if (videoInfo.TransCodeMode) {
                params.transCodeMode = videoInfo.TransCodeMode;
            }

            if (videoInfo.UserData) {
                params.userData = videoInfo.UserData;
            }
            var that = this,
                func = "getUploadAuth";
            if (uploadInfo.videoId) {
                params.videoId = uploadInfo.videoId;
                func = "refreshUploadAuth";
            } else if (uploadInfo.isImage) {
                func = "getImageUploadAuth";
            }
            _data2.default[func](params, function (result) {
                uploadInfo.videoId = result.VideoId ? result.VideoId : uploadInfo.videoId;
                that.setUploadAuthAndAddress(uploadInfo, result.UploadAuth, result.UploadAddress);
                that._state = _vodupload.VODSTATE.START;
            }, function (error) {
                that._error(uploadInfo, {
                    name: error.Code,
                    code: error.Code,
                    message: error.Message,
                    requestId: error.RequestId
                });
            });
        }

        // 设置上传凭证

    }, {
        key: 'setUploadAuthAndAddress',
        value: function setUploadAuthAndAddress(uploadInfo, uploadAuth, uploadAddress, videoId) {
            if (!uploadInfo || !uploadAuth || !uploadAddress) {
                return false;
            }
            var authKey = JSON.parse(_base2.default.decode(uploadAuth));
            if (!authKey.AccessKeyId || !authKey.AccessKeySecret || !authKey.SecurityToken || !authKey.Expiration) {
                console.error('uploadauth is invalid');
                return false;
            }

            var addressKey = {};
            var curObject = uploadInfo;
            if (uploadAddress) {
                addressKey = JSON.parse(_base2.default.decode(uploadAddress));
                if (!addressKey.Endpoint || !addressKey.Bucket || !addressKey.FileName) {
                    console.error('uploadAddress is invalid');
                    return false;
                }
            } else {
                addressKey.Endpoint = curObject.endpoint;
                addressKey.Bucket = curObject.bucket;
                addressKey.FileName = curObject.object;
            }
            this._ut = "vod";
            this._uploadWay = 'vod';
            this.options.region = this.options.region || authKey.Region;

            this.init(authKey.AccessKeyId, authKey.AccessKeySecret, authKey.SecurityToken, authKey.Expiration);
            curObject.endpoint = curObject._endpoint ? curObject._endpoint : addressKey.Endpoint;
            curObject.bucket = curObject._bucket ? curObject._bucket : addressKey.Bucket;
            curObject.object = curObject._object ? curObject._object : addressKey.FileName;
            curObject.region = this.options.region;
            if (videoId) {
                curObject.videoId = videoId;
            }
            this._ossUpload = null;
            this._uploadCore(curObject, uploadInfo.retry);
            uploadInfo.retry = false;
        }
    }, {
        key: '_refreshSTSTokenUpload',
        value: function _refreshSTSTokenUpload(uploadInfo, accessKeyId, accessKeySecret, securityToken) {
            if (!accessKeyId || !accessKeySecret || !securityToken) {
                console.log('accessKeyId、ccessKeySecret、securityToken should not be empty.');
                return false;
            }
            var params = {
                'accessKeyId': accessKeyId,
                'securityToken': securityToken,
                'accessKeySecret': accessKeySecret,
                'videoId': uploadInfo.object,
                'requestId': uploadInfo.ri,
                'region': this.options.region
            };
            var that = this,
                func = "refreshUploadAuth";
            if (uploadInfo.isImage) {
                func = "getImageUploadAuth";
            }
            _data2.default[func](params, function (result) {
                that.setUploadAuthAndAddress(uploadInfo, result.UploadAuth, UploadAddress);
                that._state = _vodupload.VODSTATE.START;
            }, function (error) {
                that._error(uploadInfo, {
                    name: error.Code,
                    code: error.Code,
                    message: error.Message,
                    requestId: error.RequestId
                });
            });
        }
    }, {
        key: '_upload',
        value: function _upload(curObject) {
            var retry = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;

            var options = this.options;
            curObject.retry = retry;
            if (options.onUploadstarted && !retry) {
                try {
                    var cp = this._getCheckoutpoint(curObject);
                    if (cp && cp.state != _vodupload.UPLOADSTATE.UPLOADING) {
                        curObject.checkpoint = cp;

                        curObject.videoId = cp.videoId;
                    }

                    options.onUploadstarted(curObject);
                } catch (e) {
                    console.log(e);
                }
            }
        }
    }, {
        key: '_uploadCore',
        value: function _uploadCore(curObject) {
            var retry = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : false;

            if (!this._ossCreditor.accessKeyId || !this._ossCreditor.accessKeySecret || !this._ossCreditor.securityToken) {
                throw new Error('AccessKeyId、AccessKeySecret、securityToken should not be null');
            }

            curObject.state = _vodupload.UPLOADSTATE.UPLOADING;
            if (!this._ossUpload) {
                curObject.endpoint = curObject.endpoint || 'http://oss-cn-hangzhou.aliyuncs.com';

                var that = this;
                this._ossUpload = new _oss2.default({
                    bucket: curObject.bucket,
                    endpoint: curObject.endpoint,
                    AccessKeyId: this._ossCreditor.accessKeyId,
                    AccessKeySecret: this._ossCreditor.accessKeySecret,
                    SecurityToken: this._ossCreditor.securityToken,
                    timeout: this.options.timeout
                }, {
                    onerror: function onerror(obj, info) {
                        that._error.call(that, obj, info);
                    },
                    oncomplete: function oncomplete(obj, info) {
                        that._complete.call(that, obj, info);
                    },
                    onprogress: function onprogress(obj, info, res) {
                        that._progress.call(that, obj, info, res);
                    }
                });
            }
            var type = _util2.default.getFileType(curObject.file.name),
                cp = this._getCheckoutpoint(curObject),
                uploadId = 0,
                vid = "",
                state = "";
            if (cp && cp.checkpoint) {
                state = cp.state;
                vid = cp.videoId;
                cp = cp.checkpoint;
            }
            if (cp && vid == curObject.videoId && state != _vodupload.UPLOADSTATE.UPLOADING) {
                cp.file = curObject.file;
                curObject.checkpoint = cp;
                uploadId = cp.uploadId;
            }
            var partSize = this._adjustPartSize(curObject);
            this._reportLog('20002', curObject, { ft: type,
                fs: curObject.file.size,
                bu: curObject.bucket,
                ok: curObject.object,
                vid: curObject.videoId || "",
                fn: curObject.file.name,
                fw: null,
                fh: null,
                ps: partSize
            });

            var config = {
                headers: {
                    'x-oss-notification': curObject.userData ? curObject.userData : ""
                },
                partSize: partSize,
                parallel: this.options.parallel
            };
            this._ossUpload.upload(curObject, config);
        }
    }, {
        key: '_findUploadIndex',
        value: function _findUploadIndex() {
            var index = -1;
            for (var i = 0; i < this._uploadList.length; i++) {
                if (this._uploadList[i].state == _vodupload.UPLOADSTATE.INIT) {
                    index = i;
                    break;
                }
            }

            return index;
        }
    }, {
        key: '_error',
        value: function _error(uploadInfo, evt) {
            if (evt.name == 'cancel') {
                try {
                    this.options.onUploadCanceled(uploadInfo, evt);
                } catch (e) {
                    console.log(e);
                }
            } else if (evt.message.indexOf('InvalidAccessKeyIdError') > 0 || evt.name == "SignatureDoesNotMatchError" || evt.code == "SecurityTokenExpired" || evt.code == "InvalidSecurityToken.Expired" || evt.code == "InvalidAccessKeyId" && this._ossCreditor.securityToken) {
                if (this.options.onUploadTokenExpired) {
                    this._state = _vodupload.VODSTATE.EXPIRE;
                    uploadInfo.state = _vodupload.UPLOADSTATE.FAIlURE;

                    try {
                        this.options.onUploadTokenExpired(uploadInfo, evt);
                    } catch (e) {
                        console.log(e);
                    }
                }
                return;
            } else if ((evt.name == "RequestTimeoutError" || evt.name == 'ConnectionTimeout' || evt.name == 'ConnectionTimeoutError') && this._retryTotal > this._retryCount) {
                var that = this;
                setTimeout(function () {
                    that._uploadCore(uploadInfo, true);
                }, that._retryDuration * 1000);
                this._retryCount++;
                return;
            } else {
                if (evt.name == "NoSuchUploadError") {
                    this._removeCheckoutpoint(uploadInfo);
                }
                this._handleError(uploadInfo, evt);
            }
        }
    }, {
        key: '_handleError',
        value: function _handleError(uploadInfo, evt) {
            var changeState = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : true;

            var state = _vodupload.UPLOADSTATE.FAIlURE;
            if (uploadInfo.state != _vodupload.UPLOADSTATE.CANCELED) {
                uploadInfo.state = _vodupload.UPLOADSTATE.FAIlURE;
                this._state = _vodupload.VODSTATE.FAILURE;
                if (this.options.onUploadFailed) {
                    if (evt && evt.code && evt.message) {
                        try {
                            this.options.onUploadFailed(uploadInfo, evt.code, evt.message);
                        } catch (e) {
                            console.log(e);
                        }
                    }
                }
            }
            if (changeState) {
                this._changeState(uploadInfo, state);
            }
            this._reportLog('20006', uploadInfo, {
                code: evt.name,
                message: evt.message,
                requestId: evt.requestId,
                fs: uploadInfo.file.size,
                bu: uploadInfo.bucket,
                ok: uploadInfo.object,
                fn: uploadInfo.file.name
            });
            this._reportLog('20004', uploadInfo, {
                requestId: evt.requestId,
                fs: uploadInfo.file.size,
                bu: uploadInfo.bucket,
                ok: uploadInfo.object,
                fn: uploadInfo.file.name

            });
            uploadInfo.ri = _guid2.default.create();
            var curIndex = this._findUploadIndex();
            if (curIndex != -1) //继续上传下一个文件
                {
                    var that = this;
                    this._state = _vodupload.VODSTATE.START;
                    setTimeout(function () {
                        that.nextUpload();
                    }, 100);
                }
        }
    }, {
        key: '_complete',
        value: function _complete(uploadInfo, result) {
            uploadInfo.state = _vodupload.UPLOADSTATE.SUCCESS;
            if (this.options.onUploadSucceed) {
                try {
                    this.options.onUploadSucceed(uploadInfo);
                } catch (e) {
                    console.log(e);
                }
            }
            var requestId = 0;
            if (result && result.res && result.res.headers) {
                requestId = result.res.headers['x-oss-request-id'];
            }
            this._removeCheckoutpoint(uploadInfo);
            var that = this;
            setTimeout(function () {
                that.nextUpload();
            }, 100);
            this._retryCount = 0;
            this._reportLog('20003', uploadInfo, {
                requestId: requestId
            });
        }
    }, {
        key: '_progress',
        value: function _progress(uploadInfo, info, res) {
            if (this.options.onUploadProgress) {
                try {
                    uploadInfo.loaded = info.loaded;
                    this.options.onUploadProgress(uploadInfo, info.total, info.loaded);
                } catch (e) {
                    console.log(e);
                }
            }
            var checkpoint = info.checkpoint,
                uploadId = 0;
            if (checkpoint) {
                uploadInfo.checkpoint = checkpoint;
                this._saveCheckoutpoint(uploadInfo, checkpoint, _vodupload.UPLOADSTATE.UPLOADING);
                uploadId = checkpoint.uploadId;
            }
            this._retryCount = 0;
            var pn = this._getPortNumber(checkpoint);
            var requestId = 0;
            if (res && res.headers) {
                requestId = res.headers['x-oss-request-id'];
            }
            if (info.loaded != 0) {
                this._reportLog('20007', uploadInfo, {
                    pn: pn,
                    requestId: requestId
                });
            }
            if (info.loaded != 1) {
                this._reportLog('20005', uploadInfo, {
                    UploadId: uploadId,
                    pn: pn + 1,
                    pr: uploadInfo.retry ? 1 : 0,
                    fs: uploadInfo.file.size,
                    bu: uploadInfo.bucket,
                    ok: uploadInfo.object,
                    fn: uploadInfo.file.name
                });
            }
            if (!uploadInfo.isImage && this._ut == 'vod' && this.options.enableUploadProgress) {
                var params = {
                    file: uploadInfo.file,
                    checkpoint: info,
                    userId: this.options.userId,
                    videoId: uploadInfo.videoId,
                    region: this.options.region,
                    fileHash: uploadInfo.fileHash
                };
                try {
                    _serverpoint2.default.upload(params);
                } catch (e) {
                    console.log(e);
                }
            }
        }
    }, {
        key: '_getPortNumber',
        value: function _getPortNumber(cp) {
            if (cp) {
                var doneParts = cp.doneParts;
                if (doneParts && doneParts.length > 0) {
                    return doneParts[doneParts.length - 1].number;
                }
            }
            return 0;
        }
    }, {
        key: '_removeCheckoutpoint',
        value: function _removeCheckoutpoint(uploadInfo) {
            var key = this._getCheckoutpointKey(uploadInfo);
            _store2.default.remove(key);
        }
    }, {
        key: '_getCheckoutpoint',
        value: function _getCheckoutpoint(uploadInfo) {
            var key = this._getCheckoutpointKey(uploadInfo);
            var value = _store2.default.get(key);
            if (value) {
                try {
                    return JSON.parse(value);
                } catch (e) {}
            }
            return "";
        }
    }, {
        key: '_saveCheckoutpoint',
        value: function _saveCheckoutpoint(uploadInfo, checkpoint, state) {
            if (checkpoint) {
                var key = this._getCheckoutpointKey(uploadInfo),
                    file = uploadInfo.file,
                    value = {
                    fileName: file.name,
                    lastModified: file.lastModified,
                    size: file.size,
                    object: uploadInfo.object,
                    videoId: uploadInfo.videoId,
                    bucket: uploadInfo.bucket,
                    endpoint: uploadInfo.endpoint,
                    checkpoint: checkpoint,
                    loaded: uploadInfo.loaded,
                    state: state
                };
                _store2.default.set(key, JSON.stringify(value));
            }
        }
    }, {
        key: '_changeState',
        value: function _changeState(uploadInfo, state) {
            var value = this._getCheckoutpoint(uploadInfo);
            if (value) {
                if (this._onbeforeunload = true) {
                    state = _vodupload.UPLOADSTATE.STOPED;
                }
                this._saveCheckoutpoint(uploadInfo, value.checkpoint, state);
            }
        }
    }, {
        key: '_getCheckoutpointKey',
        value: function _getCheckoutpointKey(info) {
            var key = 'upload_' + info.file.lastModified + '_' + info.file.name + '_' + info.file.size;
            return key;
        }
    }, {
        key: '_getCheckoutpointFromCloud',
        value: function _getCheckoutpointFromCloud(info, callback, failed) {
            var params = {
                userId: this.options.userId,
                uploadInfoList: [{
                    FileName: info.file.name,
                    FileSize: info.file.size,
                    FileCreateTime: info.file.lastModified,
                    FileHash: info.fileHash
                }],
                region: this.options.region
            };
            _serverpoint2.default.get(params, function (data) {
                callback(data);
            }, failed);
        }
    }, {
        key: '_reportLog',
        value: function _reportLog(e, info, params) {
            if (!params) {
                params = {};
            }
            params.ri = info.ri;
            if (this._ut) {
                params.ut = this._ut;
            }
            this._log.log(e, params);
        }
    }, {
        key: '_initEvent',
        value: function _initEvent() {
            var that = this;
            if (window) {
                window.onbeforeunload = function (e) {
                    that._onbeforeunload = true;
                    if (-1 == that._curIndex) {
                        return;
                    }
                    if (that._uploadList.length > that._curIndex) {
                        var _curObject = that._uploadList[that._curIndex];
                        that._changeState(_curObject, _vodupload.UPLOADSTATE.STOPED);
                    }
                };
            }
        }
    }, {
        key: '_initState',
        value: function _initState() {
            for (var i = 0; i < this._uploadList.length; i++) {
                var item = this._uploadList[i];
                if (item.state == _vodupload.UPLOADSTATE.FAIlURE || item.state == _vodupload.UPLOADSTATE.STOPED) {
                    item.state = _vodupload.UPLOADSTATE.INIT;
                }
            }
            this._state = _vodupload.VODSTATE.INIT;
        }
    }, {
        key: '_adjustPartSize',
        value: function _adjustPartSize(curObject) {
            var currentParts = curObject.file.size / this.options.partSize;
            if (currentParts > 10000) {
                return curObject.file.size / 9999;
            }
            return this.options.partSize;
        }
    }]);

    return VODUpload;
}();

exports.default = VODUpload;

/***/ }),
/* 15 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
        value: true
});
var UPLOADSTATE = exports.UPLOADSTATE = {
        INIT: "Ready",
        UPLOADING: "Uploading",
        SUCCESS: "Success",
        FAIlURE: "Failure",
        CANCELED: "Canceled",
        STOPED: "Stoped"
};

var VODSTATE = exports.VODSTATE = {
        INIT: "Init",
        START: "Start",
        STOP: "Stop",
        FAILURE: "Failure",
        EXPIRE: "Expire",
        END: "End"
};

/***/ }),
/* 16 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _ossupload = __webpack_require__(3);

var _uploaderror = __webpack_require__(17);

var _uploaderror2 = _interopRequireDefault(_uploaderror);

var _util = __webpack_require__(1);

var _util2 = _interopRequireDefault(_util);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

// import OSS  from 'ali-oss'

var OssUpload = function () {
    function OssUpload(config, callback) {
        _classCallCheck(this, OssUpload);

        if (!config) {
            // console.log('需要 config');
            return;
        }
        this._config = config;

        this.create(this._config);

        this._uploadInfo = null;
        this._callback = {};
        var moon = function moon() {};
        this._callback.onerror = callback.onerror || moon;
        this._callback.oncomplete = callback.oncomplete || moon;
        this._callback.onprogress = callback.onprogress || moon;
    }

    _createClass(OssUpload, [{
        key: 'create',
        value: function create(option) {
            option.endpoint = option.endpoint || this._config.endpoint;
            option.bucket = option.bucket || this._config.bucket;
            if (!option.AccessKeyId || !option.AccessKeySecret || !option.endpoint || !option.SecurityToken) {
                throw new Error('AccessKeyId、AccessKeySecret、endpoint should not be null');
            }
            var optionValues = {
                accessKeyId: option.AccessKeyId,
                accessKeySecret: option.AccessKeySecret,
                stsToken: option.SecurityToken,
                endpoint: option.endpoint || this._config.endpoint,
                bucket: option.bucket || this._config.bucket,
                secure: true
            };
            if (option.timeout) {
                optionValues.timeout = option.timeout;
            }
            this.oss = new OSS.Wrapper(optionValues);
        }
    }, {
        key: 'abort',
        value: function abort(uploadInfo) {
            if (uploadInfo.checkpoint) {
                var uploadId = uploadInfo.checkpoint.uploadId;
                this.oss.abortMultipartUpload(uploadInfo.object, uploadId);
            }
        }
    }, {
        key: 'getVersion',
        value: function getVersion() {}
    }, {
        key: 'cancel',
        value: function cancel() {
            if (this.oss.cancel) {
                this.oss.cancel();
            }
        }
    }, {
        key: 'upload',
        value: function upload(uploadInfo, options) {
            this._uploadInfo = uploadInfo;
            var that = this;
            var progress = function progress(percentage, checkpoint, res) {
                return function (done) {
                    that._progress(percentage, checkpoint, res);
                    done();
                };
            };
            var option = {
                parallel: options.parallel || this._config.parallel || _ossupload.UPLOADDEFAULT.PARALLEL,
                partSize: options.partSize || this._config.partSize || _ossupload.UPLOADDEFAULT.PARTSIZE,
                progress: progress
            };
            if (options.headers) {
                option.headers = options.headers;
            }
            if (uploadInfo.checkpoint) {
                option.checkpoint = uploadInfo.checkpoint;
            }
            if (!uploadInfo.bucket) {
                this.oss.options.bucket = uploadInfo.bucket;
            }

            if (!uploadInfo.endpoint) {
                this.oss.options.endpoint = uploadInfo.endpoint;
            }

            this.oss.multipartUpload(uploadInfo.object, uploadInfo.file, option).then(function (result, res) {
                that._complete(result);
            }).catch(function (err) {
                if (that.oss.cancel) {
                    if (that.oss && that.oss.isCancel()) {
                        console.log('oss is cancel as error');
                    } else {
                        that.oss.cancel();
                    }
                }
                that._error(err);
            });
        }
    }, {
        key: 'header',
        value: function header(uploadInfo, success, error) {
            this.oss.get(uploadInfo.object).then(function (result) {
                success(result);
            }).catch(function (err) {
                error(err);
            });
        }
    }, {
        key: '_progress',
        value: function _progress(percentage, checkpoint, res) {
            this._callback.onprogress(this._uploadInfo, {
                loaded: percentage,
                total: this._uploadInfo.file.size,
                checkpoint: checkpoint }, res);
        }
    }, {
        key: '_error',
        value: function _error(errorInfo) {
            this._callback.onerror(this._uploadInfo, errorInfo);
        }
    }, {
        key: '_complete',
        value: function _complete(result) {
            this._callback.oncomplete(this._uploadInfo, result);
        }
    }]);

    return OssUpload;
}();

exports.default = OssUpload;

/***/ }),
/* 17 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var VODUploadError = function () {
    function VODUploadError() {
        _classCallCheck(this, VODUploadError);
    }

    _createClass(VODUploadError, null, [{
        key: "format",
        value: function format(code) {
            if (arguments.length < 2) {
                return null;
            }

            var str = arguments[1];
            for (var i = 1; i < arguments.length; i++) {
                var re = new RegExp('\\{' + (i - 1) + '\\}', 'gm');
                str = str.replace(re, arguments[i + 1]);
            }

            return { "code": code, "message": str };
        }
    }, {
        key: "CODE",
        get: function get() {
            return {
                SUCCESS: "Successful",
                EmptyValue: "InvalidParameter.EmptyValue",
                STSInvalid: "InvalidParameter.TokenInvalid",
                ReadFileError: "ReadFileError",
                FILEDUPLICATION: "FileDuplication",
                UploadALEADRYSTARTED: "UploadAlearyStarted"

            };
        }
    }, {
        key: "MESSAGE",
        get: function get() {
            return {
                SUCCESS: "Successful",
                EmptyValue: "参数 {0} 不能为空。",
                STSInvalid: "STS参数非法， accessKeyId、accessKeySecret、secretToken、expireTime都不能为空。",
                ReadFileError: "读取文件{0}{1}失败.",
                FILEDUPLICATION: "文件重复添加 {0}",
                UploadALEADRYSTARTED: "重复开始."
            };
        }
    }]);

    return VODUploadError;
}();

exports.default = VODUploadError;

/***/ }),
/* 18 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(Buffer) {

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Base64 = function () {
  function Base64() {
    _classCallCheck(this, Base64);
  }

  _createClass(Base64, null, [{
    key: 'encode',
    value: function encode(string) {
      return new Buffer(string).toString('base64');
    }
  }, {
    key: 'decode',
    value: function decode(string) {
      return new Buffer(string, 'base64').toString();
    }
  }]);

  return Base64;
}();

exports.default = Base64;
/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__(19).Buffer))

/***/ }),
/* 19 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(global) {/*!
 * The buffer module from node.js, for the browser.
 *
 * @author   Feross Aboukhadijeh <feross@feross.org> <http://feross.org>
 * @license  MIT
 */
/* eslint-disable no-proto */



var base64 = __webpack_require__(21)
var ieee754 = __webpack_require__(22)
var isArray = __webpack_require__(23)

exports.Buffer = Buffer
exports.SlowBuffer = SlowBuffer
exports.INSPECT_MAX_BYTES = 50

/**
 * If `Buffer.TYPED_ARRAY_SUPPORT`:
 *   === true    Use Uint8Array implementation (fastest)
 *   === false   Use Object implementation (most compatible, even IE6)
 *
 * Browsers that support typed arrays are IE 10+, Firefox 4+, Chrome 7+, Safari 5.1+,
 * Opera 11.6+, iOS 4.2+.
 *
 * Due to various browser bugs, sometimes the Object implementation will be used even
 * when the browser supports typed arrays.
 *
 * Note:
 *
 *   - Firefox 4-29 lacks support for adding new properties to `Uint8Array` instances,
 *     See: https://bugzilla.mozilla.org/show_bug.cgi?id=695438.
 *
 *   - Chrome 9-10 is missing the `TypedArray.prototype.subarray` function.
 *
 *   - IE10 has a broken `TypedArray.prototype.subarray` function which returns arrays of
 *     incorrect length in some situations.

 * We detect these buggy browsers and set `Buffer.TYPED_ARRAY_SUPPORT` to `false` so they
 * get the Object implementation, which is slower but behaves correctly.
 */
Buffer.TYPED_ARRAY_SUPPORT = global.TYPED_ARRAY_SUPPORT !== undefined
  ? global.TYPED_ARRAY_SUPPORT
  : typedArraySupport()

/*
 * Export kMaxLength after typed array support is determined.
 */
exports.kMaxLength = kMaxLength()

function typedArraySupport () {
  try {
    var arr = new Uint8Array(1)
    arr.__proto__ = {__proto__: Uint8Array.prototype, foo: function () { return 42 }}
    return arr.foo() === 42 && // typed array instances can be augmented
        typeof arr.subarray === 'function' && // chrome 9-10 lack `subarray`
        arr.subarray(1, 1).byteLength === 0 // ie10 has broken `subarray`
  } catch (e) {
    return false
  }
}

function kMaxLength () {
  return Buffer.TYPED_ARRAY_SUPPORT
    ? 0x7fffffff
    : 0x3fffffff
}

function createBuffer (that, length) {
  if (kMaxLength() < length) {
    throw new RangeError('Invalid typed array length')
  }
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    // Return an augmented `Uint8Array` instance, for best performance
    that = new Uint8Array(length)
    that.__proto__ = Buffer.prototype
  } else {
    // Fallback: Return an object instance of the Buffer class
    if (that === null) {
      that = new Buffer(length)
    }
    that.length = length
  }

  return that
}

/**
 * The Buffer constructor returns instances of `Uint8Array` that have their
 * prototype changed to `Buffer.prototype`. Furthermore, `Buffer` is a subclass of
 * `Uint8Array`, so the returned instances will have all the node `Buffer` methods
 * and the `Uint8Array` methods. Square bracket notation works as expected -- it
 * returns a single octet.
 *
 * The `Uint8Array` prototype remains unmodified.
 */

function Buffer (arg, encodingOrOffset, length) {
  if (!Buffer.TYPED_ARRAY_SUPPORT && !(this instanceof Buffer)) {
    return new Buffer(arg, encodingOrOffset, length)
  }

  // Common case.
  if (typeof arg === 'number') {
    if (typeof encodingOrOffset === 'string') {
      throw new Error(
        'If encoding is specified then the first argument must be a string'
      )
    }
    return allocUnsafe(this, arg)
  }
  return from(this, arg, encodingOrOffset, length)
}

Buffer.poolSize = 8192 // not used by this implementation

// TODO: Legacy, not needed anymore. Remove in next major version.
Buffer._augment = function (arr) {
  arr.__proto__ = Buffer.prototype
  return arr
}

function from (that, value, encodingOrOffset, length) {
  if (typeof value === 'number') {
    throw new TypeError('"value" argument must not be a number')
  }

  if (typeof ArrayBuffer !== 'undefined' && value instanceof ArrayBuffer) {
    return fromArrayBuffer(that, value, encodingOrOffset, length)
  }

  if (typeof value === 'string') {
    return fromString(that, value, encodingOrOffset)
  }

  return fromObject(that, value)
}

/**
 * Functionally equivalent to Buffer(arg, encoding) but throws a TypeError
 * if value is a number.
 * Buffer.from(str[, encoding])
 * Buffer.from(array)
 * Buffer.from(buffer)
 * Buffer.from(arrayBuffer[, byteOffset[, length]])
 **/
Buffer.from = function (value, encodingOrOffset, length) {
  return from(null, value, encodingOrOffset, length)
}

if (Buffer.TYPED_ARRAY_SUPPORT) {
  Buffer.prototype.__proto__ = Uint8Array.prototype
  Buffer.__proto__ = Uint8Array
  if (typeof Symbol !== 'undefined' && Symbol.species &&
      Buffer[Symbol.species] === Buffer) {
    // Fix subarray() in ES2016. See: https://github.com/feross/buffer/pull/97
    Object.defineProperty(Buffer, Symbol.species, {
      value: null,
      configurable: true
    })
  }
}

function assertSize (size) {
  if (typeof size !== 'number') {
    throw new TypeError('"size" argument must be a number')
  } else if (size < 0) {
    throw new RangeError('"size" argument must not be negative')
  }
}

function alloc (that, size, fill, encoding) {
  assertSize(size)
  if (size <= 0) {
    return createBuffer(that, size)
  }
  if (fill !== undefined) {
    // Only pay attention to encoding if it's a string. This
    // prevents accidentally sending in a number that would
    // be interpretted as a start offset.
    return typeof encoding === 'string'
      ? createBuffer(that, size).fill(fill, encoding)
      : createBuffer(that, size).fill(fill)
  }
  return createBuffer(that, size)
}

/**
 * Creates a new filled Buffer instance.
 * alloc(size[, fill[, encoding]])
 **/
Buffer.alloc = function (size, fill, encoding) {
  return alloc(null, size, fill, encoding)
}

function allocUnsafe (that, size) {
  assertSize(size)
  that = createBuffer(that, size < 0 ? 0 : checked(size) | 0)
  if (!Buffer.TYPED_ARRAY_SUPPORT) {
    for (var i = 0; i < size; ++i) {
      that[i] = 0
    }
  }
  return that
}

/**
 * Equivalent to Buffer(num), by default creates a non-zero-filled Buffer instance.
 * */
Buffer.allocUnsafe = function (size) {
  return allocUnsafe(null, size)
}
/**
 * Equivalent to SlowBuffer(num), by default creates a non-zero-filled Buffer instance.
 */
Buffer.allocUnsafeSlow = function (size) {
  return allocUnsafe(null, size)
}

function fromString (that, string, encoding) {
  if (typeof encoding !== 'string' || encoding === '') {
    encoding = 'utf8'
  }

  if (!Buffer.isEncoding(encoding)) {
    throw new TypeError('"encoding" must be a valid string encoding')
  }

  var length = byteLength(string, encoding) | 0
  that = createBuffer(that, length)

  var actual = that.write(string, encoding)

  if (actual !== length) {
    // Writing a hex string, for example, that contains invalid characters will
    // cause everything after the first invalid character to be ignored. (e.g.
    // 'abxxcd' will be treated as 'ab')
    that = that.slice(0, actual)
  }

  return that
}

function fromArrayLike (that, array) {
  var length = array.length < 0 ? 0 : checked(array.length) | 0
  that = createBuffer(that, length)
  for (var i = 0; i < length; i += 1) {
    that[i] = array[i] & 255
  }
  return that
}

function fromArrayBuffer (that, array, byteOffset, length) {
  array.byteLength // this throws if `array` is not a valid ArrayBuffer

  if (byteOffset < 0 || array.byteLength < byteOffset) {
    throw new RangeError('\'offset\' is out of bounds')
  }

  if (array.byteLength < byteOffset + (length || 0)) {
    throw new RangeError('\'length\' is out of bounds')
  }

  if (byteOffset === undefined && length === undefined) {
    array = new Uint8Array(array)
  } else if (length === undefined) {
    array = new Uint8Array(array, byteOffset)
  } else {
    array = new Uint8Array(array, byteOffset, length)
  }

  if (Buffer.TYPED_ARRAY_SUPPORT) {
    // Return an augmented `Uint8Array` instance, for best performance
    that = array
    that.__proto__ = Buffer.prototype
  } else {
    // Fallback: Return an object instance of the Buffer class
    that = fromArrayLike(that, array)
  }
  return that
}

function fromObject (that, obj) {
  if (Buffer.isBuffer(obj)) {
    var len = checked(obj.length) | 0
    that = createBuffer(that, len)

    if (that.length === 0) {
      return that
    }

    obj.copy(that, 0, 0, len)
    return that
  }

  if (obj) {
    if ((typeof ArrayBuffer !== 'undefined' &&
        obj.buffer instanceof ArrayBuffer) || 'length' in obj) {
      if (typeof obj.length !== 'number' || isnan(obj.length)) {
        return createBuffer(that, 0)
      }
      return fromArrayLike(that, obj)
    }

    if (obj.type === 'Buffer' && isArray(obj.data)) {
      return fromArrayLike(that, obj.data)
    }
  }

  throw new TypeError('First argument must be a string, Buffer, ArrayBuffer, Array, or array-like object.')
}

function checked (length) {
  // Note: cannot use `length < kMaxLength()` here because that fails when
  // length is NaN (which is otherwise coerced to zero.)
  if (length >= kMaxLength()) {
    throw new RangeError('Attempt to allocate Buffer larger than maximum ' +
                         'size: 0x' + kMaxLength().toString(16) + ' bytes')
  }
  return length | 0
}

function SlowBuffer (length) {
  if (+length != length) { // eslint-disable-line eqeqeq
    length = 0
  }
  return Buffer.alloc(+length)
}

Buffer.isBuffer = function isBuffer (b) {
  return !!(b != null && b._isBuffer)
}

Buffer.compare = function compare (a, b) {
  if (!Buffer.isBuffer(a) || !Buffer.isBuffer(b)) {
    throw new TypeError('Arguments must be Buffers')
  }

  if (a === b) return 0

  var x = a.length
  var y = b.length

  for (var i = 0, len = Math.min(x, y); i < len; ++i) {
    if (a[i] !== b[i]) {
      x = a[i]
      y = b[i]
      break
    }
  }

  if (x < y) return -1
  if (y < x) return 1
  return 0
}

Buffer.isEncoding = function isEncoding (encoding) {
  switch (String(encoding).toLowerCase()) {
    case 'hex':
    case 'utf8':
    case 'utf-8':
    case 'ascii':
    case 'latin1':
    case 'binary':
    case 'base64':
    case 'ucs2':
    case 'ucs-2':
    case 'utf16le':
    case 'utf-16le':
      return true
    default:
      return false
  }
}

Buffer.concat = function concat (list, length) {
  if (!isArray(list)) {
    throw new TypeError('"list" argument must be an Array of Buffers')
  }

  if (list.length === 0) {
    return Buffer.alloc(0)
  }

  var i
  if (length === undefined) {
    length = 0
    for (i = 0; i < list.length; ++i) {
      length += list[i].length
    }
  }

  var buffer = Buffer.allocUnsafe(length)
  var pos = 0
  for (i = 0; i < list.length; ++i) {
    var buf = list[i]
    if (!Buffer.isBuffer(buf)) {
      throw new TypeError('"list" argument must be an Array of Buffers')
    }
    buf.copy(buffer, pos)
    pos += buf.length
  }
  return buffer
}

function byteLength (string, encoding) {
  if (Buffer.isBuffer(string)) {
    return string.length
  }
  if (typeof ArrayBuffer !== 'undefined' && typeof ArrayBuffer.isView === 'function' &&
      (ArrayBuffer.isView(string) || string instanceof ArrayBuffer)) {
    return string.byteLength
  }
  if (typeof string !== 'string') {
    string = '' + string
  }

  var len = string.length
  if (len === 0) return 0

  // Use a for loop to avoid recursion
  var loweredCase = false
  for (;;) {
    switch (encoding) {
      case 'ascii':
      case 'latin1':
      case 'binary':
        return len
      case 'utf8':
      case 'utf-8':
      case undefined:
        return utf8ToBytes(string).length
      case 'ucs2':
      case 'ucs-2':
      case 'utf16le':
      case 'utf-16le':
        return len * 2
      case 'hex':
        return len >>> 1
      case 'base64':
        return base64ToBytes(string).length
      default:
        if (loweredCase) return utf8ToBytes(string).length // assume utf8
        encoding = ('' + encoding).toLowerCase()
        loweredCase = true
    }
  }
}
Buffer.byteLength = byteLength

function slowToString (encoding, start, end) {
  var loweredCase = false

  // No need to verify that "this.length <= MAX_UINT32" since it's a read-only
  // property of a typed array.

  // This behaves neither like String nor Uint8Array in that we set start/end
  // to their upper/lower bounds if the value passed is out of range.
  // undefined is handled specially as per ECMA-262 6th Edition,
  // Section 13.3.3.7 Runtime Semantics: KeyedBindingInitialization.
  if (start === undefined || start < 0) {
    start = 0
  }
  // Return early if start > this.length. Done here to prevent potential uint32
  // coercion fail below.
  if (start > this.length) {
    return ''
  }

  if (end === undefined || end > this.length) {
    end = this.length
  }

  if (end <= 0) {
    return ''
  }

  // Force coersion to uint32. This will also coerce falsey/NaN values to 0.
  end >>>= 0
  start >>>= 0

  if (end <= start) {
    return ''
  }

  if (!encoding) encoding = 'utf8'

  while (true) {
    switch (encoding) {
      case 'hex':
        return hexSlice(this, start, end)

      case 'utf8':
      case 'utf-8':
        return utf8Slice(this, start, end)

      case 'ascii':
        return asciiSlice(this, start, end)

      case 'latin1':
      case 'binary':
        return latin1Slice(this, start, end)

      case 'base64':
        return base64Slice(this, start, end)

      case 'ucs2':
      case 'ucs-2':
      case 'utf16le':
      case 'utf-16le':
        return utf16leSlice(this, start, end)

      default:
        if (loweredCase) throw new TypeError('Unknown encoding: ' + encoding)
        encoding = (encoding + '').toLowerCase()
        loweredCase = true
    }
  }
}

// The property is used by `Buffer.isBuffer` and `is-buffer` (in Safari 5-7) to detect
// Buffer instances.
Buffer.prototype._isBuffer = true

function swap (b, n, m) {
  var i = b[n]
  b[n] = b[m]
  b[m] = i
}

Buffer.prototype.swap16 = function swap16 () {
  var len = this.length
  if (len % 2 !== 0) {
    throw new RangeError('Buffer size must be a multiple of 16-bits')
  }
  for (var i = 0; i < len; i += 2) {
    swap(this, i, i + 1)
  }
  return this
}

Buffer.prototype.swap32 = function swap32 () {
  var len = this.length
  if (len % 4 !== 0) {
    throw new RangeError('Buffer size must be a multiple of 32-bits')
  }
  for (var i = 0; i < len; i += 4) {
    swap(this, i, i + 3)
    swap(this, i + 1, i + 2)
  }
  return this
}

Buffer.prototype.swap64 = function swap64 () {
  var len = this.length
  if (len % 8 !== 0) {
    throw new RangeError('Buffer size must be a multiple of 64-bits')
  }
  for (var i = 0; i < len; i += 8) {
    swap(this, i, i + 7)
    swap(this, i + 1, i + 6)
    swap(this, i + 2, i + 5)
    swap(this, i + 3, i + 4)
  }
  return this
}

Buffer.prototype.toString = function toString () {
  var length = this.length | 0
  if (length === 0) return ''
  if (arguments.length === 0) return utf8Slice(this, 0, length)
  return slowToString.apply(this, arguments)
}

Buffer.prototype.equals = function equals (b) {
  if (!Buffer.isBuffer(b)) throw new TypeError('Argument must be a Buffer')
  if (this === b) return true
  return Buffer.compare(this, b) === 0
}

Buffer.prototype.inspect = function inspect () {
  var str = ''
  var max = exports.INSPECT_MAX_BYTES
  if (this.length > 0) {
    str = this.toString('hex', 0, max).match(/.{2}/g).join(' ')
    if (this.length > max) str += ' ... '
  }
  return '<Buffer ' + str + '>'
}

Buffer.prototype.compare = function compare (target, start, end, thisStart, thisEnd) {
  if (!Buffer.isBuffer(target)) {
    throw new TypeError('Argument must be a Buffer')
  }

  if (start === undefined) {
    start = 0
  }
  if (end === undefined) {
    end = target ? target.length : 0
  }
  if (thisStart === undefined) {
    thisStart = 0
  }
  if (thisEnd === undefined) {
    thisEnd = this.length
  }

  if (start < 0 || end > target.length || thisStart < 0 || thisEnd > this.length) {
    throw new RangeError('out of range index')
  }

  if (thisStart >= thisEnd && start >= end) {
    return 0
  }
  if (thisStart >= thisEnd) {
    return -1
  }
  if (start >= end) {
    return 1
  }

  start >>>= 0
  end >>>= 0
  thisStart >>>= 0
  thisEnd >>>= 0

  if (this === target) return 0

  var x = thisEnd - thisStart
  var y = end - start
  var len = Math.min(x, y)

  var thisCopy = this.slice(thisStart, thisEnd)
  var targetCopy = target.slice(start, end)

  for (var i = 0; i < len; ++i) {
    if (thisCopy[i] !== targetCopy[i]) {
      x = thisCopy[i]
      y = targetCopy[i]
      break
    }
  }

  if (x < y) return -1
  if (y < x) return 1
  return 0
}

// Finds either the first index of `val` in `buffer` at offset >= `byteOffset`,
// OR the last index of `val` in `buffer` at offset <= `byteOffset`.
//
// Arguments:
// - buffer - a Buffer to search
// - val - a string, Buffer, or number
// - byteOffset - an index into `buffer`; will be clamped to an int32
// - encoding - an optional encoding, relevant is val is a string
// - dir - true for indexOf, false for lastIndexOf
function bidirectionalIndexOf (buffer, val, byteOffset, encoding, dir) {
  // Empty buffer means no match
  if (buffer.length === 0) return -1

  // Normalize byteOffset
  if (typeof byteOffset === 'string') {
    encoding = byteOffset
    byteOffset = 0
  } else if (byteOffset > 0x7fffffff) {
    byteOffset = 0x7fffffff
  } else if (byteOffset < -0x80000000) {
    byteOffset = -0x80000000
  }
  byteOffset = +byteOffset  // Coerce to Number.
  if (isNaN(byteOffset)) {
    // byteOffset: it it's undefined, null, NaN, "foo", etc, search whole buffer
    byteOffset = dir ? 0 : (buffer.length - 1)
  }

  // Normalize byteOffset: negative offsets start from the end of the buffer
  if (byteOffset < 0) byteOffset = buffer.length + byteOffset
  if (byteOffset >= buffer.length) {
    if (dir) return -1
    else byteOffset = buffer.length - 1
  } else if (byteOffset < 0) {
    if (dir) byteOffset = 0
    else return -1
  }

  // Normalize val
  if (typeof val === 'string') {
    val = Buffer.from(val, encoding)
  }

  // Finally, search either indexOf (if dir is true) or lastIndexOf
  if (Buffer.isBuffer(val)) {
    // Special case: looking for empty string/buffer always fails
    if (val.length === 0) {
      return -1
    }
    return arrayIndexOf(buffer, val, byteOffset, encoding, dir)
  } else if (typeof val === 'number') {
    val = val & 0xFF // Search for a byte value [0-255]
    if (Buffer.TYPED_ARRAY_SUPPORT &&
        typeof Uint8Array.prototype.indexOf === 'function') {
      if (dir) {
        return Uint8Array.prototype.indexOf.call(buffer, val, byteOffset)
      } else {
        return Uint8Array.prototype.lastIndexOf.call(buffer, val, byteOffset)
      }
    }
    return arrayIndexOf(buffer, [ val ], byteOffset, encoding, dir)
  }

  throw new TypeError('val must be string, number or Buffer')
}

function arrayIndexOf (arr, val, byteOffset, encoding, dir) {
  var indexSize = 1
  var arrLength = arr.length
  var valLength = val.length

  if (encoding !== undefined) {
    encoding = String(encoding).toLowerCase()
    if (encoding === 'ucs2' || encoding === 'ucs-2' ||
        encoding === 'utf16le' || encoding === 'utf-16le') {
      if (arr.length < 2 || val.length < 2) {
        return -1
      }
      indexSize = 2
      arrLength /= 2
      valLength /= 2
      byteOffset /= 2
    }
  }

  function read (buf, i) {
    if (indexSize === 1) {
      return buf[i]
    } else {
      return buf.readUInt16BE(i * indexSize)
    }
  }

  var i
  if (dir) {
    var foundIndex = -1
    for (i = byteOffset; i < arrLength; i++) {
      if (read(arr, i) === read(val, foundIndex === -1 ? 0 : i - foundIndex)) {
        if (foundIndex === -1) foundIndex = i
        if (i - foundIndex + 1 === valLength) return foundIndex * indexSize
      } else {
        if (foundIndex !== -1) i -= i - foundIndex
        foundIndex = -1
      }
    }
  } else {
    if (byteOffset + valLength > arrLength) byteOffset = arrLength - valLength
    for (i = byteOffset; i >= 0; i--) {
      var found = true
      for (var j = 0; j < valLength; j++) {
        if (read(arr, i + j) !== read(val, j)) {
          found = false
          break
        }
      }
      if (found) return i
    }
  }

  return -1
}

Buffer.prototype.includes = function includes (val, byteOffset, encoding) {
  return this.indexOf(val, byteOffset, encoding) !== -1
}

Buffer.prototype.indexOf = function indexOf (val, byteOffset, encoding) {
  return bidirectionalIndexOf(this, val, byteOffset, encoding, true)
}

Buffer.prototype.lastIndexOf = function lastIndexOf (val, byteOffset, encoding) {
  return bidirectionalIndexOf(this, val, byteOffset, encoding, false)
}

function hexWrite (buf, string, offset, length) {
  offset = Number(offset) || 0
  var remaining = buf.length - offset
  if (!length) {
    length = remaining
  } else {
    length = Number(length)
    if (length > remaining) {
      length = remaining
    }
  }

  // must be an even number of digits
  var strLen = string.length
  if (strLen % 2 !== 0) throw new TypeError('Invalid hex string')

  if (length > strLen / 2) {
    length = strLen / 2
  }
  for (var i = 0; i < length; ++i) {
    var parsed = parseInt(string.substr(i * 2, 2), 16)
    if (isNaN(parsed)) return i
    buf[offset + i] = parsed
  }
  return i
}

function utf8Write (buf, string, offset, length) {
  return blitBuffer(utf8ToBytes(string, buf.length - offset), buf, offset, length)
}

function asciiWrite (buf, string, offset, length) {
  return blitBuffer(asciiToBytes(string), buf, offset, length)
}

function latin1Write (buf, string, offset, length) {
  return asciiWrite(buf, string, offset, length)
}

function base64Write (buf, string, offset, length) {
  return blitBuffer(base64ToBytes(string), buf, offset, length)
}

function ucs2Write (buf, string, offset, length) {
  return blitBuffer(utf16leToBytes(string, buf.length - offset), buf, offset, length)
}

Buffer.prototype.write = function write (string, offset, length, encoding) {
  // Buffer#write(string)
  if (offset === undefined) {
    encoding = 'utf8'
    length = this.length
    offset = 0
  // Buffer#write(string, encoding)
  } else if (length === undefined && typeof offset === 'string') {
    encoding = offset
    length = this.length
    offset = 0
  // Buffer#write(string, offset[, length][, encoding])
  } else if (isFinite(offset)) {
    offset = offset | 0
    if (isFinite(length)) {
      length = length | 0
      if (encoding === undefined) encoding = 'utf8'
    } else {
      encoding = length
      length = undefined
    }
  // legacy write(string, encoding, offset, length) - remove in v0.13
  } else {
    throw new Error(
      'Buffer.write(string, encoding, offset[, length]) is no longer supported'
    )
  }

  var remaining = this.length - offset
  if (length === undefined || length > remaining) length = remaining

  if ((string.length > 0 && (length < 0 || offset < 0)) || offset > this.length) {
    throw new RangeError('Attempt to write outside buffer bounds')
  }

  if (!encoding) encoding = 'utf8'

  var loweredCase = false
  for (;;) {
    switch (encoding) {
      case 'hex':
        return hexWrite(this, string, offset, length)

      case 'utf8':
      case 'utf-8':
        return utf8Write(this, string, offset, length)

      case 'ascii':
        return asciiWrite(this, string, offset, length)

      case 'latin1':
      case 'binary':
        return latin1Write(this, string, offset, length)

      case 'base64':
        // Warning: maxLength not taken into account in base64Write
        return base64Write(this, string, offset, length)

      case 'ucs2':
      case 'ucs-2':
      case 'utf16le':
      case 'utf-16le':
        return ucs2Write(this, string, offset, length)

      default:
        if (loweredCase) throw new TypeError('Unknown encoding: ' + encoding)
        encoding = ('' + encoding).toLowerCase()
        loweredCase = true
    }
  }
}

Buffer.prototype.toJSON = function toJSON () {
  return {
    type: 'Buffer',
    data: Array.prototype.slice.call(this._arr || this, 0)
  }
}

function base64Slice (buf, start, end) {
  if (start === 0 && end === buf.length) {
    return base64.fromByteArray(buf)
  } else {
    return base64.fromByteArray(buf.slice(start, end))
  }
}

function utf8Slice (buf, start, end) {
  end = Math.min(buf.length, end)
  var res = []

  var i = start
  while (i < end) {
    var firstByte = buf[i]
    var codePoint = null
    var bytesPerSequence = (firstByte > 0xEF) ? 4
      : (firstByte > 0xDF) ? 3
      : (firstByte > 0xBF) ? 2
      : 1

    if (i + bytesPerSequence <= end) {
      var secondByte, thirdByte, fourthByte, tempCodePoint

      switch (bytesPerSequence) {
        case 1:
          if (firstByte < 0x80) {
            codePoint = firstByte
          }
          break
        case 2:
          secondByte = buf[i + 1]
          if ((secondByte & 0xC0) === 0x80) {
            tempCodePoint = (firstByte & 0x1F) << 0x6 | (secondByte & 0x3F)
            if (tempCodePoint > 0x7F) {
              codePoint = tempCodePoint
            }
          }
          break
        case 3:
          secondByte = buf[i + 1]
          thirdByte = buf[i + 2]
          if ((secondByte & 0xC0) === 0x80 && (thirdByte & 0xC0) === 0x80) {
            tempCodePoint = (firstByte & 0xF) << 0xC | (secondByte & 0x3F) << 0x6 | (thirdByte & 0x3F)
            if (tempCodePoint > 0x7FF && (tempCodePoint < 0xD800 || tempCodePoint > 0xDFFF)) {
              codePoint = tempCodePoint
            }
          }
          break
        case 4:
          secondByte = buf[i + 1]
          thirdByte = buf[i + 2]
          fourthByte = buf[i + 3]
          if ((secondByte & 0xC0) === 0x80 && (thirdByte & 0xC0) === 0x80 && (fourthByte & 0xC0) === 0x80) {
            tempCodePoint = (firstByte & 0xF) << 0x12 | (secondByte & 0x3F) << 0xC | (thirdByte & 0x3F) << 0x6 | (fourthByte & 0x3F)
            if (tempCodePoint > 0xFFFF && tempCodePoint < 0x110000) {
              codePoint = tempCodePoint
            }
          }
      }
    }

    if (codePoint === null) {
      // we did not generate a valid codePoint so insert a
      // replacement char (U+FFFD) and advance only 1 byte
      codePoint = 0xFFFD
      bytesPerSequence = 1
    } else if (codePoint > 0xFFFF) {
      // encode to utf16 (surrogate pair dance)
      codePoint -= 0x10000
      res.push(codePoint >>> 10 & 0x3FF | 0xD800)
      codePoint = 0xDC00 | codePoint & 0x3FF
    }

    res.push(codePoint)
    i += bytesPerSequence
  }

  return decodeCodePointsArray(res)
}

// Based on http://stackoverflow.com/a/22747272/680742, the browser with
// the lowest limit is Chrome, with 0x10000 args.
// We go 1 magnitude less, for safety
var MAX_ARGUMENTS_LENGTH = 0x1000

function decodeCodePointsArray (codePoints) {
  var len = codePoints.length
  if (len <= MAX_ARGUMENTS_LENGTH) {
    return String.fromCharCode.apply(String, codePoints) // avoid extra slice()
  }

  // Decode in chunks to avoid "call stack size exceeded".
  var res = ''
  var i = 0
  while (i < len) {
    res += String.fromCharCode.apply(
      String,
      codePoints.slice(i, i += MAX_ARGUMENTS_LENGTH)
    )
  }
  return res
}

function asciiSlice (buf, start, end) {
  var ret = ''
  end = Math.min(buf.length, end)

  for (var i = start; i < end; ++i) {
    ret += String.fromCharCode(buf[i] & 0x7F)
  }
  return ret
}

function latin1Slice (buf, start, end) {
  var ret = ''
  end = Math.min(buf.length, end)

  for (var i = start; i < end; ++i) {
    ret += String.fromCharCode(buf[i])
  }
  return ret
}

function hexSlice (buf, start, end) {
  var len = buf.length

  if (!start || start < 0) start = 0
  if (!end || end < 0 || end > len) end = len

  var out = ''
  for (var i = start; i < end; ++i) {
    out += toHex(buf[i])
  }
  return out
}

function utf16leSlice (buf, start, end) {
  var bytes = buf.slice(start, end)
  var res = ''
  for (var i = 0; i < bytes.length; i += 2) {
    res += String.fromCharCode(bytes[i] + bytes[i + 1] * 256)
  }
  return res
}

Buffer.prototype.slice = function slice (start, end) {
  var len = this.length
  start = ~~start
  end = end === undefined ? len : ~~end

  if (start < 0) {
    start += len
    if (start < 0) start = 0
  } else if (start > len) {
    start = len
  }

  if (end < 0) {
    end += len
    if (end < 0) end = 0
  } else if (end > len) {
    end = len
  }

  if (end < start) end = start

  var newBuf
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    newBuf = this.subarray(start, end)
    newBuf.__proto__ = Buffer.prototype
  } else {
    var sliceLen = end - start
    newBuf = new Buffer(sliceLen, undefined)
    for (var i = 0; i < sliceLen; ++i) {
      newBuf[i] = this[i + start]
    }
  }

  return newBuf
}

/*
 * Need to make sure that buffer isn't trying to write out of bounds.
 */
function checkOffset (offset, ext, length) {
  if ((offset % 1) !== 0 || offset < 0) throw new RangeError('offset is not uint')
  if (offset + ext > length) throw new RangeError('Trying to access beyond buffer length')
}

Buffer.prototype.readUIntLE = function readUIntLE (offset, byteLength, noAssert) {
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) checkOffset(offset, byteLength, this.length)

  var val = this[offset]
  var mul = 1
  var i = 0
  while (++i < byteLength && (mul *= 0x100)) {
    val += this[offset + i] * mul
  }

  return val
}

Buffer.prototype.readUIntBE = function readUIntBE (offset, byteLength, noAssert) {
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) {
    checkOffset(offset, byteLength, this.length)
  }

  var val = this[offset + --byteLength]
  var mul = 1
  while (byteLength > 0 && (mul *= 0x100)) {
    val += this[offset + --byteLength] * mul
  }

  return val
}

Buffer.prototype.readUInt8 = function readUInt8 (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 1, this.length)
  return this[offset]
}

Buffer.prototype.readUInt16LE = function readUInt16LE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 2, this.length)
  return this[offset] | (this[offset + 1] << 8)
}

Buffer.prototype.readUInt16BE = function readUInt16BE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 2, this.length)
  return (this[offset] << 8) | this[offset + 1]
}

Buffer.prototype.readUInt32LE = function readUInt32LE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)

  return ((this[offset]) |
      (this[offset + 1] << 8) |
      (this[offset + 2] << 16)) +
      (this[offset + 3] * 0x1000000)
}

Buffer.prototype.readUInt32BE = function readUInt32BE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)

  return (this[offset] * 0x1000000) +
    ((this[offset + 1] << 16) |
    (this[offset + 2] << 8) |
    this[offset + 3])
}

Buffer.prototype.readIntLE = function readIntLE (offset, byteLength, noAssert) {
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) checkOffset(offset, byteLength, this.length)

  var val = this[offset]
  var mul = 1
  var i = 0
  while (++i < byteLength && (mul *= 0x100)) {
    val += this[offset + i] * mul
  }
  mul *= 0x80

  if (val >= mul) val -= Math.pow(2, 8 * byteLength)

  return val
}

Buffer.prototype.readIntBE = function readIntBE (offset, byteLength, noAssert) {
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) checkOffset(offset, byteLength, this.length)

  var i = byteLength
  var mul = 1
  var val = this[offset + --i]
  while (i > 0 && (mul *= 0x100)) {
    val += this[offset + --i] * mul
  }
  mul *= 0x80

  if (val >= mul) val -= Math.pow(2, 8 * byteLength)

  return val
}

Buffer.prototype.readInt8 = function readInt8 (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 1, this.length)
  if (!(this[offset] & 0x80)) return (this[offset])
  return ((0xff - this[offset] + 1) * -1)
}

Buffer.prototype.readInt16LE = function readInt16LE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 2, this.length)
  var val = this[offset] | (this[offset + 1] << 8)
  return (val & 0x8000) ? val | 0xFFFF0000 : val
}

Buffer.prototype.readInt16BE = function readInt16BE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 2, this.length)
  var val = this[offset + 1] | (this[offset] << 8)
  return (val & 0x8000) ? val | 0xFFFF0000 : val
}

Buffer.prototype.readInt32LE = function readInt32LE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)

  return (this[offset]) |
    (this[offset + 1] << 8) |
    (this[offset + 2] << 16) |
    (this[offset + 3] << 24)
}

Buffer.prototype.readInt32BE = function readInt32BE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)

  return (this[offset] << 24) |
    (this[offset + 1] << 16) |
    (this[offset + 2] << 8) |
    (this[offset + 3])
}

Buffer.prototype.readFloatLE = function readFloatLE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)
  return ieee754.read(this, offset, true, 23, 4)
}

Buffer.prototype.readFloatBE = function readFloatBE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 4, this.length)
  return ieee754.read(this, offset, false, 23, 4)
}

Buffer.prototype.readDoubleLE = function readDoubleLE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 8, this.length)
  return ieee754.read(this, offset, true, 52, 8)
}

Buffer.prototype.readDoubleBE = function readDoubleBE (offset, noAssert) {
  if (!noAssert) checkOffset(offset, 8, this.length)
  return ieee754.read(this, offset, false, 52, 8)
}

function checkInt (buf, value, offset, ext, max, min) {
  if (!Buffer.isBuffer(buf)) throw new TypeError('"buffer" argument must be a Buffer instance')
  if (value > max || value < min) throw new RangeError('"value" argument is out of bounds')
  if (offset + ext > buf.length) throw new RangeError('Index out of range')
}

Buffer.prototype.writeUIntLE = function writeUIntLE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) {
    var maxBytes = Math.pow(2, 8 * byteLength) - 1
    checkInt(this, value, offset, byteLength, maxBytes, 0)
  }

  var mul = 1
  var i = 0
  this[offset] = value & 0xFF
  while (++i < byteLength && (mul *= 0x100)) {
    this[offset + i] = (value / mul) & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeUIntBE = function writeUIntBE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset | 0
  byteLength = byteLength | 0
  if (!noAssert) {
    var maxBytes = Math.pow(2, 8 * byteLength) - 1
    checkInt(this, value, offset, byteLength, maxBytes, 0)
  }

  var i = byteLength - 1
  var mul = 1
  this[offset + i] = value & 0xFF
  while (--i >= 0 && (mul *= 0x100)) {
    this[offset + i] = (value / mul) & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeUInt8 = function writeUInt8 (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 1, 0xff, 0)
  if (!Buffer.TYPED_ARRAY_SUPPORT) value = Math.floor(value)
  this[offset] = (value & 0xff)
  return offset + 1
}

function objectWriteUInt16 (buf, value, offset, littleEndian) {
  if (value < 0) value = 0xffff + value + 1
  for (var i = 0, j = Math.min(buf.length - offset, 2); i < j; ++i) {
    buf[offset + i] = (value & (0xff << (8 * (littleEndian ? i : 1 - i)))) >>>
      (littleEndian ? i : 1 - i) * 8
  }
}

Buffer.prototype.writeUInt16LE = function writeUInt16LE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 2, 0xffff, 0)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value & 0xff)
    this[offset + 1] = (value >>> 8)
  } else {
    objectWriteUInt16(this, value, offset, true)
  }
  return offset + 2
}

Buffer.prototype.writeUInt16BE = function writeUInt16BE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 2, 0xffff, 0)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value >>> 8)
    this[offset + 1] = (value & 0xff)
  } else {
    objectWriteUInt16(this, value, offset, false)
  }
  return offset + 2
}

function objectWriteUInt32 (buf, value, offset, littleEndian) {
  if (value < 0) value = 0xffffffff + value + 1
  for (var i = 0, j = Math.min(buf.length - offset, 4); i < j; ++i) {
    buf[offset + i] = (value >>> (littleEndian ? i : 3 - i) * 8) & 0xff
  }
}

Buffer.prototype.writeUInt32LE = function writeUInt32LE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 4, 0xffffffff, 0)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset + 3] = (value >>> 24)
    this[offset + 2] = (value >>> 16)
    this[offset + 1] = (value >>> 8)
    this[offset] = (value & 0xff)
  } else {
    objectWriteUInt32(this, value, offset, true)
  }
  return offset + 4
}

Buffer.prototype.writeUInt32BE = function writeUInt32BE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 4, 0xffffffff, 0)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value >>> 24)
    this[offset + 1] = (value >>> 16)
    this[offset + 2] = (value >>> 8)
    this[offset + 3] = (value & 0xff)
  } else {
    objectWriteUInt32(this, value, offset, false)
  }
  return offset + 4
}

Buffer.prototype.writeIntLE = function writeIntLE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) {
    var limit = Math.pow(2, 8 * byteLength - 1)

    checkInt(this, value, offset, byteLength, limit - 1, -limit)
  }

  var i = 0
  var mul = 1
  var sub = 0
  this[offset] = value & 0xFF
  while (++i < byteLength && (mul *= 0x100)) {
    if (value < 0 && sub === 0 && this[offset + i - 1] !== 0) {
      sub = 1
    }
    this[offset + i] = ((value / mul) >> 0) - sub & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeIntBE = function writeIntBE (value, offset, byteLength, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) {
    var limit = Math.pow(2, 8 * byteLength - 1)

    checkInt(this, value, offset, byteLength, limit - 1, -limit)
  }

  var i = byteLength - 1
  var mul = 1
  var sub = 0
  this[offset + i] = value & 0xFF
  while (--i >= 0 && (mul *= 0x100)) {
    if (value < 0 && sub === 0 && this[offset + i + 1] !== 0) {
      sub = 1
    }
    this[offset + i] = ((value / mul) >> 0) - sub & 0xFF
  }

  return offset + byteLength
}

Buffer.prototype.writeInt8 = function writeInt8 (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 1, 0x7f, -0x80)
  if (!Buffer.TYPED_ARRAY_SUPPORT) value = Math.floor(value)
  if (value < 0) value = 0xff + value + 1
  this[offset] = (value & 0xff)
  return offset + 1
}

Buffer.prototype.writeInt16LE = function writeInt16LE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 2, 0x7fff, -0x8000)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value & 0xff)
    this[offset + 1] = (value >>> 8)
  } else {
    objectWriteUInt16(this, value, offset, true)
  }
  return offset + 2
}

Buffer.prototype.writeInt16BE = function writeInt16BE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 2, 0x7fff, -0x8000)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value >>> 8)
    this[offset + 1] = (value & 0xff)
  } else {
    objectWriteUInt16(this, value, offset, false)
  }
  return offset + 2
}

Buffer.prototype.writeInt32LE = function writeInt32LE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 4, 0x7fffffff, -0x80000000)
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value & 0xff)
    this[offset + 1] = (value >>> 8)
    this[offset + 2] = (value >>> 16)
    this[offset + 3] = (value >>> 24)
  } else {
    objectWriteUInt32(this, value, offset, true)
  }
  return offset + 4
}

Buffer.prototype.writeInt32BE = function writeInt32BE (value, offset, noAssert) {
  value = +value
  offset = offset | 0
  if (!noAssert) checkInt(this, value, offset, 4, 0x7fffffff, -0x80000000)
  if (value < 0) value = 0xffffffff + value + 1
  if (Buffer.TYPED_ARRAY_SUPPORT) {
    this[offset] = (value >>> 24)
    this[offset + 1] = (value >>> 16)
    this[offset + 2] = (value >>> 8)
    this[offset + 3] = (value & 0xff)
  } else {
    objectWriteUInt32(this, value, offset, false)
  }
  return offset + 4
}

function checkIEEE754 (buf, value, offset, ext, max, min) {
  if (offset + ext > buf.length) throw new RangeError('Index out of range')
  if (offset < 0) throw new RangeError('Index out of range')
}

function writeFloat (buf, value, offset, littleEndian, noAssert) {
  if (!noAssert) {
    checkIEEE754(buf, value, offset, 4, 3.4028234663852886e+38, -3.4028234663852886e+38)
  }
  ieee754.write(buf, value, offset, littleEndian, 23, 4)
  return offset + 4
}

Buffer.prototype.writeFloatLE = function writeFloatLE (value, offset, noAssert) {
  return writeFloat(this, value, offset, true, noAssert)
}

Buffer.prototype.writeFloatBE = function writeFloatBE (value, offset, noAssert) {
  return writeFloat(this, value, offset, false, noAssert)
}

function writeDouble (buf, value, offset, littleEndian, noAssert) {
  if (!noAssert) {
    checkIEEE754(buf, value, offset, 8, 1.7976931348623157E+308, -1.7976931348623157E+308)
  }
  ieee754.write(buf, value, offset, littleEndian, 52, 8)
  return offset + 8
}

Buffer.prototype.writeDoubleLE = function writeDoubleLE (value, offset, noAssert) {
  return writeDouble(this, value, offset, true, noAssert)
}

Buffer.prototype.writeDoubleBE = function writeDoubleBE (value, offset, noAssert) {
  return writeDouble(this, value, offset, false, noAssert)
}

// copy(targetBuffer, targetStart=0, sourceStart=0, sourceEnd=buffer.length)
Buffer.prototype.copy = function copy (target, targetStart, start, end) {
  if (!start) start = 0
  if (!end && end !== 0) end = this.length
  if (targetStart >= target.length) targetStart = target.length
  if (!targetStart) targetStart = 0
  if (end > 0 && end < start) end = start

  // Copy 0 bytes; we're done
  if (end === start) return 0
  if (target.length === 0 || this.length === 0) return 0

  // Fatal error conditions
  if (targetStart < 0) {
    throw new RangeError('targetStart out of bounds')
  }
  if (start < 0 || start >= this.length) throw new RangeError('sourceStart out of bounds')
  if (end < 0) throw new RangeError('sourceEnd out of bounds')

  // Are we oob?
  if (end > this.length) end = this.length
  if (target.length - targetStart < end - start) {
    end = target.length - targetStart + start
  }

  var len = end - start
  var i

  if (this === target && start < targetStart && targetStart < end) {
    // descending copy from end
    for (i = len - 1; i >= 0; --i) {
      target[i + targetStart] = this[i + start]
    }
  } else if (len < 1000 || !Buffer.TYPED_ARRAY_SUPPORT) {
    // ascending copy from start
    for (i = 0; i < len; ++i) {
      target[i + targetStart] = this[i + start]
    }
  } else {
    Uint8Array.prototype.set.call(
      target,
      this.subarray(start, start + len),
      targetStart
    )
  }

  return len
}

// Usage:
//    buffer.fill(number[, offset[, end]])
//    buffer.fill(buffer[, offset[, end]])
//    buffer.fill(string[, offset[, end]][, encoding])
Buffer.prototype.fill = function fill (val, start, end, encoding) {
  // Handle string cases:
  if (typeof val === 'string') {
    if (typeof start === 'string') {
      encoding = start
      start = 0
      end = this.length
    } else if (typeof end === 'string') {
      encoding = end
      end = this.length
    }
    if (val.length === 1) {
      var code = val.charCodeAt(0)
      if (code < 256) {
        val = code
      }
    }
    if (encoding !== undefined && typeof encoding !== 'string') {
      throw new TypeError('encoding must be a string')
    }
    if (typeof encoding === 'string' && !Buffer.isEncoding(encoding)) {
      throw new TypeError('Unknown encoding: ' + encoding)
    }
  } else if (typeof val === 'number') {
    val = val & 255
  }

  // Invalid ranges are not set to a default, so can range check early.
  if (start < 0 || this.length < start || this.length < end) {
    throw new RangeError('Out of range index')
  }

  if (end <= start) {
    return this
  }

  start = start >>> 0
  end = end === undefined ? this.length : end >>> 0

  if (!val) val = 0

  var i
  if (typeof val === 'number') {
    for (i = start; i < end; ++i) {
      this[i] = val
    }
  } else {
    var bytes = Buffer.isBuffer(val)
      ? val
      : utf8ToBytes(new Buffer(val, encoding).toString())
    var len = bytes.length
    for (i = 0; i < end - start; ++i) {
      this[i + start] = bytes[i % len]
    }
  }

  return this
}

// HELPER FUNCTIONS
// ================

var INVALID_BASE64_RE = /[^+\/0-9A-Za-z-_]/g

function base64clean (str) {
  // Node strips out invalid characters like \n and \t from the string, base64-js does not
  str = stringtrim(str).replace(INVALID_BASE64_RE, '')
  // Node converts strings with length < 2 to ''
  if (str.length < 2) return ''
  // Node allows for non-padded base64 strings (missing trailing ===), base64-js does not
  while (str.length % 4 !== 0) {
    str = str + '='
  }
  return str
}

function stringtrim (str) {
  if (str.trim) return str.trim()
  return str.replace(/^\s+|\s+$/g, '')
}

function toHex (n) {
  if (n < 16) return '0' + n.toString(16)
  return n.toString(16)
}

function utf8ToBytes (string, units) {
  units = units || Infinity
  var codePoint
  var length = string.length
  var leadSurrogate = null
  var bytes = []

  for (var i = 0; i < length; ++i) {
    codePoint = string.charCodeAt(i)

    // is surrogate component
    if (codePoint > 0xD7FF && codePoint < 0xE000) {
      // last char was a lead
      if (!leadSurrogate) {
        // no lead yet
        if (codePoint > 0xDBFF) {
          // unexpected trail
          if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
          continue
        } else if (i + 1 === length) {
          // unpaired lead
          if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
          continue
        }

        // valid lead
        leadSurrogate = codePoint

        continue
      }

      // 2 leads in a row
      if (codePoint < 0xDC00) {
        if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
        leadSurrogate = codePoint
        continue
      }

      // valid surrogate pair
      codePoint = (leadSurrogate - 0xD800 << 10 | codePoint - 0xDC00) + 0x10000
    } else if (leadSurrogate) {
      // valid bmp char, but last char was a lead
      if ((units -= 3) > -1) bytes.push(0xEF, 0xBF, 0xBD)
    }

    leadSurrogate = null

    // encode utf8
    if (codePoint < 0x80) {
      if ((units -= 1) < 0) break
      bytes.push(codePoint)
    } else if (codePoint < 0x800) {
      if ((units -= 2) < 0) break
      bytes.push(
        codePoint >> 0x6 | 0xC0,
        codePoint & 0x3F | 0x80
      )
    } else if (codePoint < 0x10000) {
      if ((units -= 3) < 0) break
      bytes.push(
        codePoint >> 0xC | 0xE0,
        codePoint >> 0x6 & 0x3F | 0x80,
        codePoint & 0x3F | 0x80
      )
    } else if (codePoint < 0x110000) {
      if ((units -= 4) < 0) break
      bytes.push(
        codePoint >> 0x12 | 0xF0,
        codePoint >> 0xC & 0x3F | 0x80,
        codePoint >> 0x6 & 0x3F | 0x80,
        codePoint & 0x3F | 0x80
      )
    } else {
      throw new Error('Invalid code point')
    }
  }

  return bytes
}

function asciiToBytes (str) {
  var byteArray = []
  for (var i = 0; i < str.length; ++i) {
    // Node's code seems to be doing this and not & 0x7F..
    byteArray.push(str.charCodeAt(i) & 0xFF)
  }
  return byteArray
}

function utf16leToBytes (str, units) {
  var c, hi, lo
  var byteArray = []
  for (var i = 0; i < str.length; ++i) {
    if ((units -= 2) < 0) break

    c = str.charCodeAt(i)
    hi = c >> 8
    lo = c % 256
    byteArray.push(lo)
    byteArray.push(hi)
  }

  return byteArray
}

function base64ToBytes (str) {
  return base64.toByteArray(base64clean(str))
}

function blitBuffer (src, dst, offset, length) {
  for (var i = 0; i < length; ++i) {
    if ((i + offset >= dst.length) || (i >= src.length)) break
    dst[i + offset] = src[i]
  }
  return i
}

function isnan (val) {
  return val !== val // eslint-disable-line no-self-compare
}

/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__(20)))

/***/ }),
/* 20 */
/***/ (function(module, exports) {

var g;

// This works in non-strict mode
g = (function() {
	return this;
})();

try {
	// This works if eval is allowed (see CSP)
	g = g || Function("return this")() || (1,eval)("this");
} catch(e) {
	// This works if the window reference is available
	if(typeof window === "object")
		g = window;
}

// g can still be undefined, but nothing to do about it...
// We return undefined, instead of nothing here, so it's
// easier to handle this case. if(!global) { ...}

module.exports = g;


/***/ }),
/* 21 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


exports.byteLength = byteLength
exports.toByteArray = toByteArray
exports.fromByteArray = fromByteArray

var lookup = []
var revLookup = []
var Arr = typeof Uint8Array !== 'undefined' ? Uint8Array : Array

var code = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'
for (var i = 0, len = code.length; i < len; ++i) {
  lookup[i] = code[i]
  revLookup[code.charCodeAt(i)] = i
}

revLookup['-'.charCodeAt(0)] = 62
revLookup['_'.charCodeAt(0)] = 63

function placeHoldersCount (b64) {
  var len = b64.length
  if (len % 4 > 0) {
    throw new Error('Invalid string. Length must be a multiple of 4')
  }

  // the number of equal signs (place holders)
  // if there are two placeholders, than the two characters before it
  // represent one byte
  // if there is only one, then the three characters before it represent 2 bytes
  // this is just a cheap hack to not do indexOf twice
  return b64[len - 2] === '=' ? 2 : b64[len - 1] === '=' ? 1 : 0
}

function byteLength (b64) {
  // base64 is 4/3 + up to two characters of the original data
  return (b64.length * 3 / 4) - placeHoldersCount(b64)
}

function toByteArray (b64) {
  var i, l, tmp, placeHolders, arr
  var len = b64.length
  placeHolders = placeHoldersCount(b64)

  arr = new Arr((len * 3 / 4) - placeHolders)

  // if there are placeholders, only get up to the last complete 4 chars
  l = placeHolders > 0 ? len - 4 : len

  var L = 0

  for (i = 0; i < l; i += 4) {
    tmp = (revLookup[b64.charCodeAt(i)] << 18) | (revLookup[b64.charCodeAt(i + 1)] << 12) | (revLookup[b64.charCodeAt(i + 2)] << 6) | revLookup[b64.charCodeAt(i + 3)]
    arr[L++] = (tmp >> 16) & 0xFF
    arr[L++] = (tmp >> 8) & 0xFF
    arr[L++] = tmp & 0xFF
  }

  if (placeHolders === 2) {
    tmp = (revLookup[b64.charCodeAt(i)] << 2) | (revLookup[b64.charCodeAt(i + 1)] >> 4)
    arr[L++] = tmp & 0xFF
  } else if (placeHolders === 1) {
    tmp = (revLookup[b64.charCodeAt(i)] << 10) | (revLookup[b64.charCodeAt(i + 1)] << 4) | (revLookup[b64.charCodeAt(i + 2)] >> 2)
    arr[L++] = (tmp >> 8) & 0xFF
    arr[L++] = tmp & 0xFF
  }

  return arr
}

function tripletToBase64 (num) {
  return lookup[num >> 18 & 0x3F] + lookup[num >> 12 & 0x3F] + lookup[num >> 6 & 0x3F] + lookup[num & 0x3F]
}

function encodeChunk (uint8, start, end) {
  var tmp
  var output = []
  for (var i = start; i < end; i += 3) {
    tmp = (uint8[i] << 16) + (uint8[i + 1] << 8) + (uint8[i + 2])
    output.push(tripletToBase64(tmp))
  }
  return output.join('')
}

function fromByteArray (uint8) {
  var tmp
  var len = uint8.length
  var extraBytes = len % 3 // if we have 1 byte left, pad 2 bytes
  var output = ''
  var parts = []
  var maxChunkLength = 16383 // must be multiple of 3

  // go through the array every three bytes, we'll deal with trailing stuff later
  for (var i = 0, len2 = len - extraBytes; i < len2; i += maxChunkLength) {
    parts.push(encodeChunk(uint8, i, (i + maxChunkLength) > len2 ? len2 : (i + maxChunkLength)))
  }

  // pad the end with zeros, but make sure to not forget the extra bytes
  if (extraBytes === 1) {
    tmp = uint8[len - 1]
    output += lookup[tmp >> 2]
    output += lookup[(tmp << 4) & 0x3F]
    output += '=='
  } else if (extraBytes === 2) {
    tmp = (uint8[len - 2] << 8) + (uint8[len - 1])
    output += lookup[tmp >> 10]
    output += lookup[(tmp >> 4) & 0x3F]
    output += lookup[(tmp << 2) & 0x3F]
    output += '='
  }

  parts.push(output)

  return parts.join('')
}


/***/ }),
/* 22 */
/***/ (function(module, exports) {

exports.read = function (buffer, offset, isLE, mLen, nBytes) {
  var e, m
  var eLen = nBytes * 8 - mLen - 1
  var eMax = (1 << eLen) - 1
  var eBias = eMax >> 1
  var nBits = -7
  var i = isLE ? (nBytes - 1) : 0
  var d = isLE ? -1 : 1
  var s = buffer[offset + i]

  i += d

  e = s & ((1 << (-nBits)) - 1)
  s >>= (-nBits)
  nBits += eLen
  for (; nBits > 0; e = e * 256 + buffer[offset + i], i += d, nBits -= 8) {}

  m = e & ((1 << (-nBits)) - 1)
  e >>= (-nBits)
  nBits += mLen
  for (; nBits > 0; m = m * 256 + buffer[offset + i], i += d, nBits -= 8) {}

  if (e === 0) {
    e = 1 - eBias
  } else if (e === eMax) {
    return m ? NaN : ((s ? -1 : 1) * Infinity)
  } else {
    m = m + Math.pow(2, mLen)
    e = e - eBias
  }
  return (s ? -1 : 1) * m * Math.pow(2, e - mLen)
}

exports.write = function (buffer, value, offset, isLE, mLen, nBytes) {
  var e, m, c
  var eLen = nBytes * 8 - mLen - 1
  var eMax = (1 << eLen) - 1
  var eBias = eMax >> 1
  var rt = (mLen === 23 ? Math.pow(2, -24) - Math.pow(2, -77) : 0)
  var i = isLE ? 0 : (nBytes - 1)
  var d = isLE ? 1 : -1
  var s = value < 0 || (value === 0 && 1 / value < 0) ? 1 : 0

  value = Math.abs(value)

  if (isNaN(value) || value === Infinity) {
    m = isNaN(value) ? 1 : 0
    e = eMax
  } else {
    e = Math.floor(Math.log(value) / Math.LN2)
    if (value * (c = Math.pow(2, -e)) < 1) {
      e--
      c *= 2
    }
    if (e + eBias >= 1) {
      value += rt / c
    } else {
      value += rt * Math.pow(2, 1 - eBias)
    }
    if (value * c >= 2) {
      e++
      c /= 2
    }

    if (e + eBias >= eMax) {
      m = 0
      e = eMax
    } else if (e + eBias >= 1) {
      m = (value * c - 1) * Math.pow(2, mLen)
      e = e + eBias
    } else {
      m = value * Math.pow(2, eBias - 1) * Math.pow(2, mLen)
      e = 0
    }
  }

  for (; mLen >= 8; buffer[offset + i] = m & 0xff, i += d, m /= 256, mLen -= 8) {}

  e = (e << mLen) | m
  eLen += mLen
  for (; eLen > 0; buffer[offset + i] = e & 0xff, i += d, e /= 256, eLen -= 8) {}

  buffer[offset + i - d] |= s * 128
}


/***/ }),
/* 23 */
/***/ (function(module, exports) {

var toString = {}.toString;

module.exports = Array.isArray || function (arr) {
  return toString.call(arr) == '[object Array]';
};


/***/ }),
/* 24 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Store = function () {
	function Store() {
		_classCallCheck(this, Store);
	}

	_createClass(Store, null, [{
		key: 'set',
		value: function set(key, data) {
			try {
				if (window.localStorage) {
					localStorage.setItem(key, data);
				}
			} catch (e) {
				window[key + '_localStorage'] = data;
			}
		}
	}, {
		key: 'get',
		value: function get(key) {
			var items;
			try {
				if (window.localStorage) {
					return localStorage.getItem(key);
				}
			} catch (e) {
				return window[key + '_localStorage'];
			}

			return "";
		}
	}, {
		key: 'remove',
		value: function remove(key) {
			try {
				if (window.localStorage) {
					localStorage.removeItem(key);
				}
			} catch (e) {
				delete window[key + '_localStorage'];
			}
		}
	}]);

	return Store;
}();

exports.default = Store;

/***/ }),
/* 25 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Cookie = function () {
    function Cookie() {
        _classCallCheck(this, Cookie);
    }

    _createClass(Cookie, null, [{
        key: 'get',
        value: function get(cname) {
            var name = cname + '';
            var ca = document.cookie.split(';');
            for (var i = 0; i < ca.length; i++) {
                var c = ca[i].trim();
                if (c.indexOf(name) == 0) {
                    return unescape(c.substring(name.length + 1, c.length));
                }
            }
            return '';
        }
    }, {
        key: 'set',
        value: function set(cname, cvalue, exdays) {
            var d = new Date();
            d.setTime(d.getTime() + exdays * 24 * 60 * 60 * 1000);
            var expires = 'expires=' + d.toGMTString();
            document.cookie = cname + '=' + escape(cvalue) + '; ' + expires;
        }
    }]);

    return Cookie;
}();

exports.default = Cookie;

/***/ }),
/* 26 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _io = __webpack_require__(8);

var _io2 = _interopRequireDefault(_io);

var _signature = __webpack_require__(9);

var _signature2 = _interopRequireDefault(_signature);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Data = function () {
	function Data() {
		_classCallCheck(this, Data);
	}

	_createClass(Data, null, [{
		key: 'refreshUploadAuth',
		value: function refreshUploadAuth(params, success, error) {
			var randNum = _signature2.default.randomUUID();
			var SignatureNonceNum = _signature2.default.randomUUID();
			var SignatureMethodT = 'HMAC-SHA1';

			var newAry = {
				'AccessKeyId': params.accessKeyId,
				'SecurityToken': params.securityToken,
				'Action': 'RefreshUploadVideo',
				'VideoId': params.videoId,
				'Version': '2017-03-21',
				'Format': 'JSON',
				'SignatureMethod': SignatureMethodT,
				'SignatureVersion': '1.0',
				'SignatureNonce': SignatureNonceNum,
				'RequestId': params.requestId
			};

			var pbugramsdic = _signature2.default.makeUTF8sort(newAry, '=', '&') + '&Signature=' + _signature2.default.aliyunEncodeURI(_signature2.default.makeChangeSiga(newAry, params.accessKeySecret));

			var httpUrlend = 'https://vod.' + params.region + '.aliyuncs.com/?' + pbugramsdic;

			_io2.default.get(httpUrlend, function (data) {
				var data = JSON.parse(data);
				if (success) {
					success(data);
				}
			}, function (errorText) {
				if (error) {
					var arg = JSON.parse(errorText);
					error(arg);
				}
			});
		}
	}, {
		key: 'getUploadAuth',
		value: function getUploadAuth(params, success, error) {
			var randNum = _signature2.default.randomUUID();
			var SignatureNonceNum = _signature2.default.randomUUID();
			var SignatureMethodT = 'HMAC-SHA1';

			var newAry = {
				'AccessKeyId': params.accessKeyId,
				'SecurityToken': params.securityToken,
				'Action': 'CreateUploadVideo',
				'Title': params.title,
				'FileName': params.fileName,
				'Version': '2017-03-21',
				'Format': 'JSON',
				'SignatureMethod': SignatureMethodT,
				'SignatureVersion': '1.0',
				'SignatureNonce': SignatureNonceNum,
				'RequestId': params.requestId
			};

			if (params.fileSize) {
				newAry.FileSize = params.fileSize;
			}
			if (params.description) {
				newAry.Description = params.description;
			}

			if (params.cateId) {
				newAry.CateId = params.cateId;
			}

			if (params.tags) {
				newAry.Tags = params.tags;
			}

			if (params.templateGroupId) {
				newAry.TemplateGroupId = params.templateGroupId;
			}

			if (params.storageLocation) {
				newAry.StorageLocation = params.storageLocation;
			}

			if (params.coverUrl) {
				newAry.CoverURL = params.coverUrl;
			}

			if (params.transCodeMode) {
				newAry.TransCodeMode = params.transCodeMode;
			}

			if (params.userData) {
				newAry.UserData = params.userData;
			}

			var pbugramsdic = _signature2.default.makeUTF8sort(newAry, '=', '&') + '&Signature=' + _signature2.default.aliyunEncodeURI(_signature2.default.makeChangeSiga(newAry, params.accessKeySecret));

			var httpUrlend = 'https://vod.' + params.region + '.aliyuncs.com/?' + pbugramsdic;

			_io2.default.get(httpUrlend, function (data) {
				var data = JSON.parse(data);
				if (success) {
					success(data);
				}
			}, function (errorText) {
				if (error) {
					var arg = JSON.parse(errorText);
					error(arg);
				}
			});
		}
	}, {
		key: 'getImageUploadAuth',
		value: function getImageUploadAuth(params, success, error) {
			var randNum = _signature2.default.randomUUID();
			var SignatureNonceNum = _signature2.default.randomUUID();
			var SignatureMethodT = 'HMAC-SHA1';

			var newAry = {
				'AccessKeyId': params.accessKeyId,
				'SecurityToken': params.securityToken,
				'Action': 'CreateUploadImage',
				'ImageType': params.imageType ? params.imageType : 'default',
				'Version': '2017-03-21',
				'Format': 'JSON',
				'SignatureMethod': SignatureMethodT,
				'SignatureVersion': '1.0',
				'SignatureNonce': SignatureNonceNum,
				'RequestId': params.requestId
			};

			if (params.title) {
				newAry.Title = params.title;
			}
			if (params.imageExt) {
				newAry.ImageExt = params.imageExt;
			}

			if (params.tags) {
				newAry.Tags = params.tags;
			}

			if (params.storageLocation) {
				newAry.StorageLocation = params.storageLocation;
			}

			var pbugramsdic = _signature2.default.makeUTF8sort(newAry, '=', '&') + '&Signature=' + _signature2.default.aliyunEncodeURI(_signature2.default.makeChangeSiga(newAry, params.accessKeySecret));

			var httpUrlend = 'https://vod.' + params.region + '.aliyuncs.com/?' + pbugramsdic;

			_io2.default.get(httpUrlend, function (data) {
				var data = JSON.parse(data);
				if (success) {
					success(data);
				}
			}, function (errorText) {
				if (error) {
					var arg = JSON.parse(errorText);
					error(arg);
				}
			});
		}
	}]);

	return Data;
}();

exports.default = Data;

/***/ }),
/* 27 */
/***/ (function(module, exports, __webpack_require__) {

;(function (root, factory, undef) {
	if (true) {
		// CommonJS
		module.exports = exports = factory(__webpack_require__(0), __webpack_require__(28), __webpack_require__(29));
	}
	else if (typeof define === "function" && define.amd) {
		// AMD
		define(["./core", "./sha1", "./hmac"], factory);
	}
	else {
		// Global (browser)
		factory(root.CryptoJS);
	}
}(this, function (CryptoJS) {

	return CryptoJS.HmacSHA1;

}));

/***/ }),
/* 28 */
/***/ (function(module, exports, __webpack_require__) {

;(function (root, factory) {
	if (true) {
		// CommonJS
		module.exports = exports = factory(__webpack_require__(0));
	}
	else if (typeof define === "function" && define.amd) {
		// AMD
		define(["./core"], factory);
	}
	else {
		// Global (browser)
		factory(root.CryptoJS);
	}
}(this, function (CryptoJS) {

	(function () {
	    // Shortcuts
	    var C = CryptoJS;
	    var C_lib = C.lib;
	    var WordArray = C_lib.WordArray;
	    var Hasher = C_lib.Hasher;
	    var C_algo = C.algo;

	    // Reusable object
	    var W = [];

	    /**
	     * SHA-1 hash algorithm.
	     */
	    var SHA1 = C_algo.SHA1 = Hasher.extend({
	        _doReset: function () {
	            this._hash = new WordArray.init([
	                0x67452301, 0xefcdab89,
	                0x98badcfe, 0x10325476,
	                0xc3d2e1f0
	            ]);
	        },

	        _doProcessBlock: function (M, offset) {
	            // Shortcut
	            var H = this._hash.words;

	            // Working variables
	            var a = H[0];
	            var b = H[1];
	            var c = H[2];
	            var d = H[3];
	            var e = H[4];

	            // Computation
	            for (var i = 0; i < 80; i++) {
	                if (i < 16) {
	                    W[i] = M[offset + i] | 0;
	                } else {
	                    var n = W[i - 3] ^ W[i - 8] ^ W[i - 14] ^ W[i - 16];
	                    W[i] = (n << 1) | (n >>> 31);
	                }

	                var t = ((a << 5) | (a >>> 27)) + e + W[i];
	                if (i < 20) {
	                    t += ((b & c) | (~b & d)) + 0x5a827999;
	                } else if (i < 40) {
	                    t += (b ^ c ^ d) + 0x6ed9eba1;
	                } else if (i < 60) {
	                    t += ((b & c) | (b & d) | (c & d)) - 0x70e44324;
	                } else /* if (i < 80) */ {
	                    t += (b ^ c ^ d) - 0x359d3e2a;
	                }

	                e = d;
	                d = c;
	                c = (b << 30) | (b >>> 2);
	                b = a;
	                a = t;
	            }

	            // Intermediate hash value
	            H[0] = (H[0] + a) | 0;
	            H[1] = (H[1] + b) | 0;
	            H[2] = (H[2] + c) | 0;
	            H[3] = (H[3] + d) | 0;
	            H[4] = (H[4] + e) | 0;
	        },

	        _doFinalize: function () {
	            // Shortcuts
	            var data = this._data;
	            var dataWords = data.words;

	            var nBitsTotal = this._nDataBytes * 8;
	            var nBitsLeft = data.sigBytes * 8;

	            // Add padding
	            dataWords[nBitsLeft >>> 5] |= 0x80 << (24 - nBitsLeft % 32);
	            dataWords[(((nBitsLeft + 64) >>> 9) << 4) + 14] = Math.floor(nBitsTotal / 0x100000000);
	            dataWords[(((nBitsLeft + 64) >>> 9) << 4) + 15] = nBitsTotal;
	            data.sigBytes = dataWords.length * 4;

	            // Hash final blocks
	            this._process();

	            // Return final computed hash
	            return this._hash;
	        },

	        clone: function () {
	            var clone = Hasher.clone.call(this);
	            clone._hash = this._hash.clone();

	            return clone;
	        }
	    });

	    /**
	     * Shortcut function to the hasher's object interface.
	     *
	     * @param {WordArray|string} message The message to hash.
	     *
	     * @return {WordArray} The hash.
	     *
	     * @static
	     *
	     * @example
	     *
	     *     var hash = CryptoJS.SHA1('message');
	     *     var hash = CryptoJS.SHA1(wordArray);
	     */
	    C.SHA1 = Hasher._createHelper(SHA1);

	    /**
	     * Shortcut function to the HMAC's object interface.
	     *
	     * @param {WordArray|string} message The message to hash.
	     * @param {WordArray|string} key The secret key.
	     *
	     * @return {WordArray} The HMAC.
	     *
	     * @static
	     *
	     * @example
	     *
	     *     var hmac = CryptoJS.HmacSHA1(message, key);
	     */
	    C.HmacSHA1 = Hasher._createHmacHelper(SHA1);
	}());


	return CryptoJS.SHA1;

}));

/***/ }),
/* 29 */
/***/ (function(module, exports, __webpack_require__) {

;(function (root, factory) {
	if (true) {
		// CommonJS
		module.exports = exports = factory(__webpack_require__(0));
	}
	else if (typeof define === "function" && define.amd) {
		// AMD
		define(["./core"], factory);
	}
	else {
		// Global (browser)
		factory(root.CryptoJS);
	}
}(this, function (CryptoJS) {

	(function () {
	    // Shortcuts
	    var C = CryptoJS;
	    var C_lib = C.lib;
	    var Base = C_lib.Base;
	    var C_enc = C.enc;
	    var Utf8 = C_enc.Utf8;
	    var C_algo = C.algo;

	    /**
	     * HMAC algorithm.
	     */
	    var HMAC = C_algo.HMAC = Base.extend({
	        /**
	         * Initializes a newly created HMAC.
	         *
	         * @param {Hasher} hasher The hash algorithm to use.
	         * @param {WordArray|string} key The secret key.
	         *
	         * @example
	         *
	         *     var hmacHasher = CryptoJS.algo.HMAC.create(CryptoJS.algo.SHA256, key);
	         */
	        init: function (hasher, key) {
	            // Init hasher
	            hasher = this._hasher = new hasher.init();

	            // Convert string to WordArray, else assume WordArray already
	            if (typeof key == 'string') {
	                key = Utf8.parse(key);
	            }

	            // Shortcuts
	            var hasherBlockSize = hasher.blockSize;
	            var hasherBlockSizeBytes = hasherBlockSize * 4;

	            // Allow arbitrary length keys
	            if (key.sigBytes > hasherBlockSizeBytes) {
	                key = hasher.finalize(key);
	            }

	            // Clamp excess bits
	            key.clamp();

	            // Clone key for inner and outer pads
	            var oKey = this._oKey = key.clone();
	            var iKey = this._iKey = key.clone();

	            // Shortcuts
	            var oKeyWords = oKey.words;
	            var iKeyWords = iKey.words;

	            // XOR keys with pad constants
	            for (var i = 0; i < hasherBlockSize; i++) {
	                oKeyWords[i] ^= 0x5c5c5c5c;
	                iKeyWords[i] ^= 0x36363636;
	            }
	            oKey.sigBytes = iKey.sigBytes = hasherBlockSizeBytes;

	            // Set initial values
	            this.reset();
	        },

	        /**
	         * Resets this HMAC to its initial state.
	         *
	         * @example
	         *
	         *     hmacHasher.reset();
	         */
	        reset: function () {
	            // Shortcut
	            var hasher = this._hasher;

	            // Reset
	            hasher.reset();
	            hasher.update(this._iKey);
	        },

	        /**
	         * Updates this HMAC with a message.
	         *
	         * @param {WordArray|string} messageUpdate The message to append.
	         *
	         * @return {HMAC} This HMAC instance.
	         *
	         * @example
	         *
	         *     hmacHasher.update('message');
	         *     hmacHasher.update(wordArray);
	         */
	        update: function (messageUpdate) {
	            this._hasher.update(messageUpdate);

	            // Chainable
	            return this;
	        },

	        /**
	         * Finalizes the HMAC computation.
	         * Note that the finalize operation is effectively a destructive, read-once operation.
	         *
	         * @param {WordArray|string} messageUpdate (Optional) A final message update.
	         *
	         * @return {WordArray} The HMAC.
	         *
	         * @example
	         *
	         *     var hmac = hmacHasher.finalize();
	         *     var hmac = hmacHasher.finalize('message');
	         *     var hmac = hmacHasher.finalize(wordArray);
	         */
	        finalize: function (messageUpdate) {
	            // Shortcut
	            var hasher = this._hasher;

	            // Compute HMAC
	            var innerHash = hasher.finalize(messageUpdate);
	            hasher.reset();
	            var hmac = hasher.finalize(this._oKey.clone().concat(innerHash));

	            return hmac;
	        }
	    });
	}());


}));

/***/ }),
/* 30 */
/***/ (function(module, exports, __webpack_require__) {

;(function (root, factory) {
	if (true) {
		// CommonJS
		module.exports = exports = factory(__webpack_require__(0));
	}
	else if (typeof define === "function" && define.amd) {
		// AMD
		define(["./core"], factory);
	}
	else {
		// Global (browser)
		factory(root.CryptoJS);
	}
}(this, function (CryptoJS) {

	(function () {
	    // Shortcuts
	    var C = CryptoJS;
	    var C_lib = C.lib;
	    var WordArray = C_lib.WordArray;
	    var C_enc = C.enc;

	    /**
	     * Base64 encoding strategy.
	     */
	    var Base64 = C_enc.Base64 = {
	        /**
	         * Converts a word array to a Base64 string.
	         *
	         * @param {WordArray} wordArray The word array.
	         *
	         * @return {string} The Base64 string.
	         *
	         * @static
	         *
	         * @example
	         *
	         *     var base64String = CryptoJS.enc.Base64.stringify(wordArray);
	         */
	        stringify: function (wordArray) {
	            // Shortcuts
	            var words = wordArray.words;
	            var sigBytes = wordArray.sigBytes;
	            var map = this._map;

	            // Clamp excess bits
	            wordArray.clamp();

	            // Convert
	            var base64Chars = [];
	            for (var i = 0; i < sigBytes; i += 3) {
	                var byte1 = (words[i >>> 2]       >>> (24 - (i % 4) * 8))       & 0xff;
	                var byte2 = (words[(i + 1) >>> 2] >>> (24 - ((i + 1) % 4) * 8)) & 0xff;
	                var byte3 = (words[(i + 2) >>> 2] >>> (24 - ((i + 2) % 4) * 8)) & 0xff;

	                var triplet = (byte1 << 16) | (byte2 << 8) | byte3;

	                for (var j = 0; (j < 4) && (i + j * 0.75 < sigBytes); j++) {
	                    base64Chars.push(map.charAt((triplet >>> (6 * (3 - j))) & 0x3f));
	                }
	            }

	            // Add padding
	            var paddingChar = map.charAt(64);
	            if (paddingChar) {
	                while (base64Chars.length % 4) {
	                    base64Chars.push(paddingChar);
	                }
	            }

	            return base64Chars.join('');
	        },

	        /**
	         * Converts a Base64 string to a word array.
	         *
	         * @param {string} base64Str The Base64 string.
	         *
	         * @return {WordArray} The word array.
	         *
	         * @static
	         *
	         * @example
	         *
	         *     var wordArray = CryptoJS.enc.Base64.parse(base64String);
	         */
	        parse: function (base64Str) {
	            // Shortcuts
	            var base64StrLength = base64Str.length;
	            var map = this._map;
	            var reverseMap = this._reverseMap;

	            if (!reverseMap) {
	                    reverseMap = this._reverseMap = [];
	                    for (var j = 0; j < map.length; j++) {
	                        reverseMap[map.charCodeAt(j)] = j;
	                    }
	            }

	            // Ignore padding
	            var paddingChar = map.charAt(64);
	            if (paddingChar) {
	                var paddingIndex = base64Str.indexOf(paddingChar);
	                if (paddingIndex !== -1) {
	                    base64StrLength = paddingIndex;
	                }
	            }

	            // Convert
	            return parseLoop(base64Str, base64StrLength, reverseMap);

	        },

	        _map: 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/='
	    };

	    function parseLoop(base64Str, base64StrLength, reverseMap) {
	      var words = [];
	      var nBytes = 0;
	      for (var i = 0; i < base64StrLength; i++) {
	          if (i % 4) {
	              var bits1 = reverseMap[base64Str.charCodeAt(i - 1)] << ((i % 4) * 2);
	              var bits2 = reverseMap[base64Str.charCodeAt(i)] >>> (6 - (i % 4) * 2);
	              words[nBytes >>> 2] |= (bits1 | bits2) << (24 - (nBytes % 4) * 8);
	              nBytes++;
	          }
	      }
	      return WordArray.create(words, nBytes);
	    }
	}());


	return CryptoJS.enc.Base64;

}));

/***/ }),
/* 31 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
	value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _io = __webpack_require__(8);

var _io2 = _interopRequireDefault(_io);

var _ua = __webpack_require__(7);

var _ua2 = _interopRequireDefault(_ua);

var _log = __webpack_require__(4);

var _log2 = _interopRequireDefault(_log);

var _config = __webpack_require__(6);

var _config2 = _interopRequireDefault(_config);

var _util = __webpack_require__(1);

var _util2 = _interopRequireDefault(_util);

var _signature = __webpack_require__(9);

var _signature2 = _interopRequireDefault(_signature);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var MD5 = __webpack_require__(2);
var hex = __webpack_require__(11);
var utf8 = __webpack_require__(10);
var secretKey = 'LZliQdg37Nm@yzJ1';

var ServerPoint = function () {
	function ServerPoint() {
		_classCallCheck(this, ServerPoint);
	}

	_createClass(ServerPoint, null, [{
		key: 'getAuthInfo',
		value: function getAuthInfo(userId, clientId, timestamp) {
			var str = userId + '|' + secretKey + '|' + timestamp;
			if (clientId) {
				str = userId + '|' + clientId + '|' + secretKey + '|' + timestamp;
			}

			var mdsStr = MD5(utf8.parse(str));
			return mdsStr.toString(hex);
		}
	}, {
		key: 'upload',
		value: function upload(params, success) {
			var timestamp = _util2.default.ISODateString(new Date());
			var authTimestamp = Math.floor(new Date().valueOf() / 1000);
			var clientId = _log2.default.getClientId();
			clientId = _log2.default.setClientId(clientId);
			var authInfo = ServerPoint.getAuthInfo(params.userId, clientId, authTimestamp);
			var SignatureNonceNum = _signature2.default.randomUUID();
			var SignatureMethodT = 'HMAC-SHA1';
			var newAry = {
				'Source': 'CLIENT',
				'BusinessType': 'UploadVideo',
				'Action': 'ReportUploadProgress',
				'TerminalType': 'h5',
				'DeviceModel': _ua2.default.browser.name + (_ua2.default.browser.version || ""),
				'AppVersion': _config2.default.version,
				'AuthTimestamp': authTimestamp,
				'Timestamp': timestamp,
				'AuthInfo': authInfo,
				'FileName': params.file.name,
				'FileSize': params.file.size,
				'FileCreateTime': params.file.lastModified,
				'FileHash': params.fileHash,
				'UploadId': params.checkpoint.checkpoint.uploadId,
				'PartSize': params.checkpoint.checkpoint.partSize,
				'DonePartsCount': params.checkpoint.checkpoint.doneParts.length,
				'UploadPoint': JSON.stringify(params.checkpoint),
				'UploadRatio': params.checkpoint.loaded,
				'UserId': params.userId,
				'VideoId': params.videoId,
				//'Version': '2017-03-21',
				'Version': '2017-03-14',
				'Format': 'JSON',
				'SignatureMethod': SignatureMethodT,
				'SignatureVersion': '1.0',
				'SignatureNonce': SignatureNonceNum
			};
			if (clientId) {
				newAry['ClientId'] = clientId;
			}

			var pbugramsdic = _signature2.default.makeUTF8sort(newAry, '=', '&') + '&Signature=' + _signature2.default.aliyunEncodeURI(_signature2.default.makeChangeSiga(newAry, params.accessKeySecret));

			var httpUrlend = 'https://vod.' + params.region + '.aliyuncs.com/?' + pbugramsdic;

			_io2.default.get(httpUrlend, function (data) {
				if (success) {
					success();
				}
			}, function (errorText) {
				if (errorText) {
					console.log(errorText);
				}
			});
		}
	}, {
		key: 'get',
		value: function get(params, success, failed) {
			var timestamp = _util2.default.ISODateString(new Date());
			var authTimestamp = Math.floor(new Date().valueOf() / 1000);
			var clientId = _log2.default.getClientId();
			var authInfo = ServerPoint.getAuthInfo(params.userId, clientId, authTimestamp);
			var SignatureNonceNum = _signature2.default.randomUUID();
			var SignatureMethodT = 'HMAC-SHA1';
			var newAry = {
				'Source': 'CLIENT',
				'BusinessType': 'UploadVideo',
				'Action': 'GetUploadProgress',
				'TerminalType': 'h5',
				'DeviceModel': _ua2.default.browser.name + (_ua2.default.browser.version || ""),
				'AppVersion': _config2.default.version,
				'AuthTimestamp': authTimestamp,
				'Timestamp': timestamp,
				'AuthInfo': authInfo,
				'UserId': params.userId,
				'UploadInfoList': JSON.stringify(params.uploadInfoList),
				//'Version': '2017-03-21',
				'Version': '2017-03-14',
				'Format': 'JSON',
				'SignatureMethod': SignatureMethodT,
				'SignatureVersion': '1.0',
				'SignatureNonce': SignatureNonceNum
			};

			if (clientId) {
				newAry['ClientId'] = clientId;
			}

			var pbugramsdic = _signature2.default.makeUTF8sort(newAry, '=', '&') + '&Signature=' + _signature2.default.aliyunEncodeURI(_signature2.default.makeChangeSiga(newAry, params.accessKeySecret));

			var httpUrlend = 'https://vod.' + params.region + '.aliyuncs.com/?' + pbugramsdic;

			_io2.default.get(httpUrlend, function (data) {
				var progress = {},
				    cid = clientId;
				data = data ? JSON.parse(data) : {};
				if (data.UploadProgress && data.UploadProgress.UploadProgressList && data.UploadProgress.UploadProgressList.length > 0) {
					progress = data.UploadProgress.UploadProgressList[0];
					cid = progress.ClientId;
				}
				_log2.default.setClientId(cid);
				if (success) {
					success(progress);
				}
			}, function (errorText) {
				if (errorText) {
					failed(errorText);
					console.log(errorText);
				}
			});
		}
	}]);

	return ServerPoint;
}();

exports.default = ServerPoint;

/***/ }),
/* 32 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
    value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var MD5 = __webpack_require__(2);
var latin1 = __webpack_require__(33);
var hex = __webpack_require__(11);

var FileService = function () {
    function FileService() {
        _classCallCheck(this, FileService);
    }

    _createClass(FileService, null, [{
        key: 'getMd5',
        value: function getMd5(file, callback, error) {
            var fileReader = new FileReader();
            fileReader.onload = function (e) {
                try {
                    if (e && e.target) {
                        var hash = MD5(latin1.parse(e.target.result));
                        var md5 = hash.toString();
                        callback(md5);
                    }
                } catch (e) {
                    console.log(e);
                }
            };
            fileReader.onerror = function (e) {
                console.log(e);
                errorCallback(e);
            };

            var start = 0;
            var end = 1024;
            var blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice;
            var blobPacket = blobSlice.call(file, start, end);
            fileReader.readAsBinaryString(blobPacket);
        }
    }]);

    return FileService;
}();

exports.default = FileService;

/***/ }),
/* 33 */
/***/ (function(module, exports, __webpack_require__) {

;(function (root, factory) {
	if (true) {
		// CommonJS
		module.exports = exports = factory(__webpack_require__(0));
	}
	else if (typeof define === "function" && define.amd) {
		// AMD
		define(["./core"], factory);
	}
	else {
		// Global (browser)
		factory(root.CryptoJS);
	}
}(this, function (CryptoJS) {

	return CryptoJS.enc.Latin1;

}));

/***/ })
/******/ ]);