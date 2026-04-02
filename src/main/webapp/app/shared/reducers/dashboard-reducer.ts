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
    habilitarAlarma: boolean | null;
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
      // convert cualquier texto recibido a los tres valores canónicos
      const normalizeEstado = (raw: string) => {
        if (!raw) return raw;
        if (raw.includes('DESCONECTADO')) return 'DESCONECTADO';
        if (raw.includes('OPERATIVO')) return 'OPERATIVO';
        if (raw.includes('ERROR')) return 'ERROR';
        return raw;
      };

      const now = Date.now();

      state.equipos = state.equipos.map(existing => {
        const incoming = action.payload.equipos.find(e => e.id === existing.id);
        if (!incoming) return existing;

        // Siempre actualizar el estado - sin filtros anti-parpadeo
        const nuevo = { ...incoming, estado: normalizeEstado(incoming.estado) };

        const pending = state.pendingWrites ?? {};
        Object.keys(pending).forEach(k => {
          if (now - (pending[k] ?? 0) > 10000) {
            delete pending[k];
          }
        });

        // Fusionar variables si hay nuevas
        if (Array.isArray(incoming.variables) && incoming.variables.length > 0) {
          // Usar key composta: dir + tipo (B o N) para mantener booleanos y numéricos separados
          const existingVarsMap = new Map<string, any>();
          existing.variables?.forEach(v => {
            const tipo = v.valorBooleano !== null ? 'B' : 'N';
            const key = `${v.dir}-${tipo}`;
            existingVarsMap.set(key, v);
          });

          const mergedVarsMap = new Map(existingVarsMap);

          incoming.variables.forEach(v => {
            const tipo = v.valorBooleano !== null ? 'B' : 'N';
            const key = `${v.dir}-${tipo}`;

            if (pending[key] && now - pending[key] < 5000) {
              const existingVar = existingVarsMap.get(key);
              if (existingVar) {
                mergedVarsMap.set(key, { ...v, valorBooleano: existingVar.valorBooleano, valorNumerico: existingVar.valorNumerico });
                return;
              }
            }

            const existente = mergedVarsMap.get(key);
            if (existente) {
              mergedVarsMap.set(key, {
                ...existente,
                ...v,
                valorBooleano: v.valorBooleano !== null ? v.valorBooleano : existente.valorBooleano,
                valorNumerico: v.valorNumerico !== null ? v.valorNumerico : existente.valorNumerico,
                id: v.id ?? existente.id,
                nombreVariable: v.nombreVariable ?? existente.nombreVariable,
                dir: v.dir ?? existente.dir,
                esLectura: existente.esLectura,
                umbralAlerta: v.umbralAlerta ?? existente.umbralAlerta,
                unidadMedida: v.unidadMedida ?? existente.unidadMedida,
                habilitarAlarma: v.habilitarAlarma ?? existente.habilitarAlarma,
              });
            } else {
              mergedVarsMap.set(key, v);
            }
          });

          nuevo.variables = Array.from(mergedVarsMap.values()).map((v, index) => ({
            ...v,
            id: v.id ?? index + 1,
            esLectura: v.esLectura ?? true,
            umbralAlerta: v.umbralAlerta ?? null,
            unidadMedida: v.unidadMedida ?? null,
            habilitarAlarma: v.habilitarAlarma ?? null,
          }));

          state.pendingWrites = pending;
        } else {
          nuevo.variables = existing.variables;
        }

        return nuevo;
      });

      // Agregar equipos nuevos que no existen en el estado
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
              habilitarAlarma: v.habilitarAlarma ?? null,
            })),
          });
        }
      });

      state.lastUpdate = action.payload.timestamp;
      state.hasReceivedData = true;
    },
    // acción disparada cuando un comando de escritura fue exitoso
    dashboardVariableWritten(state, action: PayloadAction<{ equipoId: number; dir: number; value: boolean | number }>) {
      const { equipoId, dir, value } = action.payload;
      state.equipos = state.equipos.map(e => {
        if (e.id !== equipoId || !e.variables) return e;
        const newVars = e.variables.map(v => {
          if (v.dir === dir) {
            const match =
              (typeof value === 'boolean' && v.valorBooleano !== null) || (typeof value === 'number' && v.valorNumerico !== null);
            if (match) {
              return {
                ...v,
                valorBooleano: typeof value === 'boolean' ? value : v.valorBooleano,
                valorNumerico: typeof value === 'number' ? value : v.valorNumerico,
              };
            }
          }
          return v;
        });
        return { ...e, variables: newVars };
      });
      // Usar key composta: dir + tipo
      const typeFlag = typeof value === 'boolean' ? 'B' : 'N';
      const key = `${dir}-${typeFlag}`;
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
