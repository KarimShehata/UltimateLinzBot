package ampullen;

import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class Prompt extends ListenerAdapter{
	
    private CountDownLatch latch = null;
    
	String prompt;
	MessageChannel channel;
	Consumer<String> callback = null;
	User user;
	
	String ret = null;

	public Prompt(String prompt, MessageChannel channel, User promptedUser) {
		super();
		this.prompt = prompt;
		this.channel = channel;
		this.user = promptedUser;
	}
	
	public String promptSync(){
		
		channel.sendMessage(prompt).complete();
		channel.getJDA().addEventListener(this);
		latch = new CountDownLatch(1);
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		channel.getJDA().removeEventListener(this);
		
		System.out.println("Returning answer");
		
		return ret;
	}
	
	public void promtAsync(Consumer<String> callback){
		
		this.callback = callback;

		channel.sendMessage(prompt).complete();
		channel.getJDA().addEventListener(this);
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		super.onMessageReceived(event);
		
		if(channel.equals(event.getChannel()) && event.getAuthor().equals(user)){
			ret = event.getMessage().getContentRaw();
			if(latch != null){
				latch.countDown();
			}else if(callback != null){
				callback.accept(ret);
				channel.getJDA().removeEventListener(this);
			}
			System.out.println("Callback");
		}
		System.out.println("Wrong channel or user");
	}
	
}
