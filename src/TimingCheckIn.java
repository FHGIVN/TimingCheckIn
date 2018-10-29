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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lenovo on 2018/9/6.
 * @author zmc
 */
public class TimingCheckIn {
    /**
     * DNS域名污染 域名变化 因此直接使用IP
     */
    private final static String IP = "https://45.76.204.234";
    private final static String EMAIL = "邮箱";
    private final static String PASSWD = "密码";
    private final static String REMEMBER_ME = "week";

    /**
     * private final static String ORIGIN_URL = "https://ssr.0v0.xyz";
     * DNS污染修改后
     * private final static String ORIGIN_URL = "https://bupt.0v0.xyz/";
     */
    private final static String ORIGIN_URL = IP;
    /**
     * 登录URL
     */
    private final static String LOGIN_URL = IP + "/auth/login";
    /**
     * 签到URL
     */
    private final static String CHECKIN_URL = IP + "/user/checkin";

    private final static String LOGGER_POSITION = "D:\\temp\\TimingCheckIn\\TimingCheckIn.txt";
    /**
     * 登录成功 ret为1
     */
    private final static char LOGIN_SUCCESS_FLAG = '1';
    private static String sid;

    public static void main(String[] args) throws Exception {
        String loginFormData = "email=" + EMAIL + "&passwd=" + PASSWD + "&remember_me=" + REMEMBER_ME;
        try {
            String loginResult = sendPost(LOGIN_URL, loginFormData, null);
            // 若登录成功
            if (loginCheck(loginResult)) {
                String str = sendPost(CHECKIN_URL, null, sid);
                log(new Date() + " " + checkInCheck(str));
            }
        } catch (Exception e) {
            log(e.getMessage());
        }
    }

    /**
     * 根据提交登录返回的字符串判断是否登录成功
     */
    private static boolean loginCheck(String loginResult) {
        // 登录的返回格式形如 {"ret":1,"msg":"\u6b22\u8fce\u56de\u6765"}
        char c = loginResult.charAt(loginResult.indexOf("ret") + 5);
        return LOGIN_SUCCESS_FLAG == c;
    }

    /**
     * 获取签到结果的msg
     */
    private static String checkInCheck(String str) {
        System.out.println(str);
        String[] strings = str.split(",");
        for (String s : strings) {
            if (s.contains("msg")) {
                return unicodeToString(s.substring(s.indexOf(":") + 1).replace("{", "").replace("}", ""));
            }
        }
        return null;
    }

    /**
     * unicode转中文
     */
    private static String unicodeToString(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }

    /**
     * 从登录的返回请求头中获取SessionId,供签到请求使用
     */
    private static void getSessionId(Map map) {
        List sResult = (List) map.get("Set-Cookie");
        if (sResult != null) {
            String[] strings = ((String) (sResult.get(0))).split(";");
            if (strings.length > 0) {
                sid = strings[0];
            }
        }
    }

    /**
     * 合并登录和签到的post请求
     */
    private static String sendPost(String sUrl, String param, String sessionId) throws Exception {
        String result = "";
        HttpsURLConnection.setDefaultHostnameVerifier(new TimingCheckIn().new NullHostNameVerifier());
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        URL url = new URL(sUrl);
        //编写请求头
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        //将sessionId的值写入Cookie
        if (sessionId != null) {
            conn.setRequestProperty("Cookie", sessionId);
        }

        conn.setRequestProperty("accept", "application/json, text/javascript, */*; q=0.01");
        conn.setRequestProperty("accept-Encoding", "gzip, deflate, br");
        conn.setRequestProperty("accept-Language", "zh-CN,zh;q=0.9");
        conn.setRequestProperty("connection", "Keep-Alive");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        conn.setRequestProperty("origin", ORIGIN_URL);
        // 上一页面的链接(一些系统会对此进行判断)
        conn.setRequestProperty("referer", LOGIN_URL);
        conn.setRequestProperty("user-agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36");

        // 提交模式
        conn.setRequestMethod("POST");
        // 发送POST请求必须设置如下两行!
        conn.setDoOutput(true);
        conn.setDoInput(true);
        //获取输出流
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        // 发送请求参数
        if (param != null) {
            out.print(param);
        }
        // flush输出流的缓冲
        out.flush();

        // 定义BufferedReader输入流来读取URL的响应
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        //读取返回结果
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        Map map = conn.getHeaderFields();
        if (sessionId == null) {
            getSessionId(map);
        }
        out.close();
        in.close();
        return result;
    }

    /**
     * 打印日志
     */
    private static void log(String message) throws Exception {
        try {
            // 如果文件存在，则追加内容；如果文件不存在，则需要创建文件; true,进行追加写
            PrintWriter pw = new PrintWriter(new FileWriter(new File(LOGGER_POSITION), true));
            pw.println(message);
            pw.flush();
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
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
    }};

    public class NullHostNameVerifier implements HostnameVerifier {
        /**
         * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
         */
        @Override
        public boolean verify(String arg0, SSLSession arg1) {
            return true;
        }
    }

}
