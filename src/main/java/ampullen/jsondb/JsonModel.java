package ampullen.jsondb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import ampullen.model.Tournament;
import ampullen.model.TournamentVotes;

import static ampullen.Main.jda;

public class JsonModel implements Observer{

	private static final File BASE_DIRECTORY = new File(System.getProperty("user.dir") + "/db");
	private Moshi moshi = new Moshi.Builder().build();
	
	private ObservableList<Tournament> tournaments = new ObservableList<>();
	
	private static JsonModel instance = null;

	public List<Tournament> tournaments() {
		return tournaments;
	}

	public Tournament findTouramentByName(String name){

		return this.tournaments().stream()
				.filter(x -> x.getName().toLowerCase().startsWith(name.toLowerCase()))
				.findFirst().orElse(null);
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
		
		
		File f = new File(BASE_DIRECTORY.getAbsolutePath() + "\\" + name + ".json");

		//if(empty)
		//	list.clear();
		
		JsonAdapter<List<T>> adapter = moshi.adapter(type);
		String s = adapter.toJson(list);
		s = s.replaceAll("[Ä$‰ˆ¸ƒ÷‹]", "");
		
		try (FileWriter fw = new FileWriter(f)){
			fw.write(s);
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
			
			File file = new File(BASE_DIRECTORY.getAbsolutePath() + "\\" + name + ".json");
			
			if(file.exists()){
				
				//TODO Support Umlaute, Ä
				
				try {
					List<String> list = Files.readAllLines(file.toPath());
					System.out.println(list.toString());
					String s = Files.readAllLines(file.toPath()).stream().reduce("", (x, y) -> x + y);
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

							observablelist.forEach(x -> ((Initializeable) x).init(jda));

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
			
			System.out.println(f.getName());
			
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
