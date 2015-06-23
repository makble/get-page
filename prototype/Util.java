package prototype;

import java.net.URL;


public class Util {
    public static Downloader d = new Downloader();
    public static URLBuilder ub = new URLBuilder();
    public static URL verifyUrl(String url) {
    	return d.verifyUrl(url);
    }

    public static URL NormalizeLink( String link , URL pageUrl, boolean limitHost) {
        return ub.NormalizeLink(link,pageUrl,limitHost);
    }

    public static String DownloadPage(String url) {
        return d.DownloadPage(url);
    }
}
