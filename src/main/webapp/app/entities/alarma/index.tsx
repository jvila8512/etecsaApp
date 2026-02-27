import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Alarma from './alarma';
import AlarmaDetail from './alarma-detail';
import AlarmaUpdate from './alarma-update';
import AlarmaDeleteDialog from './alarma-delete-dialog';

const AlarmaRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Alarma />} />
    <Route path="new" element={<AlarmaUpdate />} />
    <Route path=":id">
      <Route index element={<AlarmaDetail />} />
      <Route path="edit" element={<AlarmaUpdate />} />
      <Route path="delete" element={<AlarmaDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default AlarmaRoutes;
