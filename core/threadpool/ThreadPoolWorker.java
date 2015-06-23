package core.threadpool;

public class ThreadPoolWorker extends Object {


	public String initMessage ;
	public Runnable r;
	
	public boolean idleState;
    private static int nextWorkerID = 0;     
    
    private ObjectFIFO idleWorkers;       
    private int workerID;     
    private ObjectFIFO handoffBox;     
    
    private Thread internalThread;    
    /*
     * false会阻止runWork的运行
     * */
    private volatile boolean noStopRequested;     
    
    public ThreadPoolWorker(ObjectFIFO idleWorkers, String m) {     
        this.idleWorkers = idleWorkers;     
        initMessage = m;
    
        workerID = getNextWorkerID();     
        handoffBox = new ObjectFIFO(1); // only one slot     
 
        noStopRequested = true;     
    
        Runnable r = new Runnable() {     
                public void run() {     
                    try {     
                        runWork();     
                    } catch ( Exception x ) {     
                        // in case ANY exception slips through 
                        x.printStackTrace();     
                    }     
                }     
            };     
     
        internalThread = new Thread(r);     
        internalThread.start();     
    }     
    
    public static synchronized int getNextWorkerID() {     
 
        // notice: sync’d at the class level to ensure uniqueness 
 
 
        int id = nextWorkerID;     
        nextWorkerID++;     
        return id;     
    }     
    
 
    public void process(Runnable target) throws InterruptedException {
    	
    	System.out.println("tracking in process" + ((CommonRunnable)target).link);

        handoffBox.add(target);      // construct when ThreadPoolWorker constructed
    }     
    
    private void runWork() {
        while ( noStopRequested ) {     
            try {     
                System.out.println("workerID=" + workerID +     
                        ", ready for work, initMessage is " + initMessage);     
 
                // Worker is ready work. This will never block 
 
                idleWorkers.add(this); idleState = true;
    
                // wait here until the server adds a request     
                r = (Runnable) handoffBox.remove();     
                idleState = false;
    
                System.out.println("workerID=" + workerID + ", starting execution of new Runnable: " + r + ((CommonRunnable)r).link ); 
  
                runIt(r); // catches all exceptions     
            } catch ( InterruptedException x ) {     
 
                Thread.currentThread().interrupt(); // re-assert 
            }     
        }     
    }   

    public boolean isIdle(){
    	return idleState;
    }
 
    private void runIt(Runnable r) {     
        try {     
            r.run();     
        } catch ( Exception runex ) {     
            // catch any and all exceptions     
            System.err.println("Uncaught exception fell through from run()" + ((CommonRunnable)r).link); 
 
 
            runex.printStackTrace();     
        } finally {     
 
            // Clear the interrupted flag (in case it comes back 
   
            Thread.interrupted();     
        }     
    }     
    
    public void stopRequest() {     
        System.out.println("workerID=" + workerID + ", stopRequest() received.");     
        noStopRequested = false;     
        internalThread.interrupt();      //       interrupt is like the pause
    } 
    
    public void stopIt() {
    	internalThread.stop();
    }
    
    public boolean isAlive() {     
        return internalThread.isAlive();     
    } 
}

