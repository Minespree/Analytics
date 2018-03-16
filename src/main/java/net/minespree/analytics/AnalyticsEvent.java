package net.minespree.analytics;

import com.squareup.okhttp.Callback;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @since 12/10/2017
 */
public class AnalyticsEvent {
    private AnalyticsClient client = null;

    private JSONArray events = new JSONArray();
    private JSONObject event = null;
    private String category;
    private String build;

    AnalyticsEvent(AnalyticsClient client, String category, String build) {
        this.client = client;
        this.category = category;
        this.build = build;
    }

    public AnalyticsEvent newEvent(String userId) {
        return newEvent(userId, client.getSession(userId));
    }

    private AnalyticsEvent newEvent(String userId, String sessionId) {
        if (event != null) {
            terminateEvent();
        }

        event = new JSONObject();
        event.put("user_id", userId);
        event.put("session_id", sessionId);
        event.put("build", build);
        return this;
    }

    public void terminateEvent() {
        events.put(event);
        event = null;
    }

    public JSONArray getEvents() {
        return events;
    }

    public void sendAsync(Callback callback) {
        if (event != null) {
            terminateEvent();
        }
        client.sendAsync(category, events.toString(), callback);
    }

    public JSONObject send() throws Exception {
        if (event != null) {
            terminateEvent();
        }
        return client.send(category, events.toString());
    }

    public AnalyticsEvent withEventId(String eventId) {
        event.put("event_id", eventId);

        return this;
    }

    public AnalyticsEvent withArea(String area) {
        event.put("area", area);

        return this;
    }

    public AnalyticsEvent withValue(float value) {
        event.put("value", value);
        return this;
    }

    public AnalyticsEvent withBusiness(String currency, int amount) {
        event.put("currency", currency);
        event.put("amount", amount);

        return this;
    }

    public AnalyticsEvent withSource(String currency, float amount, String gainedFrom, String added) {
        event.put("flowType", "source");
        event.put("currency", currency);
        event.put("amount", amount);
        event.put("itemType", gainedFrom);
        event.put("itemId", added);

        return this;
    }

    public AnalyticsEvent withSink(String currency, float amount, String category, String gained) {
        event.put("flowType", "sink");
        event.put("currency", currency);
        event.put("amount", amount);
        event.put("itemType", category);
        event.put("itemId", gained);

        return this;
    }
}
