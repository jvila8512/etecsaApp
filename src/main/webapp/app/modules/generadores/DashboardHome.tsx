// src/main/webapp/app/modules/dashboard/DashboardHome.tsx

import React, { useEffect, useState, useRef } from 'react';
import { useAppSelector, useAppDispatch } from 'app/config/store';
import {
  getDashboardEquipos,
  getDashboardConnected,
  dashboardDataReceived,
  dashboardConnected,
  dashboardDisconnected,
  getDashboardHasReceivedData,
} from 'app/shared/reducers/dashboard-reducer';

import { DataView, DataViewLayoutOptions } from 'primereact/dataview';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Button } from 'primereact/button';
import { Skeleton } from 'primereact/skeleton';

// ── Tipos ────────────────────────────────────────────────────
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

interface DashboardHomeProps {
  onEntrar: (equipo: EquipoDTO) => void;
}

// ── Helpers ──────────────────────────────────────────────────
const getSeverity = (estado: string) => {
  if (estado === 'OPERATIVO') return 'success' as const;
  if (estado === 'DESCONECTADO') return 'danger' as const;
  if (estado === 'ERROR') return 'warning' as const;
  return 'info' as const;
};

const getEstadoColor = (estado: string) => {
  if (estado === 'OPERATIVO') return '#22c55e';
  if (estado === 'DESCONECTADO') return '#ef4444';
  if (estado === 'ERROR') return '#f59e0b';
  return '#94a3b8';
};

// ── Skeleton ─────────────────────────────────────────────────
const SkeletonCard = () => (
  <div style={styles.card}>
    <Skeleton width="60%" height="1.2rem" className="mb-2" />
    <Skeleton width="40%" height="0.9rem" className="mb-3" />
    <Skeleton width="30%" height="1.5rem" borderRadius="1rem" />
  </div>
);

// ── Template GRID ─────────────────────────────────────────────
const gridItemTemplate = (equipo: EquipoDTO, onEntrar: (e: EquipoDTO) => void) => {
  const activo = equipo.estado === 'OPERATIVO';
  return (
    <div className="col-12 col-sm-6 col-md-4 col-xl-3 p-2" key={equipo.id}>
      <div style={{ ...styles.card, borderLeft: `4px solid ${getEstadoColor(equipo.estado)}` }}>
        {/* Indicador pulsante */}
        <div style={styles.pulseWrapper}>
          <span
            style={{
              ...styles.pulse,
              backgroundColor: getEstadoColor(equipo.estado),
              boxShadow: activo ? `0 0 0 4px ${getEstadoColor(equipo.estado)}33` : 'none',
            }}
          />
        </div>

        <div style={styles.cardContent}>
          {/* Nombre */}
          <p style={styles.equipoNombre}>{equipo.nombre}</p>

          {/* IP */}
          <p style={styles.equipoIp}>
            <span style={styles.ipIcon}>⬡</span>
            {equipo.direccionIp}
          </p>

          {/* Estado */}
          <Tag
            value={equipo.estado}
            severity={getSeverity(equipo.estado)}
            style={{ fontSize: '0.72rem', letterSpacing: '0.05em', fontWeight: 700 }}
          />
        </div>

        {/* Botón Entrar — solo si OPERATIVO */}
        <div style={styles.cardFooter}>
          <Button
            icon={activo ? 'pi pi-arrow-right' : 'pi pi-ban'}
            rounded
            text
            size="small"
            disabled={!activo}
            severity={activo ? undefined : 'secondary'}
            onClick={() => activo && onEntrar(equipo)}
            style={{ width: '100%' }}
            tooltip={!activo ? `Equipo ${equipo.estado.toLowerCase()}` : 'Ver variables en tiempo real'}
            tooltipOptions={{ position: 'top' }}
          />
        </div>
      </div>
    </div>
  );
};

// ── Template LIST ─────────────────────────────────────────────
const listItemTemplate = (equipo: EquipoDTO, onEntrar: (e: EquipoDTO) => void) => {
  const activo = equipo.estado === 'OPERATIVO';
  return (
    <div
      key={equipo.id}
      style={{
        ...styles.listRow,
        borderLeft: `3px solid ${getEstadoColor(equipo.estado)}`,
      }}
    >
      <div style={styles.listDot}>
        <span style={{ ...styles.pulse, backgroundColor: getEstadoColor(equipo.estado) }} />
      </div>
      <span style={styles.listNombre}>{equipo.nombre}</span>
      <span style={styles.listIp}>{equipo.direccionIp}</span>
      <Tag value={equipo.estado} severity={getSeverity(equipo.estado)} style={{ fontSize: '0.7rem', fontWeight: 700 }} />
      <Button
        icon={activo ? 'pi pi-arrow-right' : 'pi pi-ban'}
        rounded
        text
        size="small"
        disabled={!activo}
        severity={activo ? undefined : 'secondary'}
        onClick={() => activo && onEntrar(equipo)}
        tooltip={!activo ? `Equipo ${equipo.estado.toLowerCase()}` : 'Ver variables en tiempo real'}
        tooltipOptions={{ position: 'top' }}
      />
    </div>
  );
};

