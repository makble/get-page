package prototype;

import java.io.IOException;
import java.io.StringReader;

import org.cyberneko.html.HTMLConfiguration;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.TreeWalker;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

public class DOMBuilder {
    Reporter r = new Reporter();

    public TreeWalker GetWalkerByDoc ( Document document ) {
        DocumentTraversal traversal = (DocumentTraversal) document;
        TreeWalker w = traversal.createTreeWalker(document, NodeFilter.SHOW_ELEMENT, null, false);
        
        return w;
    }

    public Document GetDoc ( String content ) {
        Document docLocal;
        DOMParser parser = new DOMParser(new HTMLConfiguration());
        try {
            parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
        } catch (SAXNotRecognizedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXNotSupportedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            parser.parse(new org.xml.sax.InputSource(new StringReader(content.replace("</div></div>", "</div>\r\n</div>"))));
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        docLocal = parser.getDocument();
        
        return docLocal;
    }
}
