package net.minespree.analytics;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.net.URLCodec;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @since 12/10/2017
 */
public class AnalyticsClient {
    public final static String DESIGN_CATEGORY = "design";
    public final static String ERROR_CATEGORY = "error";
    public final static String USER_CATEGORY = "user";
    public final static String BUSINESS_CATEGORY = "business";

    private final static String API_HOOK = "http://api.gameanalytics.com/1";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    @Getter
    private static AnalyticsClient client = null;
    private final OkHttpClient httpClient = new OkHttpClient();
    private String gameKey;
    private String secretKey;
    private Map<String, String> sessions = new HashMap<>();
    @Getter
    @Setter
    private String build;

    private AnalyticsClient(String gameKey, String secretKey) {
        this.gameKey = gameKey;
        this.secretKey = secretKey;
    }

    public static void createClient(String gameKey, String secretKey) {
        if (client != null)
            throw new IllegalStateException("already instanced");

        client = new AnalyticsClient(gameKey, secretKey);
    }

    public void shutdown() {
        client = null;
        sessions.clear();
    }

    private String getAuthenticationHash(String payload) {
        return DigestUtils.md5Hex(payload + secretKey);
    }

    String getSession(String userId) {
        if (!sessions.containsKey(userId)) {
            UUID session = UUID.randomUUID();
            String sessionStr = session.toString();
            sessions.put(userId, sessionStr);
            return sessionStr;
        }

        return sessions.get(userId);
    }

    public void endSession(String userId) {
        sessions.remove(userId);
    }

    void sendAsync(String category, String payload, Callback callback) {
        createCall(category, payload).enqueue(callback);
    }

    JSONObject send(String category, String payload) throws Exception {
        try {
            String response = createCall(category, payload).execute().body().string();

            if (!response.startsWith("{") || !response.endsWith("}")) {
                throw new IllegalArgumentException("invalid json");
            }

            return new JSONObject(response);
        } catch (IOException | JSONException e) {
            throw new Exception(e);
        }
    }

    private Call createCall(String category, String payload) {
        URLCodec codec = new URLCodec("UTF-8");

        String urlParams;
        try {
            urlParams = "/" + codec.encode(gameKey) + "/" + codec.encode(category);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        Request request = new Request.Builder()
                .url(API_HOOK + urlParams)
                .header("Authorization", getAuthenticationHash(payload))
                .post(RequestBody.create(JSON, payload))
                .build();

        return httpClient.newCall(request);
    }

    public AnalyticsEvent event(String category) {
        return new AnalyticsEvent(this, category, build);
    }
}
