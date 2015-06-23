package test;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestDelete {


    public static void main (String [] args ) {
        System.out.println (listRecursively("c:\\tmp\\testdelete\\aaa\\abc.gif"));
    }
    


    public static Boolean hasExtension(String name, String extension) {
        String ext = getExtension(name);
        if (ext == null) {
            return false;
        }

        if( extension.toLowerCase().equals(ext) ) {
            return true;
        } else {
            return false;
        }
    }

    public static String getExtension(String name) {
        Pattern p = Pattern.compile("\\.[^.\\\\]*$"); 
        Matcher m = p.matcher(name);
        if ( m.find()) {
            System.out.println(m.group(0));
            return m.group(0).toLowerCase();
        }
        return null;
    }

    public static void deleteRecursively(String dir) {
        File f = new File(dir);
        if ( ! f.isDirectory()) {
            System.out.println ("Its not a directory: " +  dir);
            return;
        }

        File[] fileList = f.listFiles();
        for ( File file :  fileList) {
            if( file.isDirectory()) {
                deleteRecursively(dir + "\\" + file.getName());
                continue;
            }
            
            if(file.delete()) {
                System.out.println ("Deleting " + dir + "\\" + file.getName());
            }
        }        
    }

    public static List<String> listRecursively(String dir) {
        File f = new File(dir);
        List<String> ret = new ArrayList<String>();
        
        if( ! f.isDirectory()) {
            return ret; // return empty list
        }

        File [] fileList = f.listFiles();
        for( File file : fileList) {
            if( file.isDirectory()) {
                ret.addAll (listRecursively(dir + "\\" + file.getName()));
            } else {
                ret.add(dir + "\\" + file.getName());
            }
        }
        return ret;
    }
}

