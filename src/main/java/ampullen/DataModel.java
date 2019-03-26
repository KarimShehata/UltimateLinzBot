package ampullen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class DataModel {
    public List<PollData> PollData = new ArrayList<>();

    public DataModel() {
    }

    public static DataModel Initialize() {
        return new DataModel();
    }

    public void Save() {
        try {
            Writer writer = new FileWriter("DataModel.json");
            Gson gson = new GsonBuilder().create();
            gson.toJson(PollData, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
