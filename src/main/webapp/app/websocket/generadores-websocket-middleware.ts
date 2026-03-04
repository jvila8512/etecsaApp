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
import { dashboardVariableWritten } from '../shared/reducers/dashboard-reducer';

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
        case 'COMANDO_EJECUTADO': {
          store.dispatch(generadoresCommandSent(message.payload));
          // actualización inmediata del dashboard para que el detalle muestre el valor nuevo
          const { grupoId, tipo, direccion, valor, exito } = message.payload;
          if (exito && (tipo === 'COIL' || tipo === 'REGISTRO')) {
            const equipoId = parseInt(grupoId, 10);
            const v = tipo === 'COIL' ? valor === true : (valor as number);
            store.dispatch(
              // acción importada en este módulo al comienzo
              dashboardVariableWritten({ equipoId, dir: direccion, value: v }),
            );
          }
          break;
        }
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
