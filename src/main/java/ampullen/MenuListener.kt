package ampullen

import net.dv8tion.jda.core.hooks.ListenerAdapter
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class MenuListener() : ListenerAdapter(){
	
	override fun onMessageReceived(event: MessageReceivedEvent)  {
		System.out.println("kotlin test")
	}
	
}