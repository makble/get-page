package prototype;

import java.net.URL;


public class URLBuilder {
    Reporter r = new Reporter();

    private void outln ( String str ) {
        r.info(str);
    }

    public URL NormalizeLink( String link , URL pageUrl, boolean limitHost) {

        String protocol = pageUrl.getProtocol();

        if ( link.length() < 1 ) {
            outln(" :null ,discarded");
            return null;
        }
       
        if ( link.charAt(0) == '#' ) {
            outln(" :#start ,discarded");
            return null;
        }
        
        
        if ( link.indexOf("mailto:") != -1 ) {
            outln(" :mailto ,discarded");
            return null;
        }
        
        if ( link.toLowerCase().indexOf("javascript:") != -1 ) {
            outln(" :jscode ,discarded");
            return null;
        }
        
        String noPara = link.substring(0,(link.indexOf('?') != -1 ? link.indexOf('?')  : link.length()));
        if ( noPara.indexOf("://") == -1 ) {

            if ( link.charAt(0) == '/') {

                if( link.length() > 1 && link.charAt(1) == '/') {
                    link = link.replaceFirst("^//", pageUrl.getProtocol()+"://");
                } else {
                    
                    link = protocol + "://" + pageUrl.getHost() + link;
                }
            }
            else {

                String file  = pageUrl.getFile();
                
                if ( file.indexOf('/') == -1 ) {

                    link = protocol + "://" + pageUrl.getHost() + "/"  + link;
                } else {
                    
                    String path = file.substring(0,file.lastIndexOf('/') + 1);
                    String lastfile = file.substring(file.lastIndexOf('/') + 1 , file.length());
                    if ( lastfile.length() != 0 && lastfile.indexOf('.') == -1 && lastfile.charAt(0) != '?')
                    {

                        path += lastfile + "/";
                    } 
                    r.debug("相对路径处理之前" + protocol + "://" + pageUrl.getHost() + path  + link);
                    path = path.replaceAll("(\\./)*", "");
                    link = protocol + "://" + pageUrl.getHost() + path  + link;
                    r.debug("相对路径处理之后" + link);
                    
                    String arr[] = link.split("/");

                    boolean firstmet = false;
                    int firstlocation = 0;
                    int length = 0;
                    int i = 0;
                    for(String part : arr) {
                        if(part.equals("..")) {
                            if(firstmet == false){
                                firstmet = true;
                                firstlocation = i; 
                            }
                            length++;
                            
                        }
                        i++;
                    }
                    int start = 0;
                    int end = arr.length;
                    
                    if(firstmet == true) {
                        start = firstlocation - length;
                        end = firstlocation + length - 1;
                        
                        String newlink = "";
                        for(int j = 0 ; j < arr.length ; j++ ) {
                            if(j < start || j > end)
                                newlink += arr[j]+"/"; 
                        }
                        link = newlink.substring(0,newlink.length()-1);
                    }
                    
                }
            }
        }
        
        int index = link.indexOf('#');            
        if ( index != -1 ) {
            link = link.substring(0, index);
        }
        
        
        URL verifiedLink  = Util.verifyUrl(link);
        if ( verifiedLink == null ) {

            r.warn(" :illegal url ,discarded");
            return null;
        }

        return verifiedLink;
        
    }
}
