import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import EventoPlantilla from './evento-plantilla';
import EventoPlantillaDetail from './evento-plantilla-detail';
import EventoPlantillaUpdate from './evento-plantilla-update';
import EventoPlantillaDeleteDialog from './evento-plantilla-delete-dialog';

const EventoPlantillaRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<EventoPlantilla />} />
    <Route path="new" element={<EventoPlantillaUpdate />} />
    <Route path=":id">
      <Route index element={<EventoPlantillaDetail />} />
      <Route path="edit" element={<EventoPlantillaUpdate />} />
      <Route path="delete" element={<EventoPlantillaDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default EventoPlantillaRoutes;
