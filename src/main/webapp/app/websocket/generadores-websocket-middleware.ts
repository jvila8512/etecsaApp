// src/app/websocket/generadores-websocket-middleware.ts (SIN CONSOLE)

import { Middleware, AnyAction } from '@reduxjs/toolkit';
import {
  connectGeneradores,
  disconnectGeneradores,
  receiveGeneradoresMessages,
  unsubscribeGeneradores,
  sendGeneradorCommand,
} from './generadores-websocket';
import { getAccount, logoutSession } from 'app/shared/reducers/authentication';
import {
  generadoresWebSocketConnected,
  generadoresWebSocketDisconnected,
  generadoresDataReceived,
  generadoresCommandSent,
  generadoresAlertReceived,
} from '../shared/reducers/generadores-reducer';

// ==================== TIPOS ====================
interface SendGeneradorCommandAction extends AnyAction {
  type: 'GENERADORES_SEND_COMMAND';
  payload: {
    commandType: string;
    payload: any;
  };
}

interface GeneradoresWebSocketMessage {
  type: string;
  payload: any;
}

// ==================== ACTION CREATOR ====================
export const sendGeneradorCommandAction = (commandType: string, payload: any): SendGeneradorCommandAction => ({
  type: 'GENERADORES_SEND_COMMAND',
  payload: {
    commandType,
    payload,
  },
});

// ==================== MIDDLEWARE SILENCIOSO ====================
const generadoresWebsocketMiddleware: Middleware = store => next => (action: AnyAction) => {
  // 1. CONECTAR cuando el usuario se autentica
  if (getAccount.fulfilled.match(action)) {
    connectGeneradores();

    receiveGeneradoresMessages().subscribe((message: GeneradoresWebSocketMessage) => {
      switch (message.type) {
        case 'TIEMPO_REAL':
          store.dispatch(generadoresDataReceived(message.payload));
          break;
        case 'COMANDO_EJECUTADO':
          store.dispatch(generadoresCommandSent(message.payload));
          break;
        case 'ALERTA_RECIBIDA':
          store.dispatch(generadoresAlertReceived(message.payload));
          break;
        default:
          // No console aquí
          break;
      }
    });

    store.dispatch(generadoresWebSocketConnected());
  }

  // 2. DESCONECTAR cuando el usuario cierra sesión
  else if (getAccount.rejected.match(action) || action.type === logoutSession().type) {
    unsubscribeGeneradores();
    disconnectGeneradores();
    store.dispatch(generadoresWebSocketDisconnected());
  }

  // 3. MANEJAR ENVÍO DE COMANDOS
  else if (action.type === 'GENERADORES_SEND_COMMAND') {
    const commandAction = action as SendGeneradorCommandAction;
    sendGeneradorCommand(commandAction.payload.commandType, commandAction.payload.payload);
  }

  return next(action);
};

export default generadoresWebsocketMiddleware;
