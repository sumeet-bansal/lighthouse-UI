package controller;

import org.springframework.web.bind.annotation.*;

import com.mongodb.MongoClient;
import com.mongodb.client.*;

import org.bson.Document;

public class MongoConnector {
	
	static final String DB_NAME = "LH_DB";
	static final String COL_NAME = "LH_COL";
	static final int DEFAULT_PORT = 27017;

	private static MongoClient client;
	private static MongoDatabase database;
	private static MongoCollection<Document> collection;

	/**
	 * Checks connection and initializes the cache if successful.
	 */
	@ResponseBody
	@RequestMapping(value="/connectDB", method=RequestMethod.POST)
	public static void connectToDatabase() {
		System.out.println("\n[DATABASE MESSAGE] Connecting to database...");

		// check connection
		MongoClient ping = new MongoClient();
		MongoDatabase db = ping.getDatabase("ping");
		db.drop();
		ping.close();

		// connects with server
		client = new MongoClient("localhost", DEFAULT_PORT);
		System.out.println("[DATABASE MESSAGE] Server connection successful @ localhost:" + DEFAULT_PORT);

		// connects with Database
		database = client.getDatabase(DB_NAME);

		// creates Collection
		collection = database.getCollection(COL_NAME);
		System.out.println(
				"[DATABASE MESSAGE] Database connection successful @ " + DB_NAME + "." + COL_NAME);
	}
	
	/**
	 * Disconnects the Mongo connection safely.
	 */
	public static void disconnect() {
		client.close();
	}
	
	public static DirTree populate() {
		DirTree tree = new DirTree();
		MongoCursor<String> cursor = collection.distinct("path", String.class).iterator();
		while (cursor.hasNext()) {
			tree.insert(cursor.next());
		}
		return tree;
	}
}