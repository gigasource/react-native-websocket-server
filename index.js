import { NativeModules, NativeEventEmitter, Platform } from 'react-native';
import EventEmitter from 'eventemitter3';
import Socket from "./Socket";
const { RNWebsocketServer } = NativeModules;
const nativeEventEmitter = new NativeEventEmitter(NativeModules.RNWebsocketServer);

export default class WebsocketServer extends EventEmitter {
    constructor (ipAddress, port = 3770) {
        super();
        this.started = false;
        this.ipAddress = ipAddress;
        this.port = port;
        this.nativeEventEmitter = nativeEventEmitter;
        this.socketMap = {};
        this._registerEvents();
    }

    /**
     * Starts websocket server
     */
    start () {
        if (this.started) return;
        this.started = true;
        RNWebsocketServer.start(this.ipAddress, this.port);
    }

    /**
     * Stops/closes websocket server
     */
    stop () {
        RNWebsocketServer.stop();
    }

    write(payload) {
        RNWebsocketServer.write(payload);
    }

    _registerEvents() {
        console.log('Registering events');
        this.nativeEventEmitter.addListener('connection', (evt) => {
            this.socketMap[evt.id] = new Socket(evt.id);
            this.emit('connection', this.socketMap[evt.id]);
        })
        this.nativeEventEmitter.addListener('message', (evt) => {
            const socket = this.socketMap[evt.id];
            if (socket) {
                socket.emit('message', Platform.OS === 'ios' ? evt : JSON.parse(evt.payload));
            }
        })
        this.nativeEventEmitter.addListener('disconnected', (evt) => {
            const socket = this.socketMap[evt.id];
            if (socket) {
                socket.emit('disconnected');
                delete this.socketMap[evt.id];
            }
        })
    }
}
