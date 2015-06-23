package test;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.TreeWalker;

import cz.vutbr.web.domassign.StyleMap;

import DomainObject.DomainObject;
import DomainObject.LinksNextPage;

import prototype.*;

public class DraftingTest {
	public static Reporter r = new Reporter();
    public static void main ( String [] args ) {
        compareString("3adafsf", "1sdfsdf");
        System.out.println("Hello, World");
        compareString("1info", "2debug");
        String currentLevel = "4error";

        if ( isNotSuppressed("4error", currentLevel) ) {
            System.out.println("error messsage is printed");
        } else {
            System.out.println("error messsage is not printed");
        }

        if ( isNotSuppressed("1info", currentLevel) ) {
            System.out.println("info messsage is printed");
        } else {
            System.out.println("info messsage is not printed");
        }

        if ( isNotSuppressed("3warning", currentLevel) ) {
            System.out.println("warning message get output");
        } else {
            System.out.println("warning message is suppressed ");
        }
        
        r.info("=======================");
        r.info("testing testEquals");
        testEquals();

        

        r.info("=======================");
        r.info("testing testListCopy");
        testListCopy();

        r.info("=======================");
        r.info("testing testGenerateLink");
        testGenerateLink();


        r.info("=======================");
        r.info("testing testCompare");
        testCompare();
            
        //        r.info("=======================");
        //        r.info("testing testFindTitleLinksAndNext");
        //        testFindTitleLinksAndNext();
        
//        r.info("=======================");
//        r.info("testing testFindFontSize");
//        testFindFontSize();

//        r.info("=======================");
//        r.info("testing testFindSelector");
//        testFindSelector();
        
//        r.info("=======================");
//        r.info("testing testNewRssGrab");
//        testNewRssGrab();

//        r.info("=======================");
//        r.info("testing testNewRssGrabValley");
//        testNewRssGrabValley();

            
//        r.info("=======================");
//        r.info("testing analyzer");
//        testAnalyzer();

//        System.out.println("test suits =============================");
//        testReporter();
//        testDefaultCharset();
//        r.info("=======================");
//        r.info("start of tesing testGetPageInfo");
//        testGetPageInfo();
//
//        r.info("=======================");
//        r.info("testing downloader");
//        testDownloader();
//
//        r.info("=======================");
//        r.info("testing DOM");
//        testDOM();
//        
//        r.info("=======================");
//        r.info("testing ExtractInfo");
//        testExtracter();
                
    }

    /*
    ** what I use this for, to decide report error level
    ** its hard to get the context back when its interrupted
    ** I was interrupted by Emacs when doing this.
     */
    public static void compareString(String a , String b) {
        if (a.compareTo(b) > 0) {
            System.out.println("a bigger than b");
        } else {
            System.out.println("a less than b");
        }
    }

