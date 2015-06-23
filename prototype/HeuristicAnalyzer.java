package prototype;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.TreeWalker;

import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.domassign.StyleMap;


public class HeuristicAnalyzer {
    
    Reporter r = new Reporter();


    public List findPostLinkAndNext(String url) {
        List ret = new ArrayList<String>();

        URL urlPost = Util.verifyUrl(url);        
        String postContent = "";
        
        try {
            postContent = Util.DownloadPage(url);
        } catch (RuntimeException e1) {
            e1.printStackTrace();
        }
        
        long startTime = System.nanoTime();
        DOMBuilder dbuilder = new DOMBuilder();
        InfoExtracter ie = new InfoExtracter();
        Document doc = dbuilder.GetDoc(postContent);        
        TreeWalker w = dbuilder.GetWalkerByDoc(doc);
        
        StyleMap decl = ie.getStyleMap(urlPost.toString(), doc);
        long elapsedTimeAftergetStyleMap = System.nanoTime() - startTime;
        r.info("time elapsed after getStyleMap : " + ((double)elapsedTimeAftergetStyleMap / 1000000000.0) + ", url is " + url.toString());
        
        Element current = null;
        
        Map<String, List<Element>> linkCategories = new TreeMap<String, List<Element>>();
        List<Element> h1a = new ArrayList<Element>();
        List<Element> h2a = new ArrayList<Element>();
        List<Element> h3a = new ArrayList<Element>();
        List<Element> nextLinks = new ArrayList<Element>();
        Map<String,List<Element>> linkByFontSize = new TreeMap<String, List<Element>>();
        
        Map<String, List<Element>> nextLinksCategories = new TreeMap<String, List<Element>>();
        int postsPerPage = 0;

        while( (current = (Element)w.nextNode()) != null) {
            NodeData n = decl.get(current);

            String fontSize = findFontSize(current,decl);
            
            if( current.getTagName().toLowerCase().equals("a")) {
                                 
                // fontSize should not be null
                if( fontSize == null ) {

                } else {
                    //r.info("添加一个链接 按字体, font size is " + fontSize + " selector is " + findSelector(current)  + " link text is " + current.getTextContent());
                    createOrAdd ( linkByFontSize, fontSize, current );
                }
                
                if( ((Element)current.getParentNode()).getTagName().toLowerCase().equals("h1") ) {
                    h1a.add(current);
                    continue;
                }
                
                if( ((Element)current.getParentNode()).getTagName().toLowerCase().equals("h2") ) {
                    h2a.add(current);
                    continue;
                }
                
                if( ((Element)current.getParentNode()).getTagName().toLowerCase().equals("h3") ) {
                    h3a.add(current);
                    continue;
                }
                
               
                String nextLinkText = current.getTextContent().toLowerCase();
                if(nextLinkText.contains("older") || nextLinkText.contains("next") ||
                   nextLinkText.contains("→") || nextLinkText.contains("»") ||
                   nextLinkText.contains("previous posts") || nextLinkText.contains("下一页") ||
                   nextLinkText.contains("more posts") || nextLinkText.contains("previous entries")
                ) {
                    r.info("possible next link: " + nextLinkText + findSelector(current));
                    nextLinks.add(current);
                    if ( nextLinksCategories.get(nextLinkText) == null ) {
                        List<Element> cat = new ArrayList<Element>();
                        cat.add(current);
                        nextLinksCategories.put(nextLinkText, cat);
                    } else {
                        List<Element> cat = nextLinksCategories.get(nextLinkText);
                        cat.add(current);
                    }
                    continue;
                }
            }
                      
        }

        long elapsedTimeAfterWhile = System.nanoTime() - startTime;
        r.info("time elapsed when find selector after while loop: " + ((double)elapsedTimeAfterWhile / 1000000000.0) + ", url is " + url.toString());

        // 这个明显应该放到while之外. 
        linkCategories.put("h1a", h1a);
        linkCategories.put("h2a", h2a);
        linkCategories.put("h3a", h3a);
            
        int count = 0;
        String candidate = null;

        candidate = findMaxList(linkCategories);
        if (candidate != null ) {
        	count = linkCategories.get(candidate).size();
        } else {
        	// let count = 0
        }
        
        Map<String, List<Element>> candidateMap = linkCategories;
        
        int leastLinksPerPage = 3;
        if ( count < leastLinksPerPage ) {
            candidateMap = linkByFontSize;
            r.info("正文链接数量应该大于一个最小值, candidate数量过少, 可能错了, 试图选择字号最大的那一组");
            
            count = 0; // not count, just a number, list size or font size
            for ( Map.Entry<String, List<Element>> entry : linkByFontSize.entrySet()) {
                if( entry.getValue().size() < leastLinksPerPage ) continue;
                int newcount = extractNumber(entry.getKey());
                if( newcount > count ) {
                    count = newcount;
                    candidate = entry.getKey();
                }
            }

            r.info( "candidate in font size links is " + candidate);
        }

        if ( candidate != null ) {
            Map<String, List<Element>> linkCategoriesBySelector = categorizeBySelector(candidateMap.get(candidate));
            
            Element titleLink ;
            if( linkCategoriesBySelector.size() > 1 ) {
                titleLink = linkCategoriesBySelector.get(findMaxList(linkCategoriesBySelector)).get(0);
            } else {
                titleLink = candidateMap.get(candidate).get(0);
            }
                
            postsPerPage = candidateMap.get(candidate).size();
            
            String selector = findSelector( titleLink );
            
            r.info(" suggest post selector : " + selector );
            ret.add(selector + " {}");
        }

        long elapsedTimeAfterPostLink = System.nanoTime() - startTime;
        r.info("time elapsed when find selector after found post selector: " + ((double)elapsedTimeAfterPostLink / 1000000000.0) + ", url is " + url.toString());
        
        
        for ( Map.Entry<String, List<Element>> entry : nextLinksCategories.entrySet()) {
            
            if ( entry.getValue().size() >= postsPerPage ) {
                for( Element e : entry.getValue()) {
                    nextLinks.remove(e);
                }
            }
        }
        
        r.info( "after filter , size of nextLinks is "  + nextLinks.size());
        
        String nextlink = "";
        if( nextLinks.size() > 0 ) {
            r.info("suggest next link: " + findSelector(nextLinks.get(nextLinks.size() - 1)));
            nextlink = findSelector(nextLinks.get(nextLinks.size() - 1));
            ret.add(nextlink + "  {}");
        } else {
            r.info("next links has no items");
        }
        
        long elapsedTime = System.nanoTime() - startTime;
        r.info("time elapsed when find selector after downloading: " + ((double)elapsedTime / 1000000000.0) + ", url is " + url.toString());

        return ret;
    }

