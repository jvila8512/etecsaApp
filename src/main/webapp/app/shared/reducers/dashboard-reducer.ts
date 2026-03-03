import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface DashboardEquipo {
  id: number;
  nombre: string;
  direccionIp: string;
  estado: string;
  ultimoHeartbeat: string | null;
  variables: Array<{
    nombreVariable: string;
    valorNumerico: number | null;
    valorBooleano: boolean | null;
    timestampActualizacion: string | null;
  }>;
}

export interface DashboardState {
  connected: boolean;
  equipos: DashboardEquipo[];
  lastUpdate: number | null;
}

const initialState: DashboardState = {
  connected: false,
  equipos: [],
  lastUpdate: null,
};

export const DashboardSlice = createSlice({
  name: 'dashboard',
  initialState,
  reducers: {
    dashboardConnected(state) {
      state.connected = true;
    },
    dashboardDisconnected(state) {
      state.connected = false;
      state.equipos = [];
    },
    dashboardDataReceived(state, action: PayloadAction<{ equipos: DashboardEquipo[]; timestamp: number }>) {
      state.equipos = action.payload.equipos;
      state.lastUpdate = action.payload.timestamp;
    },
  },
});

export const { dashboardConnected, dashboardDisconnected, dashboardDataReceived } = DashboardSlice.actions;
export const getDashboardEquipos = (state: any) => state.dashboard?.equipos ?? [];
export const getDashboardConnected = (state: any) => state.dashboard?.connected ?? false;
export default DashboardSlice.reducer;