// ══════════════════════════════════════════════════════════════
//  COMPONENTE PRINCIPAL
// ══════════════════════════════════════════════════════════════
const DashboardHome: React.FC<DashboardHomeProps> = ({ onEntrar }) => {
  const dispatch = useAppDispatch();
  const equipos = useAppSelector(getDashboardEquipos) as EquipoDTO[];
  const isConnected = useAppSelector(getDashboardConnected);
  const hasReceivedData = useAppSelector(getDashboardHasReceivedData);

  const [layout, setLayout] = useState<'grid' | 'list'>('grid');
  const [globalFilter, setGlobalFilter] = useState('');
  const subscriberRef = useRef<any>(null);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | undefined>();

  // Determinar si mostrar skeleton: solo si NO tenemos datos y NO están cargados aún
  const loading = !hasReceivedData && equipos.length === 0;

  // ── WebSocket ─────────────────────────────────────────────
  useEffect(() => {
    const onMessage = (message: any) => {
      try {
        const payload = JSON.parse(message.body);
        dispatch(dashboardDataReceived(payload));
      } catch (error) {
        console.error('❌ Error parsing WebSocket message:', error);
      }
    };

    // Solo suscribirse si WebSocket está conectado y no hay suscripción activa
    if (window['stompClient']?.connected && !subscriberRef.current) {
      dispatch(dashboardConnected());
      subscriberRef.current = window['stompClient'].subscribe('/topic/dashboard', onMessage);
    }

    // Timeout: Solo mostrar skeleton 3s si NO tenemos datos aún
    if (!hasReceivedData && !timeoutRef.current) {
      timeoutRef.current = setTimeout(() => {
        // Los skeletons se ocultarán automáticamente cuando lleguen datos
      }, 3000);
    }

    return () => {
      // Limpiar solo el timeout, NO la suscripción
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
        timeoutRef.current = undefined;
      }
      // NO desuscribirse al desmontar — mantener conexión
    };
  }, [dispatch, hasReceivedData]);

  // ── Filtro local ──────────────────────────────────────────
  const equiposFiltrados = globalFilter
    ? equipos?.filter(
        (e: EquipoDTO) =>
          e.nombre?.toLowerCase().includes(globalFilter.toLowerCase()) ||
          e.direccionIp?.includes(globalFilter) ||
          e.estado?.toLowerCase().includes(globalFilter.toLowerCase()),
      )
    : equipos;

  // ── Contadores ────────────────────────────────────────────
  const operativos = equipos?.filter(e => e.estado === 'OPERATIVO').length ?? 0;
  const desconectados = equipos?.filter(e => e.estado === 'DESCONECTADO').length ?? 0;
  const errores = equipos?.filter(e => e.estado === 'ERROR').length ?? 0;

  // ── Header DataView ───────────────────────────────────────
  const header = () => (
    <div style={styles.dvHeader}>
      <div style={styles.dvHeaderLeft}>
        <span style={styles.dvTitle}>Monitoreo en Tiempo Real</span>

        {/* Badge WebSocket */}
        <span
          style={{
            ...styles.wsBadge,
            backgroundColor: isConnected ? '#22c55e22' : '#ef444422',
            color: isConnected ? '#16a34a' : '#dc2626',
            border: `1px solid ${isConnected ? '#22c55e' : '#ef4444'}`,
          }}
        >
          <span style={{ ...styles.wsDot, backgroundColor: isConnected ? '#22c55e' : '#ef4444' }} />
          {isConnected ? 'WebSocket activo' : 'Sin conexión'}
        </span>

        {/* Contadores */}
        {equipos?.length > 0 && (
          <>
            <span style={styles.countBadge}>{equipos.length} equipos</span>
            {operativos > 0 && (
              <span style={{ ...styles.countBadge, background: '#dcfce7', color: '#166534' }}>{operativos} operativos</span>
            )}
            {errores > 0 && <span style={{ ...styles.countBadge, background: '#fef9c3', color: '#854d0e' }}>{errores} errores</span>}
            {desconectados > 0 && (
              <span style={{ ...styles.countBadge, background: '#fee2e2', color: '#991b1b' }}>{desconectados} desconect.</span>
            )}
          </>
        )}
      </div>

      <div style={styles.dvHeaderRight}>
        <span className="p-input-icon-left">
          <i className="pi pi-search" style={{ color: '#94a3b8' }} />
          <InputText
            value={globalFilter}
            onChange={e => setGlobalFilter(e.target.value)}
            placeholder="Buscar equipo, IP o estado..."
            style={styles.searchInput}
          />
        </span>
        <DataViewLayoutOptions layout={layout} onChange={e => setLayout(e.value as 'grid' | 'list')} />
      </div>
    </div>
  );

  // ── Skeletons ─────────────────────────────────────────────
  const skeletons = Array.from({ length: 8 });

  return (
    <div style={styles.pageWrapper}>
      <div style={styles.dashWrapper}>
        {loading ? (
          <>
            {header()}
            <div className="row p-2">
              {skeletons.map((_, i) => (
                <div key={i} className="col-12 col-sm-6 col-md-4 col-xl-3 p-2">
                  <SkeletonCard />
                </div>
              ))}
            </div>
          </>
        ) : (
          <DataView
            value={equiposFiltrados}
            itemTemplate={item => (layout === 'grid' ? gridItemTemplate(item, onEntrar) : listItemTemplate(item, onEntrar))}
            layout={layout}
            header={header()}
            paginator
            rows={40}
            rowsPerPageOptions={[20, 40, 100]}
            emptyMessage="Sin equipos disponibles"
            style={{ border: 'none', background: 'transparent' }}
          />
        )}
      </div>
    </div>
  );
};

