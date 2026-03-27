import dayjs from 'dayjs';
import { ISitio } from 'app/shared/model/sitio.model';
import { IEspecialidad } from 'app/shared/model/especialidad.model';
import { EstadoEquipo } from 'app/shared/model/enumerations/estado-equipo.model';

export interface IEquipo {
  id?: number;
  nombre?: string;
  direccionIp?: string;
  modbusSlaveId?: number | null;
  modelo?: string | null;
  firmwareVersion?: string | null;
  estado?: keyof typeof EstadoEquipo;
  ultimoHeartbeat?: dayjs.Dayjs | null;
  intervaloBase?: number | null;
  sitio?: ISitio | null;
  especialidades?: IEspecialidad[] | null;
}

export const defaultValue: Readonly<IEquipo> = {};
