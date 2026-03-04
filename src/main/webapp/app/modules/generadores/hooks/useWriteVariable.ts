// src/main/webapp/app/modules/generadores/hooks/useWriteVariable.ts

import { useCallback, useRef } from 'react';
import { sendGeneradorCommand } from 'app/websocket/generadores-websocket';

interface WriteVariableResult {
  success: boolean;
  message: string;
}

interface UseWriteVariableOptions {
  onSuccess?: (result: WriteVariableResult) => void;
  onError?: (error: any) => void;
}

/**
 * Hook para escribir variables en el PLC a través de WebSocket
 * Soporta dos tipos:
 * - BOOLEANO: Envía ESCRIBIR_COIL
 * - NUMÉRICO: Envía ESCRIBIR_REGISTRO
 */
export const useWriteVariable = (equipoId: number, options?: UseWriteVariableOptions) => {
  const isWritingRef = useRef(false);

  /**
   * Escribir un valor booleano en una bobina (COIL)
   * @param nombreVariable nombre descriptivo para mostrar en mensajes
   * @param address dirección/modbus del registro
   * @param valor valor booleano a escribir
   */
  const writeBoolean = useCallback(
    (nombreVariable: string, address: number, valor: boolean) => {
      if (isWritingRef.current) {
        console.warn('⚠️ Escritura en progreso, ignora solicitud duplicada');
        return;
      }

      isWritingRef.current = true;
      try {
        const payload = {
          generatorId: String(equipoId),
          commandType: 'COIL',
          address,
          booleanValue: valor,
        };

        sendGeneradorCommand('ESCRIBIR_COIL', payload);

        const result: WriteVariableResult = {
          success: true,
          message: `${nombreVariable} = ${valor ? 'ON' : 'OFF'}`,
        };

        options?.onSuccess?.(result);
      } catch (error) {
        console.error('❌ Error escribiendo booleano:', error);
        options?.onError?.(error);
      } finally {
        isWritingRef.current = false;
      }
    },
    [equipoId, options],
  );

  /**
   * Escribir un valor numérico en un registro (REGISTER)
   */
  const writeNumeric = useCallback(
    (nombreVariable: string, address: number, valor: number) => {
      if (isWritingRef.current) {
        console.warn('⚠️ Escritura en progreso, ignora solicitud duplicada');
        return;
      }

      isWritingRef.current = true;
      try {
        const payload = {
          generatorId: String(equipoId),
          commandType: 'REGISTER',
          address,
          value: valor,
        };

        sendGeneradorCommand('ESCRIBIR_REGISTRO', payload);

        const result: WriteVariableResult = {
          success: true,
          message: `${nombreVariable} ← ${valor}`,
        };

        options?.onSuccess?.(result);
      } catch (error) {
        console.error('❌ Error escribiendo numérico:', error);
        options?.onError?.(error);
      } finally {
        isWritingRef.current = false;
      }
    },
    [equipoId, options],
  );

  return { writeBoolean, writeNumeric, isWriting: isWritingRef.current };
};
