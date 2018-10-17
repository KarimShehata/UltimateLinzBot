package ampullen;

import java.awt.Color;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.menu.ButtonMenu;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.RichPresence;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageEmbedEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class MainListener extends ListenerAdapter{

	
	@Override
	public void onMessageEmbed(MessageEmbedEvent event) {
		super.onMessageEmbed(event);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		super.onMessageReceived(event);
		
		JDA jda = event.getJDA();
		
		User author = event.getAuthor();
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();
        
        //System.out.println("On message: " + event.toString());
        //System.out.println("Message: " + message.getContentRaw() + " | " + message.getContentDisplay());
        
        if(message.getContentRaw().equals("!embed")) {
        	
        	System.out.println("Got ping");
        	
        	//channel.sendMessage("Pong!").complete();
        	
        	message.delete().complete();
        	
        	EmbedBuilder builder = new EmbedBuilder();
    		builder.setTitle("amPullen Title");
    		builder.setColor(new Color(0xF40C0C));
    		builder.setDescription("Das ist eine Beschreibung");
    		builder.addBlankField(false);
    		builder.addField("Key", "Value", true);
    		//builder.setFooter("Footer", "https://github.com/WardNetwork");
    		//builder.setImage("http://www.ampullen.net/assets/images/ap_avatar.jpeg");
    		builder.setAuthor("Raphael");
    		builder.setThumbnail("http://www.ampullen.net/assets/images/ap_avatar.jpeg");
        	
    		channel.sendMessage(builder.build()).complete();
    		
    		Game game = RichPresence.playing("Ultimate Frisbee");
    		game.asRichPresence();
    		
        }else if(message.getContentRaw().equals("poll")) {
        	
        	
        	EventWaiter waiter = new EventWaiter();
        	
        	ButtonMenu.Builder b = new ButtonMenu.Builder();
        	b.setText("ButtonMenu 1");
        	b.addChoice(jda.getEmoteById(":thumbsup:"));
        	b.addChoice("laughing");
        	b.addChoice("thumbsdown");
        	b.setEventWaiter(waiter);
        	b.setAction(x -> System.out.println(x));
        	ButtonMenu m = b.build();
        	m.display(channel);
        	
        	//waiter.waitForEvent(, condition, action);
        	
        }
		
	}
	
	/*@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		System.out.println("onMessageReactionAdd");
		
		User user = event.getUser();
		
		Message m = event.getChannel().getMessageById(event.getMessageId()).complete();
		
		//Except Poll bot
		if(m.getAuthor().getName().toLowerCase().contains("poll")) {
			return;
		}
		
		MessageReaction newest = event.getReaction();
		for(MessageReaction r : m.getReactions()) {
			if(r.equals(newest)) {
				continue;
			}
			r.getUsers().complete().forEach(x -> {
				if(user.getIdLong() == x.getIdLong()) {
					//Remove reaction
					r.removeReaction(user).complete();
				}
			});
		}
		
		super.onMessageReactionAdd(event);
	}*/

}
