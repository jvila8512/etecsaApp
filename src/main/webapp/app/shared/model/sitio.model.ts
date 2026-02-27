import dayjs from 'dayjs';

export interface ISitio {
  id?: number;
  nombre?: string;
  codigo?: string;
  ubicacion?: string;
  fechaRegistro?: dayjs.Dayjs | null;
}

export const defaultValue: Readonly<ISitio> = {};
