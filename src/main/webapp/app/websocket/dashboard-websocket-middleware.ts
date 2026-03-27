import { Middleware, AnyAction } from '@reduxjs/toolkit';
import { connectDashboard, disconnectDashboard, receiveDashboardActualizaciones } from './dashboard-websocket';
import { getAccount, logoutSession } from 'app/shared/reducers/authentication';
import { dashboardConnected, dashboardDisconnected, dashboardDataReceived } from '../shared/reducers/dashboard-reducer';

/**
 * Middleware que conecta al WebSocket del dashboard cuando el usuario inicia sesión
 * y desconecta cuando cierra sesión.
 */
const dashboardWebsocketMiddleware: Middleware = store => next => (action: AnyAction) => {
  if (getAccount.fulfilled.match(action)) {
    connectDashboard();

    // Escuchar actualizaciones (que tienen las variables)
    receiveDashboardActualizaciones().subscribe((payload: any) => {
      store.dispatch(dashboardDataReceived({ equipos: payload.equipos ?? [], timestamp: payload.timestamp ?? Date.now() }));
    });

    store.dispatch(dashboardConnected());
  } else if (getAccount.rejected.match(action) || action.type === logoutSession().type) {
    disconnectDashboard();
    store.dispatch(dashboardDisconnected());
  }
  return next(action);
};

export default dashboardWebsocketMiddleware;