    public static boolean isNotSuppressed(String level , String currentLevel) {
        if (level.compareTo(currentLevel) >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void testReporter() {
        Reporter r = new Reporter();
        r.Error("error no " + 3);
        r.Debug("when I want to see some values in runtime");
        r.reportLevel = "1info";
        r.Error("error has level higher than warning, it printed");
        r.Debug("debug is lower level, is suppressed");
        
        r.info("start testing aliasing of reporter method");
        r.e("error message can be printed with simply e");
        r.error("think about it , don'w worry to the wrong method, because every method you can think of just works, this is error");
        r.err("or err");
        r.Error("of course Error should work too");

        r.debug("debug it");
        r.d("just d it");

        r.warn("warn it");
        r.warning("warning it");

        r.info("info it");
    }
    
    public static void testDefaultCharset() {
        r.info(Charset.defaultCharset().toString());
        r.info("default file encoding : " + System.getProperty("file.encoding"));
        // r.info("irs encoding: " + InputStreamReader.getEncoding().toString());
    }


    public static String yahoo = "http://yahoo.com/";
    public static String so =  "http://stackoverflow.com/questions/924208/how-to-convert-nanoseconds-to-seconds-using-the-timeunit-enum";
    public static String soSelector = "h1 a.question-hyperlink {}";
    public static String blog = "http://zhangley.com/knProxy/index.php?url=yE1i1tyZyB1O1I1yye1lyb1EyXye0iyV1yyUyR1E1G1KyL1pyZ0f0Xyx1m1B1s1sya";


    public static void testDownloader() {
        Downloader d = new Downloader();
        r.info(d.DownloadPage(so));
    }

    public static void testDOM() {
        URLBuilder ub = new URLBuilder();
        Downloader d = new Downloader();
        String content = d.DownloadPage(so);
        DOMBuilder dbuilder = new DOMBuilder();
        TreeWalker w = dbuilder.GetWalkerByDoc( dbuilder.GetDoc(content));
        Element current;
        while ((current = (Element) w.nextNode()) != null) {
            // print all eles's tag name
            //r.info(current.getTagName());

            // only print all link href
            if( current.getTagName().toLowerCase().equals("a") && current.hasAttribute("href")) {
                r.info(current.getAttribute("href") +
                       " Normalized: " + ub.NormalizeLink(current.getAttribute("href"), d.verifyUrl(so), false));
            }
        }

        URL url = d.verifyUrl(so);
        r.info(url.getProtocol());
    }


    public static void getList(String selector, String url) {
        InfoExtracter ie = new InfoExtracter();
        Downloader d = new Downloader();
        String pageContent = d.DownloadPage(url);
        DOMBuilder dbuilder = new DOMBuilder();
        TreeWalker w = dbuilder.GetWalkerByDoc( dbuilder.GetDoc(pageContent ));
        ie.BasicQueryFindLink(selector, w, url);
        
    }

    public static void testAnalyzer() {
        URLBuilder ub = new URLBuilder();
        Downloader d = new Downloader();
        String url = blog;
        String content = d.DownloadPage(url);
        DOMBuilder dbuilder = new DOMBuilder();
        TreeWalker w = dbuilder.GetWalkerByDoc( dbuilder.GetDoc(content));

        DomainObject dr = new DomainObject();
        InfoExtracter ie = new InfoExtracter();

        dr.domain = "makble.com";
        dr.linkSelector = "h2.pagetitle a {}";
        dr.nextPageSelector = ".navigation-bott div a {}";

        LinksNextPage lnp = ie.AnalyzeIndexPage(w, dr, d.verifyUrl(url));
        for( String link :  lnp.links ) {
            r.info("link found: " + link);
        }
        r.info("next Page is : " + lnp.nextPage);

    }

    public static void testNewRssGrab() {
        RssGrab rg = new RssGrab();
        DomainObject dr = new DomainObject();
        dr.domain = "zhangley.com/knProxy/index.php?url=yE1i1tyZyB1O1I1yye1lyb1EyXye0iyV1yyUyR1E1G1KyL1pyZ0f0Xyx1m1B1s1sya";
        dr.linkSelector = "h2.pagetitle a {}";
        dr.nextPageSelector = ".navigation-bott div.leftnav a {}";
        rg.GoWithDomain(dr);
    }

    // getTextContent is nil?
    public static void testNewRssGrabValley() {
        RssGrab rg = new RssGrab();
        DomainObject dr = new DomainObject();
        dr.domain = "hackingthevalley.com";
        dr.linkSelector = "h1.entry-title a {}";
        dr.nextPageSelector = "a.older-posts {}";
        rg.GoWithDomain(dr);
    }

    public static void testFindSelector() {
        URLBuilder ub = new URLBuilder();
        Downloader d = new Downloader();
        String content = d.DownloadPage(so);
        DOMBuilder dbuilder = new DOMBuilder();
        TreeWalker w = dbuilder.GetWalkerByDoc( dbuilder.GetDoc(content) );
        Element current;
        HeuristicAnalyzer ha = new HeuristicAnalyzer();
        while ((current = (Element) w.nextNode()) != null) {
            if( current.getTagName().toLowerCase().equals("a") && current.hasAttribute("href")) {
                r.info(current.getAttribute("href") +
                       " Normalized: " + ub.NormalizeLink(current.getAttribute("href"), d.verifyUrl(so), false));
                r.info("selector for the element " + ha.findSelector(current));
            }
        }
    }

    public static void testFindFontSize() {
        URLBuilder ub = new URLBuilder();
        Downloader d = new Downloader();
        String fontSizeTestUrl = "http://127.0.0.116/test-fontsize.html";
        String content = d.DownloadPage(fontSizeTestUrl);
        DOMBuilder dbuilder = new DOMBuilder();
        Document doc = dbuilder.GetDoc(content);
        TreeWalker w = dbuilder.GetWalkerByDoc( doc );
        InfoExtracter ie = new InfoExtracter();


        StyleMap decl = ie.getStyleMap(fontSizeTestUrl, doc);
        HeuristicAnalyzer ha = new HeuristicAnalyzer();

        Element current;

        r.info("2.5em extract " + ha.extractNumber("2.5em"));
        r.info("2.5em extract " + ha.extractNumberEM("2.5em"));
        while ((current = (Element) w.nextNode()) != null) {
            r.info("font size of tag:  " + current.getTagName() + " font size is : "  + ha.findFontSize(current, decl));
        }
    }

    public static void testFindTitleLinksAndNext() {
        HeuristicAnalyzer ha = new HeuristicAnalyzer();
        String url = "http://rrees.me/"; //"http://127.0.0.116/test-fontsize.html";//"http://makble.com/";
        List ret = ha.findPostLinkAndNext(url);

        if( ret.size() > 0)
            r.info("title link selector: " + ret.get(0));
        if( ret.size() > 1)
            r.info("next selector: " + ret.get(1));
        else {
        	r.warn("not found any selector from : " + url);
        }
    }

    private static int i = 0;
    private static String GenerateLink (String url , String text) {
        i += 1;
        return url + " - " + text + " - " + i;
    }

    public static void testGenerateLink() {
        r.info(GenerateLink("http://xx.com/", "hello"));
        r.info(GenerateLink("http://xx.com/", "world"));
        r.info(GenerateLink("http://xx.com/", "haha"));
    }

    public static void testCompare() {
        // AtomicReference, 任意类型, 怎么比较两个list
        List<Integer> l1 = new ArrayList<Integer>();
        List<Integer> l2 = new ArrayList<Integer>();
        l1.add(1);
        l1.add(2);
        l2.add(1);
        l2.add(2);
        if(l1.equals(l2)) {
            r.info("l1 equals l2");
        } else {
            r.info("l1 not equals l2");
        }
        
    }
    
    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }
    
    public static void testAtomicReference(){
    	final AtomicReference<List<Integer>> atomicList = new AtomicReference<List<Integer>>();
    	List<Integer> init = new ArrayList<Integer>();
    	init.add(1);
    	atomicList.set(init);
    	
    	Thread t = new Thread () {
    		public void run() {
    			for(;;){
    				
    				// lets add an element to atomic list
    				List<Integer> old_value = atomicList.get();
    				old_value.add(safeLongToInt(Thread.currentThread().getId()));
    				List<Integer> new_value = old_value;
    				atomicList.compareAndSet(old_value, new_value);
    			}
    		}
    	};
    	t.start();
    	try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    
    
    
    public static void testListCopy (){
    	List<String> l1 = new ArrayList<String>();
    	l1.add("hello world");
    	l1.add("big world");
    	
    	List<String> l2 = new ArrayList<String>(l1);
    	l2.add("l2 string");
    	
    	r.info("l1 size:" + l1.size());
    	r.info("l2 size:" + l2.size());
    	
    	l1.add("l1 added");
    	r.info("l1 size:" + l1.size());
    	r.info("l2 size:" + l2.size());
    	
    }
    
    public static void testEquals(){
    	Integer ref1 = new Integer(1);
    	Integer ref2 = new Integer(1);
    	if (ref1 == ref2) {
    	    System.out.println ("ref1 == ref2");
    	} else {
    		System.out.println ("ref1 and ref2 are not same reference");
    	}

    	Integer ref3 = ref1;
    	
    	if (ref1 == ref3) {
    	    System.out.println ("ref1 == ref3");
    	}
    	
    	List<Integer> l1 = new ArrayList<Integer>();
    	l1.add(1);

    	List<Integer> l2 = new ArrayList<Integer>();
    	l2.add(1);

    	if( l1 == l2) {
    	    System.out.println ("l1 == l2");
    	} else {
    	    System.out.println ("l1 != l2");
    	}
    	
    	List<Integer> l3 = l1;
    	l3.add(2);
    	if( l1 == l3) {
    	    System.out.println ("l1 == l3");
    	} else {
    	    System.out.println ("l1 != l3");
    	}
    }
}

