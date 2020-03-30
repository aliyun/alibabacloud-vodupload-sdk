//Copyright (C) 2020 Alibaba Group Holding Limited
export default class Store
{
	static set(key,data)
	{
		try
		{
			if(window.localStorage)
			{
				localStorage.setItem(key, data);
			}
	    }catch(e)
	    {
	    	window[key+'_localStorage'] = data;
	    }
	}

	static get(key)
	{
		var items;
		try
		{
			if(window.localStorage)
		    {
				return localStorage.getItem(key);
			}
		}catch(e)
	    {
	    	return window[key+'_localStorage'];
	    }

	    return "";
	}

	static remove(key)
	{
		try
		{
			if(window.localStorage)
		    {
				localStorage.removeItem(key);
			}
		}catch(e)
	    {
	    	delete window[key+'_localStorage'];
	    }

	}
}
