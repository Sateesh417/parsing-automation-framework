package constants;

public class Config {

    // 👉 ENV control
    public static final String ENV = "dev";  // change to "prod" when needed

    // 👉 URLs
    public static final String DEV_URL = "https://medsensedev-c5fbg8htfbhtgqck.centralindia-01.azurewebsites.net";
    public static final String PROD_URL = "https://api.yira.ai";

    // 👉 API Keys
    public static final String DEV_API_KEY = "YOUR_KEY";
    public static final String PROD_API_KEY = "YOUR_KEY";

    // TenantIds
    public static final String PROD_TENANT_ID = "testing-id-1-1ae6";
    public static final String DEV_TENANT_ID = "dev-testing-db64";

    // ProjectIds
    public static final String PROD_PROJECT_ID = "bd760a58-2d44-4089-b471-cc046ea0a70d";
    public static final String DEV_PROJECT_ID = "517abd93-87a9-44a1-93d4-72eb773fa638";

    public static final String WEBHOOK_URL = "https://yirahealthcampapidev.azurewebsites.net/api/Account/webhooktest";

    public static final String DEV_DB = "MedSenseDev";
    public static final String PROD_DB = "MEDPARSER";

    public static final String COLLECTION = "parsing_jobs";

    public static final String MONGO_URL = "mongodb+srv://username:password@cluster...";

    public static String getUploadUrl(String tenantId, String projectId) {
        return getApiPrefix() + "/tenants/" + tenantId + "/projects/" + projectId + "/reports";
    }

    public static String getFinalUrl(String tenantId, String reportId) {
        return getApiPrefix() + "/tenants/" + tenantId + "/reports/" + reportId;
    }

    public static String getPdfPath(String fileName){

        return System.getProperty("user.dir")
                + "/src/test/resources/files/"
                + fileName;
    }
    //  Base URL
    public static String getBaseUrl() {

        switch (ENV.toLowerCase()) {
            case "prod":
                return PROD_URL;
            default:
                return DEV_URL;
        }
    }

    // 👉 API PREFIX (IMPORTANT)
    public static String getApiPrefix() {

        switch (ENV.toLowerCase()) {
            case "prod":
                return "/v1";
            default:
                return "/api/v1";
        }
    }

    //  API KEY
    public static String getApiKey() {

        switch (ENV.toLowerCase()) {
            case "prod":
                return PROD_API_KEY;
            default:
                return DEV_API_KEY;
        }
    }

    // Tenet ID
    public static String getTenantId() {

        switch (ENV.toLowerCase()) {
            case "prod":
                return PROD_TENANT_ID;
            default:
                return DEV_TENANT_ID;
        }
    }

    // Project ID's
    public static String getProjectId() {

        switch (ENV.toLowerCase()) {
            case "prod":
                return PROD_PROJECT_ID;
            default:
                return DEV_PROJECT_ID;
        }
    }

    // DB Name
    public static String getMongoDb() {

        switch (ENV.toLowerCase()) {
            case "prod":
                return PROD_DB;
            default:
                return DEV_DB;
        }
    }



}