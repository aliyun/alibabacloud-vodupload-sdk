//Copyright (C) 2020 Alibaba Group Holding Limited
export default class IO
{
	/**
	 * Simple http request for retrieving external files (e.g. text tracks)
	 * @param  {String}    url             URL of resource
	 * @param  {Function} onSuccess       Success callback
	 * @param  {Function=} onError         Error callback
	 * @param  {Boolean=}   withCredentials Flag which allow credentials
	 * @private
	 */
	static get(url, onSuccess, onError, asyncValue, withCredentials) {
	  var request;

	  onError = onError || function() {};

	  if (typeof XMLHttpRequest === 'undefined') {
	    // Shim XMLHttpRequest for older IEs
	    window.XMLHttpRequest = function() {
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
      request.onreadystatechange = function() {
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
	    if(typeof asyncValue == 'undefined')
	    {
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
}