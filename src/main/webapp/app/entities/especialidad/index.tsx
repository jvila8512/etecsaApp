import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Especialidad from './especialidad';
import EspecialidadDetail from './especialidad-detail';
import EspecialidadUpdate from './especialidad-update';
import EspecialidadDeleteDialog from './especialidad-delete-dialog';

const EspecialidadRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Especialidad />} />
    <Route path="new" element={<EspecialidadUpdate />} />
    <Route path=":id">
      <Route index element={<EspecialidadDetail />} />
      <Route path="edit" element={<EspecialidadUpdate />} />
      <Route path="delete" element={<EspecialidadDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default EspecialidadRoutes;
