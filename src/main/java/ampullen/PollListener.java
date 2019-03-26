package ampullen;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import java.util.ArrayList;

public class PollListener extends ListenerAdapter {

    String commandString;

    public PollListener(String commandString) {
        this.commandString = commandString;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);

        MessageChannel messageChannel = event.getChannel();
        String messageString = event.getMessage().getContentRaw().trim();

        String command = messageString.split(" ")[0];

        if (event.getAuthor().isBot()) return;

        if(event.getChannel().getType() == ChannelType.PRIVATE) return;

        if (!command.equals(commandString)) return;

        // Probably not necessary
        if (!Utilities.HasMemberRole(event.getMember(), "Vereinsmitglied")) {
            MessageTimer.deleteAfter(event.getMessage(), 0);
            return;
        }

        boolean pollCreated = PollManager.create(messageString, messageChannel);
        if (!pollCreated) {
            // todo: return more useful feedback
            sendHelpMessage(messageChannel);
        }

        MessageTimer.deleteAfter(event.getMessage(), 0);
    }

    private void sendHelpMessage(MessageChannel messageChannel) {
        int padding = 12;

        String info = "Verwendung: \n" +
                commandString + " name /t Type [S/M] /o optionA, optionB, optionC, .. /e emoteA, emoteB, emoteC, ..\n" +
                "(Sollte ein Wert Leerzeichen enthalten bitte in \"\" stellen.)";

        String commands = Utilities.padRight("help", padding) + "Ruft die Hilfe auf\n"
                + Utilities.padRight("create", padding) + "Erstellt ein neues Turnier\n"
                + Utilities.padRight("info [x]", padding) + "Infos zu aktuellen Turnier [x = Turniername]";

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.append(info);
        //messageBuilder.appendCodeBlock(commands, "");

        Message message = Utilities.sendMessage(messageChannel, messageBuilder.build());

        MessageTimer.deleteAfter(message, 10000);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        super.onMessageReactionAdd(event);

        if (event.getUser().isBot()) return;

        String messageId = event.getMessageId();

        PollManager selectedPollManager = PollManager.getPollManagerByMessageId(messageId);

        if (selectedPollManager == null) return; //not a poll or poll not found

        if (!selectedPollManager.verifyReaction(event.getReaction())) {
            event.getReaction().removeReaction(event.getUser()).complete();
            return;
        }

        selectedPollManager.addUserVote(event);

        Message message = event.getChannel().getMessageById(messageId).complete();
        message.editMessage(selectedPollManager.createPollMessage()).queue();
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        super.onMessageReactionRemove(event);

        if (event.getUser().isBot()) return;

        String messageId = event.getMessageId();

        PollManager selectedPollManager = PollManager.getPollManagerByMessageId(messageId);

        if (selectedPollManager == null) {
            System.out.println("No Poll Manager found!");
            return;
        }

        if (!selectedPollManager.verifyReaction(event.getReaction())) {
            return;
        }

        selectedPollManager.removeUserVote(event.getMember(), event.getReaction());

        Message message = event.getChannel().getMessageById(messageId).complete();
        message.editMessage(selectedPollManager.createPollMessage()).queue();
    }
}