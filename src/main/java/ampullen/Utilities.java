package ampullen;

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

}
