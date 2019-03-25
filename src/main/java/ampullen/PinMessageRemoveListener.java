package ampullen;

import net.dv8tion.jda.core.entities.Category;
import net.dv8tion.jda.core.entities.MessageType;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.function.Consumer;

public class PinMessageRemoveListener extends ListenerAdapter {

    TextChannel channel;

    int count = 0;
    Runnable runnable;

    public PinMessageRemoveListener(TextChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if(event.getTextChannel() != null && event.getTextChannel().getIdLong() == channel.getIdLong()){

            if(event.getMessage().getType().getId() == MessageType.CHANNEL_PINNED_ADD.getId()){
                event.getMessage().delete().queue();
                System.out.println(event.getMessage().getIdLong() + " message deleted from pinremover ");

                if(count > 0){
                    count--;
                    if(count == 0){
                        runnable.run();
                    }
                }

            }
        }


        super.onMessageReceived(event);
    }

    public void afterCount(int count, Runnable runnable){
        this.count = count;
        this.runnable = runnable;
    }
}
