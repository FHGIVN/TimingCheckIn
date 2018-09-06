import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2018/9/6.
 */
public class TimingCheckIn {
    private final static String email = "帐号";
    private final static String passwd = "密码";
    private final static String remember_me = "week";

    private final static String loginUrl = "https://ssr.0v0.xyz/auth/login";
    private final static String originURL = "https://ssr.0v0.xyz";
    private final static String checkinUrl = "https://ssr.0v0.xyz/user/checkin";

    private static String sid;

    public static void main(String[] args) {
        // 1登录
        StringBuilder loginFormData = new StringBuilder();
        loginFormData.append("email=").append(email).append("&passwd=").append(passwd).append("&remember_me=").append(remember_me);

        String loginResult = SendPostLogin(loginUrl, loginFormData.toString());
        boolean isLoginSuccess = loginCheck(loginResult);
//        System.out.println(isLoginSuccess);

        if(isLoginSuccess){
            String str = SendPostCheck(checkinUrl,sid);
            System.out.println(str);
        }
    }

    /**
     * 根据提交登录返回的字符串判断是否登录成功
     * @param loginResult
     * */
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
     * 登录请求 Post
     * @param sUrl  访问链接
     * @param param 请求参数
     */
    private static String SendPostLogin(String sUrl, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";

        try {
            URL url = new URL(sUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();//编写请求头
            //将sessionId的值写入Cookie，ASPXAUTH为空，这里可以不要(按照具体的验证机制来写)

//          conn.setRequestProperty("Cookie", sessionId);
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
            out.print(param);
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
            getSessionId(map);
        } catch (IOException e) {
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

    /**
     * 签到请求 Post
     * @param sUrl  访问链接
     * @param sessionId session
     */
    private static String SendPostCheck(String sUrl, String sessionId) {
        PrintWriter out;
        BufferedReader in;
        String result = "";
        try {
            URL url = new URL(sUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();//编写请求头
            //将sessionId的值写入Cookie，ASPXAUTH为空，这里可以不要(按照具体的验证机制来写)


            conn.setRequestProperty("Host", sUrl);

            conn.setRequestProperty("accept", "application/json, text/javascript, */*; q=0.01");
            conn.setRequestProperty("accept-Encoding", "gzip, deflate, br");
            conn.setRequestProperty("accept-Language", "zh-CN,zh;q=0.9");
            conn.setRequestProperty("cookie", sessionId);
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
//            out.print(param);
            // flush输出流的缓冲
            out.flush();

            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            //读取返回结果
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 输出cookie
     * @param map
     * */
    private static void printCookie(Map map) {
        System.out.println("输出Cookie:");
        for (Object key : map.keySet()) {
            System.out.println("key= " + key + " and value= " + map.get(key));
        }
    }

    /**
     * 从请求头中获取SessionId
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
}
