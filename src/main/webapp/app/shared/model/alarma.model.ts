import dayjs from 'dayjs';
import { IEventoEquipo } from 'app/shared/model/evento-equipo.model';
import { IUser } from 'app/shared/model/user.model';
import { Severidad } from 'app/shared/model/enumerations/severidad.model';
import { EstadoAlarma } from 'app/shared/model/enumerations/estado-alarma.model';

export interface IAlarma {
  id?: number;
  descripcion?: string;
  activatedAt?: dayjs.Dayjs;
  deactivatedAt?: dayjs.Dayjs | null;
  severidad?: keyof typeof Severidad;
  estado?: keyof typeof EstadoAlarma;
  mensajeUsuario?: string | null;
  evento?: IEventoEquipo | null;
  acknowledgedBy?: IUser | null;
}

export const defaultValue: Readonly<IAlarma> = {};
