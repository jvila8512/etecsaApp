import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
import { Observable } from 'rxjs';
import { Storage } from 'react-jhipster';

/**
 * Conexión WebSocket al tópico /topic/dashboard.
 * El backend envía cada 5 segundos la lista de equipos con su estado y variables.
 */
let stompClient: any = null;
let subscriber: any = null;
let connection: Promise<any>;
let connectedPromise: ((v: any) => void) | null = null;
let listener: Observable<any>;
let listenerObserver: any;
let alreadyConnectedOnce = false;

const createConnection = (): Promise<any> => new Promise(resolve => (connectedPromise = resolve));
const createListener = (): Observable<any> => new Observable(observer => (listenerObserver = observer));

const subscribeToDashboard = (): void => {
  connection.then(() => {
    if (!stompClient) return;
    subscriber = stompClient.subscribe('/topic/dashboard', (data: any) => {
      listenerObserver.next(JSON.parse(data.body));
    });
  });
};

export const connectDashboard = (): void => {
  if (connectedPromise !== null || alreadyConnectedOnce) return;
  connection = createConnection();
  listener = createListener();

  const loc = window.location;
  const baseElement = document.querySelector('base');
  const baseHref = baseElement ? baseElement.getAttribute('href')?.replace(/\/$/, '') : '';
  let url = `//${loc.host}${baseHref}/websocket/dashboard`;
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
      subscribeToDashboard();
    },
    (err: any) => {
      console.error('Error conectando al dashboard:', err);
      connectedPromise = null;
      alreadyConnectedOnce = false;
    },
  );
};

export const disconnectDashboard = (): void => {
  if (stompClient) {
    if (stompClient.connected) stompClient.disconnect();
    stompClient = null;
  }
  alreadyConnectedOnce = false;
};

export const receiveDashboard = (): Observable<any> => listener;