// ── Estilos ───────────────────────────────────────────────────
const styles: Record<string, React.CSSProperties> = {
  pageWrapper: {
    minHeight: '100vh',
    background: '#f1f5f9',
    padding: '1.5rem',
    fontFamily: "'Inter', sans-serif",
  },
  dashWrapper: {
    background: '#ffffff',
    borderRadius: '16px',
    boxShadow: '0 1px 3px rgba(0,0,0,0.08), 0 8px 32px rgba(0,0,0,0.04)',
    overflow: 'hidden',
  },
  dvHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: '1rem',
    padding: '1rem 1.25rem',
    borderBottom: '1px solid #e2e8f0',
    background: '#f8fafc',
  },
  dvHeaderLeft: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
    flexWrap: 'wrap',
  },
  dvHeaderRight: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.75rem',
  },
  dvTitle: {
    fontWeight: 700,
    fontSize: '1rem',
    color: '#0f172a',
    letterSpacing: '-0.01em',
  },
  wsBadge: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '6px',
    padding: '3px 10px',
    borderRadius: '999px',
    fontSize: '0.72rem',
    fontWeight: 600,
  },
  wsDot: {
    width: 7,
    height: 7,
    borderRadius: '50%',
    display: 'inline-block',
  },
  countBadge: {
    background: '#e2e8f0',
    color: '#475569',
    borderRadius: '999px',
    padding: '2px 10px',
    fontSize: '0.72rem',
    fontWeight: 600,
  },
  searchInput: {
    height: '36px',
    fontSize: '0.85rem',
    borderRadius: '8px',
    border: '1px solid #e2e8f0',
    paddingLeft: '2.2rem',
    width: '220px',
  },
  // ── Tarjeta grid ──
  card: {
    background: '#ffffff',
    border: '1px solid #e2e8f0',
    borderRadius: '12px',
    padding: '1rem 1rem 0.75rem 1.1rem',
    position: 'relative',
    transition: 'box-shadow 0.2s, transform 0.2s',
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
    gap: '0.25rem',
  },
  pulseWrapper: {
    position: 'absolute',
    top: '1rem',
    right: '1rem',
  },
  pulse: {
    width: 10,
    height: 10,
    borderRadius: '50%',
    display: 'inline-block',
  },
  cardContent: {
    display: 'flex',
    flexDirection: 'column',
    gap: '0.35rem',
    flex: 1,
  },
  cardFooter: {
    marginTop: '0.75rem',
    paddingTop: '0.75rem',
    borderTop: '1px solid #f1f5f9',
  },
  equipoNombre: {
    margin: 0,
    fontWeight: 700,
    fontSize: '0.92rem',
    color: '#0f172a',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    maxWidth: '85%',
  },
  equipoIp: {
    margin: 0,
    fontSize: '0.78rem',
    color: '#64748b',
    fontFamily: 'monospace',
    display: 'flex',
    alignItems: 'center',
    gap: '5px',
  },
  ipIcon: {
    fontSize: '0.7rem',
    color: '#94a3b8',
  },
  // ── Fila list ──
  listRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '1rem',
    padding: '0.6rem 1rem',
    borderBottom: '1px solid #f1f5f9',
    background: '#fff',
    transition: 'background 0.15s',
  },
  listDot: {
    width: 20,
    display: 'flex',
    justifyContent: 'center',
  },
  listNombre: {
    fontWeight: 600,
    fontSize: '0.88rem',
    color: '#0f172a',
    flex: 2,
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
  },
  listIp: {
    fontFamily: 'monospace',
    fontSize: '0.8rem',
    color: '#64748b',
    flex: 1,
  },
};

export default DashboardHome;
