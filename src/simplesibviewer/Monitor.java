package simplesibviewer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Monitor
{
	private Lock lock = new ReentrantLock();
	
	public void startPrinting()
	{
		lock.lock();
	}
	
	public void stopPrinting()
	{
		lock.unlock();
	}
}