package prototype;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.traversal.TreeWalker;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.domassign.Analyzer;


public class GetLinkCommonImpl implements GetLinkInterface {
	Reporter r = new Reporter();
    public Boolean ShouldIHandleThis(URL url) {

        return true;
    }
    

    public List<String> GetLink ( CombinedSelector selector, Element current, TreeWalker w , URL url) {
        List<String> ret = new ArrayList<String>();
        if( !current.getTagName().toLowerCase().equals("a")) {
            // null means null, we need next token
            return null;
        }

        if ( Analyzer.matchSelector(selector, current, w)) {
            r.info("link selector matched an a tag: Url: " + current.getAttribute("href") + ", text is " + current.getTextContent());
            URL linkUrl = Util.NormalizeLink( current.getAttribute("href"), url, true );

            if ( linkUrl != null ) {
                ret.add(linkUrl.toString());
                ret.add(current.getTextContent());
                return ret;
            }
            return null;
        }
        return null;
    }

}
