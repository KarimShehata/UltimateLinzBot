package ampullen;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.core.entities.Message;

public class MessageTimer {

	public static void deleteAfter(final Message m, long millis) {
		
		System.out.println("Start waiting " + millis);
		ScheduledExecutorService s = Executors.newScheduledThreadPool(1);
		s.schedule(() -> {
			
			System.out.println("Executing");
			System.out.println(m.getIdLong());
			m.delete().complete();
			
			
		}, millis, TimeUnit.MILLISECONDS); 
		
	}
	
}
