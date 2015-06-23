package core.threadpool;
public class ThreadPool extends Object {
    private ObjectFIFO idleWorkers;
    private ThreadPoolWorker[] workerList;
    
    public ThreadPool(int numberOfThreads , String initMessage ) {     
          // make sure that it’s at least one     
          numberOfThreads = Math.max(1, numberOfThreads);     
         
           idleWorkers = new ObjectFIFO(numberOfThreads);     
           workerList = new ThreadPoolWorker[numberOfThreads];     
       
           for ( int i = 0; i < workerList.length; i++ ) {     
 
               workerList[i] = new ThreadPoolWorker(idleWorkers , initMessage); 
 
 
           }     
       }     
       
 

       public void execute(Runnable target) throws InterruptedException {  
           ThreadPoolWorker worker = (ThreadPoolWorker) idleWorkers.remove(); 
 
           worker.process(target);     
       }     
       
       public void stopRequestIdleWorkers() {     
           try {     
               Object[] idle = idleWorkers.removeAll();     
               for ( int i = 0; i < idle.length; i++ ) {     
                   ( (ThreadPoolWorker) idle[i] ).stopRequest();     
               }     
           } catch ( InterruptedException x ) {     
               Thread.currentThread().interrupt(); // re-assert     
           }     
       }  

       public void showPending() {
    	   
    	   
    	   for ( int i = 0; i < workerList.length; i++ ) {     
               if ( !workerList[i].isIdle()) {     
                   System.out.println("url is " + ((CommonRunnable)workerList[i].r).link);     
               }     
           }
       }
       public int getIdleWorkersSize() {
    	   return idleWorkers.getSize();
       }
       public boolean isIdleWorkersFull() {
    	   return idleWorkers.isFull();
       }
       
       public void stopAllWorkers() {
    	   for ( int i = 0; i < workerList.length; i++ ) {     
               if ( workerList[i].isAlive() ) {     
                   workerList[i].stopRequest();     
               }     
           }
       }
        
       public void stopRequestAllWorkers() {     
           // Stop the idle one’s first      
           // productive.     
           stopRequestIdleWorkers();     
       
           // give the idle workers a quick chance to die     
 
           try { Thread.sleep(250); } catch ( InterruptedException x ) { } 
 
           for ( int i = 0; i < workerList.length; i++ ) {     
               if ( workerList[i].isAlive() ) {     
                   workerList[i].stopRequest();     
               }     
           }     
       }
    
}