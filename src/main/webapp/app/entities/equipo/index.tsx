import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Equipo from './equipo';
import EquipoDetail from './equipo-detail';
import EquipoUpdate from './equipo-update';
import EquipoDeleteDialog from './equipo-delete-dialog';

const EquipoRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Equipo />} />
    <Route path="new" element={<EquipoUpdate />} />
    <Route path=":id">
      <Route index element={<EquipoDetail />} />
      <Route path="edit" element={<EquipoUpdate />} />
      <Route path="delete" element={<EquipoDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default EquipoRoutes;
