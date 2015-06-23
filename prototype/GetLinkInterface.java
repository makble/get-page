package prototype;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.traversal.TreeWalker;

import cz.vutbr.web.css.CombinedSelector;

interface GetLinkInterface {
    public Boolean ShouldIHandleThis(URL url) ;
    public List<String> GetLink ( CombinedSelector selector, Element current, TreeWalker w , URL url);
}
