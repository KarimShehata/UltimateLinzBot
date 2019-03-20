package ampullen;

import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class PinMessageRemoveListener extends ListenerAdapter {

    TextChannel channel;

    public PinMessageRemoveListener(TextChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if(event.getTextChannel().getIdLong() == channel.getIdLong()){

            if(event.getMessage().getType().getId() == MessageType.CHANNEL_PINNED_ADD.getId()){
                event.getMessage().delete().queue();
            }
        }


        super.onMessageReceived(event);
    }
}
