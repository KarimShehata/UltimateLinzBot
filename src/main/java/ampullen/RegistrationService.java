package ampullen;

import java.util.ArrayList;

public class RegistrationService {

    public final ArrayList<UltimateLinzUser> UltimateLinzUsers;

    public enum RegistrationStep
    {
        NotStarted,
        Name,
        Surname,
        Sex,
        Birthday,
        Email,
        Phone,
        Done {
            @Override
            public RegistrationStep next() {
                return null; // see below for options for this line
            };
        };

        public RegistrationStep next() {
            // No bounds checking required here, because the last instance overrides
            return values()[ordinal() + 1];
        }
    }

    public RegistrationService() {
        UltimateLinzUsers = new ArrayList<>();
        //todo load users from json
    }
}
