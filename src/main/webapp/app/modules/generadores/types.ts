// src/main/webapp/app/modules/generadores/types.ts
export interface VariableEquipo {
  id: number;
  nombre: string;
  tipoRegistro: 'BIT_LOGICO_M' | 'PALABRA_MW' | 'PALABRA_DOBLE_MD';
  tipoDato: 'BOOLEAN' | 'INT16' | 'UINT16' | 'INT32' | 'FLOAT32';
  dir: number;
  valorBooleano?: boolean;
  valorNumerico?: number;
  unidad?: string;
  escribible: boolean;
  funcionEscritura?: string;
}

export interface EquipoDTO {
  id: number;
  nombre: string;
  direccionIp: string;
  estado: 'OPERATIVO' | 'DESCONECTADO' | 'ERROR';
  variables?: VariableEquipo[];
}
