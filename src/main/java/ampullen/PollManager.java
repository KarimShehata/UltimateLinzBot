package ampullen;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PollManager {

    static ArrayList<PollManager> managers = new ArrayList<>();

    private PollData PollData;
    private Message Message;
    private HashMap<Member, ArrayList<MessageReaction>> UserVotes;

    public PollManager() {
        PollData = new PollData();
        UserVotes = new HashMap<>();
    }

    static boolean create(String commandString, MessageChannel messageChannel) {

        // split by commands
        String[] commandParts = commandString.split("(?=/)");

        if (commandParts.length != 4)
            return false;

        // /poll name /T Type /O optionA, optionB, optionC, .. /E emoteA, emoteB, emoteC, ..

        String pollName = commandParts[0].replace(Main.prefix + "p ", "").trim();
        String pollType = commandParts[1].replace("/t ", "").trim();
        String[] options = commandParts[2].replace("/o ", "").split(",");
        String[] emotes = commandParts[3].replace("/e ", "").split(",");

        PollManager pollManager = new PollManager();

        pollManager.PollData.Name = pollName;

        switch (pollType) {
            case "S":
                pollManager.PollData.Type = ampullen.PollType.SingleChoice;
                break;
            case "M":
                pollManager.PollData.Type = ampullen.PollType.MultipleChoice;
                break;
            default:
                return false;
        }

        boolean areOptionsValid = pollManager.createOptions(options, emotes);

        if (!areOptionsValid)
            return false;

        Message message = pollManager.createPollMessage();

        pollManager.Message = Utilities.sendMessage(messageChannel, message);
        pollManager.PollData.MessageId = pollManager.Message.getId();
        pollManager.addInitialReactions();

        managers.add(pollManager);

        Main.DataModel.PollData.add(pollManager.PollData);
        Main.DataModel.Save();

        return true;
    }

    public static PollManager getPollManagerByMessageId(String messageId) {

        PollManager selectedPollManager = null;

        for (PollManager pollManager : managers) {
            if (!pollManager.PollData.MessageId.equals(messageId)) continue;

            selectedPollManager = pollManager;
        }

        return selectedPollManager;
    }

    Message createPollMessage() {

        String name = PollData.Name;

        String table = createAsciiTable();

        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.append(name);
        messageBuilder.appendCodeBlock(table, "");

        return messageBuilder.build();
    }

    private boolean createOptions(String[] options, String[] emotes) {

        if (options.length != emotes.length)
            return false;

        for (int i = 0; i < options.length; i++) {
            PollData.VoteOptions.add(new VoteOption(options[i].trim(), emotes[i].trim()));
        }

        return true;
    }

    String createAsciiTable() {

        int nameColumnPadding = calculateNameColumnPadding();

        // create table header
        StringBuilder header = new StringBuilder(" " + Utilities.padRight("Name", nameColumnPadding) + " |");

        for (VoteOption voteOption : PollData.VoteOptions) {
            header.append(" ").append(voteOption.Name).append(" |");
        }

        header = new StringBuilder(header.substring(0, header.length() - 2) + "\n");

        // create separator
        StringBuilder separator = new StringBuilder();

        for (char ignored : header.toString().toCharArray()) {
            separator.append("=");
        }

        separator.append("\n");

        // create entries
        StringBuilder entries = new StringBuilder();

        PollData.VoteOptions.forEach(voteOption -> voteOption.Count = 0);

        for (Map.Entry<Member, ArrayList<MessageReaction>> userVote : UserVotes.entrySet()) {

            entries.append(" ").append(Utilities.padRight(userVote.getKey().getNickname(), nameColumnPadding)).append(" |");

            for (VoteOption voteOption : PollData.VoteOptions) {

                boolean found = false;
                for (MessageReaction messageReaction : userVote.getValue()) {
                    if (voteOption.Emote.equals(messageReaction.getReactionEmote().getName())) {
                        found = true;
                        voteOption.Count++;
                        entries.append(" ").append(Utilities.padRight("X", voteOption.Name.length())).append(" |");
                        break;
                    }
                }

                if (!found)
                    entries.append(" ").append(Utilities.padRight("", voteOption.Name.length())).append(" |");
            }

            entries = new StringBuilder(entries.substring(0, entries.length() - 1));
            entries.append("\n");
        }

        // create summary
        StringBuilder summary = new StringBuilder(" " + Utilities.padRight(Integer.toString(UserVotes.size()), nameColumnPadding) + " |");

        for (VoteOption voteOption : PollData.VoteOptions) {
            summary.append(" ").append(Utilities.padRight(Integer.toString(voteOption.Count), voteOption.Name.length())).append(" |");
        }

        summary = new StringBuilder(summary.substring(0, summary.length() - 1));

        return header +
                separator.toString() +
                entries +
                separator +
                summary;
    }

    private int calculateNameColumnPadding() {

        int maxLength = "Name".length();

        for (Map.Entry<Member, ArrayList<MessageReaction>> userVote : UserVotes.entrySet()) {
            int userNameLength = userVote.getKey().getNickname().length();
            if (userNameLength > maxLength)
                maxLength = userNameLength;
        }

        return maxLength;
    }

    void addInitialReactions() {
        for (VoteOption voteOption : PollData.VoteOptions) {
            Message.addReaction(voteOption.Emote).queue();
        }
    }

    Message getPollMessageByMessageId(String messageId){
        Message message = null;

        List<TextChannel> textChannels = Main.JDA.getTextChannels();
        for (TextChannel textChannel : textChannels) {
            try {
                message = textChannel.getMessageById(messageId).complete();
            }
            catch (Exception ex)
            {
            }
        }

        return message;
    }

    void addUserVote(MessageReactionAddEvent event) {

        Member member = event.getMember();
        MessageReaction messageReaction = event.getReaction();

        ArrayList<MessageReaction> messageReactions = UserVotes.get(member);

        if (messageReactions == null) {
            messageReactions = new ArrayList<>();
            messageReactions.add(messageReaction);
        } else {
            if (PollData.Type == PollType.SingleChoice) {
                //event.getChannel().getMessageById(event.getMessageId()).complete().
            }

            if (!messageReactions.contains(messageReaction)) //if not already voted
                messageReactions.add(messageReaction);
        }

        UserVotes.put(member, messageReactions);
    }

    void removeUserVote(Member member, MessageReaction messageReaction) {

        ArrayList<MessageReaction> messageReactions = UserVotes.get(member);

        if (messageReactions == null)  //already voted
            return;

        messageReactions.remove(messageReaction);

        if (messageReactions.size() <= 0)
            UserVotes.remove(member, messageReactions);
    }

    boolean verifyReaction(MessageReaction messageReaction) {

        for (VoteOption voteOption : PollData.VoteOptions) {
            if (voteOption.Emote.equals(messageReaction.getReactionEmote().getName())) {
                return true;
            }
        }
        return false;
    }
}
