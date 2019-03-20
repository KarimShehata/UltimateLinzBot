package ampullen.helper;

import java.util.List;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;

public class PersistentMessage {
	
	String messageStr;
	public EmoteLimiter limiter;
	MessageChannel c;
	List<String> emotes;
	Message message;
	boolean pinned = false;
	
	public PersistentMessage(MessageChannel c, String message, List<String> emotes) {
		this.messageStr = message;
		this.c = c;
		this.emotes = emotes;
	}
	
	public PersistentMessage(MessageChannel c, String message, List<String> emotes, boolean pinned) {
		this(c, message, emotes);
		this.pinned = pinned;
	}
	
	public PersistentMessage(Message m, List<String> emotes, boolean pinned) {
		this.message = m;
		this.emotes = emotes;
		this.pinned = pinned;
		this.c = m.getChannel();
	}
	
	public void init() {
		if(message == null) {
			message = c.sendMessage(message).complete();
		}
		limiter = new EmoteLimiter(message)
				.setAllowedEmotes(emotes)
				.setDisplayAllowed(true)
				.setLimitEmotes(true)
				.setLimitReactions(true);
		limiter.start(c);
		
		if(pinned) {
			message.pin().complete();
			System.out.println("PersistentMsg pin complete");
		}
	}

	public Message getMessage() {
		return message;
	}
}
