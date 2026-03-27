import dayjs from 'dayjs';
import { IEquipo } from 'app/shared/model/equipo.model';
import { IEventoPlantilla } from 'app/shared/model/evento-plantilla.model';
import { TipoRegistro } from 'app/shared/model/enumerations/tipo-registro.model';
import { TipoDato } from 'app/shared/model/enumerations/tipo-dato.model';
import { Severidad } from 'app/shared/model/enumerations/severidad.model';

export interface IEventoEquipo {
  id?: number;
  nombreVariable?: string;
  direccionModbus?: number;
  tipoRegistro?: keyof typeof TipoRegistro;
  tipoDato?: keyof typeof TipoDato;
  esEscribible?: boolean | null;
  valorNumerico?: number | null;
  valorBooleano?: boolean | null;
  timestampActualizacion?: dayjs.Dayjs | null;
  intervaloLectura?: number | null;
  umbralAlerta?: number | null;
  severidadAlerta?: keyof typeof Severidad | null;
  equipo?: IEquipo | null;
  plantilla?: IEventoPlantilla | null;
}

export const defaultValue: Readonly<IEventoEquipo> = {
  esEscribible: false,
  valorBooleano: false,
};
