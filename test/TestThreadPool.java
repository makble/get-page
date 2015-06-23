package test;

import prototype.Reporter;
import core.threadpool.CommonRunnable;
import core.threadpool.ThreadPool;

public class TestThreadPool {
    public ThreadPool tp;
    public Reporter r = new Reporter();
    public static void main ( String[] args ) {
        TestThreadPool instance = new TestThreadPool();
        instance.tp = new ThreadPool(10, "creating 10 thread for testing");
        instance.Init();
    }
    
    public void Task() {
        try {
            Thread.sleep(1000);
            r.debug("Task running");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void Init() {
        for ( int i = 0 ; i < 20 ; i ++) {
            Runnable r = new CommonRunnable() {
                public void run () {
                    Task();
                }
            };
            try {
                tp.execute( r );
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
        
        while(!tp.isIdleWorkersFull()) {
            // we have no idea about which thread is running which url. 
            r.info("System 正在等待最后几个站点线程结束, 线程没有全部结束, 站点线程正在运行数: "  + ( 10 - tp.getIdleWorkersSize() ) );
            tp.showPending();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        r.info("System will ending now. The main thread pool will shut down. Start shut down all threads in thread pool");
        tp.stopRequestAllWorkers();
    }
}
