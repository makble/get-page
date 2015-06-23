package prototype;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.traversal.TreeWalker;

import cz.vutbr.web.css.CombinedSelector;
import cz.vutbr.web.domassign.Analyzer;
/**
 ** h3 onclick property and a regula expresion
 */
public class GetLinkObjectPartnerImpl implements GetLinkInterface {
	Reporter r = new Reporter();
    public Boolean ShouldIHandleThis(URL url) {
        if (url.toString().indexOf("objectpartners.com") != -1 ) 
            return true;
        return false;
    }

    private String ExtractLink (String onclick) {
        // (.replaceFirst (.substring s 15) ".$" "")
        return onclick.substring(15).replaceFirst (".$", "");
    }
    
    public List<String> GetLink ( CombinedSelector selector, Element current, TreeWalker w , URL url) {
        List<String> ret = new ArrayList<String>();
        if( !current.getTagName().toLowerCase().equals("h3")) {
            // null means null, we need next token
            return null;
        }

        if ( Analyzer.matchSelector(selector, current, w)) {
            r.info("link selector matched an h3 tag: Url: " + ExtractLink(current.getAttribute("onclick")) + ", text is " + current.getTextContent());
            ret.add(ExtractLink(current.getAttribute("onclick")));
            ret.add(current.getTextContent());
            return ret;

        }
        return null;
    }

}
