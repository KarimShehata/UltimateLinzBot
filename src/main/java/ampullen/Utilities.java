package ampullen;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.Role;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utilities {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static final String FemaleEmoteString = "\ud83d\udeba";
    public static final String MaleEmoteString = "\ud83d\udeb9";

    public static boolean validateEmailAddress(String emailAddress) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailAddress);
        return matcher.find();
    }

    public static String padRight(String string, int n) {
        return String.format("%1$-" + n + "s", string);
    }

    public static String padLeft(String string, int n) {
        return String.format("%1$" + n + "s", string);
    }

    public static boolean HasMemberRole(Member member, String roleName) {
        for (Role role : member.getRoles())
        {
            if(role.getName().equals(roleName)) return true;
        }

        return false;
    }

    public static Message sendMessage(MessageChannel messageChannel, Message message) {
        return messageChannel.sendMessage(message).complete();
    }
}
