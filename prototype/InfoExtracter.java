package prototype;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.traversal.TreeWalker;

import DomainObject.DomainObject;
import DomainObject.LinksNextPage;

import com.sun.corba.se.impl.orbutil.graph.Node;

import cz.vutbr.web.css.CSSException;
import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.css.RuleSet;
import cz.vutbr.web.css.StyleSheet;
import cz.vutbr.web.domassign.Analyzer;
import cz.vutbr.web.domassign.StyleMap;


public class InfoExtracter {
    Reporter r = new Reporter();

    public StyleMap getStyleMap( String url, Document doc ) {
        try {
            URL urlBase = new URL(url);
            StyleMap decl = CSSFactory.assignDOM(doc, urlBase, "screen", true);
            return decl;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public CombinedSelector CreateSelector ( String selector ) {
        StyleSheet ss;
        RuleSet rs;
        CombinedSelector cs = null;
        CombinedSelector endcs = null;
        
        try {
            ss = CSSFactory.parse(selector);
            rs = (RuleSet)ss.get(0); // 从parse到返回一个RuleSet实例的整个过程.
            cs = (CombinedSelector) rs.getSelectors().get(0);
            return cs;
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CSSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cs;
    }

    public ArrayList<Element> BasicQueryFindLink(String selector, TreeWalker w, String url) {
        Element current;
        CombinedSelector linkSelector = CreateSelector( selector );
        ArrayList<Element> ret = new ArrayList<Element>();

        while ((current = (Element) w.nextNode()) != null ){
            if( !current.getTagName().toLowerCase().equals("a")) continue;
            if ( Analyzer.matchSelector(linkSelector, current, w)) {
                if( current.hasAttribute("href")) {
                    r.info("found a link match " + selector + ", the text is " + current.getTextContent() + ", the href is " + current.getAttribute("href") + " in url: " + url);
                    ret.add(current);
                }
            }
        }

        return ret;
    }

    public ArrayList<Element> FindElements(String selector, TreeWalker w, String url) {
        Element current;
        CombinedSelector linkSelector = CreateSelector( selector );
        ArrayList<Element> ret = new ArrayList<Element>();

        while ((current = (Element) w.nextNode()) != null ){
            if ( Analyzer.matchSelector(linkSelector, current, w)) {
                ret.add(current);
            }
        }

        return ret;
    }
    
    public Element FindOneLink(String selector, TreeWalker w, String url) {
        ArrayList<Element> list = BasicQueryFindLink(selector, w, url);
        if( list.size() >= 1) return list.get(0);
        else return null;
    }

    public Element FindElement (String selector , TreeWalker w) {
        Element current;
        CombinedSelector linkSelector = CreateSelector( selector );
        ArrayList<Element> ret = new ArrayList<Element>();

        while ((current = (Element) w.nextNode()) != null ){
            if ( Analyzer.matchSelector(linkSelector, current, w)) {
                return current;
            }
        }
        return null;
    }

    public List<GetLinkInterface> ConstructLinkHandlers() {
        List<GetLinkInterface> ret = new ArrayList<GetLinkInterface> ();
        ret.add(new GetLinkPragMagImpl());        
        ret.add(new GetLinkObjectPartnerImpl());
        ret.add(new GetLinkCommonImpl());
        return ret;
    }

    public LinksNextPage  AnalyzeIndexPage( TreeWalker w , DomainObject domainRecord, URL url) {
        // traverse the dom tree
        r.info("分析indexpage , link selector: " + domainRecord.linkSelector + ", url is : " + url.toString());
        r.info("分析indexpage , nextpage selector: " + domainRecord.nextPageSelector + ", url is : " +  url.toString());
        CombinedSelector linkSelector = CreateSelector ( domainRecord.linkSelector );
        CombinedSelector nextPageSelector = CreateSelector ( domainRecord.nextPageSelector  );
                
        // now traverse the DOM TREE and locate what we want
        LinksNextPage lnp = new LinksNextPage ();
        List<String> nextPages = new ArrayList<String>();
        List<Element> nextElements = new ArrayList<Element>();
        List<GetLinkInterface> linkHandlers = ConstructLinkHandlers();
        
        Element current;
        while ((current = (Element) w.nextNode()) != null) {
            for( GetLinkInterface handler : linkHandlers) {
                if (handler.ShouldIHandleThis(url)) {
                    // do it and break
                    List<String> ret = handler.GetLink(linkSelector, current, w, url);
                    if ( ret != null) {
                        lnp.links.add(ret.get(0));
                        lnp.texts.add(ret.get(1));
                    }
                    break;
                }
            }
            
            // now for next elemnt, we don't wnat to change it yet
            if( !current.getTagName().toLowerCase().equals("a")) continue;

            // next page 应该只有唯一的一个, 但是一些网站根本无法区分两个.
            if ( Analyzer.matchSelector(nextPageSelector, current, w)) {
                r.info("find a nextpage link " + current.getAttribute("href") + " in index page:" + url + ", text is " + current.getTextContent());
                URL nextPageUrl = Util.NormalizeLink( current.getAttribute("href"), url, true );
                if ( nextPageUrl != null ) {
                    r.info("after normalize, next page link , before replace white space: " + nextPageUrl.toString());
                    nextPages.add(nextPageUrl.toString().replace(" ", "%20"));
                    nextElements.add(current);
                }
            }            
        }
        
        r.info( "in index page, doamin is " + domainRecord.domain + " url is " + url  +  " number of nextpage link is " + nextElements.size());

        
        if(nextPages.size() == 1)
            lnp.nextPage = nextPages.get(0);
        else if (nextPages.size() > 1) {

            r.info("multiple next link int url :" + url.toString());
            for ( int i = 0 ; i < nextPages.size(); i++ ) {
                String tc = nextElements.get(i).getTextContent();
                if(
                        nextElements.get(i).getAttribute("title").toLowerCase().indexOf("next") != -1 || 
                        nextElements.get(i).getTextContent().toLowerCase().indexOf("old") != -1 ||
                        nextElements.get(i).getTextContent().toLowerCase().indexOf("→") != -1 ||
                        nextElements.get(i).getTextContent().toLowerCase().indexOf("next") != -1 ||
                        nextElements.get(i).getTextContent().toLowerCase().indexOf("»") != -1 || // 
                        nextElements.get(i).getTextContent().toLowerCase().indexOf("previous posts") != -1 ||
                        nextElements.get(i).getTextContent().toLowerCase().indexOf(">>") != -1 ||
                        nextElements.get(i).getTextContent().toLowerCase().indexOf("下一页") != -1) {
                    r.info(i + "th used: " + nextPages.get(i));
                    lnp.nextPage = nextPages.get(i);
                    break;
                }
                    
            }
        }
        
        
        return lnp;
    }
}
