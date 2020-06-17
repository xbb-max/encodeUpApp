/****************************
文件名:NetUtils.java

创建时间:2013-3-27
所在包:
作者:罗泽锋
说明:网络模块通用工具类
 ****************************/

package cn.yunting.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NetUtils {
	// 协议测试开关
	public static boolean PROTOCOL_TEST = true;
	// 网络数据每次只读取一个字节开关
	private static boolean READ_BUFFER = false;
	// 使用Socket方式开关
	public static boolean USE_SOCKET = false;

	public final static int CANCEL_DOWNLOAD = 9;
	public final static int DOWNLOAD_FAILED = 10;
	public final static int UPDATA_PROCESS = 11;
	public final static int UPDATA_PROCESS_SUCCESS = 12;
	public final static int ALREADY_DOWNLOAD = 13;
	private static final int DefaultReadBufferSize = 1000;

	public static byte[] getHttpDataUsePost(String url, String InputParam,
                                            int timerout, int repeatCount, boolean isPost, String action) {
		return getHttpDataUsePost(url, InputParam, timerout, repeatCount,
				false, isPost,action);
	}
	public static byte[] post_png_Params(String url, String params, Map<String, File> files)
	{
		byte[] pResultBuf = null;
		try {
			StringBuffer commparam = new StringBuffer();
			commparam.append(getCommonParam(params));

			String BOUNDARY = java.util.UUID.randomUUID().toString();
			String PREFIX = "--", LINEND = "\r\n";
			String MULTIPART_FROM_DATA = "multipart/form-data";
			String CHARSET = "UTF-8";


			URL uri = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
			conn.setReadTimeout(10 * 1000); // 缓存的最长时间
			conn.setDoInput(true);// 允许输入
			conn.setDoOutput(true);// 允许输出
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST");
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Charsert", "UTF-8");
			conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

			LogUtils.x("---commparam---"+commparam);
			String[] value = commparam.toString().split("&");

			String[] a = value[0].split("=");
			Map<String, String> params2 = new HashMap<String, String>();

			params2.put(a[0], a[1]);
			String[] b = value[1].split("=");
			params2.put(b[0], b[1]);
			String[] c = value[2].split("=");
			params2.put(c[0], c[1]);
			String[] d = value[3].split("=");
			params2.put(d[0], d[1]);
			// 首先组拼文本类型的参数
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> entry : params2.entrySet()) {
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINEND);
				sb.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + LINEND);
				sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
				sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
				sb.append(LINEND);
				sb.append(entry.getValue());
				sb.append(LINEND);
			}
			DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
			outStream.write(sb.toString().getBytes());
			// 发送文件数据
			if (files != null)
				for (Map.Entry<String, File> file : files.entrySet()) {
					StringBuilder sb1 = new StringBuilder();
					sb1.append(PREFIX);
					sb1.append(BOUNDARY);
					sb1.append(LINEND);
					LogUtils.x("file.getValue().getName() "+file.getValue().getName());
					if (file.getValue().getName().equals("photo1")) {
						sb1.append("Content-Disposition: form-data; name=\"cit\"; filename=\""
								+ file.getValue().getName()+".png" + "\"" + LINEND);
					}else if(file.getValue().getName().equals("photo2")){
						sb1.append("Content-Disposition: form-data; name=\"cib\"; filename=\""
								+ file.getValue().getName()+".png" + "\"" + LINEND);
					}else if(file.getValue().getName().equals("photo3")){
						sb1.append("Content-Disposition: form-data; name=\"cih\"; filename=\""
								+ file.getValue().getName()+".png" + "\"" + LINEND);
					}else if(file.getValue().getName().equals("photo4")){
						sb1.append("Content-Disposition: form-data; name=\"cia\"; filename=\""
								+ file.getValue().getName()+".png" + "\"" + LINEND);
					}
					sb1.append("Content-Type: application/octet-stream; charset=" + CHARSET + LINEND);
					sb1.append(LINEND);
					outStream.write(sb1.toString().getBytes());


					InputStream is = new FileInputStream(file.getValue());
					byte[] buffer = new byte[1024];
					int len = 0;
					while ((len = is.read(buffer)) != -1) {
						outStream.write(buffer, 0, len);
					}


					is.close();
					outStream.write(LINEND.getBytes());
				}


			// 请求结束标志
			byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
			outStream.write(end_data);
			outStream.flush();
			// 得到响应码
			int res = conn.getResponseCode();
			InputStream in = conn.getInputStream();
			if (res == 200) {

				pResultBuf = getBytesFromStream(in, -1);
				String rea = new String(pResultBuf);
//	            int ch;
//	            while ((ch = in.read()) != -1) {
//	                sb2.append((char) ch);
//	            }
			}
			outStream.close();
			conn.disconnect();
			return pResultBuf;
		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}

	}

	public static String getCommonParam(String InputParam) {
		StringBuffer commparam = new StringBuffer();
		/*
		try {
			StringBuffer sb = new StringBuffer();
			if (InputParam.indexOf("userid") < 0) {
				if ((UserInfoManager.getInstance().info != null) && (!TextUtils.isEmpty(UserInfoManager.getInstance().info.userid))) {
					sb.append("userid=").append(UserInfoManager.getInstance().info.userid);
				}else {
					sb.append("userid=0");
				}
			}
			if (InputParam.indexOf("mobile") < 0) {
				if (UserInfoManager.getInstance().info!=null  && !TextUtils.isEmpty(UserInfoManager.getInstance().info.mobile)) {
					sb.append("&mobile=").append(UserInfoManager.getInstance().info.mobile);
				}else {
					sb.append("&mobile=").append("0");
				}
			}
			sb.toString().trim();
			sb.append("&");
			sb.append(InputParam);
			String sourceString = sb.toString();// "userid=0&mobile=13503335187";
			LogUtils.x("sourceString " + sourceString);
			String tmpsString = CommUtils.makeParamEncrypt(sourceString);
			System.out.println("tmpsString " + tmpsString);
			CommUtils.addParam(commparam, "data", tmpsString);
			String urlEncoder = java.net.URLEncoder.encode(sourceString, "gbk");
			CommUtils.addParam(commparam, "secret", CommUtils.MD5(urlEncoder));
		} catch (Exception e) {
			// TODO: handle exception
		}*/
		LogUtils.x("------xbb getCommonParam InputParam "+InputParam);
		StringBuffer sb = new StringBuffer();
		commparam.append(sb.append(InputParam).toString());
		addParam(commparam, "format", "json");
		return commparam.toString();


	}



	// post方式获取url对应的数据，可以传入参数，超时时间，重试次数
	public static byte[] getHttpDataUsePost(String url, String InputParam,
                                            int timerout, int repeatCount, boolean test, boolean isPost, String action) {

		byte[] pResultBuf = null;
		int reConnectHttp = 0;
		boolean done = false;
		while (true) {
			if (USE_SOCKET) {
				pResultBuf = getHttpDataUseSocket(url, InputParam, timerout,
						test);
				if (pResultBuf != null) {
					done = true;
					break;
				}
			} else {
				HttpURLConnection con = null;
				OutputStream os = null;
				DataOutputStream dos = null;
				try {
					StringBuffer sb = new StringBuffer();
					if (!isPost) {
						sb.append(url);
						sb.append("?");
						//sb.append(code);
						//sb.append("&");
						sb.append(getCommonParam(InputParam));
						// sb.append("&");
						// sb.append(InputParam);
					} else {
						sb.append(url);
					}
					URL dataUrl = new URL(sb.toString());
					con = (HttpURLConnection) dataUrl.openConnection();
					con.setUseCaches(false);
					if (isPost) {

						StringBuffer commparam = new StringBuffer();
//						commparam.append("action=");
//						commparam.append(action);
//						commparam.append("&");
						commparam.append(getCommonParam(InputParam));

						LogUtils.x("-----" + "sb.toString() " + sb.toString() + "?" + commparam.toString());
//						LogUtils.writeLogtoFile(sb.toString() + "?" + commparam.toString());
						// commparam.append(InputParam);

						con.setRequestMethod("POST");
						con.setRequestProperty("Proxy-Connection", "Keep-Alive");

						con.setRequestProperty("Content-Type",
								"application/x-www-form-urlencoded");
						con.setDoOutput(true);
						con.setDoInput(true);
						con.setConnectTimeout(timerout);
						con.setReadTimeout(timerout);
						os = con.getOutputStream();
						dos = new DataOutputStream(os);
						dos.write(commparam.toString().getBytes());

					}
					int res = con.getResponseCode();
					LogUtils.x("----协议交互结果 res ---- " + res);
					// 由于服务器返回的content-length并不准确，忽略此参数，以实际收到为准。
					int len = -1;

					if ((res == 200)) {
						InputStream is = con.getInputStream();
						pResultBuf = getBytesFromStream(is, len);
						done = true;
						break;
					}

				} catch (Exception ex) {
					LogUtils.x("----协议交互出现异常---- "+ex.getMessage());
				} finally {
					if (dos != null)
						try {
							dos.close();
						} catch (IOException e) {
						}
					if (os != null)
						try {
							os.close();
						} catch (IOException e) {
						}
					if (con != null)
						con.disconnect();

				}
			}
			reConnectHttp++;
			if (done || reConnectHttp >= repeatCount) {
				break;
			} else {
				try {
					Thread.sleep(3000);
				} catch (Exception e) {
				}
			}
		}

		return pResultBuf;
	}

	public static String ToEncoder(String para) {
		String p = "";
		if (TextUtils.isEmpty(para)) {
			return p;
		} else {
			try {

				p = java.net.URLEncoder.encode(para, "utf-8");
				if (TextUtils.isEmpty(p))
					return "";
				else
					return p;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				// LogUtils.PST(e);
			}
		}
		return "";
	}

	// 添加参数
	public static void addParam(StringBuffer s, String key, String value) {
		String v = ToEncoder(value);
		String lastChar = null;
		String str = s.toString();
		int len = str.length();
		if (len > 0)
			lastChar = str.substring(len - 1, len);
		if (lastChar != null && !lastChar.equals("&"))
			s.append("&");
		s.append(key);
		s.append("=");
		s.append(v);
	}

	public static byte[] getHttpDataUseSocket(String url, String postData,
                                              int timerout, boolean test) {
		byte[] ret = null;
		Socket socket = null;
		OutputStream os = null;
		InputStream ins = null;
		try {
			URL u = new URL(url);
			int port = u.getPort();
			if (port < 0)
				port = 80;
			String host = u.getHost();
			String path = u.getPath();
			String query = u.getQuery();

			InetAddress serverAddr = null;
			serverAddr = InetAddress.getByName(host);
			socket = new Socket();
			InetSocketAddress isa = null;
			isa = new InetSocketAddress(serverAddr.getHostAddress(), port);

			socket.connect(isa, timerout);

			socket.setSoTimeout(timerout);
			socket.setKeepAlive(true);

			boolean usePost = !TextUtils.isEmpty(postData);

			os = socket.getOutputStream();
			ins = socket.getInputStream();

			StringBuffer sb = new StringBuffer();
			if (usePost)
				sb.append("POST ");
			else
				sb.append("GET ");
			if (TextUtils.isEmpty(path)) {
				sb.append("/");
			} else {
				sb.append(path);
			}
			if (!TextUtils.isEmpty(query)) {
				sb.append("?");
				sb.append(query);
			}

			sb.append(" HTTP/1.0\r\n");
			sb.append("Host:");
			sb.append(host);
			sb.append(":");
			sb.append(port);
			sb.append("\r\n");
			sb.append("Accept: */*");
			sb.append("\r\n");
			sb.append("Accept-Language: zh-cn");
			sb.append("\r\n");
			sb.append("User-Agent: Mozilla/4.0 (compatible; MSIE 5.00; Windows 98)");
			sb.append("\r\n");
			sb.append("Content-Type: application/x-www-form-urlencoded");
			sb.append("\r\n");

			sb.append("Connection: Keep-Alive\r\n");
			if (usePost) {
				sb.append("Content-Length: ");
				sb.append(postData.length());
				sb.append("\r\n");
			}
			sb.append("\r\n");
			if (usePost)
				sb.append(postData);

			os.write(sb.toString().getBytes());
			os.flush();

			ret = getBytesFromStreamUseSocket(ins);

		} catch (Exception e) {
		} finally {
			try {
				if (ins != null)
					ins.close();
				if (os != null)
					os.close();
				if (socket != null)
					socket.close();
			} catch (IOException e) {
			}
		}
		return ret;
	}

	public static byte[] getBytesFromStream(InputStream is, int len) {
		byte[] ret = null;
		int readSize = 0;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			if (READ_BUFFER) {
				int ch = 0;
				while ((ch = is.read()) != -1) {
					readSize++;
					baos.write(ch);
					if (len > 0 && readSize >= len) {
						break;
					}
				}
			} else {
				byte[] buf = new byte[DefaultReadBufferSize];
				while (true) {
					int readLen = is.read(buf, 0, buf.length);
					if (readLen == -1) {
						break;
					}
					readSize += readLen;
					baos.write(buf, 0, readLen);
					if (len > 0 && readSize >= len) {
						break;
					}
				}
				buf = null;
			}
			ret = baos.toByteArray();
		} catch (Exception e) {
		} finally {
			try {
				baos.close();
			} catch (IOException e) {
			}
		}
		return ret;
	}

	private static byte[] getBytesFromStreamUseSocket(InputStream is) {
		byte[] ret = null;

		int readSize = 0;
		int len = -1;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			int headIndex = -1;
			int resCode = -1;
			byte[] buf = new byte[DefaultReadBufferSize];
			int readLen = 0;
			while (true) {
				if (READ_BUFFER) {
					int ch = 0;
					if ((ch = is.read()) != -1) {
						readSize++;
						baos.write(ch);

					} else {
						break;
					}
				} else {
					readLen = is.read(buf, 0, buf.length);
					if (readLen == -1) {
						break;
					}
					readSize += readLen;
					baos.write(buf, 0, readLen);
				}
				if (headIndex == -1) {
					if (readSize > 20) {
						String s = new String(baos.toByteArray());
						headIndex = checkHeaderEndPos(s);
					}
				}
				if (resCode == -1 && headIndex > 0) {
					String s = new String(baos.toByteArray());
					resCode = getResCode(s);
				}
				if (len == -1 && headIndex > 0) {
					String s = new String(baos.toByteArray());
					len = getContentLength(s);
				}

				if (len > 0 && readSize >= len + headIndex) {
					break;
				}
			}

			byte[] data = baos.toByteArray();

			int bodyLen = readSize - headIndex;
			if (headIndex > 0 && bodyLen > 0 && resCode == 200) {
				ret = new byte[bodyLen];
				System.arraycopy(data, headIndex, ret, 0, bodyLen);
			}

			baos.close();
			baos = null;

		} catch (Exception e) {
		}

		if (ret != null) {
		}

		return ret;
	}

	private static int checkHeaderEndPos(String s) {
		int ret = -1;
		int off = 0;
		int i = s.indexOf("\r\n\r\n");
		if (i < 0) {
			i = s.indexOf("\n\n");
			if (i < 0) {
				i = s.indexOf("\r\r");
			}
			off = 2;
		} else {
			off = 4;
		}

		if (i > 0) {
			ret = i + off;
		}

		return ret;
	}
	public static int convert2int(String value) {
		int ret = 0;
		if (!TextUtils.isEmpty(value)) {
			try {
				ret = Integer.parseInt(value);
			} catch (NumberFormatException e) {
			}
		}
		return ret;
	}
	private static int getResCode(String s) {
		int ret = -1;
		if (s.length() > 0) {
			String str = s.toLowerCase();
			int i = str.indexOf("http/1.1");
			if (i == 0) {
				String v[] = str.split(" ");
				if (v.length >= 2) {
					ret = convert2int(v[1]);
				}
			}
		}
		return ret;
	}

	private static int getContentLength(String s) {
		int ret = -1;
		if (s.length() > 0) {
			String str = s.toLowerCase();
			String line[] = str.split("\n");
			for (int i = 0; i < line.length; i++) {
				if (line[i].contains("content-length")) {
					String nv[] = line[i].split(":");
					if (nv.length > 1) {
						ret = convert2int(nv[1].trim());

					}
				}
			}
		}
		return ret;
	}

	public static class ErrInfo {
		long offTime;
		int err;

		public static ErrInfo newErrInfo(long offTime, int err) {
			ErrInfo ei = new ErrInfo(offTime, err);
			return ei;
		}

		public ErrInfo(long offTime, int err) {
			this.offTime = offTime;
			this.err = err;
		}
	}

	public static ArrayList<ErrInfo> offTimeList = new ArrayList<ErrInfo>();

	public static String OffTimeString = "";

	public static String replace(String strSource, String strFrom, String strTo) {
		if (strFrom == null || strFrom.equals(""))
			return strSource;
		String strDest = "";
		int intFromLen = strFrom.length();
		int intPos;
		while ((intPos = strSource.indexOf(strFrom)) != -1) {
			strDest = strDest + strSource.substring(0, intPos);
			strDest = strDest + strTo;
			strSource = strSource.substring(intPos + intFromLen);
		}
		strDest = strDest + strSource;
		return strDest;
	}
	private static String gImsi = "";
	private static String gImei2 = "";
	public static String getImei2(Context context) {

		if (TextUtils.isEmpty(gImei2)) {

		}
		return gImei2;
	}

	public static String getImsi(Context context) {
		if (TextUtils.isEmpty(gImsi)) {
			if (context != null) {
			}
		}
		return gImsi;
	}
	public static String getHttpUrlString(String serverUrl)
	{
		try {
			URL url = new URL(serverUrl);
			HttpURLConnection hc = (HttpURLConnection) url.openConnection();
			hc.setDoInput(true);
			hc.setConnectTimeout(5000);
			hc.setReadTimeout(5000);
			hc.setInstanceFollowRedirects(true);
			int code = hc.getResponseCode();
			int contentLength = hc.getContentLength();
			if (code == HttpURLConnection.HTTP_OK) {
				InputStream is = hc.getInputStream();
				byte[] bs = getBytesFromStream(is, contentLength);
				is.close();
				String reString = new String(bs);
				return reString;
			}
		} catch (Exception e) {
			// TODO: handle exception
			LogUtils.x("----getHttpUrlString ----- e="+e.getMessage());
			return "";
		}
		return "";
	}

	// post方式获取url对应的数据，可以传入参数，超时时间，重试次数
	public static byte[] getHttpDataUsePost(String hostAddress,
                                            String ServiceName, String InputParam, int timerout, int repeatCount) {
		byte[] pResultBuf = null;
		String url = "http://" + hostAddress + "/" + ServiceName;
		LogUtils.x("---url--"+url +" InputParam "+InputParam);
		pResultBuf = getHttpDataUsePost(url, InputParam, timerout, repeatCount);
		return pResultBuf;
	}

	public static byte[] getHttpDataUsePost(String url, String InputParam,
                                            int timerout, int repeatCount) {
		return getHttpDataUsePost(url, InputParam, timerout, repeatCount, false);
	}

	// post方式获取url对应的数据，可以传入参数，超时时间，重试次数
	public static byte[] getHttpDataUsePost(String url, String InputParam,
                                            int timerout, int repeatCount, boolean test) {

		// url = RadioIpToUnicomIp(url,true);
		byte[] pResultBuf = null;
		int reConnectHttp = 0;
		boolean done = false;
		while (true) {
			if (USE_SOCKET) {
				pResultBuf = getHttpDataUseSocket(url, InputParam, timerout,
						test);
				if (pResultBuf != null) {
					done = true;
					break;
				}
			} else {
				HttpURLConnection con = null;
				OutputStream os = null;
				DataOutputStream dos = null;
				try {
					// LogUtils.DebugLog("DownLoadList" + " url " + url);
					URL dataUrl = new URL(url);
					con = (HttpURLConnection) dataUrl.openConnection();
					con.setUseCaches(false);
					con.setRequestMethod("POST");
					con.setRequestProperty("Proxy-Connection", "Keep-Alive");

					con.setRequestProperty("Content-Type",
							"application/x-www-form-urlencoded");
					con.setDoOutput(true);
					con.setDoInput(true);
					con.setConnectTimeout(timerout);
					con.setReadTimeout(timerout);
					os = con.getOutputStream();
					dos = new DataOutputStream(os);
					dos.write(InputParam.getBytes());
					int code = con.getResponseCode();

					LogUtils.x("DownLoadList getBytesFromStream con.getContentLength(): "
									+ con.getContentLength());
					// 由于服务器返回的content-length并不准确，忽略此参数，以实际收到为准。
					int len = -1;

					if ((code == 200)) {
						InputStream is = con.getInputStream();
						pResultBuf = getBytesFromStream(is, len);
						done = true;
						break;
					}

				} catch (Exception ex) {

				} finally {
					if (dos != null)
						try {
							dos.close();
						} catch (IOException e) {
						}
					if (os != null)
						try {
							os.close();
						} catch (IOException e) {
						}
					if (con != null)
						con.disconnect();

				}
			}
			reConnectHttp++;
			if (done || reConnectHttp >= repeatCount) {
				break;
			} else {
				try {
					Thread.sleep(3000);
				} catch (Exception e) {
				}
			}
		}

		return pResultBuf;
	}

}
