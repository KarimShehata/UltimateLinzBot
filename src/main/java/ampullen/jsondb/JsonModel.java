package ampullen.jsondb;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONWriter;
import org.omg.PortableInterceptor.AdapterStateHelper;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import ampullen.model.Tournament;

public class JsonModel implements Observer{

	private static final File BASE_DIRECTORY = new File(System.getProperty("user.dir") + "/db");
	private Moshi moshi = new Moshi.Builder().build();
	
	private List<Tournament> tournaments = new ArrayList<>();
	
	private static JsonModel instance = null;

	public List<Tournament> getTournaments() {
		return tournaments;
	}
	
	public void addTournament(Tournament t){
		t.addObserver(this);
		tournaments.add(t);
		save(tournaments);
	}

	public void setTournaments(List<Tournament> tournaments) {
		subscribe(tournaments);
		this.tournaments = tournaments;
	}
	
	private JsonModel() {
		
		load();
		
	}
	
	private <T> void save(List<T> list){
		
		if(!BASE_DIRECTORY.exists()){
			BASE_DIRECTORY.mkdirs();
		}
		
		String name = "";
		Type type = null;
		
		if(list.get(0) instanceof Tournament){
			name = "Tournament";
			type = Types.newParameterizedType(List.class, Tournament.class);
		}//TODO Other Classes
		
		
		File f = new File(BASE_DIRECTORY.getAbsolutePath() + "\\" + name + ".json");

		JsonAdapter<List<T>> adapter = moshi.adapter(type);
		String s = adapter.toJson(list);
		
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
				
				//TODO Support Umlaute, €
				
				try {
					List<String> list = Files.readAllLines(file.toPath());
					System.out.println(list.toString());
					String s = Files.readAllLines(file.toPath()).stream().reduce("", (x, y) -> x + y);
					JsonAdapter adapter = moshi.adapter(t);
					Object o = adapter.fromJson(s);
					Object element = ((List)o).get(0);
					
					if(element instanceof Tournament){
						tournaments = ((List<Tournament>)o);
						//TODO Other Classes
					}else{
						System.out.println("Error 1");
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
			
			if(f.getType().getName().equals(List.class.getName())){
				list.add(f);
			}
			
		}
		return list;
	}

	@Override
	public void update(Observable observable) {
		
		if(observable instanceof Tournament){
			
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
