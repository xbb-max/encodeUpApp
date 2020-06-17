/****************************
文件名:JsonUtils.java

创建时间:2013-3-29
所在包:
作者:罗泽锋
说明:Json解析模块通用工具类
 ****************************/

package cn.yunting.utils;


import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JsonUtils {
	public static String getString(JSONObject jo, String key) {
		String ret = "";
		if (jo != null) {
			try {
				String r = jo.getString(key);

				if (!TextUtils.isEmpty(r))
					ret = r;
			} catch (JSONException e) {
			}
		}
		return ret;
	}
	
	public static String getString(JSONArray jo, int index) {
		String word;
		try {
			word = jo.getString(index);
			return word;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static boolean getBoolean(JSONObject jo, String key) {
		boolean ret = false;
		if (jo != null) {
			try {
				ret = jo.getBoolean(key);

			} catch (JSONException e) {
			}
		}
		return ret;
	}

	public static int getInt(JSONObject jo, String key) {
		int ret = 0;
		if (jo != null) {
			try {
				ret = jo.getInt(key);

			} catch (JSONException e) {
			}
		}
		return ret;
	}

	public static long getLong(JSONObject jo, String key) {
		if (jo != null) {
			try {
				return jo.getLong(key);
			} catch (JSONException e) {
			}
		}
		return 0;
	}

	public static double getDouble(JSONObject jo, String key) {
		if (jo != null) {
			try {
				return jo.getDouble(key);
			} catch (JSONException e) {
			}
		}
		return 0;
	}
	
	public static JSONObject getJSONObject(JSONObject jo, String key) {
		if (jo != null) {
			try {
				return jo.getJSONObject(key);
			} catch (JSONException e) {
			}
		}
		return null;
	}

	public static JSONArray getJSONArray(JSONObject jo, String key) {
		JSONArray jsonArray = null;
		try {
			jsonArray = (JSONArray) jo.getJSONArray(key);
		} catch (JSONException e) {
			jsonArray = new JSONArray();
		}
		return jsonArray;
	}



	public static JSONObject getJsonArray(JSONArray jsonArray, int index) {
		try {
			Object obj = jsonArray.get(index);
			if (obj instanceof JSONObject) {
				return (JSONObject)obj;
			}
		} catch (JSONException e) {
		}
		return null;
	}

}
