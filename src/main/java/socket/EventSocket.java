package socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ClientEndpoint
@ServerEndpoint(value = "/task")
public class EventSocket {

    private static List<String> allTokens = new ArrayList<>();
    private Session sess;

    @OnOpen
    public void onWebSocketConnect(Session sess) throws IOException {
        String token = getToken(sess);
        if (allTokens.contains(token)) {
            sess.close();
            System.out.println("ТАКОЙ ТОКЕН УЖЕ ОТКРЫТ!!!");
        } else {
            System.out.println("ТОКЕН ДОБАВЛЕН В БАЗУ: " + token);
            allTokens.add(token);
            this.sess = sess;
        }
    }

    private String getToken(Session sess) {
        Map<String, List<String>> requestParam = sess.getRequestParameterMap();
        return requestParam.get("token").get(0);
    }

    @OnMessage
    public void onWebSocketText(String json) {
        System.out.println("Received TEXT json: " + json);
//        json = "{\"message\":\"start\",\"parameters\":[{\"name\":\"view\",\"value\":\"\"},{\"name\":\"min_id\",\"value\":\"\"},{\"name\":\"q\",\"value\":\"\"},{\"name\":\"search[city_id]\",\"value\":\"\"},{\"name\":\"search[region_id]\",\"value\":\"\"},{\"name\":\"search[district_id]\",\"value\":\"0\"},{\"name\":\"search[dist]\",\"value\":\"0\"},{\"name\":\"search[filter_float_price:from]\",\"value\":\"\"},{\"name\":\"search[filter_float_price:to]\",\"value\":\"\"},{\"name\":\"search[category_id]\",\"value\":\"\"},{\"name\":\"page\",\"value\":\"1\"}]}";

//        System.out.println("MESSAGE FROM: " + sess.getId());

        /*JSONObject obj = new JSONObject(json);
        String message = obj.getString("message");

        switch (message) {
            case "start": {
                System.out.println("Получена команда к старту, ПАРАМЕТРЫ:");
                JSONArray params = obj.getJSONArray("parameters");
                for (int i = 0; i < params.length(); i++) {
                    String name = params.getJSONObject(i).getString("name");
                    String value = params.getJSONObject(i).getString("value");

                    System.out.println("    *" + name + ": " + value);
                }
            }
        }*/
    }

    @OnClose
    public void onWebSocketClose(CloseReason reason) {
        String token = getToken(sess);
        System.out.println("ЗАКРЫВАЮ СОЕДЕНЕНИЕ, TOKEN: " + token);
        allTokens.remove(token);
    }

    @OnError
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }
}