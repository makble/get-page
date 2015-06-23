package prototype;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.traversal.TreeWalker;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.domassign.Analyzer;

public class GetLinkPragMagImpl implements GetLinkInterface {
    Reporter r = new Reporter();
    public Boolean ShouldIHandleThis(URL url) {
        if (url.toString().indexOf("pragprog.com") != -1 ) 
            return true;
        return false;
    }

    private static int i = 0;
    private String GenerateLink (String url , String text) {
        i += 1;
        return url + " - " + text + " - ";
    }
    
    public List<String> GetLink ( CombinedSelector selector, Element current, TreeWalker w , URL url) {
        List<String> ret = new ArrayList<String>();
        if( !current.getTagName().toLowerCase().equals("li")) {
            // null means null, we need next token
            return null;
        }

        if ( Analyzer.matchSelector(selector, current, w)) {
            r.info("title selector matched an li tag: Url: " + "no url" + ", text is " + current.getTextContent());
            ret.add(GenerateLink(url.toString(), current.getTextContent()));
            ret.add(current.getTextContent());
            return ret;
        }
        return null;
    }

}
