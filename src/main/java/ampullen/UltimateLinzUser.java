package ampullen;

import ampullen.registration.RegistrationService;
import net.dv8tion.jda.core.entities.User;

public class UltimateLinzUser {

    public User User;
    public UserData UserData;
    public boolean IsRegistrationComplete;
    public RegistrationService.RegistrationStep RegistrationStep;

    public UltimateLinzUser(User user) {
        User = user;
        RegistrationStep = RegistrationService.RegistrationStep.NotStarted;
        UserData = new UserData();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof UltimateLinzUser){
            UltimateLinzUser toCompare = (UltimateLinzUser) o;
            return User.getId().equals(toCompare.User.getId());
        }
        return false;
    }
}