    public Map<String, List<Element>> categorizeBySelector (List<Element> list) {
        Map<String, List<Element>> linkCategoriesBySelector = new TreeMap<String, List<Element>>();
        for( Element e : list) {
            String sel  = findSelector(e);
            if(linkCategoriesBySelector.get(sel) == null ) {
                List<Element> newSelector = new ArrayList<Element>();
                newSelector.add(e);
                linkCategoriesBySelector.put(sel, newSelector);
            } else {
                linkCategoriesBySelector.get(sel).add(e);
            }
        }
        return linkCategoriesBySelector;
    }

    public  <E> void createOrAdd(Map<String, List<E>> m, String key, E e ) {
        if ( m.get(key) == null ) {
            List<E> cat = new ArrayList<E>();
            cat.add(e);
            m.put(key, cat);
        } else {
            List<E> cat = m.get(key);
            cat.add(e);
        }
    }
    
    public  <E> String findMaxList( Map<String, List<E>>  m) {
        int count = 0 ;
        String candidate = null;
        
        for ( Map.Entry<String, List<E>> entry : m.entrySet()) {
            if ( entry.getValue().size() > count ) {
                count = entry.getValue().size();
                // update candidate
                candidate = entry.getKey();
            }
        }
        return candidate;
    }
    
    public boolean TagMatch( String title, Element content ) {
        String contentClass = content.getAttribute("class");
        if( title.indexOf("post-title") != -1 && contentClass.indexOf("post-content") != -1) return true;
        if( title.indexOf("post_title") != -1 && contentClass.indexOf("post_content") != -1) return true;
        if( title.indexOf("entry-title") != -1 && contentClass.indexOf("entry-content") != -1) return true;
        if( title.indexOf("entry_title") != -1 && contentClass.indexOf("entry_content") != -1) return true;
        
        return false;
    }

