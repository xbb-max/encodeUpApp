package cn.yunting.utils;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public abstract class BaseProtocolPage implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final int DEFSULT_MSG_ARG1 = 0;
	// 抽象方法，有派生类决定自己的协议动作名称
	abstract public String getActionName();
		
	// 抽象方法，有派生类决定自己协议动作对应的参数
	abstract public String getExtParam(Object param);

	// 抽象方法，由派生类决定自己协议成功的消息what
	abstract public int getMsgWhatOk();

	// 抽象方法，由派生类决定自己协议错误的消息what
	abstract public int getMsgWhatError();

	// 抽象方法，派生类必须实现此方法，根据不同协议解析响应的数据
	abstract public Object parserJson(byte[] response);

	// 抽象方法，有派生类 实现，用来保存具体协议数据对象
	abstract public void setData(Object data);
	
	// 抽象方法，有派生类决定自己协议动作对应的参数
	abstract public Map<String, File> getPhotoFiles();
	// 更新协议数据的方法，更新结果通过handler的msg异步传送给调用者
	public void refresh(Object param) {
		if (param!= null) {
			mParam = param;
		}
		stratDownloadThread();
	}

	private DownloadProtocolTask task;
	private void stratDownloadThread() {
		// TODO Auto-generated method stub
		task = new DownloadProtocolTask(this);
		task.execute("");
	}

	private class DownloadProtocolTask extends AsyncTask<Object, Void, Boolean> {
		BaseProtocolPage protocolPage;

		public DownloadProtocolTask(BaseProtocolPage baseProtocolPage) {
			protocolPage = baseProtocolPage;
		}

		@Override
		protected Boolean doInBackground(Object... params) {
			try {
				protocolPage.runDownload();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean ret) {
		}

		@Override
		protected void onCancelled(Boolean ret) {
			super.onCancelled(ret);
		}
	}
	public BaseProtocolPage(String url, Object param, Handler handler) {
		init(url, param, handler);
	}

	// 请求协议数据的url地址
	private String pageUrl = null;

	// 协议参数保存对象
	public Object mParam = null;
	// 协议返回码
	private String errcode = "";
	// 协议返回消息
	public String msg = "";
	protected Object waitmHandlerList;
	// Handler数组成员变量
	private ArrayList<Handler> mHandlerList = new ArrayList<Handler>();

	private void init(String url, Object param, Handler handler) {
		// TODO Auto-generated method stub
		waitmHandlerList = new Object();
		if (url != null)
			pageUrl = url;
		if (param != null)
			mParam = param;
		addHandler(handler);
	}

	// 添加Handler对象
	public void addHandler(Handler handler) {
		synchronized (waitmHandlerList) {
			if (handler != null) {
				if (!mHandlerList.contains(handler)) {
					mHandlerList.add(handler);
				}
			}
		}
	}
	// 下载协议数据对象并解析处理函数
	private synchronized Message runDownload() {

		Message ret = new Message();

		byte[] response = null;
		
//		response = NetUtils.getHttpDataUsePost(pageUrl,
//				getActionName(), getExtParam(mParam));
		StringBuffer sb = new StringBuffer();
//		sb.append(pageUrl);
//		sb.append("?action=");
//		sb.append(getActionName());
        sb.append("http://");
        sb.append("39.106.73.84:8082/api/");
        sb.append(getActionName());
//        sb.append("/mobileCard/ServiceCenter.do");

//		if (getActionName().equals("activateCade"))
//		{
//			response = NetUtils.post_png_Params(sb.toString(), getExtParam(mParam),getPhotoFiles());
//		}else
		if (getActionName().equals("uploadIdentity"))
		{
			response = NetUtils.getHttpDataUsePost(sb.toString(), getExtParam(mParam),
					60 * 1000, 1,true,getActionName());
		}else
		response = NetUtils.getHttpDataUsePost(sb.toString(), getExtParam(mParam),
						10 * 1000, 3,true,getActionName());


		errcode = "";
		msg = "";

		int msgWhat = -1;
		// 解释数据
		Object res = parserJson(response);
		// 判断是否解析成功
		if (res != null) {
			if (msgWhat < 0) {
				msgWhat = getMsgWhatOk();
				setData(res);
			}
		} else {
			msgWhat = getMsgWhatError();
		}
		ret.what = msgWhat;
		sendMessag2UI(msgWhat, DEFSULT_MSG_ARG1);
		return ret;
	}
	private void sendMessag2UI(int msgWhat, int msgArg1) {
		// 发送消息给UI
		if (mHandlerList.size() == 0)
			return;

		for (int i = 0; i < mHandlerList.size(); i++) {
			Message msg = mHandlerList.get(i).obtainMessage();
			msg.what = msgWhat;
			msg.arg1 = msgArg1;
			mHandlerList.get(i).sendMessage(msg);
		}
	}
	public JSONArray getJsonArray(byte[] response) {
		if (response != null) {
			try {
				String json = new String(response,"utf-8");//CommUtils.byteToString(response);
				// 解析数据
				 System.out.println("mylog------json "+json);
				try {
					JSONObject jo = new JSONObject(json);
					errcode = JsonUtils.getString(jo, "errcode");
					msg = JsonUtils.getString(jo, "msg");
					if (msg == null) msg = "";
					//
					if (errcode.equals("100000")) {
						JSONArray jsonArray = JsonUtils.getJSONArray(jo, "result");
						return jsonArray;
					}else {
						msg = JsonUtils.getString(jo, "msg");
					}
				} catch (JSONException e) {
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return null;
	}
}
