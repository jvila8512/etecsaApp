import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import EventoEquipo from './evento-equipo';
import EventoEquipoDetail from './evento-equipo-detail';
import EventoEquipoUpdate from './evento-equipo-update';
import EventoEquipoDeleteDialog from './evento-equipo-delete-dialog';

const EventoEquipoRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<EventoEquipo />} />
    <Route path="new" element={<EventoEquipoUpdate />} />
    <Route path=":id">
      <Route index element={<EventoEquipoDetail />} />
      <Route path="edit" element={<EventoEquipoUpdate />} />
      <Route path="delete" element={<EventoEquipoDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default EventoEquipoRoutes;
