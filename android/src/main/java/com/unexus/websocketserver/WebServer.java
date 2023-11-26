package com.unexus.websocketserver;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by umsi on 27/11/2017.
 */

public class WebServer extends WebSocketServer {
    private DeviceEventManagerModule.RCTDeviceEventEmitter rctEvtEmitter;
    private HashMap<WebSocket, Integer> connMap;
    private HashMap<Integer, WebSocket> clientIdsMap;
    private int clientSocketIds = 0;
    public WebServer(InetSocketAddress inetSocketAddress, ReactApplicationContext reactContext) {
        super(inetSocketAddress);
        connMap = new HashMap<>();
        clientIdsMap = new HashMap<>();
        rctEvtEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        try {
            String jsonString = (new JSONObject()).put("type", "onMessage")
                    .put("data", conn.getRemoteSocketAddress().getHostName() + " entered the room")
                    .toString();
            int clientId = getNewClientId();
            connMap.put(conn, clientId);
            clientIdsMap.put(clientId, conn);

            WritableMap eventParams = Arguments.createMap();
            eventParams.putInt("id", clientId);
            sendEvent("connection", eventParams);

            broadcast(jsonString);
        } catch (JSONException e) {
            broadcast(e.getMessage());
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        try {
            String jsonString = (new JSONObject()).put("type", "onMessage")
                    .put("data", conn.getRemoteSocketAddress().getHostName() + " has left the room")
                    .toString();
            int clientId = connMap.get(conn);
            clientIdsMap.remove(clientId);
            connMap.remove(conn);
            WritableMap eventParams = Arguments.createMap();
            eventParams.putInt("id", clientId);
            sendEvent("disconnected", eventParams);

            broadcast(jsonString);
        } catch (JSONException e) {
            broadcast(e.getMessage());
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            String jsonString = (new JSONObject()).put("type", "onMessage")
                    .put("data", conn.getRemoteSocketAddress().getHostName() + ": " + message)
                    .toString();
            String messageString = (new JSONObject()).put("data", message).toString();
            int clientId = connMap.get(conn);

            WritableMap eventParams = Arguments.createMap();
            eventParams.putInt("id", clientId);
            eventParams.putString("payload", messageString);
            sendEvent("message", eventParams);

            broadcast(jsonString);
        } catch (JSONException e) {
            broadcast(e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        try {
            String jsonString = (new JSONObject()).put("type", "onError")
                    .put("data", ex.getMessage())
                    .toString();

            broadcast(jsonString);
        } catch (JSONException e) {
            broadcast(e.getMessage());
        }
    }

    @Override
    public void onStart() {
        try {
            String jsonString = (new JSONObject()).put("type", "onStart")
                    .put("data", "Websocket server now starting...")
                    .toString();

            broadcast(jsonString);
        } catch (JSONException e) {
            broadcast(e.getMessage());
        }
    }

    private int getNewClientId() {
        return clientSocketIds++;
    }

    private void sendEvent(String eventName, WritableMap params) {
        rctEvtEmitter.emit(eventName, params);
    }

    public void write(int id, String payload) {
        WebSocket conn = clientIdsMap.get(id);
        conn.send(payload);
    }

    public void setNewContext(ReactApplicationContext reactContext) {
        rctEvtEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);
    }
}

