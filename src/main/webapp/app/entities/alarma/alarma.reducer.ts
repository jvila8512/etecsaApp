import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';
import { cleanEntity } from 'app/shared/util/entity-utils';
import { EntityState, IQueryParams, createEntitySlice, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { IAlarma, defaultValue } from 'app/shared/model/alarma.model';

export interface AlarmaDeteccion {
  eventoId: number;
  descripcion?: string;
  severidad?: string;
  mensajeUsuario?: string;
  valorActual?: number;
  umbral?: number;
  valorBooleano?: boolean;
  esAlarma: boolean;
}

interface AlarmaState extends EntityState<IAlarma> {
  alarmasActivasMap: Record<number, IAlarma[]>;
}

const initialState: AlarmaState = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
  alarmasActivasMap: {},
};

const apiUrl = 'api/alarmas';

// Actions

export const getEntities = createAsyncThunk(
  'alarma/fetch_entity_list',
  async ({ page, size, sort }: IQueryParams) => {
    const requestUrl = `${apiUrl}?${sort ? `page=${page}&size=${size}&sort=${sort}&` : ''}cacheBuster=${new Date().getTime()}`;
    return axios.get<IAlarma[]>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const getEntity = createAsyncThunk(
  'alarma/fetch_entity',
  async (id: string | number) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<IAlarma>(requestUrl);
  },
  { serializeError: serializeAxiosError },
);

export const createEntity = createAsyncThunk(
  'alarma/create_entity',
  async (entity: IAlarma, thunkAPI) => {
    const result = await axios.post<IAlarma>(apiUrl, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const updateEntity = createAsyncThunk(
  'alarma/update_entity',
  async (entity: IAlarma, thunkAPI) => {
    const result = await axios.put<IAlarma>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const partialUpdateEntity = createAsyncThunk(
  'alarma/partial_update_entity',
  async (entity: IAlarma, thunkAPI) => {
    const result = await axios.patch<IAlarma>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const deleteEntity = createAsyncThunk(
  'alarma/delete_entity',
  async (id: string | number, thunkAPI) => {
    const requestUrl = `${apiUrl}/${id}`;
    const result = await axios.delete<IAlarma>(requestUrl);
    thunkAPI.dispatch(getEntities({}));
    return result;
  },
  { serializeError: serializeAxiosError },
);

export const procesarDeteccion = createAsyncThunk(
  'alarma/procesar_deteccion',
  async (deteccion: AlarmaDeteccion, thunkAPI) => {
    const result = await axios.post<IAlarma>(`${apiUrl}/procesar-deteccion`, deteccion);
    if (result.status === 204) {
      return null;
    }
    return result.data;
  },
  { serializeError: serializeAxiosError },
);

export const reconocerAlarma = createAsyncThunk(
  'alarma/reconocer',
  async (id: number, thunkAPI) => {
    const result = await axios.post<IAlarma>(`${apiUrl}/${id}/reconocer`);
    thunkAPI.dispatch(getEntities({}));
    return result.data;
  },
  { serializeError: serializeAxiosError },
);

export const finalizarAlarma = createAsyncThunk(
  'alarma/finalizar',
  async (id: number, thunkAPI) => {
    const result = await axios.post<IAlarma>(`${apiUrl}/${id}/finalizar`);
    thunkAPI.dispatch(getEntities({}));
    return result.data;
  },
  { serializeError: serializeAxiosError },
);

export const getAlarmasActivasParaEvento = createAsyncThunk(
  'alarma/activas_por_evento',
  async (eventoId: number) => {
    const result = await axios.get<IAlarma[]>(`${apiUrl}/activas/evento/${eventoId}`);
    return { eventoId, alarmas: result.data };
  },
  { serializeError: serializeAxiosError },
);

export const getAlarmasActivasBulk = createAsyncThunk(
  'alarma/activas_bulk',
  async (eventoIds: number[]) => {
    const promises = eventoIds.map(id => axios.get<IAlarma[]>(`${apiUrl}/activas/evento/${id}`));
    const results = await Promise.all(promises);
    const map: Record<number, IAlarma[]> = {};
    eventoIds.forEach((id, index) => {
      map[id] = results[index].data;
    });
    return map;
  },
  { serializeError: serializeAxiosError },
);

// slice

export const AlarmaSlice = createEntitySlice({
  name: 'alarma',
  initialState: initialState as unknown as EntityState<IAlarma> & { alarmasActivasMap: Record<number, IAlarma[]> },
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        state.loading = false;
        state.entity = action.payload.data;
      })
      .addCase(deleteEntity.fulfilled, state => {
        state.updating = false;
        state.updateSuccess = true;
        state.entity = {};
      })
      .addCase(getAlarmasActivasParaEvento.fulfilled, (state, action) => {
        const { eventoId, alarmas } = action.payload;
        const s = state as unknown as AlarmaState;
        s.alarmasActivasMap[eventoId] = alarmas;
      })
      .addCase(getAlarmasActivasBulk.fulfilled, (state, action) => {
        const s = state as unknown as AlarmaState;
        s.alarmasActivasMap = action.payload;
      })
      .addMatcher(isFulfilled(getEntities), (state, action) => {
        const { data, headers } = action.payload;

        return {
          ...state,
          loading: false,
          entities: data,
          totalItems: parseInt(headers['x-total-count'], 10),
        };
      })
      .addMatcher(isFulfilled(createEntity, updateEntity, partialUpdateEntity), (state, action) => {
        state.updating = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isFulfilled(procesarDeteccion), (state, action) => {
        if (action.payload) {
          const nuevaAlarma = action.payload;
          if (nuevaAlarma.evento?.id) {
            const s = state as unknown as AlarmaState;
            if (!s.alarmasActivasMap[nuevaAlarma.evento.id]) {
              s.alarmasActivasMap[nuevaAlarma.evento.id] = [];
            }
            s.alarmasActivasMap[nuevaAlarma.evento.id].push(nuevaAlarma);
          }
        }
      })
      .addMatcher(isFulfilled(reconocerAlarma, finalizarAlarma), (state, action) => {
        const alarmaActualizada = action.payload;
        if (alarmaActualizada?.evento?.id) {
          const eventoId = alarmaActualizada.evento.id;
          const s = state as unknown as AlarmaState;
          if (s.alarmasActivasMap[eventoId]) {
            if (alarmaActualizada.estado === 'FINALIZADA') {
              s.alarmasActivasMap[eventoId] = s.alarmasActivasMap[eventoId].filter(a => a.id !== alarmaActualizada.id);
            } else {
              const index = s.alarmasActivasMap[eventoId].findIndex(a => a.id === alarmaActualizada.id);
              if (index >= 0) {
                s.alarmasActivasMap[eventoId][index] = alarmaActualizada;
              }
            }
          }
        }
      })
      .addMatcher(isPending(getEntities, getEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity, deleteEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.updating = true;
      });
  },
});

export const { reset } = AlarmaSlice.actions;

// Reducer
export default AlarmaSlice.reducer;
