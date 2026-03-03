// src/main/webapp/app/modules/dashboard/index.tsx
// Registrar ruta en app/routes.tsx:
//   <Route path="/dashboard" element={<Dashboard />} />

import React, { useState } from 'react';
import EquipoDetalle from './EquipoDetalle';
import DashboardHome from './DashboardHome';
import { EquipoDTO } from './types';

const Dashboard: React.FC = () => {
  const [equipoActivo, setEquipoActivo] = useState<EquipoDTO | null>(null);

  return equipoActivo ? (
    <EquipoDetalle equipo={equipoActivo} onVolver={() => setEquipoActivo(null)} />
  ) : (
    <DashboardHome onEntrar={setEquipoActivo} />
  );
};

export default Dashboard;
