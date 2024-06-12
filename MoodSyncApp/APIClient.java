import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class APIClient {

    private static final String API_KEY = "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";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public static String getChatbotResponse(String conversationContext) {
        try {
            // Escape newlines and other special characters in conversationContext
            String escapedContext = conversationContext.replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\"");

            String requestBody = "{"
                    + "\"messages\": [{\"role\": \"user\", \"content\": \"" + escapedContext + "\"}],"
                    + "\"model\": \"mixtral-8x7b-32768\","
                    + "\"temperature\": 1,"
                    + "\"max_tokens\": 32768,"
                    + "\"top_p\": 1,"
                    + "\"stream\": false," // Set stream to false to receive complete response
                    + "\"stop\": null"
                    + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Debugging: Print the response body
            System.out.println("Response body: " + response.body());

            // Parse JSON response
            JSONObject jsonResponse = new JSONObject(response.body());
            if (!jsonResponse.has("choices")) {
                throw new JSONException("JSONObject[\"choices\"] not found.");
            }
            JSONArray choicesArray = jsonResponse.getJSONArray("choices");
            JSONObject firstChoice = choicesArray.getJSONObject(0);
            JSONObject messageObject = firstChoice.getJSONObject("message");
            String content = messageObject.getString("content");

            // Return the content
            return content;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error: Unable to get response from the chatbot.";
        } catch (JSONException e) {
            e.printStackTrace();
            return "Error: Unexpected response format.";
        }
    }
}
