package prototype;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
   

public class Downloader {
    Reporter r = new Reporter();
    
    // semantically this just create a URL for a string url.

    public URL verifyUrl( String url ){
        if ( !url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://"))
            return null;
                                
        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(url);
        } catch ( Exception e ) {
            r.error("构造URL错误, https? :" + url);
            e.printStackTrace();
            return null;
        }
        return verifiedUrl;
    }

    private static void trustAllHttpsCertificates() throws Exception {  
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];  
        javax.net.ssl.TrustManager tm = new miTM();  
        trustAllCerts[0] = tm;  
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");  
        sc.init(null, trustAllCerts, null);  
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());  
    }  
  
    static class miTM implements javax.net.ssl.TrustManager,javax.net.ssl.X509TrustManager {  
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {  
            return null;  
        }  
  
        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {  
            return true;  
        }  
  
        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)throws java.security.cert.CertificateException {  
            return;  
        }  
  
        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) throws java.security.cert.CertificateException {  
            return;  
        }  
    }

    private static volatile long tid = -1; 
 
    public String DownloadPageInternal(String url) {
        URL verifiedurl = verifyUrl(url);
        GetPageInfo gpi = new GetPageInfo();

        r.info("开始下载页面:" + url);

        try {
            
            
            if (url.toLowerCase().startsWith("https://")) {
                HostnameVerifier hv = new HostnameVerifier() {  
                    public boolean verify(String urlHostName, SSLSession session) {  
                        System.out.println("Warning: URL Host: " + urlHostName + " vs. "  
                                + session.getPeerHost());  
                        return true;  
                    }  
                };  

                try {
                    trustAllHttpsCertificates();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }  
                HttpsURLConnection.setDefaultHostnameVerifier(hv);
            }
            

            HttpURLConnection con = (HttpURLConnection )verifiedurl.openConnection();
            con.setReadTimeout(100000); 
            con.setConnectTimeout(100000); // deafult is 0, infinity, I don't think this will work
            con.setDoOutput(true);
            //con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803 Fedora/3.5");
            con.setRequestProperty("User-Agent",
                                   "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)");
            //con.setRequestProperty("Accept-Encoding", "gzip,deflate");
            con.connect();

            
            InputStream ins = con.getInputStream();
            GZIPInputStream gzin = null;            
            String charset = gpi.getCharset(verifiedurl);

            if ( gpi.gziped ) {
                    gzin = new GZIPInputStream(ins);
            }                            
            
            if( gzin != null) ins = gzin;            

            BufferedReader reader = new BufferedReader(new InputStreamReader( ins ,charset));
            
            // 从这里到下载完成之间经常会出现僵死现象, 即无限等待. 
            r.info("in DownloadPage: stream is opend: " + url);
       
            long startTime = System.nanoTime();
            String line;
            StringBuffer pageBuffer  = new StringBuffer();
            while((line = reader.readLine()) != null){

                pageBuffer.append(line+ "\r\n");
            }
            long elapsedTime = System.nanoTime() - startTime;
            r.info("time elapsed when downloading: " + ((double)elapsedTime / 1000000000.0) + ", url is " + url.toString());
       
            r.info("完成下载" + url);
            ins.close();
            if(gzin != null ) gzin.close();
            reader.close();
            con.disconnect();
            
            return pageBuffer.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            if (e.getMessage().indexOf("GZIP") != -1 ) {
                r.debug("it is a gzip error" + url);
            }

            if ( e.getMessage().indexOf("reset") != -1){
                return "Connection reset";
            }
            
            if ( e.getMessage().indexOf("Read timed out") != -1){
                // return DownloadPage(url);
                return "Read time out";
            }

            if ( e.getMessage().indexOf("Connection timed out") != -1){
                return "Connection time out";
            }

            // Connection time out is big problem

            e.printStackTrace();
        }

        return "<html><body></body></html>";
        
    }

    public String DownloadPage(String url) {
        String downloaded = "";

        for ( int i = 0 ; i < 3 ; i++ ) {
            downloaded = DownloadPageInternal(url);
            if ( downloaded == null || (downloaded.equals("Connection time out") || downloaded.equals("Connection reset") || downloaded.equals("Read time out")) ) {
                r.warn(  " retry the " + ( i + 1) + "th retry");
            } else {
                return downloaded;
            }
        }

        return downloaded;
    }
}

