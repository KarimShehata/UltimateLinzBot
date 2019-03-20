package ampullen.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.util.Pair;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class EmoteLimiter extends ListenerAdapter{
	
	boolean limitEmotes;
	List<String> allowedEmotes;
	
	boolean limitReactions;
	boolean displayAllowed;
	
	List<Long> messages = new ArrayList<>();
	
	List<EmoteListener> listeners = new ArrayList<>();

	MessageChannel c;

	public EmoteLimiter(Message m) {
		this();
		addMessage(m);
	}
	
	public EmoteLimiter() {
	}
	
	public void start(MessageChannel c) {
		this.c = c;

		List<Message> sm = 
			messages.stream()
				.map(x -> c.getMessageById(x).complete()).collect(Collectors.toList());
		
		if(sm.stream().allMatch(x -> x.getReactions().size() == 0)) {
			
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

	public void manageBotReactions(){
		manageBotReactions(messages.toArray(new Long[0]));
	}

	public void manageBotReactions(Long... messages){

		Arrays.stream(messages).map(x -> c.getMessageById(x).complete()).forEach(m -> {

			boolean botReactionsActive = m.getReactions().stream().allMatch(x -> x.getUsers().complete().stream().anyMatch(User::isBot));

			List<Pair<MessageReaction, Integer>> sums =
					m.getReactions().stream()
					.map(x -> new Pair<>(x, x.getUsers().complete().size()))
					.collect(Collectors.toList());

			User bot = m.getJDA().getSelfUser();

			if(botReactionsActive && sums.stream().allMatch(x -> x.getValue() > 1)){

				m.getReactions().forEach(messageReaction -> messageReaction.removeReaction(bot).complete());

			}else if(!botReactionsActive && sums.stream().anyMatch(x -> x.getValue() < 2)){

				allowedEmotes.forEach(s -> m.addReaction(s).complete());

			}

		});

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
	
	public boolean removeMessage(long l) {
		return messages.remove(messages.indexOf(l)) != null;
	}
	
	public void addEmoteListener(EmoteListener e) {
		listeners.add(e);
	}

	public interface EmoteListener{
		public void emoteAdd(MessageReactionAddEvent e);
		public void emoteRemove(MessageReactionAddEvent e, MessageReaction r);
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		super.onMessageReactionRemove(event);

		long id = event.getMessageIdLong();

		if(messages.contains(id)) {

			manageBotReactions(id);

		}

	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		super.onMessageReactionAdd(event);
		
		long id = event.getMessageIdLong();

		Message m = event.getChannel().getMessageById(event.getMessageId()).complete();
		User user = event.getUser();
		
		if(messages.contains(id)) {
			
			if(limitEmotes) {
				if(event.getReactionEmote().getEmote() == null || !allowedEmotes.contains(event.getReactionEmote().getEmote().getName())) {
					
					System.out.println("Remove1 (forbidden emote): " + event.getReactionEmote().getName());
					event.getReaction().removeReaction(user).complete();
					return;
					
				}
			}
			
			if(limitReactions) {
				
				//Except Bots
				if(event.getUser().isBot()){
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
							System.out.println("Remove2 (other remote selected)");
							r.removeReaction(user).complete();
							listeners.forEach(y -> y.emoteRemove(event, r));
						}
					});
				}
				
			}
			
			listeners.forEach(x -> x.emoteAdd(event));
			manageBotReactions(id);
			
		}
		
	}
	
}
