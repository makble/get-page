package DomainObject;

import java.util.LinkedList;
import java.util.List;

public 	class LinksNextPage {
    public List<String> texts;
	public List<String> links;
	public String nextPage;		
	
	public LinksNextPage () {
		links = new LinkedList<String>();
		texts = new LinkedList<String>();
		nextPage = null;
	}
}