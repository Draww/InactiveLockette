package gvlfm78.plugin.InactiveLockette.updates;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class SpigotUpdateChecker {

    private static final String USER_AGENT  = "InactiveLockette";
    private static final String PROJECT_ID = "25644";
    private static final String VERSIONS_URL = "https://api.spiget.org/v2/resources/" + PROJECT_ID + "/versions?size=15000";
    private static final String UPDATES_URL = "https://api.spiget.org/v2/resources/" + PROJECT_ID + "/updates?size=15000";
    private String latestVersion = "";

    boolean getNewUpdateAvailable(){
        try {
            JSONArray versionsArray = getArray(VERSIONS_URL);
            latestVersion = ((JSONObject) versionsArray.get(versionsArray.size() - 1)).get("name").toString();
            return ILUpdateChecker.shouldUpdate(latestVersion);
        }
        catch (Exception e){
            return false;
        }
    }

    String getUpdateURL(){
        try {
            JSONArray updatesArray = getArray(UPDATES_URL);
            String updateId = ((JSONObject) updatesArray.get(updatesArray.size() - 1)).get("id").toString();
            return "https://www.spigotmc.org/resources/hotels.2047/update?update=" + updateId;
        }
        catch (Exception e){
            return "Error getting update URL";
        }
    }

    String getLatestVersion(){
        return latestVersion;
    }

    private JSONArray getArray(String urlString){
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.addRequestProperty("User-Agent", USER_AGENT); // Set User-Agent

            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);

            return (JSONArray) JSONValue.parseWithException(reader);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
