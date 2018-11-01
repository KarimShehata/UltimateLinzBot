package ampullen;

import net.dv8tion.jda.core.entities.User;

public class UltimateLinzUser {

    public User User;
    public boolean IsRegistrationComplete;
    public RegistrationService.RegistrationStep RegistrationStep;

    public UltimateLinzUser(User user) {
        User = user;
        RegistrationStep = RegistrationService.RegistrationStep.NotStarted;
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