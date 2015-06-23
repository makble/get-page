package prototype;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cljinterface.DBInterface;

import core.threadpool.CommonRunnable;
import core.threadpool.ThreadPool;

import DomainObject.DomainObject;
import DomainObject.LinksNextPage;


public class RssGrab {
    Reporter r = new Reporter();

    // accepting testing DBInterface
    public void TestDBInterface	( DBInterface i ) {
        r.info("calling DBInterface method: " + i.IsExist("Hello"));
    }

    public void GoWithDomain( DomainObject rs ) {

        ThreadPool mtSave = new ThreadPool(16, "creating 16 thread worker for domain " + rs.domain);
        String domainName = rs.domain;
        DOMBuilder dbuilder = new DOMBuilder();
        InfoExtracter ie = new InfoExtracter();

        String homeUrl = "http://" + domainName;// + "/";
        
        URL homeVerifiedUrl = Util.verifyUrl (homeUrl);        
        String homePage = Util.DownloadPage ( homeUrl );        
        String currentPage = homePage; 
        String lastPage = homeUrl.toString();

        int pageSize ;
        while ( true ) {
            LinksNextPage lnp = ie.AnalyzeIndexPage( dbuilder.GetWalkerByDoc( dbuilder.GetDoc(currentPage )) , rs , homeVerifiedUrl);
            r.info("found " + lnp.links.size() +" after analyzeindexpage: " + homeVerifiedUrl.toString());
            pageSize = lnp.links.size(); // sitepoint need
            
            boolean isAllNew = true;

            for( String link : lnp.links ) {
                
                if (IsPostExistNb(link)) {
                    r.info("post link already grabbed: " + link);
                    isAllNew = false;
                    break;
                } else {
                    Runnable task = createSaveRunnable( link, rs );
                    ((CommonRunnable)task).setLink(link);

                    try {
                        Thread.sleep(1000);
                        r.info("grab a thread to process page:" + link + " ,. under url" + homeVerifiedUrl.toString());                                            
                        mtSave.execute( task );
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if ( !isAllNew ) {
                r.info("更新到此处停止, 碰到了已经抓取的项目" + homeVerifiedUrl.toString());
                break;
                }
            
            r.info("nextPage link from " + lastPage + ":" + lnp.nextPage);

            if ( lnp.nextPage == null ) {
 
                break; // no nextpage break the loop
                
                
            }
            
            currentPage = null;
            if(IsIndexExist(lnp.nextPage)) {
            } else {
                currentPage = Util.DownloadPage ( lnp.nextPage );
                if(currentPage != null ) {

                } else {
                    break;
                }
            }
            
            lastPage = lnp.nextPage; //
            
            homeVerifiedUrl = Util.verifyUrl (lnp.nextPage );
        }
        
        int tryCount = 0;
        while ( !mtSave.isIdleWorkersFull()) {
            tryCount++;
            if ( tryCount > 3 ) {
                r.info("等待未完成的线程超时, 强行结束, 仍然pending的线程数量:" + (16 - mtSave.getIdleWorkersSize()) + " , site is " + domainName );
                mtSave.stopRequestAllWorkers();
                break;
            }
            r.info("IdleWorkers of site:" + domainName + " is not Full, size is " + mtSave.getIdleWorkersSize() + ", waiting for some ms");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } 
        
        r.info("IdleWorkers of site: " + domainName + " should be Full now");
        mtSave.stopRequestIdleWorkers();
        mtSave = null;        
    }

    public boolean IsPostExistNb(String link ) {
        return false;
    }

    private Runnable createSaveRunnable( final String link_ , final DomainObject rs){            
         Runnable aRunnable = new  CommonRunnable (){             
             public void run(){
                 this.link = link_;
                 AnalyzeAndSave(link_, rs);
             }
         };
     
         return aRunnable;
     
     }

    public void AnalyzeAndSave( String url, DomainObject rs ) {
        // do something here
        r.info("do work for a content, url is :" + url);
    }

    public String ReplaceIncreasex(String input, int x) {
        Pattern digitPattern = Pattern.compile("(\\d+)"); // EDIT: Increment each digit.

        Matcher matcher = digitPattern.matcher(input);
        StringBuffer result = new StringBuffer();
        while (matcher.find())
        {
            matcher.appendReplacement(result, String.valueOf(Integer.parseInt(matcher.group(1)) + x));
        }
        matcher.appendTail(result);
        return result.toString();

    }

    public boolean IsIndexExist(String url) {
        return false;
    }
}
