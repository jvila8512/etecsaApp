import { createSlice, PayloadAction } from '@reduxjs/toolkit';

// ==================== TIPOS ====================
interface GeneradorDatos {
  conectado: boolean;
  ip: string;
  estado: string;
  datosValidos: boolean;
  memorias: { [key: string]: boolean };
  registros: { [key: string]: number };
  voltaje?: string;
  frecuencia?: string;
  temperatura?: string;
  nivelCombustible?: string;
  error?: string;
}

interface GeneradoresState {
  connected: boolean;
  datosTiempoReal: any;
  ultimoComando: any;
  alertas: any[];
  grupos: { [key: string]: GeneradorDatos };
}

// ==================== ESTADO INICIAL ====================
const initialState: GeneradoresState = {
  connected: false,
  datosTiempoReal: null,
  ultimoComando: null,
  alertas: [],
  grupos: {},
};

// ==================== SLICE ====================
export const GeneradoresSlice = createSlice({
  name: 'generadores',
  initialState,
  reducers: {
    generadoresWebSocketConnected(state) {
      state.connected = true;
    },
    generadoresWebSocketDisconnected(state) {
      state.connected = false;
      state.datosTiempoReal = null;
      state.grupos = {};
      state.alertas = [];
    },
    generadoresDataReceived(state, action: PayloadAction<any>) {
      state.datosTiempoReal = action.payload;
      if (action.payload.grupos) {
        state.grupos = action.payload.grupos;
      }
    },
    generadoresCommandSent(state, action: PayloadAction<any>) {
      state.ultimoComando = action.payload;
    },
    generadoresAlertReceived(state, action: PayloadAction<any>) {
      // Mantener solo las últimas 10 alertas
      state.alertas = [action.payload, ...state.alertas.slice(0, 9)];
    },
    clearGeneradoresAlerts(state) {
      state.alertas = [];
    },
    updateGrupoStatus(state, action: PayloadAction<{ grupoId: string; datos: Partial<GeneradorDatos> }>) {
      const { grupoId, datos } = action.payload;
      if (state.grupos[grupoId]) {
        state.grupos[grupoId] = { ...state.grupos[grupoId], ...datos };
      }
    },
  },
});

// ==================== EXPORTAR ACCIONES ====================
export const {
  generadoresWebSocketConnected,
  generadoresWebSocketDisconnected,
  generadoresDataReceived,
  generadoresCommandSent,
  generadoresAlertReceived,
  clearGeneradoresAlerts,
  updateGrupoStatus,
} = GeneradoresSlice.actions;

// ==================== SELECTORS ====================
export const getGeneradoresState = (state: any) => state.generadores;
export const getGeneradoresConnected = (state: any) => state.generadores.connected;
export const getGeneradoresDatosTiempoReal = (state: any) => state.generadores.datosTiempoReal;
export const getGeneradoresGrupos = (state: any) => state.generadores.grupos;
export const getGeneradoresAlertas = (state: any) => state.generadores.alertas;
export const getGruposConectados = (state: any) =>
  Object.entries(state.generadores.grupos)
    .filter(([_, grupo]: [string, any]) => grupo.conectado)
    .reduce((acc, [id, grupo]) => ({ ...acc, [id]: grupo }), {});
export const getGrupoById = (state: any, grupoId: string) => state.generadores.grupos[grupoId];

// ==================== REDUCER ====================
export default GeneradoresSlice.reducer;
