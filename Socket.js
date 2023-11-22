import EventEmitter from 'eventemitter3';
import { NativeModules } from 'react-native';
const RNWebsocketServer = NativeModules.RNWebsocketServer;

export default class Socket extends EventEmitter {
  constructor(id) {
    super();
    this.id = id;
  }

  send(payload) {
    RNWebsocketServer.write(this.id, payload);
  }
}
