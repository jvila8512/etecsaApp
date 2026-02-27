import { IEquipo } from 'app/shared/model/equipo.model';

export interface IEspecialidad {
  id?: number;
  nombre?: string;
  codigo?: string;
  descripcionTecnica?: string | null;
  equipos?: IEquipo[] | null;
}

export const defaultValue: Readonly<IEspecialidad> = {};
