package ampullen;

import java.text.SimpleDateFormat;
import java.util.Date;

public class UserData {
    public String Phone;
    public String Name;
    public String Surname;
    public Date Birthday;
    public String Email;
    public Sex Sex;

    public String toString() {
        return  "Name: '" + this.Name + "'\n" +
                "Surname: '" + this.Surname + "'\n" +
                "Birthday: '" + new SimpleDateFormat("dd.MM.yyyy").format(this.Birthday) + "'\n" +
                "Sex: '" + this.Sex.toString()+ "'\n" +
                "Email: '" + this.Email+ "'\n" +
                "Phone: '" + this.Phone + "'\n";
    }
}


