package ampullen.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class ListenerAdapterCommand extends ListenerAdapter{
	
	String cmd;
	
	public ListenerAdapterCommand(String cmd) {
		this.cmd = cmd;
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		super.onMessageReceived(event);
		
		MessageChannel channel = event.getChannel();
		String msg = event.getMessage().getContentRaw();
		
		if(!event.getJDA().getSelfUser().equals(event.getAuthor())){
			if(channel.getType() == ChannelType.GROUP || channel.getType() == ChannelType.PRIVATE || channel.getType() == ChannelType.TEXT){
				if(msg.startsWith(cmd)){
					
					System.out.println("Message: " + event.getMessage().getContentDisplay() + " MessageId " + event.getMessageId());
					boolean b = command(event, msg);
					
					if(!b){
						//TODO Command not recognized
					}
					
				}
			}
		}
	}
	
	public abstract void help(MessageReceivedEvent event, String[] msg);

	private boolean command(MessageReceivedEvent event, String msg) {
		
		String[] tokens = msg.split(" ");
		
		if(tokens.length < 2){
			help(event, tokens);
			return true;
		}
		
		for(Method m : this.getClass().getMethods()){
			
			if(m.getName().equalsIgnoreCase(tokens[1])){
				
				try {
					m.invoke(this, event, tokens);
					return true;
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					//TODO, Command not found, help()
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public void send(MessageChannel c, String msg){
		c.sendMessage(msg).submit();
	}

}
