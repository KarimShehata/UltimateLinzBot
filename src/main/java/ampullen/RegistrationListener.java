package ampullen;

import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegistrationListener extends ListenerAdapter {

    private final RegistrationService registrationService;

    public RegistrationListener() {
        registrationService = new RegistrationService();
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent guildMemberJoinEvent) {
        User user = guildMemberJoinEvent.getUser();

        if (registrationService.UltimateLinzUsers.contains(new UltimateLinzUser(user))) {
            //todo welcome back
        } else {
            UltimateLinzUser ultimateLinzUser = new UltimateLinzUser(user);
            registrationService.UltimateLinzUsers.add(ultimateLinzUser);

            StartRegistration(ultimateLinzUser);
        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent privateMessageReceivedEvent) {
        super.onPrivateMessageReceived(privateMessageReceivedEvent);

        User user = privateMessageReceivedEvent.getAuthor();

        // ignore bot messages
        if (user.isBot()) return;

        UltimateLinzUser ultimateLinzUser = getUltimateLinzUser(user);

        if (ultimateLinzUser == null) return;

        if (ultimateLinzUser.IsRegistrationComplete) return;

        // todo process response
        String message = privateMessageReceivedEvent.getMessage().getContentDisplay();

        boolean isResponseValid = CheckAndSaveUserResponse(ultimateLinzUser, message);

        if(isResponseValid)
            sendNextRegistrationMessage(ultimateLinzUser);
        else
            resendRegistrationMessage(ultimateLinzUser);
    }

    private void resendRegistrationMessage(UltimateLinzUser ultimateLinzUser) {
        sendRegistrationMessage(ultimateLinzUser);
    }

    private void sendNextRegistrationMessage(UltimateLinzUser ultimateLinzUser) {
        ultimateLinzUser.RegistrationStep = ultimateLinzUser.RegistrationStep.next();

        sendRegistrationMessage(ultimateLinzUser);
    }

    private boolean CheckAndSaveUserResponse(UltimateLinzUser ultimateLinzUser, String message) {
        switch (ultimateLinzUser.RegistrationStep) {
            case NotStarted:
                break;
            case Name:
                if (message.length() > 1)
                    return true;
                break;
            case Surname:
                if (message.length() > 1)
                    return true;
                break;
            case Sex:
                break;
            case Birthday:
                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
                try {
                    Date birthdate = dateFormat.parse(message);
                    return true;
                } catch (ParseException e) {
                    e.printStackTrace();
                    return false;
                }
            case Email:
                return Utilities.validateEmailAddress(message);
            case Phone:
                try {
                    int phoneNumber = Integer.parseInt(message);
                    return message.length() > 9;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            case Done:
                break;
        }

        return false;
    }

    private UltimateLinzUser getUltimateLinzUser(User user) {
        for (UltimateLinzUser ulu : registrationService.UltimateLinzUsers) {
            if (ulu.User.getId().equals(user.getId())) {
                return ulu;
            }
        }
        return null;
    }

    @Override
    public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent privateMessageReactionAddEvent) {
        super.onPrivateMessageReactionAdd(privateMessageReactionAddEvent);

        User user = privateMessageReactionAddEvent.getUser();

        if (user.isBot()) return;

        UltimateLinzUser ultimateLinzUser = getUltimateLinzUser(user);

        if (ultimateLinzUser == null) return;

        // todo check if valid reaction
        sendNextRegistrationMessage(ultimateLinzUser);
    }

    private void sendRegistrationMessage(UltimateLinzUser ultimateLinzUser) {

        UltimateLinzUser finalUltimateLinzUser = ultimateLinzUser;

        int index = finalUltimateLinzUser.RegistrationStep.ordinal() - 1;
        String message = Messages.RegistrationForm[index];

        switch (finalUltimateLinzUser.RegistrationStep) {
            case NotStarted:
            case Name:
            case Surname:
            case Birthday:
            case Email:
            case Phone:
            case Done:
                ultimateLinzUser.User.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue());
                break;
            case Sex:
                ultimateLinzUser.User.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(message).queue(message1 -> {
                    message1.addReaction("\ud83d\udeba").queue();
                    message1.addReaction("\ud83d\udeb9").queue();
                }));
                break;
        }
    }

    private void StartRegistration(UltimateLinzUser ultimateLinzUser) {
        ultimateLinzUser.RegistrationStep = ultimateLinzUser.RegistrationStep.next();
        ultimateLinzUser.User.openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(Messages.WelcomeMessage).queue());
        sendRegistrationMessage(ultimateLinzUser);
    }
}