package controller;

import org.springframework.web.bind.annotation.*;

import com.mongodb.MongoClient;
import com.mongodb.client.*;

import java.util.*;

import org.bson.Document;

public class MongoConnector{
	
	private MongoDatabase database;
	private MongoCollection<Document> collection;
	
	protected Map<String,Set<String>> fabrics=new HashMap<String,Set<String>>();
	protected Map<String,Set<String>> nodes=new HashMap<String,Set<String>>();
	protected Map<String,Set<String>> files=new HashMap<String,Set<String>>();
	protected Set<String> paths=new TreeSet<String>();
	
//	public static void main(String[] args) {
//		MongoConnector m = new MongoConnector();
//		m.connectToDatabase();
//		m.populate();
//		System.out.println(m.fabrics);
//		System.out.println(m.nodes);
//		System.out.println(m.files);
//	}
	
	@ResponseBody
	@RequestMapping(value="/connectDB", method=RequestMethod.POST)
	public void connectToDatabase() {
		// connects with server
		@SuppressWarnings("resource")
		MongoClient mongoClient = new MongoClient("localhost", 27017);
		//System.out.println("Successfully connected to server");

		// connects with Database
		database = mongoClient.getDatabase("ADS_DB");
		//System.out.println("Connected to database " + database.getName());

		// creates Collection
		String colName = "ADS_COL";
		collection = database.getCollection(colName);
		//System.out.println("Accessed collection " + colName);
	}
	
	public void populate() {
		Document filter = new Document();
		MongoCursor<Document> cursor = collection.find(filter).iterator();
		while (cursor.hasNext()) {
			Document doc = cursor.next();
			paths.add(doc.getString("path").substring(doc.getString("path").indexOf(doc.getString("environment"))));
		}
		for (String s : paths) {
			String env=s.substring(0,s.indexOf('/'));
			String fab=s.substring(env.length()+1,env.length()+1+s.substring(env.length()+1).indexOf('/'));
			String nod=s.substring(env.length()+fab.length()+2,s.lastIndexOf('/'));
			String fil=s.substring(s.lastIndexOf('/')+1);
			if(fabrics.containsKey(env)) {
				Set<String> fabs=fabrics.get(env);
				fabs.add(fab);
				fabrics.put(env,fabs);
			}
			else {
				Set<String> fabs=new TreeSet<String>();
				fabs.add(fab);
				fabrics.put(env,fabs);
			}
			
			if(nodes.containsKey(fab)) {
				Set<String> nods=nodes.get(fab);
				nods.add(nod);
				nodes.put(env+"."+fab,nods);
			}
			else {
				Set<String> nods=new TreeSet<String>();
				nods.add(nod);
				nodes.put(env+"."+fab,nods);
			}
			
			if(files.containsKey(nod)) {
				Set<String> fils=files.get(nod);
				fils.add(fil);
				files.put(env+"."+fab+"."+nod,fils);
			}
			else {
				Set<String> fils=new TreeSet<String>();
				fils.add(fil);
				files.put(env+"."+fab+"."+nod,fils);
			}
		}
		Set<String> allFab=new HashSet<String>();
		Iterator<Set<String>> fab=fabrics.values().iterator();
		while(fab.hasNext()) {
			Iterator<String> info = fab.next().iterator();
			while(info.hasNext()) {
				allFab.add(info.next());
			}
		}
		fabrics.put("*", allFab);
		Set<String> allNod=new HashSet<String>();
		Iterator<Set<String>> nod=nodes.values().iterator();
		while(nod.hasNext()) {
			Iterator<String> info = nod.next().iterator();
			while(info.hasNext()) {
				allNod.add(info.next());
			}
		}
		nodes.put("*", allNod);
		Set<String> allFil=new HashSet<String>();
		Iterator<Set<String>> fil=files.values().iterator();
		while(fil.hasNext()) {
			Iterator<String> info = fil.next().iterator();
			while(info.hasNext()) {
				allFil.add(info.next());
			}
		}
		nodes.put("*", allNod);
	}
}