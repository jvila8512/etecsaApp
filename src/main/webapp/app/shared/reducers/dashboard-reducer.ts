import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface DashboardEquipo {
  id: number;
  nombre: string;
  direccionIp: string;
  estado: string;
  ultimoHeartbeat: string | null;
  variables: Array<{
    id: number;
    nombreVariable: string;
    dir: number;
    valorNumerico: number | null;
    valorBooleano: boolean | null;
    esLectura: boolean;
    umbralAlerta: number | null;
    unidadMedida: string | null;
    timestampActualizacion: string | null;
  }>;
}

export interface DashboardState {
  connected: boolean;
  equipos: DashboardEquipo[];
  lastUpdate: number | null;
  hasReceivedData: boolean;
  // mapa de escrituras pendientes para evitar sobrescrituras por lecturas posteriores
  pendingWrites?: { [key: string]: number };
}

const initialState: DashboardState = {
  connected: false,
  equipos: [],
  lastUpdate: null,
  hasReceivedData: false,
  pendingWrites: {},
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
      console.warn('[REDUCER] dashboardDataReceived - equipos:', action.payload.equipos?.length);
      if (action.payload.equipos?.length > 0) {
        console.warn('[REDUCER] Primer equipo:', JSON.stringify(action.payload.equipos[0]));
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

      // crear mapa de variables previas (equipoId -> "dir-Tipo" -> variable)
      const prevVarMap = new Map<number, Map<string, any>>();
      state.equipos.forEach(e => {
        const m = new Map<string, any>();
        e.variables?.forEach(v => {
          const key = `${e.id}-${v.dir}-${v.valorBooleano !== null ? 'B' : 'N'}`;
          m.set(key, v);
        });
        prevVarMap.set(e.id, m);
      });

      // guardamos la última fecha recibida para cada equipo si hace falta
      const lastTimestamps = new Map<number, number>();
      // si tenemos lastUpdate previo, asignarlo a todos, después se sobrescribirá
      if (state.lastUpdate !== null) {
        const prev = state.lastUpdate; // ya es number debido al chequeo
        state.equipos.forEach(e => lastTimestamps.set(e.id, prev));
      }

      // MERGE: actualizar solo los equipos del payload, mantener los demás
      const payloadEquiposMap = new Map(action.payload.equipos.map(e => [e.id, e]));
      const now = Date.now();

      state.equipos = state.equipos.map(existing => {
        const incoming = payloadEquiposMap.get(existing.id);
        if (!incoming) return existing;

        const nuevo = { ...incoming, estado: normalizeEstado(incoming.estado) };
        const anterior = oldMap.get(existing.id);
        const prevTs = lastTimestamps.get(existing.id) ?? state.lastUpdate ?? action.payload.timestamp;
        const delta = action.payload.timestamp - prevTs;

        if (anterior && nuevo.estado !== anterior && delta < THRESHOLD) {
          nuevo.estado = anterior;
        }

        const pending = state.pendingWrites ?? {};
        Object.keys(pending).forEach(k => {
          if (now - (pending[k] ?? 0) > 10000) {
            delete pending[k];
          }
        });

        if (Array.isArray(incoming.variables) && incoming.variables.length > 0) {
          // Crear mapa de variables existentes por dir+tipo
          const existingVarsMap = new Map<string, any>();
          existing.variables?.forEach(v => {
            const key = `${existing.id}-${v.dir}-${v.valorBooleano !== null ? 'B' : 'N'}`;
            existingVarsMap.set(key, v);
          });

          // FUSIONAR: SIEMPRE mantener variables existentes + actualizar/agregar las del payload
          const mergedVarsMap = new Map(existingVarsMap);

          incoming.variables.forEach(v => {
            const typeFlag = v.valorBooleano !== null ? 'B' : 'N';
            const key = `${existing.id}-${v.dir}-${typeFlag}`;

            // Si hay escritura pendiente para esta variable, NO sobrescribir del PLC
            if (pending[key] && now - pending[key] < 5000) {
              const existingVar = existingVarsMap.get(key);
              if (existingVar) {
                mergedVarsMap.set(key, { ...v, valorBooleano: existingVar.valorBooleano, valorNumerico: existingVar.valorNumerico });
                return;
              }
            }

            mergedVarsMap.set(key, v);
          });

          nuevo.variables = Array.from(mergedVarsMap.values()).map((v, index) => ({
            ...v,
            id: v.id ?? index + 1,
            esLectura: v.esLectura ?? true,
            umbralAlerta: v.umbralAlerta ?? null,
            unidadMedida: v.unidadMedida ?? null,
          }));

          state.pendingWrites = pending;
        } else {
          // Payload tiene variables vacías, MANTENER las existentes
          nuevo.variables = existing.variables;
        }

        return nuevo;
      });

      // Agregar equipos nuevos
      const existingIds = new Set(state.equipos.map(e => e.id));
      action.payload.equipos.forEach(incoming => {
        if (!existingIds.has(incoming.id)) {
          state.equipos.push({
            ...incoming,
            estado: normalizeEstado(incoming.estado),
            variables: (incoming.variables || []).map((v, index) => ({
              ...v,
              id: v.id ?? index + 1,
              esLectura: v.esLectura ?? true,
              umbralAlerta: v.umbralAlerta ?? null,
              unidadMedida: v.unidadMedida ?? null,
            })),
          });
        }
      });

      // Debug: mostrar estado de variables
      const eq1500 = state.equipos.find(e => e.id === 1500);
      console.warn(
        '[REDUCER] Equipo 1500: numVars=',
        eq1500?.variables?.length,
        'vars=',
        eq1500?.variables?.map((v: any) => v.nombreVariable),
      );

      // actualización normal
      state.lastUpdate = action.payload.timestamp;
      state.hasReceivedData = true;
    },
    // acción disparada cuando un comando de escritura fue exitoso
    dashboardVariableWritten(state, action: PayloadAction<{ equipoId: number; dir: number; value: boolean | number }>) {
      const { equipoId, dir, value } = action.payload;
      // rebuild equipos array with updated variable, preserving immutability
      state.equipos = state.equipos.map(e => {
        if (e.id !== equipoId || !e.variables) return e;
        const newVars = e.variables.map(v => {
          if (
            v.dir === dir &&
            ((typeof value === 'boolean' && v.valorBooleano !== null) || (typeof value === 'number' && v.valorNumerico !== null))
          ) {
            // create new variable object with updated value
            return {
              ...v,
              valorBooleano: typeof value === 'boolean' ? value : v.valorBooleano,
              valorNumerico: typeof value === 'number' ? value : v.valorNumerico,
            };
          }
          return v;
        });
        return {
          ...e,
          variables: newVars,
        };
      });
      // marcar esta variable como escrita recientemente para evitar sobrescrituras
      const typeFlag = typeof value === 'boolean' ? 'B' : 'N';
      const key = `${equipoId}-${dir}-${typeFlag}`;
      if (!state.pendingWrites) state.pendingWrites = {};
      state.pendingWrites[key] = Date.now();
    },
  },
});

export const { dashboardConnected, dashboardDisconnected, dashboardDataReceived, dashboardVariableWritten } = DashboardSlice.actions;
export const getDashboardEquipos = (state: any) => state.dashboard?.equipos ?? [];
export const getDashboardConnected = (state: any) => state.dashboard?.connected ?? false;
export const getDashboardHasReceivedData = (state: any) => state.dashboard?.hasReceivedData ?? false;
export default DashboardSlice.reducer;
