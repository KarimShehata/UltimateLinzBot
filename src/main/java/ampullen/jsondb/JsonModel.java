package ampullen.jsondb;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import ampullen.Main;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import ampullen.model.Tournament;
import ampullen.model.TournamentVotes;
import sun.nio.cs.UTF_32;

import static ampullen.Main.JDA;

public class JsonModel implements Observer{

	private static final File BASE_DIRECTORY = new File(new File(System.getProperty("user.dir")).getParent() + File.separator + "db");
	private Moshi moshi = new Moshi.Builder().build();

	private ObservableList<Tournament> tournaments = new ObservableList<>();

	private static JsonModel instance = null;

	public List<Tournament> tournaments() {
		return tournaments;
	}

	public Tournament findTouramentByName(String name){

		return this.tournaments().stream()
				.filter(x -> x.getName().toLowerCase().startsWith(name.toLowerCase()))
				.min((o1, o2) -> Long.compare(o1.getDate(), o2.getDate()) * -1)
				.orElse(null);
	}

	private JsonModel() {

		load();
		tournaments.addObserver(this);

	}

	private <T> void save(List<T> list){

		if(!BASE_DIRECTORY.exists()){
			BASE_DIRECTORY.mkdirs();
		}

		String name = "";
		Type type = null;


		if(list.getClass().isInstance(new ObservableList<Tournament>())){
			name = "Tournament";
			type = Types.newParameterizedType(List.class, Tournament.class);
		}//TODO Other Classes


		File f = new File(BASE_DIRECTORY.getAbsolutePath() + File.separator + name + ".json");

		JsonAdapter<List<T>> adapter = moshi.adapter(type);
		String s = adapter.toJson(list);

		if(!f.exists()){
			try {
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		try(Writer writer = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)){

			writer.write(s);

		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	private void load() {

		if(!BASE_DIRECTORY.exists()){
			BASE_DIRECTORY.mkdirs();
		}

		for(Field f : getFields()){

			Type type = f.getGenericType();

			if(type instanceof ParameterizedType){
				type = ((ParameterizedType)type).getActualTypeArguments()[0];
			}

			String[] parts = type.getTypeName().split("\\.");
			String name = parts[parts.length-1];

			Type t = Types.newParameterizedType(List.class, type);

			File file = new File(BASE_DIRECTORY.getAbsolutePath() + File.separator + name + ".json");

			if(file.exists()){

				try {
					List<String> list = Files.readAllLines(file.toPath());
					System.out.println(list.toString());
					String s = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8).stream().reduce("", (x, y) -> x + y);
					JsonAdapter adapter = moshi.adapter(t);
					Object o = adapter.fromJson(s);
					ObservableList observablelist = new ObservableList(((List)o));
					if(observablelist.size() > 0){

						Object element = observablelist.get(0);

						if(element instanceof Tournament || element instanceof TournamentVotes){
							tournaments = ((ObservableList<Tournament>)observablelist);
							tournaments.forEach(x -> x.addObserver(this));
							//TODO Other Classes
						}else{
							System.out.println("Error 1");
						}

						if(element instanceof Initializeable){

							observablelist.forEach(x -> ((Initializeable) x).init(JDA));

						}

					}

				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		}

	}

	public List<Field> getFields(){
		List<Field> list = new ArrayList<>();
		for(Field f : this.getClass().getDeclaredFields()){

			if(f.getType().getName().equals(ObservableList.class.getName())){
				list.add(f);
			}

		}
		return list;
	}

	@Override
	public void update(IObservable observable) {

		if(observable instanceof Tournament || observable.getClass().isInstance(tournaments)){

			save(tournaments);

		}

	}

	public void subscribe(List<? extends Observable> list){
		list.forEach(x -> x.addObserver(this));
	}

	public static JsonModel getInstance(){
		if(instance == null){
			instance = new JsonModel();
		}
		return instance;
	}

}
