import { IEspecialidad } from 'app/shared/model/especialidad.model';

export interface IEventoPlantilla {
  id?: number;
  nombre?: string;
  descripcion?: string | null;
  scalingFactor?: number | null;
  unidadMedida?: string | null;
  funcionLectura?: string | null;
  funcionEscritura?: string | null;
  especialidad?: IEspecialidad | null;
}

export const defaultValue: Readonly<IEventoPlantilla> = {};
