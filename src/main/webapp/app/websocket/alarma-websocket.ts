import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
import { Observable } from 'rxjs';
import { Storage } from 'react-jhipster';

/**
 * Conexión WebSocket al tópico /topic/alarmas.
 * El backend envía notificaciones cuando se crea/reconoce/resuelve una alarma.
 */
let stompClient: any = null;
let subscriberNueva: any = null;
let subscriberReconocida: any = null;
let subscriberResuelta: any = null;
let connection: Promise<any>;
let connectedPromise: ((v: any) => void) | null = null;
let listenerNueva: Observable<any>;
let listenerReconocida: Observable<any>;
let listenerResuelta: Observable<any>;
let listenerNuevaObserver: any;
let listenerReconocidaObserver: any;
let listenerResueltaObserver: any;
let alreadyConnectedOnce = false;

const createConnection = (): Promise<any> => new Promise(resolve => (connectedPromise = resolve));
const createListenerNueva = (): Observable<any> => new Observable(observer => (listenerNuevaObserver = observer));
const createListenerReconocida = (): Observable<any> => new Observable(observer => (listenerReconocidaObserver = observer));
const createListenerResuelta = (): Observable<any> => new Observable(observer => (listenerResueltaObserver = observer));

const subscribeToAlarmas = (): void => {
  connection.then(() => {
    if (!stompClient) return;

    subscriberNueva = stompClient.subscribe('/topic/alarmas/nueva', (data: any) => {
      listenerNuevaObserver.next(JSON.parse(data.body));
    });

    subscriberReconocida = stompClient.subscribe('/topic/alarmas/reconocida', (data: any) => {
      listenerReconocidaObserver.next(JSON.parse(data.body));
    });

    subscriberResuelta = stompClient.subscribe('/topic/alarmas/resuelta', (data: any) => {
      listenerResueltaObserver.next(JSON.parse(data.body));
    });
  });
};

export const connectAlarmas = (): void => {
  if (connectedPromise !== null || alreadyConnectedOnce) return;
  connection = createConnection();
  listenerNueva = createListenerNueva();
  listenerReconocida = createListenerReconocida();
  listenerResuelta = createListenerResuelta();

  const loc = window.location;
  const baseElement = document.querySelector('base');
  const baseHref = baseElement ? baseElement.getAttribute('href')?.replace(/\/$/, '') : '';
  let url = `//${loc.host}${baseHref}/websocket/alarmas`;
  const authToken = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
  if (authToken) url += `?access_token=${authToken}`;

  const socket = new SockJS(url);
  stompClient = Stomp.over(socket, { protocols: ['v12.stomp'] });

  stompClient.connect(
    {},
    () => {
      connectedPromise?.('success');
      connectedPromise = null;
      alreadyConnectedOnce = true;
      subscribeToAlarmas();
    },
    (err: any) => {
      console.error('Error conectando al WebSocket de alarmas:', err);
      connectedPromise = null;
      alreadyConnectedOnce = false;
    },
  );
};

export const disconnectAlarmas = (): void => {
  if (stompClient) {
    if (stompClient.connected) {
      if (subscriberNueva) subscriberNueva.unsubscribe();
      if (subscriberReconocida) subscriberReconocida.unsubscribe();
      if (subscriberResuelta) subscriberResuelta.unsubscribe();
      stompClient.disconnect();
    }
    stompClient = null;
  }
  alreadyConnectedOnce = false;
};

export const receiveNuevaAlarma = (): Observable<any> => listenerNueva;
export const receiveAlarmaReconocida = (): Observable<any> => listenerReconocida;
export const receiveAlarmaResuelta = (): Observable<any> => listenerResuelta;
