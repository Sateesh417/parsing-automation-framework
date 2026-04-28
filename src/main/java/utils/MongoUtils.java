package utils;

import com.mongodb.client.*;
import constants.Config;
import io.qameta.allure.Step;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class MongoUtils {

    // ✅ Create only once
    private static final MongoClient client =
            MongoClients.create(Config.MONGO_URL);

    private static final MongoDatabase db =
            client.getDatabase(Config.getMongoDb());

    private static final MongoCollection<Document> collection =
            db.getCollection(Config.COLLECTION);


    // ✅ MAIN METHOD (USE THIS ONLY)
    @Step("Fetch parsed data from Mongo fallback")
    public static Document getJobByReportId(String reportId) {

        System.out.println("🔍 Querying Mongo with report_id: " + reportId);

        return collection.find(eq("report_id", reportId)).first();
    }
}