import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import SitioDetail from './sitio-detail';
import SitioUpdate from './sitio-update';
import SitioDeleteDialog from './sitio-delete-dialog';
import Sitio from './sitioCrud';

const SitioRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Sitio />} />
    <Route path="new" element={<SitioUpdate />} />
    <Route path=":id">
      <Route index element={<SitioDetail />} />
      <Route path="edit" element={<SitioUpdate />} />
      <Route path="delete" element={<SitioDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default SitioRoutes;
