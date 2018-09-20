import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2018/9/6.
 */
public class TimingCheckIn {
    // DNS域名污染 域名变化 直接使用IP
    private final static String IP = "IP地址";
    private final static String email = "邮箱";
    private final static String passwd = "密码";
    private final static String remember_me = "week";

//    private final static String loginUrl = "https://ssr.0v0.xyz/auth/login";
    private final static String loginUrl = IP + "/auth/login";
//    private final static String originURL = "https://ssr.0v0.xyz";
    private final static String originURL = IP;
//    private final static String checkinUrl = "https://ssr.0v0.xyz/user/checkin";
    private final static String checkinUrl = IP + "/user/checkin";

    private final static String logPosition = "D:\\temp\\TimingCheckIn\\TimingCheckIn.txt";
    private final static String errorLogPosition = "D:\\temp\\TimingCheckIn\\error.txt";

    private static String sid;

    public static void main(String[] args) throws FileNotFoundException {
        // 1登录
        StringBuilder loginFormData = new StringBuilder();
        loginFormData.append("email=").append(email).append("&passwd=").append(passwd).append("&remember_me=").append(remember_me);

        String loginResult = SendPost(loginUrl, loginFormData.toString(), null);
        boolean isLoginSuccess = loginCheck(loginResult);

        if (isLoginSuccess) {
            String str = SendPost(checkinUrl, null, sid);
            log();
        }
    }

    /**
     * 根据提交登录返回的字符串判断是否登录成功
     *
     * @param loginResult
     */
    private static boolean loginCheck(String loginResult) {
        // 登录的返回格式形如 {"ret":1,"msg":"\u6b22\u8fce\u56de\u6765"}
        char c = loginResult.charAt(loginResult.indexOf("ret") + 5);
        if (c == '1') {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 输出cookie
     *
     * @param map
     */
    private static void printCookie(Map map) {
        System.out.println("输出Cookie:");
        for (Object key : map.keySet()) {
            System.out.println("key= " + key + " and value= " + map.get(key));
        }
    }

    /**
     * 从登录的返回请求头中获取SessionId,供签到请求使用
     *
     * @param map
     */
    private static void getSessionId(Map map) {
        List sResult = (List) map.get("Set-Cookie");
        if (sResult != null) {
            String[] strings = ((String) (sResult.get(0))).split(";");
            if (strings != null && strings.length > 0) {
                sid = strings[0];
            }
        }
    }

    /**
     * 打印日志
     */
    private static void log() throws FileNotFoundException {
        PrintStream out = null;
        try {
            out = new PrintStream(logPosition);
            System.setOut(out);
            System.out.println(new Date() + " 签到！");
        } catch (FileNotFoundException e) {
            out = new PrintStream(errorLogPosition);
            System.setOut(out);
            System.out.println(new Date() + e.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 合并登录和签到的post请求
     */
    private static String SendPost(String sUrl, String param, String sessionId) {


        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";

        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new TimingCheckIn().new NullHostNameVerifier());
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            URL url = new URL(sUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();//编写请求头
            //将sessionId的值写入Cookie
            if (sessionId != null) {
                conn.setRequestProperty("Cookie", sessionId);
            }

            conn.setRequestProperty("Host", sUrl);
            conn.setRequestProperty("accept", "application/json, text/javascript, */*; q=0.01");
            conn.setRequestProperty("accept-Encoding", "gzip, deflate, br");
            conn.setRequestProperty("accept-Language", "zh-CN,zh;q=0.9");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty("origin", originURL);
            conn.setRequestProperty("referer", loginUrl);//上一页面的链接(一些系统会对此进行判断)
            conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36");

            conn.setRequestMethod("POST");// 提交模式
            // 发送POST请求必须设置如下两行!
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //获取输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            if (param != null) {
                out.print(param);
            }
            // flush输出流的缓冲
            out.flush();

            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            //读取返回结果
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }

            Map map = conn.getHeaderFields();
//          printCookie(map);
            if (sessionId == null) {
                getSessionId(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }



    static TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    } };

    public class NullHostNameVerifier implements HostnameVerifier {
        /*
         * (non-Javadoc)
         *
         * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String,
         * javax.net.ssl.SSLSession)
         */
        @Override
        public boolean verify(String arg0, SSLSession arg1) {
            return true;
        }
    }

}
