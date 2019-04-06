package ampullen;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HelpListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);

        if(event.getMessage().getContentRaw().startsWith(Main.prefix + "help") && event.getChannel().getType() == ChannelType.PRIVATE){

            List<String> authors = Arrays.asList("Karim Shehata", "Raphael Panic");
            Collections.shuffle(authors);

            String s = "Ultimate Linz Bot\nby " + String.join(" & ", authors) +
                    "\nFuer Informationen zur Turniererstellung: !t" +
                    "\nFuer Informationen zur Pollerstellung: !p";

            event.getChannel().sendMessage(s).queue();

        }

    }
}
