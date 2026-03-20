// src/main/webapp/app/modules/generadores/EquipoDetalle.tsx

import React, { useState, useRef, useCallback, useEffect } from 'react';
import { Button } from 'primereact/button';
import { useAppDispatch } from 'app/config/store';
import { dashboardVariableWritten } from 'app/shared/reducers/dashboard-reducer';
import { Tag } from 'primereact/tag';
import { Divider } from 'primereact/divider';
import { InputNumber } from 'primereact/inputnumber';
import { InputSwitch } from 'primereact/inputswitch';
import { Toast } from 'primereact/toast';
import { Toolbar } from 'primereact/toolbar';
import { Panel } from 'primereact/panel';
import { Knob } from 'primereact/knob';
import { Chip } from 'primereact/chip';
import { Message } from 'primereact/message';
import { ProgressBar } from 'primereact/progressbar';
import { Tooltip } from 'primereact/tooltip';

// ✅ Leer desde Redux — los datos ya llegan por el WebSocket del Home
import { useAppSelector } from 'app/config/store';
import { getDashboardEquipos, getDashboardConnected } from 'app/shared/reducers/dashboard-reducer';
import { EquipoDTO } from './types';
import { useWriteVariable } from './hooks/useWriteVariable';

// ══════════════════════════════════════════════════════════
//  TIPOS WebSocket (estructura real que llega del backend)
// ══════════════════════════════════════════════════════════
interface WsVariable {
  nombreVariable: string;
  dir: number; // ← añadido
  valorNumerico: number | null;
  valorBooleano: boolean | null;
  unidadMedida: string | null;
  esLectura: boolean;
}

interface WsEquipo {
  id: number;
  nombre: string;
  direccionIp: string;
  estado: string;
  ultimoHeartbeat: string | null;
  variables: WsVariable[];
}

// ══════════════════════════════════════════════════════════
//  HELPERS
// ══════════════════════════════════════════════════════════
const estadoSeverity = (estado: string) => {
  if (estado === 'OPERATIVO') return 'success' as const;
  if (estado === 'ERROR') return 'danger' as const;
  return 'secondary' as const;
};

const fmtNumerico = (v: number | null | undefined): string => {
  if (v === null || v === undefined) return '---';

  // Usamos Math.round para eliminar cualquier decimal .000 antes de formatear
  const valorLimpio = Math.round(v);

  // Al ser un entero, toLocaleString solo pondrá puntos de miles si es necesario
  return valorLimpio.toLocaleString('es-ES');
};

const calcPct = (val: number | null | undefined): number => {
  if (val === null || val === undefined) return 0;
  return Math.min(100, Math.max(0, (val / 65535) * 100));
};

// ══════════════════════════════════════════════════════════
//  TARJETA BIT
// ══════════════════════════════════════════════════════════
const BitCard: React.FC<{
  variable: WsVariable;
  escribible: boolean;
  isWriting?: boolean;
  onToggle: (nombre: string, address: number, val: boolean) => void;
}> = ({ variable, escribible, isWriting = false, onToggle }) => {
  const isOn = variable.valorBooleano === true;
  const cssKey = variable.nombreVariable.replace(/\s+/g, '-');

  return (
    <div
      style={{
        background: '#fff',
        border: `1px solid ${isOn ? '#bbf7d0' : '#e2e8f0'}`,
        borderLeft: `4px solid ${isOn ? '#22c55e' : '#cbd5e1'}`,
        borderRadius: 8,
        padding: '14px 16px',
        display: 'flex',
        flexDirection: 'column',
        gap: 10,
        transition: 'border-color .2s',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <div style={{ fontSize: 13, fontWeight: 600, color: '#1e293b' }}>{variable.nombreVariable}</div>
          <div style={{ fontSize: 10, color: '#94a3b8', fontFamily: 'monospace', marginTop: 2 }}>
            BOOLEAN
            {escribible && <Tag value="WRITE" severity="info" style={{ fontSize: 9, marginLeft: 6, padding: '1px 4px' }} />}
          </div>
        </div>
        <Tag
          value={isOn ? 'ON' : 'OFF'}
          severity={isOn ? 'success' : 'danger'}
          icon={isOn ? 'pi pi-check-circle' : 'pi pi-circle'}
          style={{ fontSize: 11 }}
        />
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: 12, color: '#64748b' }}>{escribible ? 'Accionable' : 'Solo lectura'}</span>
        <Tooltip target={`.toggle-ws-${cssKey}`} content={escribible ? 'Click para cambiar' : 'Solo lectura'} />
        <div className={`toggle-ws-${cssKey}`}>
          <InputSwitch
            checked={isOn}
            disabled={!escribible || isWriting}
            onChange={e => escribible && onToggle(variable.nombreVariable, variable.dir, e.value ?? false)}
            style={{ opacity: escribible && !isWriting ? 1 : 0.5 }}
          />
        </div>
      </div>
    </div>
  );
};

