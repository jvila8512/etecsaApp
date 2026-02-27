import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Sitio from './sitio';
import Equipo from './equipo';
import Especialidad from './especialidad';
import EventoEquipo from './evento-equipo';
import EventoPlantilla from './evento-plantilla';
import Alarma from './alarma';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="sitio/*" element={<Sitio />} />
        <Route path="equipo/*" element={<Equipo />} />
        <Route path="especialidad/*" element={<Especialidad />} />
        <Route path="evento-equipo/*" element={<EventoEquipo />} />
        <Route path="evento-plantilla/*" element={<EventoPlantilla />} />
        <Route path="alarma/*" element={<Alarma />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
