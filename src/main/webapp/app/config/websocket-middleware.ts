import SockJS from 'sockjs-client';

import Stomp from 'webstomp-client';
import { Observable } from 'rxjs';
import { Storage } from 'react-jhipster';

import { websocketActivityMessage } from 'app/modules/administration/administration.reducer';
import { getAccount, logoutSession } from 'app/shared/reducers/authentication';

let stompClient = null;

let subscriber = null;
let connection: Promise<any>;
let connectedPromise: any = null;
let listener: Observable<any>;
let listenerObserver: any;
let alreadyConnectedOnce = false;

const createConnection = (): Promise<any> => new Promise(resolve => (connectedPromise = resolve));

const createListener = (): Observable<any> =>
  new Observable(observer => {
    listenerObserver = observer;
  });

export const sendActivity = (page: string) => {
  console.warn('TRACKER sendActivity - Enviando a /app/activity:', page);
  connection?.then(() => {
    stompClient?.send(
      '/app/activity', // destination - con prefijo /app
      JSON.stringify({ page }), // body
      {}, // header
    );
  });
};

const subscribe = () => {
  console.warn('TRACKER - Intentando suscribir a /topic/tracker');
  connection.then(() => {
    console.warn('TRACKER - Conexión lista, creando suscripción...');
    subscriber = stompClient.subscribe('/topic/tracker', data => {
      console.warn('TRACKER - MENSAJE RECIBIDO del topic:', data.body);
      listenerObserver.next(JSON.parse(data.body));
    });
    console.warn('TRACKER - Suscrito correctamente');
  });
};

const connect = () => {
  if (connectedPromise !== null || alreadyConnectedOnce) {
    console.warn('TRACKER - Ya conectado o en proceso, retorna');
    // Ya conectado, enviar actividad de la página actual
    if (stompClient?.connected) {
      sendActivity(window.location.pathname);
    }
    return;
  }
  console.warn('TRACKER - Iniciando conexión WebSocket...');
  connection = createConnection();
  listener = createListener();

  const loc = window.location;
  const baseHref = document.querySelector('base').getAttribute('href').replace(/\/$/, '');

  const headers = {};
  let url = `//${loc.host}${baseHref}/websocket/tracker`;
  const authToken = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
  if (authToken) {
    url += `?access_token=${authToken}`;
  }
  console.warn('TRACKER - URL:', url);

  const socket = new SockJS(url);
  stompClient = Stomp.over(socket, { protocols: ['v12.stomp'] });

  stompClient.connect(
    headers,
    () => {
      console.warn('TRACKER - Conexión exitosa!');
      connectedPromise('success');
      connectedPromise = null;
      sendActivity(window.location.pathname);
      alreadyConnectedOnce = true;
    },
    error => {
      console.warn('TRACKER - Error de conexión:', error);
    },
  );
};

// Funciones para detectar navegación y enviar actividad
let currentPath = window.location.pathname;

export const setupTrackerOnNavigate = () => {
  // Override pushState para detectar navegación
  const originalPushState = window.history.pushState;
  window.history.pushState = function (...args) {
    originalPushState.apply(window.history, args);
    const newPath = window.location.pathname;
    if (newPath !== currentPath && stompClient?.connected) {
      console.warn('TRACKER - Navegación detectada:', currentPath, '->', newPath);
      sendActivity(newPath);
      currentPath = newPath;
    }
  };

  // También escuchar popstate (botones atrás/adelante del navegador)
  window.addEventListener('popstate', () => {
    const newPath = window.location.pathname;
    if (newPath !== currentPath && stompClient?.connected) {
      console.warn('TRACKER - Navegación (popstate):', currentPath, '->', newPath);
      sendActivity(newPath);
      currentPath = newPath;
    }
  });
};

const disconnect = () => {
  if (stompClient !== null) {
    if (stompClient.connected) {
      stompClient.disconnect();
    }
    stompClient = null;
  }
  alreadyConnectedOnce = false;
};

const receive = () => listener;

const unsubscribe = () => {
  if (subscriber !== null) {
    subscriber.unsubscribe();
  }
  listener = createListener();
};

export default store => next => action => {
  if (getAccount.fulfilled.match(action)) {
    const authorities = action.payload?.data?.authorities || [];
    const isAdmin = authorities.includes('ROLE_ADMIN') || authorities.includes('ADMIN');
    console.warn('TRACKER - isAdmin:', isAdmin);
    if (!alreadyConnectedOnce && isAdmin) {
      console.warn('TRACKER - Conectando y suscribiendo...');
      connect();
      subscribe();
      receive().subscribe(activity => {
        console.warn('TRACKER - Activity recibida:', activity);
        store.dispatch(websocketActivityMessage(activity));
      });
      setupTrackerOnNavigate(); // Agregar detector de navegación
    } else if (!alreadyConnectedOnce) {
      console.warn('TRACKER - No admin, solo conectando...');
      connect();
    }
  } else if (getAccount.rejected.match(action) || action.type === logoutSession().type) {
    unsubscribe();
    disconnect();
  }
  return next(action);
};

export const manuallySubscribeToTracker = () => {
  console.warn('TRACKER - Suscripción manual iniciada');
  subscribe();
  receive().subscribe(activity => {
    console.warn('TRACKER - Activity (manual):', activity);
  });
};