// ══════════════════════════════════════════════════════════
//  TARJETA REGISTRO NUMÉRICO
// ══════════════════════════════════════════════════════════
const RegCard: React.FC<{ variable: WsVariable }> = ({ variable }) => {
  const val = variable.valorNumerico;

  // Calculamos el porcentaje real para la barra y el Knob
  const pct = calcPct(val);
  const pctRedondeado = Math.round(pct);

  // El Knob se muestra si es una unidad de porcentaje, usando el valor ya escalado
  const showKnob = val !== null && val !== undefined && variable.unidadMedida === '%';

  return (
    <div
      style={{
        background: '#fff',
        border: '1px solid #e2e8f0',
        borderLeft: '4px solid #3b82f6',
        borderRadius: 8,
        padding: '14px 16px',
        display: 'flex',
        flexDirection: 'column',
        gap: 10,
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <div style={{ fontSize: 13, fontWeight: 600, color: '#1e293b' }}>{variable.nombreVariable}</div>
          <div style={{ fontSize: 10, color: '#94a3b8', fontFamily: 'monospace', marginTop: 2 }}>NUMÉRICO</div>
        </div>
        {/* Usamos pctRedondeado para que el Knob siempre esté en el rango 0-100 */}
        {showKnob && <Knob value={pctRedondeado} size={48} readOnly valueColor="#3b82f6" rangeColor="#e2e8f0" textColor="#1e293b" />}
      </div>

      <div style={{ display: 'flex', alignItems: 'baseline', gap: 6 }}>
        <span style={{ fontSize: 18, fontWeight: 700, color: '#1e293b', fontFamily: 'monospace', lineHeight: 1 }}>
          {/* Aquí aplicamos el formateo sin ceros sobrantes */}
          {fmtNumerico(val)}
        </span>
        {variable.unidadMedida && <span style={{ fontSize: 13, color: '#94a3b8' }}>{variable.unidadMedida}</span>}
      </div>

      {/* La barra de progreso también usa el porcentaje calculado */}
      <ProgressBar value={pctRedondeado} showValue={false} style={{ height: 4, borderRadius: 4 }} color="#3b82f6" />
    </div>
  );
};

// ══════════════════════════════════════════════════════════
//  TARJETA ESCRITURA
// ══════════════════════════════════════════════════════════
const WriteCard: React.FC<{
  variable: WsVariable;
  onWrite: (nombre: string, address: number, valor: number) => void;
  isLoading?: boolean;
}> = ({ variable, onWrite, isLoading = false }) => {
  const [inputVal, setInputVal] = useState<number>(variable.valorNumerico ?? 0);

  return (
    <div
      style={{
        background: '#fff',
        border: '1px solid #bfdbfe',
        borderLeft: `4px solid ${isLoading ? '#f59e0b' : '#3b82f6'}`,
        borderRadius: 8,
        padding: '14px 16px',
        display: 'flex',
        flexDirection: 'column',
        gap: 12,
        opacity: isLoading ? 0.7 : 1,
        transition: 'opacity 0.2s, border-color 0.2s',
      }}
    >
      <div>
        <div style={{ fontSize: 13, fontWeight: 600, color: '#1e293b' }}>{variable.nombreVariable}</div>
        <div style={{ fontSize: 10, color: '#94a3b8', fontFamily: 'monospace', marginTop: 2 }}>
          ESCRITURA · {variable.unidadMedida ?? '---'}
        </div>
      </div>
      <div style={{ display: 'flex', gap: 8 }}>
        <InputNumber
          value={inputVal}
          onValueChange={e => !isLoading && setInputVal(e.value ?? 0)}
          style={{ flex: 1 }}
          inputStyle={{ fontFamily: 'monospace', fontSize: 13 }}
          placeholder="Valor..."
          disabled={isLoading}
        />
        <Button
          icon={isLoading ? 'pi pi-spin pi-spinner' : 'pi pi-send'}
          size="small"
          disabled={isLoading}
          loading={isLoading}
          onClick={() => onWrite(variable.nombreVariable, variable.dir, inputVal)}
        />
      </div>
    </div>
  );
};

// ══════════════════════════════════════════════════════════
//  COMPONENTE PRINCIPAL
// ══════════════════════════════════════════════════════════
interface EquipoDetalleProps {
  equipo: EquipoDTO;
  onVolver: () => void;
}

const EquipoDetalle: React.FC<EquipoDetalleProps> = ({ equipo, onVolver }) => {
  const toast = useRef<Toast>(null);

  // ✅ LEER DESDE REDUX — el Home ya tiene la suscripción activa
  const todosLosEquipos = useAppSelector(getDashboardEquipos) as WsEquipo[];
  const isConnected = useAppSelector(getDashboardConnected);

  // 📝 Hook para escribir en el PLC
  const [writingAddress, setWritingAddress] = useState<number | null>(null);

  const { writeBoolean, writeNumeric } = useWriteVariable(equipo.id, {
    onSuccess(result) {
      setWritingAddress(null);
      toast.current?.show({
        severity: 'success',
        summary: 'Enviado al PLC',
        detail: result.message,
        life: 2500,
      });
    },
    onError(error) {
      setWritingAddress(null);
      toast.current?.show({
        severity: 'error',
        summary: 'Error al escribir',
        detail: error?.message || 'No se pudo enviar el comando',
        life: 3000,
      });
    },
  });

  // Filtrar solo el equipo que el usuario seleccionó
  const wsEquipo = todosLosEquipos?.find(e => e.id === equipo.id);
  const variables = wsEquipo?.variables ?? [];
  const estado = wsEquipo?.estado ?? equipo.estado;

  // Separar por tipo de variable
  // separar variables en tres grupos sin duplicar
  // sólo las booleanas de sólo lectura van al panel de bits; las "escribibles"
  // se muestran exclusivamente en la sección de escritura.
  const bits = variables.filter(v => v.valorBooleano !== null && v.esLectura).sort((a, b) => a.dir - b.dir);
  const registros = variables
    .filter(v => v.valorNumerico !== null && v.valorBooleano === null && v.esLectura)
    .sort((a, b) => a.dir - b.dir);
  const escritura = variables.filter(v => !v.esLectura).sort((a, b) => a.dir - b.dir);

  // Timestamp de última actualización
  const [lastUpdate, setLastUpdate] = useState<string>('--:--:--');
  useEffect(() => {
    if (wsEquipo) {
      setLastUpdate(new Date().toLocaleTimeString('es-ES'));
    }
  }, [wsEquipo]);

  // ── Handlers ─────────────────────────────────────────
  const dispatch = useAppDispatch();

  const handleToggleBit = useCallback(
    (nombre: string, address: number, val: boolean) => {
      // actualizar el store inmediatamente para que el interruptor cambie
      dispatch(dashboardVariableWritten({ equipoId: equipo.id, dir: address, value: val }));
      setWritingAddress(address);
      writeBoolean(nombre, address, val);
    },
    [writeBoolean, dispatch, equipo.id],
  );

  const handleWrite = useCallback(
    (nombre: string, address: number, valor: number) => {
      dispatch(dashboardVariableWritten({ equipoId: equipo.id, dir: address, value: valor }));
      setWritingAddress(address);
      writeNumeric(nombre, address, valor);
    },
    [writeNumeric, dispatch, equipo.id],
  );

  // ── Toolbar ───────────────────────────────────────────
  const toolbarLeft = (
    <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
      <Button icon="pi pi-arrow-left" label="Volver" text onClick={onVolver} size="small" />
      <Divider layout="vertical" style={{ margin: '0 4px', height: 30 }} />
      <div>
        <div style={{ fontSize: 17, fontWeight: 700, color: '#1e293b' }}>{equipo.nombre}</div>
        <div style={{ fontSize: 11, color: '#94a3b8', fontFamily: 'monospace' }}>
          ID: {equipo.id} · {equipo.direccionIp}
        </div>
      </div>
    </div>
  );

  const toolbarRight = (
    <div style={{ display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap' }}>
      {/* Badge WebSocket */}
      <span
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: 6,
          padding: '3px 10px',
          borderRadius: 999,
          fontSize: 11,
          fontWeight: 600,
          background: isConnected ? '#22c55e22' : '#ef444422',
          color: isConnected ? '#16a34a' : '#dc2626',
          border: `1px solid ${isConnected ? '#22c55e' : '#ef4444'}`,
        }}
      >
        <span style={{ width: 7, height: 7, borderRadius: '50%', background: 'currentColor', display: 'inline-block' }} />
        {isConnected ? 'En vivo' : 'Sin señal'}
      </span>

      <span style={{ fontSize: 11, color: '#94a3b8', fontFamily: 'monospace' }}>
        <i className="pi pi-clock" style={{ marginRight: 4 }} />
        {lastUpdate}
      </span>

      <Chip
        label={`${bits.length} bits`}
        icon="pi pi-circle-fill"
        style={{ fontSize: 11, background: '#f0fdf4', color: '#166534', border: '1px solid #bbf7d0' }}
      />
      <Chip
        label={`${registros.length} registros`}
        icon="pi pi-sliders-h"
        style={{ fontSize: 11, background: '#eff6ff', color: '#1e40af', border: '1px solid #bfdbfe' }}
      />

      <Tag value={estado} severity={estadoSeverity(estado)} icon="pi pi-circle-fill" />
    </div>
  );

  const sectionHeader = (title: string, count: number, severity: any, icon: string) => (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
      <i className={`pi ${icon}`} style={{ fontSize: 16, color: '#64748b' }} />
      <span style={{ fontSize: 15, fontWeight: 700 }}>{title}</span>
      <Tag value={`${count}`} severity={severity} rounded style={{ fontSize: 11 }} />
    </div>
  );

  return (
    <div style={{ background: '#f8fafc', minHeight: '100vh', paddingBottom: 40 }}>
      <Toast ref={toast} />

      <Toolbar
        start={toolbarLeft}
        end={toolbarRight}
        style={{ background: '#fff', borderRadius: 0, borderBottom: '1px solid #e2e8f0', padding: '10px 24px', marginBottom: 24 }}
      />

      <div style={{ padding: '0 24px', display: 'flex', flexDirection: 'column', gap: 20 }}>
        {/* Sin datos aún */}
        {variables.length === 0 && (
          <Message
            severity={isConnected ? 'info' : 'warn'}
            text={isConnected ? 'Esperando datos del PLC...' : 'WebSocket no conectado. Verifica la conexión.'}
          />
        )}

        {/* ── BITS BOOLEANOS ── */}
        {bits.length > 0 && (
          <Panel
            header={sectionHeader('Variables Booleanas', bits.length, 'success', 'pi-circle-fill')}
            toggleable
            style={{ border: '1px solid #e2e8f0', borderRadius: 8 }}
          >
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(210px, 1fr))', gap: 12 }}>
              {bits.map(v => (
                <BitCard
                  key={`bit-${v.dir}-${v.nombreVariable}`}
                  variable={v}
                  escribible={!v.esLectura}
                  isWriting={writingAddress === v.dir}
                  onToggle={(name, _address, val) => handleToggleBit(name, v.dir, val)}
                />
              ))}
            </div>
          </Panel>
        )}

        {/* ── REGISTROS NUMÉRICOS ── */}
        {registros.length > 0 && (
          <Panel
            header={sectionHeader('Registros Numéricos', registros.length, 'info', 'pi-chart-bar')}
            toggleable
            style={{ border: '1px solid #e2e8f0', borderRadius: 8 }}
          >
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(210px, 1fr))', gap: 12 }}>
              {registros.map(v => (
                <RegCard key={`reg-${v.dir}-${v.nombreVariable}`} variable={v} />
              ))}
            </div>
          </Panel>
        )}

        {/* ── ESCRITURA ── */}
        {escritura.length > 0 && (
          <Panel
            header={sectionHeader('Variables de Escritura', escritura.length, 'warning', 'pi-pencil')}
            toggleable
            style={{ border: '1px solid #e2e8f0', borderRadius: 8 }}
          >
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: 12 }}>
              {escritura.map(v =>
                v.valorBooleano !== null ? (
                  <BitCard
                    key={`wr-bit-${v.dir}-${v.nombreVariable}`}
                    variable={v}
                    escribible={true}
                    isWriting={writingAddress === v.dir}
                    onToggle={(name, _address, val) => handleToggleBit(name, v.dir, val)}
                  />
                ) : (
                  <WriteCard
                    key={`wr-num-${v.dir}-${v.nombreVariable}`}
                    variable={v}
                    isLoading={writingAddress === v.dir}
                    onWrite={(name, _address, val) => handleWrite(name, v.dir, val)}
                  />
                ),
              )}
            </div>
          </Panel>
        )}
      </div>
    </div>
  );
};

export default EquipoDetalle;
