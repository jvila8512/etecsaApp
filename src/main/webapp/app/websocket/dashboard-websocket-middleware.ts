import { Middleware, AnyAction } from '@reduxjs/toolkit';
import {
  connectDashboard,
  disconnectDashboard,
  receiveDashboardActualizaciones,
  receiveDashboardCompleto,
  getDashboardConnectionPromise,
} from './dashboard-websocket';
import { getAccount, logoutSession } from 'app/shared/reducers/authentication';
import { dashboardConnected, dashboardDisconnected, dashboardDataReceived } from '../shared/reducers/dashboard-reducer';

/**
 * Middleware que conecta al WebSocket del dashboard cuando el usuario inicia sesión
 * y desconecta cuando cierra sesión.
 */
const dashboardWebsocketMiddleware: Middleware = store => next => (action: AnyAction) => {
  if (getAccount.fulfilled.match(action)) {
    connectDashboard();

    // IMPORTANTE: Esperar a que la conexión STOMP esté lista antes de suscribirse
    const connectionPromise = getDashboardConnectionPromise();
    if (connectionPromise) {
      connectionPromise.then(() => {
        // Suscribirse a dashboard completo
        receiveDashboardCompleto().subscribe((payload: any) => {
          store.dispatch(dashboardDataReceived({ equipos: payload.equipos ?? [], timestamp: payload.timestamp ?? Date.now() }));
        });
        // Suscribirse a actualizaciones rápidas
        receiveDashboardActualizaciones().subscribe((payload: any) => {
          store.dispatch(dashboardDataReceived({ equipos: payload.equipos ?? [], timestamp: payload.timestamp ?? Date.now() }));
        });
      });
    } else {
      // Fallback: suscribir directamente
      receiveDashboardCompleto().subscribe((payload: any) => {
        store.dispatch(dashboardDataReceived({ equipos: payload.equipos ?? [], timestamp: payload.timestamp ?? Date.now() }));
      });
      receiveDashboardActualizaciones().subscribe((payload: any) => {
        store.dispatch(dashboardDataReceived({ equipos: payload.equipos ?? [], timestamp: payload.timestamp ?? Date.now() }));
      });
    }

    store.dispatch(dashboardConnected());
  } else if (getAccount.rejected.match(action) || action.type === logoutSession().type) {
    disconnectDashboard();
    store.dispatch(dashboardDisconnected());
  }
  return next(action);
};

export default dashboardWebsocketMiddleware;
