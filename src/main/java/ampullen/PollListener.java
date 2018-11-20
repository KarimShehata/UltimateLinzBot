package ampullen;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;

public class PollListener extends ListenerAdapter {

    String commandString = "/poll";
    ArrayList<PollManager> pollManagers = new ArrayList<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);

        MessageChannel messageChannel = event.getChannel();
        String messageString = event.getMessage().getContentRaw();

        // /poll name /T type /O optionA, optionB, optionC, .. /E emoteA, emoteB, emoteC, ..
        if(messageString.startsWith(commandString)){

            PollManager pollManager = PollManager.createPoll(messageString.trim());
            if(pollManager != null)
            {
                Message message = createPollMessage(pollManager);

                pollManager.PollMessage = send(messageChannel, message);
                pollManager.addReactionsToMessage();

                pollManagers.add(pollManager);
            }
            else
            {
                sendHelpMessage(messageChannel);
            }
        }
    }

    private Message createPollMessage(PollManager pollManager) {

        String name = pollManager.PollName;

        String table = pollManager.createAsciiTable();

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.append(name);
        messageBuilder.appendCodeBlock(table, "");

        return messageBuilder.build();
    }

    private void sendHelpMessage(MessageChannel messageChannel) {
        int padding = 12;

        String info = "Verwendung: /poll \"name\" /T type /O optionA, optionB, optionC, .. /E emoteA, emoteB, emoteC, ..\n";

        String commands = Utilities.padRight("help", padding) + "Ruft die Hilfe auf\n"
                        + Utilities.padRight("create", padding) + "Erstellt ein neues Turnier\n"
                        + Utilities.padRight("info [x]", padding) + "Infos zu aktuellen Turnier [x = Turniername]";

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.append(info);
        //messageBuilder.appendCodeBlock(commands, "");

        send(messageChannel, messageBuilder.build());
    }

    public Message send(MessageChannel messageChannel, Message message){
         return messageChannel.sendMessage(message).complete();
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        super.onMessageReactionAdd(event);

        if(event.getUser().isBot()) return;

        String messageId = event.getMessageId();

        PollManager selectedPollManager = getPollManagerByMessageId(messageId);

        if(selectedPollManager == null) {
            System.out.println("Not a Poll!");
            return;
        }

        if (!selectedPollManager.verifyReaction(event.getReaction())) {
            event.getReaction().removeReaction(event.getUser()).complete();
            return;
        }

        selectedPollManager.addUserVote(event.getUser(), event.getReaction());

        Message message = event.getChannel().getMessageById(messageId).complete();
        message.editMessage(createPollMessage(selectedPollManager)).queue();
    }

    private PollManager getPollManagerByMessageId(String messageId) {

        PollManager selectedPollManager = null;

        for (PollManager pollManager : pollManagers) {
            if (!pollManager.PollMessage.getId().equals(messageId)) continue;

            selectedPollManager = pollManager;
        }

        return selectedPollManager;
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        super.onMessageReactionRemove(event);

        if(event.getUser().isBot()) return;

        String messageId = event.getMessageId();

        PollManager selectedPollManager = getPollManagerByMessageId(messageId);

        if(selectedPollManager == null) {
            System.out.println("No Poll Manager found!");
            return;
        }

        if (!selectedPollManager.verifyReaction(event.getReaction())) {
            return;
        }

        selectedPollManager.removeUserVote(event.getUser(), event.getReaction());

        Message message = event.getChannel().getMessageById(messageId).complete();
        message.editMessage(createPollMessage(selectedPollManager)).queue();
    }
}