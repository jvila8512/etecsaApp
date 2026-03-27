// src/app/websocket/generadores-websocket.ts

import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';

import { Observable, Observer } from 'rxjs';
import { Storage } from 'react-jhipster';

// ==================== TIPOS ====================
interface GeneradoresMessage {
  type: string;
  payload: any;
}

interface StompClient {
  connected: boolean;
  connect(headers: any, connectCallback: () => void, errorCallback?: (error: any) => void): void;
  disconnect(): void;
  subscribe(destination: string, callback: (message: any) => void): any;
  send(destination: string, body: string, headers: any): void;
}

// ==================== VARIABLES GLOBALES ====================
let generadoresStompClient: StompClient | null = null;
let generadoresSubscriber: any = null;
let generadoresConnection: Promise<any>;
let generadoresConnectedPromise: ((value: any) => void) | null = null;
let generadoresListener: Observable<GeneradoresMessage>;
let generadoresListenerObserver: Observer<GeneradoresMessage>;
let generadoresAlreadyConnectedOnce = false;

// ==================== CONEXIÓN ====================
const createGeneradoresConnection = (): Promise<any> =>
  new Promise(resolve => {
    generadoresConnectedPromise = resolve;
  });

const createGeneradoresListener = (): Observable<GeneradoresMessage> =>
  new Observable(observer => {
    generadoresListenerObserver = observer;
  });

// ==================== SUSCRIPCIÓN A TOPICS ====================
const subscribeToGeneradoresTopics = (): void => {
  generadoresConnection.then(() => {
    if (!generadoresStompClient) return;

    // Topic 1: Datos en tiempo real
    generadoresSubscriber = generadoresStompClient.subscribe('/topic/generadores/tiempo-real', (data: any) => {
      generadoresListenerObserver.next({
        type: 'TIEMPO_REAL',
        payload: JSON.parse(data.body),
      });
    });

    // Topic 2: Respuestas de comandos
    generadoresStompClient.subscribe('/topic/generadores/comandos', (data: any) => {
      generadoresListenerObserver.next({
        type: 'COMANDO_EJECUTADO',
        payload: JSON.parse(data.body),
      });
    });

    // Topic 3: Alertas
    generadoresStompClient.subscribe('/topic/generadores/alertas', (data: any) => {
      generadoresListenerObserver.next({
        type: 'ALERTA_RECIBIDA',
        payload: JSON.parse(data.body),
      });
    });

    // console.log('📡 Suscrito a topics de generadores');
  });
};

// ==================== ENVÍO DE COMANDOS ====================
export const sendGeneradorCommand = (commandType: string, payload: any): void => {
  console.warn('[GENERADORES WS] Intentando enviar comando:', commandType, payload);

  // Si ya está conectado, enviar inmediatamente
  if (generadoresStompClient?.connected) {
    console.warn('[GENERADORES WS] Ya conectado, enviando...');
    enviarComando(commandType, payload);
    return;
  }

  console.warn('[GENERADORES WS] No conectado, esperando conexión...');
  // Si hay una conexión en progreso, esperar
  generadoresConnection
    ?.then(() => {
      console.warn('[GENERADORES WS] Conexión establecida, enviando...');
      if (generadoresStompClient?.connected) {
        enviarComando(commandType, payload);
      }
    })
    .catch(() => {
      console.warn('[GENERADORES WS] Error en conexión');
    });
};

const enviarComando = (commandType: string, payload: any): void => {
  if (!generadoresStompClient) return;

  switch (commandType) {
    case 'CONECTAR_GRUPO':
      generadoresStompClient.send('/app/generadores/connect', JSON.stringify(payload), {});
      break;

    case 'ESCRIBIR_COIL':
      generadoresStompClient.send(
        '/app/generadores/writeCoil',
        JSON.stringify({
          generatorId: payload.grupoId || payload.generatorId,
          address: payload.direccion || payload.address,
          booleanValue: payload.valor || payload.booleanValue,
        }),
        {},
      );
      break;

    case 'ESCRIBIR_REGISTRO':
      generadoresStompClient.send(
        '/app/generadores/writeRegister',
        JSON.stringify({
          generatorId: payload.grupoId || payload.generatorId,
          address: payload.direccion || payload.address,
          value: payload.valor || payload.value,
        }),
        {},
      );
      break;

    case 'LECTURA_INMEDIATA':
      generadoresStompClient.send('/app/generadores/leer-ahora', JSON.stringify(payload), {});
      break;

    default:
      console.warn('Tipo de comando desconocido:', commandType);
  }
};

// ==================== CONEXIÓN WEBSOCKET ====================
const connectGeneradores = (): void => {
  if (generadoresConnectedPromise !== null || generadoresAlreadyConnectedOnce) {
    return;
  }

  generadoresConnection = createGeneradoresConnection();
  generadoresListener = createGeneradoresListener();

  // Construir URL (mismo patrón que JHipster)
  const loc = window.location;
  const baseElement = document.querySelector('base');
  const baseHref = baseElement ? baseElement.getAttribute('href')?.replace(/\/$/, '') : '';

  const headers = {};
  let url = `//${loc.host}${baseHref}/websocket/generadores`;

  // Usar token de autenticación (opcional)
  const authToken = Storage.local.get('jhi-authenticationToken') || Storage.session.get('jhi-authenticationToken');
  if (authToken) {
    url += `?access_token=${authToken}`;
  }

  const socket = new SockJS(url);
  generadoresStompClient = Stomp.over(socket, { protocols: ['v12.stomp'] }) as StompClient;

  generadoresStompClient.connect(
    headers,
    () => {
      // Conexión exitosa
      if (generadoresConnectedPromise) {
        generadoresConnectedPromise('success');
      }
      generadoresConnectedPromise = null;
      generadoresAlreadyConnectedOnce = true;

      subscribeToGeneradoresTopics();
    },
    (error: any) => {
      // Error de conexión
      console.error('❌ Error conectando al WebSocket de generadores:', error);
      generadoresConnectedPromise = null;
      generadoresAlreadyConnectedOnce = false;
    },
  );
};

// ==================== DESCONEXIÓN ====================
const disconnectGeneradores = (): void => {
  if (generadoresStompClient !== null) {
    if (generadoresStompClient.connected) {
      generadoresStompClient.disconnect();
    }
    generadoresStompClient = null;
  }
  generadoresAlreadyConnectedOnce = false;
};

// ==================== RECEPCIÓN DE MENSAJES ====================
const receiveGeneradoresMessages = (): Observable<GeneradoresMessage> => generadoresListener;

const unsubscribeGeneradores = (): void => {
  if (generadoresSubscriber !== null) {
    generadoresSubscriber.unsubscribe();
  }
  generadoresListener = createGeneradoresListener();
};

// ==================== EXPORTAR FUNCIONES PÚBLICAS ====================
export { connectGeneradores, disconnectGeneradores, receiveGeneradoresMessages, unsubscribeGeneradores };
