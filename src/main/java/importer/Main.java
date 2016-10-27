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
 * A simple utility that takes an OpenStreetMap OSM/XML export file and inserts
 * it into MongoDB.
 * 
 * Refer to: https://github.com/PanierAvide/BasicOSMParser
 * 
 * @author johnny
 *
 */
public class Main {

	// private static final String DB_HOST =
	// "mongodb://xxxx:xxxx@ds023315.mlab.com:23315/coffeeshop";
	private static final String DB_HOST = "mongodb://localhost/coffeeshop";

	public static void main(String[] args) {
		OSMParser p = new OSMParser();
		File osmFile = new File("src/main/resources/sample-data/sample_data.osm");

		try {

			// read the input file
			Map<String, Element> result = p.parse(osmFile);

			if (result == null) {
				System.out.println("Parsing resulted in null");
				return;
			}

			// extract and convert
			Map<String, Node> found = convertInputFile(result);

			// open the database and get the collection
			MongoClientURI uri = new MongoClientURI(DB_HOST);
			MongoClient client = new MongoClient(uri);
			MongoDatabase db = client.getDatabase("coffeeshop");

			MongoCollection<Document> collection = db.getCollection("coffeeshops");
			collection.drop();

			// convert to MongoDB documents
			List<Document> documents = convertToDocuments(found); 

			// add all and create index (required for geospatial search)
			collection.insertMany(documents);
			System.out.println(collection.count() + " records inserted");
			collection.createIndex(new Document("location", "2dsphere"));

			client.close();

		} 
		catch (IOException | SAXException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts to MongoDB {@link Document}s to be inserted into the database.
	 * 
	 * @param found a map of entries to convert, must not be null, but can be empty.
	 * 
	 * @return a {@link List} of {@link Document}
	 */
	private static List<Document> convertToDocuments(Map<String, Node> found) {
		List<Document> documents = new LinkedList<>();

		found.keySet().forEach(key -> {

			Node x = found.get(key);
			if (x.getTags().containsKey("name")) {
				Position coordinate = new Position(x.getLon(), x.getLat());
				Point point = new Point(coordinate);
				Document document = new Document("openStreetMapId", x.getId()).append("location", point)
						.append("timestamp", x.getTimestamp()).append("user", x.getUser()).append("node", key);

				x.getTags().keySet().forEach(tag -> {
					document.append(tag.replaceAll("\\.", "_"), x.getTags().get(tag).toString());
				});

				documents.add(document);
			}
		});

		return documents;
	}

	/**
	 * Extracts all {@link Node}s from the results that are cafes or coffee shops.
	 * Note that this step is redundant if the result set obtained from OpenStreetMap is
	 * correct, and only contains the required data. 
	 *  
	 * @param result the imported OSM file.
	 * @return a {@link Map} of filtered data
	 */
	private static Map<String, Node> convertInputFile(Map<String, Element> result) {
		
		Map<String, Node> found = new HashMap<>();
		
		result.keySet().forEach(key -> {
			Element element = result.get(key);
			// not necessary if you get the export right :)
			if (element instanceof Node) {
				Map<String, String> tags = element.getTags();
				// again, no need for these checks if the export only
				// gets the required tags
				// - but getting that working isn't the easiest task...
				if (tags.containsKey("amenity") && tags.get("amenity").equalsIgnoreCase("cafe")) {
					found.put(key, (Node) element);
				}
				if (tags.containsKey("cuisine") && tags.get("cuisine").equalsIgnoreCase("coffee_shop")) {
					found.put(key, (Node) element);
				}
			}
		});

		return found;
	}

}
