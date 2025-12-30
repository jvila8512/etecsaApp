/* eslint-disable prettier/prettier */
import { useAppSelector, useAppDispatch } from 'app/config/store';
import { sendGeneradorCommandAction } from '../../websocket/generadores-websocket-middleware';
import {
  getGeneradoresState,
  getGeneradoresConnected,
  getGeneradoresDatosTiempoReal,
  getGeneradoresGrupos,
  getGeneradoresAlertas,
} from '../reducers/generadores-reducer';

export const useGeneradores = () => {
  const dispatch = useAppDispatch();

  const generadoresState = useAppSelector(getGeneradoresState);
  const connected = useAppSelector(getGeneradoresConnected);
  const datosTiempoReal = useAppSelector(getGeneradoresDatosTiempoReal);
  const grupos = useAppSelector(getGeneradoresGrupos);
  const alertas = useAppSelector(getGeneradoresAlertas);

  const connectToGenerador = (grupoId: string, ip: string) => {
    dispatch(sendGeneradorCommandAction('CONECTAR_GRUPO', { grupoId, accion: 'conectar', ip }));
  };

  const disconnectGenerador = (grupoId: string) => {
    dispatch(sendGeneradorCommandAction('CONECTAR_GRUPO', { grupoId, accion: 'desconectar' }));
  };

  const writeCoil = (grupoId: string, direccion: number, valor: boolean) => {
    dispatch(sendGeneradorCommandAction('ESCRIBIR_COIL', { grupoId, direccion, valor }));
  };

  const writeRegister = (grupoId: string, direccion: number, valor: number) => {
    dispatch(sendGeneradorCommandAction('ESCRIBIR_REGISTRO', { grupoId, direccion, valor }));
  };

  const requestImmediateRead = (grupoId: string) => {
    dispatch(sendGeneradorCommandAction('LECTURA_INMEDIATA', { grupoId }));
  };

  return {
    connected,
    datosTiempoReal,
    grupos,
    alertas,
    generadoresState,
    connectToGenerador,
    disconnectGenerador,
    writeCoil,
    writeRegister,
    requestImmediateRead,
  };
};
/* eslint-enable prettier/prettier */
