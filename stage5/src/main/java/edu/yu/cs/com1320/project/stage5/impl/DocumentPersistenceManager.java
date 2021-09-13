package edu.yu.cs.com1320.project.stage5.impl;

import edu.yu.cs.com1320.project.stage5.Document;
import edu.yu.cs.com1320.project.stage5.PersistenceManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.lang.reflect.Type;
import com.google.gson.*;

/**
 * created by the document store and given to the BTree via a call to BTree.setPersistenceManager
 */
public class DocumentPersistenceManager implements PersistenceManager<URI, Document> {

	private File baseDir;
	
	public DocumentPersistenceManager() {
		this(null);
	}
    public DocumentPersistenceManager(File baseDir){
    	if (baseDir == null) {
    		this.baseDir = new File(System.getProperty("user.dir"));
    	}
    	else {
    		baseDir.mkdirs();
    		this.baseDir = baseDir; 
    	}
    }
    
    protected JsonSerializer<Document> DocumentSerializer = new JsonSerializer<Document>() {  
        @Override
        public JsonElement serialize(Document src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            String text = src.getDocumentAsTxt();
            String uri = src.getKey().toString();
            String hashCode = String.valueOf(src.getDocumentTextHashCode());
            String wordMap = wordMapToString(src.getWordMap());
            jsonObject.addProperty("text", text);
            jsonObject.addProperty("uri", uri);
            jsonObject.addProperty("hashCode", hashCode);
            jsonObject.addProperty("wordMap", wordMap);
            return jsonObject;
        }
    };
    
    protected JsonDeserializer<Document> DocumentDeserializer = new JsonDeserializer<Document>() {  
        @Override
        public Document deserialize(JsonElement json,
        							Type typeOfT, 
        							JsonDeserializationContext context)
        							throws JsonParseException {
            JsonObject jsonObject = new JsonObject();
            String text = json.getAsJsonObject().get("text").getAsString();
            String uriText = json.getAsJsonObject().get("uri").getAsString();
            URI uri = null;
            try {
            	uri = new URI(uriText);
            }
            catch (URISyntaxException e) {
            	e.printStackTrace();
            }
            int hashCode = json.getAsJsonObject().get("hashCode").getAsInt();
            String wordMapString = json.getAsJsonObject().get("wordMap").getAsString();
            Map<String,Integer> wordMap = stringToWordMap(wordMapString);
            Document doc = new DocumentImpl(uri,text,hashCode);
            doc.setWordMap(wordMap);
            return doc;
        }
    };
    
    private String wordMapToString(Map<String,Integer> wordMap) {
    	String result = new String();
    	for (String key: wordMap.keySet()) {
    		result += (key + ":"+wordMap.get(key) + ",");
    	}
    	return result;
    }
    
    private Map<String,Integer> stringToWordMap(String string) {
    	Map<String,Integer> result = new HashMap<>();
    	String[] kvPairings = string.split(",");
    	for (String pairing: kvPairings) {
    		String[] keyAndValue = pairing.split(":");
    		result.put(keyAndValue[0], Integer.valueOf(keyAndValue[1]));
    	}
    	return result;
    }

    @Override
    public void serialize(URI uri, Document val) throws IOException {
    	GsonBuilder gsonBuilder = new GsonBuilder();
    	gsonBuilder = gsonBuilder.registerTypeAdapter(val.getClass(), DocumentSerializer);
    	Gson gson = gsonBuilder.setPrettyPrinting().create();
    	String jsonDocument = gson.toJson(val);
    	String authority, path, query, fragment;
    	if (uri.getAuthority()==null) authority = "";
    		else authority = uri.getAuthority();
    	if (uri.getPath()==null) path = "";
			else path = uri.getPath();
    	if (uri.getQuery()==null) query = "";
    		else query = uri.getQuery();
    	if (uri.getFragment()==null) fragment = "";
    	 	else fragment = uri.getFragment();
    	String fileLocation = authority + path + query + fragment + ".json";
		File dirs = new File(baseDir.getAbsolutePath() + File.separator + fileLocation);
		dirs.mkdirs();
		File jsonFile = new File(dirs.getAbsolutePath());
		dirs.delete();
    	FileWriter fw = new FileWriter(jsonFile);
    	fw.write(jsonDocument);
    	fw.close();
    }

    @Override
    public Document deserialize(URI uri) throws IOException {
    	Gson gson = new GsonBuilder().registerTypeAdapter(Document.class, DocumentDeserializer).create();
    	String authority, path, query, fragment;
    	if (uri.getAuthority()==null) authority = "";
    		else authority = uri.getAuthority();
    	if (uri.getPath()==null) path = "";
			else path = uri.getPath();
    	if (uri.getQuery()==null) query = "";
    		else query = uri.getQuery();
    	if (uri.getFragment()==null) fragment = "";
    	 	else fragment = uri.getFragment();
    	String location = baseDir.getAbsolutePath()+File.separator+authority+path+query+fragment+".json";
        String json = fileToString(location);
        if (json == null) return null; // if fileToString returned null, that means there was IOException
        							   // i.e. the file wasn't there, so deserialize should return null
        Document doc = gson.fromJson(json,Document.class);
        doc.setLastUseTime(System.nanoTime());
        // erase the doc from disk, as well as any empty parent dirs
        File file = new File(location);
        boolean wasDeleted = true;
        while (file != null && wasDeleted) {
        	File parent = file.getParentFile(); // will be null if no parent
        	wasDeleted = file.delete();
        	file = parent;
        }
    	return doc;
    }
    
    private String fileToString(String location) {
    	String result = "";
    	try {
    		result = new String(Files.readAllBytes(Paths.get(location)));
    	}
    	catch (IOException e) {
    		result = null;
    	}
    	return result;
    }
  
}