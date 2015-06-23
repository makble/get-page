package prototype;

import info.monitorenter.cpdetector.io.CodepageDetectorProxy;
import info.monitorenter.cpdetector.io.HTMLCodepageDetector;
import info.monitorenter.cpdetector.io.JChardetFacade;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;


public class GetPageInfo {
    public Reporter r = new Reporter();
    public boolean gziped = false;
    public boolean deflated = false;    
    public boolean chunked = false;

    private static CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance();     
    static {     
        detector.add(new HTMLCodepageDetector(false));     
        detector.add(JChardetFacade.getInstance());     
    }

    public GetPageInfo() {
        
    }


    public String getCharset(URL url) throws IOException {
    
        // 获取http连接对象     
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        r.info("getCharset: connection opened " + url.toString());
        urlConnection.setReadTimeout(300000); 
        urlConnection.setConnectTimeout(100000); // deafult is 0, infinity, I don't think this will work        
        urlConnection.setDoOutput(true);
        //con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803 Fedora/3.5");
        urlConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)");

        long startTime = System.nanoTime();
        urlConnection.connect();
        long elapsedTime = System.nanoTime() - startTime;
        r.info("time elapsed when connecting: " + ((double)elapsedTime / 1000000000.0) + ", url is " + url.toString());
        
        r.info("getCharset: connected " + url.toString());
        
        // 网页编码     
        String strencoding = null;     
    
        // 首先根据header信息，判断页面编码   
        
        Map<String, List<String>> map = urlConnection.getHeaderFields();     
        Set<String> keys = map.keySet();     
        Iterator<String> iterator = keys.iterator();

        r.info("ResponseMessage returned: " + urlConnection.getResponseMessage() + ", " + url.toString());
    
        // 遍历,查找字符编码     
        String key = null;     
        String tmp = null;     
        while (iterator.hasNext()) {     
            key = iterator.next();     
            tmp = map.get(key).toString().toLowerCase();
            
            if (key != null && key.equals("Content-Type")) {     
                int m = tmp.indexOf("charset=");     
                if (m != -1) {     
                    strencoding = tmp.substring(m + 8).replace("]", "");   
                    r.info("charset 已分析, 根据ContentType: " + strencoding.toUpperCase() + ", " + url.toString());
                }     
            }
            
            if (key != null && key.equals("Content-Encoding")) {
                if(tmp.indexOf("gzip") != -1) {
                    r.info("gziped : " + url.toString());
                    gziped = true;
                }
            }
            
            if ( key != null && key.equals("Transfer-Encoding")) {
                if(tmp.indexOf("chunk") != -1){
                    r.info("chunked: " + url.toString());
                    chunked = true;
                }
            }
        }
        
        if (strencoding != null ) return strencoding;
        
        r.info("after get charset from Content-Type, not not succeed, start parsing meta. " + url.toString());
    

        String htmlcode = rawDownload(url);
        
        // 解析html源码，取出<meta />区域，并取出charset     
        String strbegin = "<meta";     
        String strend = ">";     
        String strtmp;     
        int begin = htmlcode.indexOf(strbegin);     
        int end = -1;     
        int inttmp;     

        while (begin > -1) {
            r.info("looping in while loop in search for meta. " + url.toString() + " begin is" + begin);

            end = htmlcode.substring(begin).indexOf(strend);     
            if (begin > -1 && end > -1) {     
                strtmp = htmlcode.substring(begin, begin + end).toLowerCase();     
                inttmp = strtmp.indexOf("charset");     
                if (inttmp > -1) {     
                    //msg("get encode from meta");
                    strencoding = strtmp.substring(inttmp + 7, end).replace(     
                            "=", "").replace("/", "").replace("\"", "")     
                            .replace("\'", "").replace(" ", "");
                    r.info("charset 已分析, 通过meta数据的分析: " + strencoding + ", URL is : " + url.toString());
                    return strencoding;     
                }     
            }     
            htmlcode = htmlcode.substring(begin+strbegin.length());     
            begin = htmlcode.indexOf(strbegin);     
        }     
        
        r.info("after get charset from meta and failed, start CodepageDetectorProxy analyze  " + url.toString());
    
        // 使用CodepageDetectorProxy分析编码
        strencoding = getFileEncoding(url);
    
        // 设置默认网页字符编码     
        if (strencoding == null) {
            r.info("CodepageDetectorProxy failed, using UTF-8 by default");
            strencoding = "UTF-8";     
        }      
        return strencoding;     
    }

    public static String getFileEncoding(URL url) {
    	Reporter r = new Reporter();
        java.nio.charset.Charset charset = null;
        try {      
            charset = detector.detectCodepage(url);      
        } catch (Exception e) {      
            r.error(e.getClass() + "检测编码失败");      
        }     
        if (charset != null)      
            return charset.name();      
        return null;     
    } 


    public String rawDownload(URL url) {
        long startTime = System.nanoTime();

        StringBuffer sb = new StringBuffer();     
        String line;
        URLConnection conn;
		try {
			conn = url.openConnection();
	        // setting these timeouts ensures the client does not deadlock indefinitely
	        // when the server has problems.
	        conn.setConnectTimeout(100000);
	        conn.setReadTimeout(100000);
	        InputStream ins = conn.getInputStream();
  
	        BufferedReader in = new BufferedReader(new InputStreamReader(ins));
	        while ((line = in.readLine()) != null) {     
	        	sb.append(line);     
	        }     
	        in.close();     

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) { // Report any errors that arise     
            r.error(e.toString());     
        }

        
        String htmlcode = sb.toString();
        long elapsedTime = System.nanoTime() - startTime;
        r.info("time elapsed when downloading: " + ((double)elapsedTime / 1000000000.0) + ", url is " + url.toString());
        return htmlcode;
    }
}
