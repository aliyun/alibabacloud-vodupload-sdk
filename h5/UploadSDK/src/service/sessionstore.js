//Copyright (C) 2020 Alibaba Group Holding Limited
export default class SessionStore
{
	static set(key,data)
	{
		try
		{
			if(window.sessionStorage)
			{
				sessionStorage.setItem(key, data);
			}
	    }catch(e)
	    {
	    	window[key+'_sessionStorage'] = data;
	    }
	}

	static get(key)
	{
		var items;
		try
		{
			if(window.sessionStorage)
		    {
				return sessionStorage.getItem(key);
			}
		}catch(e)
	    {
	    	return window[key+'_sessionStorage'];
	    }

	    return "";
	}

	static remove(key)
	{
		try
		{
			if(window.sessionStorage)
		    {
				sessionStorage.removeItem(key);
			}
		}catch(e)
	    {
	    	delete window[key+'_sessionStorage'];
	    }

	}
}
