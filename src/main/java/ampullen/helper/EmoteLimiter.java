package ampullen.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class EmoteLimiter extends ListenerAdapter{
	
	boolean limitEmotes;
	List<String> allowedEmotes;
	
	boolean limitReactions;
	boolean displayAllowed;
	
	List<Long> messages = new ArrayList<>();

	public EmoteLimiter(Message m) {
		this();
		addMessage(m);
	}
	
	public EmoteLimiter() {
	}
	
	public void start(MessageChannel c) {
		Stream<Message> sm = 
			messages.stream()
				.map(x -> c.getMessageById(x).complete());
		
		if(sm.allMatch(x -> x.getReactions().size() == 0)) {
			
			sm.forEach(x -> {
				
				allowedEmotes.stream()
					.map(y -> getEmote(y, (Channel)c))
					.forEach(y -> {
						x.addReaction(y).complete();
						System.out.println(y.getName());
					});
			});
			
		}/*else {
			
			sm.forEach(x -> {
				
				x.getReactions().stream()
					.map(y -> y.getUsers().complete())
					.flatMap(y -> y.stream())
					.distinct()
					.forEach(y -> {
						
						x.getReactions().stream().
						.sorted((a, b) -> Long.compare(a.get, y))
						
					});
				
			});
			
		}*/
		
		c.getJDA().addEventListener(this);
	}
	
	public Emote getEmote(String s, Channel c) {
		return c.getGuild().getEmotesByName(s, true).stream().findFirst().orElse(null);
	}
	
	public void stop(JDA jda) {
		jda.removeEventListener(this);
	}
	
	public EmoteLimiter setDisplayAllowed(boolean b) {
		this.displayAllowed = b;
		return this;
	}
	
	public EmoteLimiter setAllowedEmotes(List<String> allowedEmotes) {
		this.allowedEmotes = allowedEmotes;
		this.limitEmotes = true;
		return this;
	}
	
	public List<String> getAllowedEmotes() {
		return allowedEmotes;
	}

	public EmoteLimiter setLimitEmotes(boolean limitEmotes) {
		this.limitEmotes = limitEmotes;
		return this;
	}

	public EmoteLimiter setLimitReactions(boolean limitReactions) {
		this.limitReactions = limitReactions;
		return this;
	}
	
	public void addMessage(Message m) {
		addMessage(m.getIdLong());
	}
	
	public void addMessage(long l) {
		messages.add(l);
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		super.onMessageReactionAdd(event);
		
		long id = event.getMessageIdLong();

		Message m = event.getChannel().getMessageById(event.getMessageId()).complete();
		User user = event.getUser();
		
		if(messages.contains(id)) {
			
			if(limitEmotes) {
				if(!allowedEmotes.contains(event.getReactionEmote().getEmote().getName())) {
					
					System.out.println("Remove1: ");
					event.getReaction().removeReaction(user).complete();
					return;
					
				}
			}
			
			if(limitReactions) {
				
				//Except Poll bot
				if(m.getAuthor().getName().toLowerCase().contains("poll") || event.getUser().getName().toLowerCase().contains("ampullen")) {
					System.out.println("Bot return");
					return;
				}
				
				MessageReaction newest = event.getReaction();
				System.out.println(newest.toString());
				for(MessageReaction r : m.getReactions()) {
					if(r.getReactionEmote().equals(newest.getReactionEmote()) && r.getMessageIdLong() == newest.getMessageIdLong()) {
						continue;
					}
					r.getUsers().complete().forEach(x -> {
						if(user.getIdLong() == x.getIdLong()) {
							//Remove reaction
							System.out.println("Remove2");
							r.removeReaction(user).complete();
						}
					});
				}
				
			}
			
		}
		
	}
	
}