    public int extractNumber( String input) {
        // System.out.println("input is " + input );
        int n = 0;
        Pattern digitPattern = Pattern.compile("(\\d+)"); // EDIT: Increment each digit.

        Matcher matcher = digitPattern.matcher(input);
        StringBuffer result = new StringBuffer();
        if (matcher.find())
        {
            n = Integer.parseInt(matcher.group(1));
        }
        matcher.appendTail(result);
        
        return n;
        
    }

    public int extractNumberEM( String input) {
        Double d = (Double.parseDouble(input.replace("em","")) * 100);
        return d.intValue();
    }
    
    public String findFontSize( Element current, StyleMap  decl ) {
        Element titleLink = current;        
        Element parent = titleLink;        
        List<String> fontSizeStack = new ArrayList<String>();
        String baseSize = "";
        int currentSize;
        
        while( true ) {
            NodeData n = decl.get(parent);
            if (n == null ) {
                r.warn("NodeData is null");
            }

            if(n != null &&  n.getValue("font-size", true) != null ) {
                
                String literalSize =  (String)n.getValue("font-size", true).toString();
                // System.out.println("literSize for each parent " + literalSize);
                if( ! ( literalSize.toLowerCase().endsWith("px") || literalSize.toLowerCase().endsWith("pt") ) ) {
                    fontSizeStack.add(literalSize);
                }
                else {
                    baseSize = literalSize;
                    break;
                }
            }

            if( !parent.getParentNode().getNodeName().equals("#document") ) {
                parent = (Element)parent.getParentNode();
            } else {
                break;
            }
            
        }
        
        // System.out.println("baseSize is " + baseSize);
        if ( baseSize.equals("")) baseSize = "16px";
        
        if( fontSizeStack.size() == 0 ) return Integer.toString((extractNumber(baseSize)) ); // 等价于第一版的实现
        else {
            currentSize = extractNumber(baseSize);
            int multiply = 0;
            for( int i = fontSizeStack.size() - 1 ; i >= 0 ; i--) {
                String literalSize = fontSizeStack.get(i);
                int scale   = 1;
                if ( literalSize.toLowerCase().endsWith("em")) {
                    // 可能需要额外的处理, 例如em 是 2.5 3.4之类的, scale目前不是精确值
                    scale = extractNumberEM(literalSize);
                    multiply++;
                } else if ( literalSize.toLowerCase().endsWith("%")) {
                    scale = extractNumber(literalSize);
                    multiply++;
                }
                currentSize = scale * currentSize;
            }
            
            currentSize =  (int)  ( ( ( double ) currentSize)  / ( Math.pow(100, multiply) ) ); // 应该是multiply 次方.   
            
        }
        return Integer.toString(currentSize);
        
    }

    /*
    ** get selector of an element
     */
    public String findSelector(Element current ) {
        Element titleLink = current;
        Element parent = titleLink;
        String selector = "";
        
        if( parent.getTagName().toLowerCase().equals("html")) {
            return "HTML not accept";
        }

        while( !parent.getTagName().toLowerCase().equals("body")) {
            String thisSelector = parent.getTagName();
            
            // class 优先
            if( !parent.getAttribute("class").equals("" )  && !thisSelector.toLowerCase().equals("article")) {
                String classAttri = parent.getAttribute("class");
                // class split with space]
                String justClass = "";
                String [] classes = classAttri.split("\\s+");

                for( int i = 0 ; i < classes.length ; i++ ) {
                    if( classes[i].matches(".*\\d.*")) {
                        continue;
                    } else {
                        justClass = classes[i];
                        break;
                    }
                }
                
                if( !justClass.equals("")) {
                    thisSelector += "." + justClass;
                }
                
            } else if( !parent.getAttribute("id").equals("") && !thisSelector.toLowerCase().equals("article")) {

                thisSelector += "#" + parent.getAttribute("id");
            }
            
            selector = thisSelector + " > " + selector;
            if( parent.getTagName().toLowerCase().equals("html")) {
                return "HTML not accept";
            }
            parent = (Element)parent.getParentNode();
        }
        // remove end "> "
        return selector.substring(0,selector.length() - 2);
    }
}
