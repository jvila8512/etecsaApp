import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
import { Observable } from 'rxjs';
import { Storage } from 'react-jhipster';

/**
 * Conexión WebSocket para el dashboard.
 * Suscribe a dos tópicos:
 * - /topic/dashboard/completo: Todos los equipos cada 5s (para la vista)
 * - /topic/dashboard/actualizaciones: Variables según intervalo, cada 1s (para alarmas)
 */
let stompClient: any = null;
let subscriberCompleto: any = null;
let subscriberActualizaciones: any = null;
let connection: Promise<any>;
let connectedPromise: ((v: any) => void) | null = null;
let listenerCompleto: Observable<any>;
let listenerActualizaciones: Observable<any>;
let listenerCompletoObserver: any;
let listenerActualizacionesObserver: any;
let alreadyConnectedOnce = false;

const createConnection = (): Promise<any> => new Promise(resolve => (connectedPromise = resolve));
const createListenerCompleto = (): Observable<any> => new Observable(observer => (listenerCompletoObserver = observer));
const createListenerActualizaciones = (): Observable<any> => new Observable(observer => (listenerActualizacionesObserver = observer));

const subscribeToDashboard = (): void => {
  connection.then(() => {
    if (!stompClient) return;

    subscriberCompleto = stompClient.subscribe('/topic/dashboard/completo', (data: any) => {
      listenerCompletoObserver?.next(JSON.parse(data.body));
    });

    subscriberActualizaciones = stompClient.subscribe('/topic/dashboard/actualizaciones', (data: any) => {
      listenerActualizacionesObserver?.next(JSON.parse(data.body));
    });
  });
};

export const connectDashboard = (): void => {
  if (alreadyConnectedOnce) return;
  alreadyConnectedOnce = true;

  listenerCompleto = createListenerCompleto();
  listenerActualizaciones = createListenerActualizaciones();
  connection = createConnection();

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
    if (stompClient.connected) {
      if (subscriberCompleto) subscriberCompleto.unsubscribe();
      if (subscriberActualizaciones) subscriberActualizaciones.unsubscribe();
      stompClient.disconnect();
    }
    stompClient = null;
  }
  alreadyConnectedOnce = false;
};

/**
 * Dashboard completo: todos los equipos cada 5 segundos.
 * Usar para la vista del dashboard.
 */
export const receiveDashboardCompleto = (): Observable<any> => listenerCompleto;

/**
 * Actualizaciones rápidas: variables según intervalo cada 1 segundo.
 * Usar para detección de alarmas.
 */
export const receiveDashboardActualizaciones = (): Observable<any> => listenerActualizaciones;

/**
 * @deprecated Usar receiveDashboardCompleto() o receiveDashboardActualizaciones()
 */
export const receiveDashboard = (): Observable<any> => listenerCompleto;
