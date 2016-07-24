package importer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.xml.sax.SAXException;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;

import info.pavie.basicosmparser.controller.OSMParser;
import info.pavie.basicosmparser.model.Element;
import info.pavie.basicosmparser.model.Node;

/**
 * A simple utility that takes an OpenStreetMap OSM/XML export file and inserts it
 * into MongoDB.
 * 
 * Refer to:
 * https://github.com/PanierAvide/BasicOSMParser
 * 
 * 
 * @author johnny
 *
 */
public class Main {

	//private static final String DB_HOST = "mongodb://xxxx:xxxx@ds023315.mlab.com:23315/coffeeshop";
	private static final String DB_HOST = "mongodb://localhost/coffeeshop";

	public static void main(String[] args) {
		OSMParser p = new OSMParser();
		File osmFile = new File("src/main/resources/sample-data/sample_data.osm");

		try {

		    Map<String,Element> result = p.parse(osmFile);
		    Map<String,Node> found = new HashMap<>();
		    
		    if (result == null) {
		    	System.out.println("null....");
		    }
		    else {
		    	result.keySet().forEach(key -> {
		    		Element element = result.get(key);
		    		// not necessary if you get the export right :)
		    		if (element instanceof Node) {
			    		Map<String, String> tags = element.getTags();
			    		// again, no need for these checks if the export only gets the required tags
			    		// - but getting that working isn't the easiest task...
			    		if (tags.containsKey("amenity") && tags.get("amenity").equalsIgnoreCase("cafe")) {
			    			found.put(key, (Node) element);
			    		}
			    		if (tags.containsKey("cuisine") && tags.get("cuisine").equalsIgnoreCase("coffee_shop")) {
			    			found.put(key, (Node) element);
			    		}
		    		}
		    	});

		    }
		    
		    MongoClientURI uri = new MongoClientURI(DB_HOST);
	        MongoClient client = new MongoClient(uri);
	        MongoDatabase db = client.getDatabase("coffeeshop");

	        MongoCollection<Document> collection = db.getCollection("coffeeshops");
	        collection.drop();
	        
			List<Document> documents = new LinkedList<>();

		    found.keySet().forEach(key -> {
		    	
		    	Node x = found.get(key);
		    	if (x.getTags().containsKey("name")) {
			    	Position coordinate = new Position(x.getLon(), x.getLat());
					Point point = new Point(coordinate );
					Document document = new Document("openStreetMapId", x.getId())
							.append("location", point)
							.append("timestamp", x.getTimestamp())
							.append("user", x.getUser())
							.append("node", key);

					x.getTags().keySet().forEach(tag-> {
						document.append(tag.replaceAll("\\.", "_"), x.getTags().get(tag).toString());
					});
					
					documents.add(document);
					//collection.insertOne(document);
					//System.out.println(x);
		    		
		    	}
		    });
		    
			collection.insertMany(documents);
		    System.out.println(collection.count() + " records inserted");
		    collection.createIndex(new Document("location", "2dsphere"));
		    
		    client.close();

		} catch (IOException | SAXException e) {
		    e.printStackTrace();
		}
	}

}
