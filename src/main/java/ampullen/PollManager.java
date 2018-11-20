package ampullen;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class PollManager {

    private ArrayList<String> Users;
    Message PollMessage;
    private PollType PollType;
    String PollName;
    private ArrayList<VoteOption> VoteOptions;
    private HashMap<User, ArrayList<MessageReaction>> UserVotes;

    private PollManager() {
        Users = new ArrayList<>();
        VoteOptions = new ArrayList<>();
        UserVotes = new HashMap<>();
    }

    static PollManager createPoll(String commandString) {

        // split by commands
        String[] commandParts = commandString.split("(?=/)");

        if(commandParts.length != 4)
            return null;

        // /poll name /T type /O optionA, optionB, optionC, .. /E emoteA, emoteB, emoteC, ..

        String pollName = commandParts[0].replace("/poll ", "").trim();
        String pollType = commandParts[1].replace("/T ", "").trim();
        String[] options = commandParts[2].replace("/O ", "").split(",");
        String[] emotes = commandParts[3].replace("/E ", "").split(",");

        PollManager pollManager = new PollManager();

        pollManager.PollName = pollName;

        switch (pollType) {
            case "Single":
                pollManager.PollType = ampullen.PollType.SingleChoice;
                break;
            case "Multiple":
                pollManager.PollType = ampullen.PollType.MultipleChoice;
                break;
            default:
                return null;
        }

        boolean areOptionsValid = pollManager.createOptions(options, emotes);

        if(!areOptionsValid)
            return null;

        return pollManager;
    }

    private boolean createOptions(String[] options, String[] emotes) {

        if(options.length != emotes.length)
            return false;

        for (int i=0; i<options.length; i++)
        {
            VoteOptions.add( new VoteOption(options[i].trim(), emotes[i].trim()));
        }

        return true;
    }

    String createAsciiTable() {

        int nameColumnPadding = calculateNameColumnPadding();

        // create table header
        StringBuilder header = new StringBuilder(" " + Utilities.padRight("Name", nameColumnPadding) + " |");

        for (VoteOption voteOption : VoteOptions){
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

        VoteOptions.forEach(voteOption -> voteOption.Count = 0);

        for (Map.Entry<User, ArrayList<MessageReaction>> userVote : UserVotes.entrySet()) {

            entries.append(" ").append(Utilities.padRight(userVote.getKey().getName(), nameColumnPadding)).append(" |");

            for (VoteOption voteOption : VoteOptions) {

                boolean found = false;
                for (MessageReaction messageReaction : userVote.getValue()) {
                    if(voteOption.Emote.equals(messageReaction.getReactionEmote().getName())){
                        found = true;
                        voteOption.Count++;
                        entries.append(" ").append(Utilities.padRight("X", voteOption.Name.length())).append(" |");
                        break;
                    }
                }

                if(!found)
                    entries.append(" ").append(Utilities.padRight("", voteOption.Name.length())).append(" |");
            }

            entries = new StringBuilder(entries.substring(0, entries.length() - 1));
            entries.append("\n");
        }

        // create summary
        StringBuilder summary = new StringBuilder(" " + Utilities.padRight(Integer.toString(UserVotes.size()), nameColumnPadding) + " |");

        for (VoteOption voteOption : VoteOptions) {
            summary.append(" ").append(Utilities.padRight(Integer.toString(voteOption.Count), voteOption.Name.length())).append(" |");
        }

        summary = new StringBuilder(summary.substring(0, summary.length() - 1));

        return  header +
                separator.toString() +
                entries +
                separator +
                summary;
    }

    private int calculateNameColumnPadding() {

        int maxLength = "Name".length();

        for (Map.Entry<User, ArrayList<MessageReaction>> userVote : UserVotes.entrySet()){
            int userNameLength = userVote.getKey().getName().length();
            if(userNameLength > maxLength)
                maxLength =  userNameLength;
        }

        return maxLength;
    }

    void addReactionsToMessage() {
        for (VoteOption voteOption : VoteOptions){
            PollMessage.addReaction(voteOption.Emote).queue();
        }
    }

    void addUserVote(User user, MessageReaction messageReaction) {

        ArrayList<MessageReaction> messageReactions = UserVotes.get(user);

        if (messageReactions != null)  //not voted yet
        {
            if(!messageReactions.contains(messageReaction)) //if not already voted
                messageReactions.add(messageReaction);
        }
        //add vote to other votes
        else {
            messageReactions = new ArrayList<>();
            messageReactions.add(messageReaction);
        }

        UserVotes.put(user, messageReactions);
    }

    void removeUserVote(User user, MessageReaction messageReaction) {

        ArrayList<MessageReaction> messageReactions = UserVotes.get(user);

        if (messageReactions == null)  //already voted
            return;

        messageReactions.remove(messageReaction);

        if(messageReactions.size() <= 0)
            UserVotes.remove(user, messageReactions);
    }

    boolean verifyReaction(MessageReaction messageReaction) {

        for (VoteOption voteOption : VoteOptions) {
            if(voteOption.Emote.equals(messageReaction.getReactionEmote().getName())) {
                return true;
            }
        }
        return false;
    }
}
