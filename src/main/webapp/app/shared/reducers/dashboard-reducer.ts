import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface DashboardEquipo {
  id: number;
  nombre: string;
  direccionIp: string;
  estado: string;
  ultimoHeartbeat: string | null;
  variables: Array<{
    nombreVariable: string;
    dir: number; // dirección Modbus (añadida para escritura)
    valorNumerico: number | null;
    valorBooleano: boolean | null;
    timestampActualizacion: string | null;
  }>;
}

export interface DashboardState {
  connected: boolean;
  equipos: DashboardEquipo[];
  lastUpdate: number | null;
  hasReceivedData: boolean;
}

const initialState: DashboardState = {
  connected: false,
  equipos: [],
  lastUpdate: null,
  hasReceivedData: false,
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
      // Mantener cache de datos al desconectar
    },
    dashboardDataReceived(state, action: PayloadAction<{ equipos: DashboardEquipo[]; timestamp: number }>) {
      // debug: imprimimos el payload completo si la url contiene ?debug
      if (window.location.search.includes('debug')) {
        // mantenemos el console.warn para depuración, eslint ahora lo ignora
        console.warn('dashboardDataReceived payload', action.payload);
      }

      // convert cualquier texto recibido a los tres valores canónicos
      // el backend a veces antepone emojis/círculos y en ocasiones una lectura
      // fallida genera "DESCONECTADO" aunque el equipo siga hablando.
      const normalizeEstado = (raw: string) => {
        if (!raw) {
          return raw;
        }
        if (raw.includes('DESCONECTADO')) return 'DESCONECTADO';
        if (raw.includes('OPERATIVO')) return 'OPERATIVO';
        if (raw.includes('ERROR')) return 'ERROR';
        return raw;
      };

      // robustecer el filtro para ignorar cualquier parpadeo corto (ida y vuelta).
      const THRESHOLD = 10000; // ms
      const oldMap = new Map<number, string>();
      state.equipos.forEach(e => oldMap.set(e.id, e.estado));

      // guardamos la última fecha recibida para cada equipo si hace falta
      const lastTimestamps = new Map<number, number>();
      // si tenemos lastUpdate previo, asignarlo a todos, después se sobrescribirá
      if (state.lastUpdate !== null) {
        const prev = state.lastUpdate; // ya es number debido al chequeo
        state.equipos.forEach(e => lastTimestamps.set(e.id, prev));
      }

      state.equipos = action.payload.equipos.map(e => {
        const nuevo = { ...e, estado: normalizeEstado(e.estado) };
        const anterior = oldMap.get(e.id);
        const prevTs = lastTimestamps.get(e.id) ?? state.lastUpdate ?? action.payload.timestamp;
        const delta = action.payload.timestamp - prevTs;

        // debounce: si el estado cambia y luego vuelve en menos de THRESHOLD,
        // mantendremos el anterior hasta que se estabilice
        if (anterior && nuevo.estado !== anterior && delta < THRESHOLD) {
          // keep previous state and skip logging
          nuevo.estado = anterior;
        }

        return nuevo;
      });

      // actualización normal
      state.lastUpdate = action.payload.timestamp;
      state.hasReceivedData = true;
    },
    // acción disparada cuando un comando de escritura fue exitoso
    dashboardVariableWritten(state, action: PayloadAction<{ equipoId: number; dir: number; value: boolean | number }>) {
      const { equipoId, dir, value } = action.payload;
      const eq = state.equipos.find(e => e.id === equipoId);
      if (!eq || !eq.variables) return;
      const variable = eq.variables.find(v => v.dir === dir);
      if (!variable) return;
      if (typeof value === 'boolean') {
        variable.valorBooleano = value;
      } else {
        variable.valorNumerico = value; // ya es number gracias al tipo de la acción
      }
    },
  },
});

export const { dashboardConnected, dashboardDisconnected, dashboardDataReceived, dashboardVariableWritten } = DashboardSlice.actions;
export const getDashboardEquipos = (state: any) => state.dashboard?.equipos ?? [];
export const getDashboardConnected = (state: any) => state.dashboard?.connected ?? false;
export const getDashboardHasReceivedData = (state: any) => state.dashboard?.hasReceivedData ?? false;
export default DashboardSlice.reducer;
