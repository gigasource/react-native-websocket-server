package com.unexus.websocketserver;

import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketListener;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.Handshakedata;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;

/**
 * Created by umsi on 27/11/2017.
 */

public class RNWebsocketServerModule extends ReactContextBaseJavaModule {
    private static WebServer webServer = null;
    private final ReactApplicationContext mReactContext;
    public RNWebsocketServerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RNWebsocketServer";
    }

    @ReactMethod
    public void start(String ipAddress, int port) throws IOException, InterruptedException {
        if (RNWebsocketServerModule.webServer == null) {
            InetSocketAddress inetSocketAddress = new InetSocketAddress(port);

            RNWebsocketServerModule.webServer = new WebServer(inetSocketAddress, mReactContext);

            RNWebsocketServerModule.webServer.start();
        } else {
            RNWebsocketServerModule.webServer.setNewContext(mReactContext);
            RNWebsocketServerModule.webServer.start();
        }
    }

    @ReactMethod
    public void stop() throws IOException, InterruptedException {
        RNWebsocketServerModule.webServer.stop();
    }

    @ReactMethod
    public void write(int id, String payload) {
        RNWebsocketServerModule.webServer.write(id, payload);
    }
}
