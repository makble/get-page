package core.threadpool;
public class ObjectFIFO extends Object {
    private Object[] queue;
    private int capacity;
    private int size;
    private int head;
    private int tail;
    
    public ObjectFIFO ( int cap ) {
        capacity = ( cap > 0 ) ? cap : 1;
        queue = new Object[capacity];
        head = 0;
        tail = 0;
        size = 0;
    }
    
    public int getCapacity () {
        return capacity;
    }
    
    public synchronized int getSize () {
        return size;
    }

    public synchronized boolean isEmpty () {
        return ( size == 0 );
    }

    public synchronized boolean isFull () {
        return ( size == capacity );
    }
    
    public synchronized void add ( Object obj ) throws InterruptedException {
		waitWhileFull ();
		queue[head] = obj;
		head = ( head + 1 ) % capacity;
		size++;
		notifyAll(); // implement in native code. , 按照后来先得的顺序?
    }
    
    public synchronized void addEach  ( Object[] list ) throws InterruptedException {
		for ( int i = 0 ; i < list.length ; i++ ) {
			add(list[i]);
		}
    }
    
    public synchronized Object remove  ( ) throws InterruptedException {
		// 因为wait的机制, tail和head永远不会冲突 . 
		waitWhileEmpty();
		
		Object obj = queue[tail];
		queue[tail] = null;
		
		tail = ( tail + 1 ) % capacity;
		size--;
		
		notifyAll();
		
		return obj;
    }
    
    public synchronized Object[] removeAll  ( ) throws InterruptedException {
		Object[] list = new Object[size];
		
		for ( int i = 0 ; i < list.length ; i++ ) {
			list[i] = remove();
		}
		
		return list;
    }
    
    public synchronized Object[] removeAtLeastOne()      
               throws InterruptedException {     
 
        waitWhileEmpty(); // wait for at least one to be in 

        return removeAll();     
    }
    
    public synchronized boolean waitUntilEmpty(long msTimeout)  throws InterruptedException {     
        
           if ( msTimeout == 0L ) {     
               waitUntilEmpty();  // use other method     
               return true;     
           }     
        
           // wait only for the specified amount of time     
 
           long endTime = System.currentTimeMillis() + msTimeout; 
 
 
           long msRemaining = msTimeout;     
        
           while ( !isEmpty() && ( msRemaining > 0L ) ) {     
               wait(msRemaining);     
               msRemaining = endTime - System.currentTimeMillis(); 
           }     
        
           // May have timed out, or may have met condition,      
           // calc return value.     
           return isEmpty();     
    }
    
    public synchronized void waitUntilEmpty()      
               throws InterruptedException {     
        
           while ( !isEmpty() ) {     
               wait();     
           }     
    }     
     
    public synchronized void waitWhileEmpty()      
            throws InterruptedException {     
     
        
        while ( isEmpty() ) {     
            // ObjectFIFOTest.print("is empty and we will wait before wait waitWhileEmpty");
            wait();
            // ObjectFIFOTest.print("wakeup from waitWhileEmpty");
        }
    }       
     
    public synchronized void waitUntilFull()      
            throws InterruptedException {     
     
        while ( !isFull() ) {     
            wait();     
        }     
    }     
     
    public synchronized void waitWhileFull()      
            throws InterruptedException {     
     
        while ( isFull() ) {     
            wait(); // tells the calling thread to give up the monitor and go to sleep until some other thread enters the same monitor and calls notify( ).
                    // wait on this object.  A thread that calls wait() on any object becomes inactive until another thread calls notify() on that object. 

        }     
    }
}











































