import './home.scss';

import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Translate } from 'react-jhipster';
import { useAppSelector, useAppDispatch } from 'app/config/store';

import {
  getDashboardEquipos,
  getDashboardConnected,
  dashboardDataReceived,
  dashboardConnected,
  dashboardDisconnected,
} from 'app/shared/reducers/dashboard-reducer';

import { DataView, DataViewLayoutOptions } from 'primereact/dataview';
import { InputText } from 'primereact/inputtext';
import { Tag } from 'primereact/tag';
import { Skeleton } from 'primereact/skeleton';

// ─── Helpers ────────────────────────────────────────────────

const getSeverity = (estado: string) => {
  if (estado === 'OPERATIVO') return 'success';
  if (estado === 'DESCONECTADO') return 'danger';
  if (estado === 'ERROR') return 'warning';
  return 'info';
};

const getEstadoColor = (estado: string) => {
  if (estado === 'OPERATIVO') return '#22c55e';
  if (estado === 'DESCONECTADO') return '#ef4444';
  if (estado === 'ERROR') return '#f59e0b';
  return '#94a3b8';
};

// ─── Skeleton loader para cuando no hay datos aún ───────────

const SkeletonCard = () => (
  <div style={styles.card}>
    <Skeleton width="60%" height="1.2rem" className="mb-2" />
    <Skeleton width="40%" height="0.9rem" className="mb-3" />
    <Skeleton width="30%" height="1.5rem" borderRadius="1rem" />
  </div>
);

// ─── Template: Vista en GRID (tarjeta) ──────────────────────

const gridItemTemplate = (equipo: any) => (
  <div className="col-12 col-sm-6 col-md-4 col-xl-3 p-2" key={equipo.id}>
    <div style={{ ...styles.card, borderLeft: `4px solid ${getEstadoColor(equipo.estado)}` }}>
      {/* Indicador pulsante */}
      <div style={styles.pulseWrapper}>
        <span
          style={{
            ...styles.pulse,
            backgroundColor: getEstadoColor(equipo.estado),
            boxShadow: equipo.estado === 'OPERATIVO' ? `0 0 0 4px ${getEstadoColor(equipo.estado)}33` : 'none',
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
    </div>
  </div>
);

// ─── Template: Vista en LIST (fila) ─────────────────────────

const listItemTemplate = (equipo: any) => (
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
  </div>
);

// ─── Vista NO logueado ───────────────────────────────────────

const NotLoggedView = () => (
  <div style={styles.notLoggedWrapper}>
    <div style={styles.notLoggedCard}>
      <div style={styles.lockIcon}>🔒</div>
      <h2 style={styles.notLoggedTitle}>Acceso Restringido</h2>
      <p style={styles.notLoggedSubtitle}>Debes iniciar sesión para ver el monitoreo en tiempo real de los equipos.</p>
      <Link to="/login" style={styles.loginBtn}>
        Iniciar Sesión
      </Link>
    </div>
  </div>
);

// ─── Componente principal ────────────────────────────────────

export const Home = () => {
  const dispatch = useAppDispatch();
  const account = useAppSelector(state => state.authentication.account);
  const equipos = useAppSelector(getDashboardEquipos);
  const isConnected = useAppSelector(getDashboardConnected);

  const [layout, setLayout] = useState<'grid' | 'list'>('grid');
  const [globalFilter, setGlobalFilter] = useState('');
  const [loading, setLoading] = useState(true);

  // WebSocket
  useEffect(() => {
    const onMessage = (message: any) => {
      const payload = JSON.parse(message.body);
      dispatch(dashboardDataReceived(payload));
      setLoading(false);
    };

    if (window['stompClient']?.connected) {
      dispatch(dashboardConnected());
      window['stompClient'].subscribe('/topic/dashboard', onMessage);
    }

    // Si no llegan datos en 8s, quitar el skeleton igual
    const timeout = setTimeout(() => setLoading(false), 8000);

    return () => {
      dispatch(dashboardDisconnected());
      clearTimeout(timeout);
    };
  }, [dispatch]);

  // Filtrar equipos localmente
  const equiposFiltrados = globalFilter
    ? equipos?.filter(
        (e: any) =>
          e.nombre?.toLowerCase().includes(globalFilter.toLowerCase()) ||
          e.direccionIp?.includes(globalFilter) ||
          e.estado?.toLowerCase().includes(globalFilter.toLowerCase()),
      )
    : equipos;

  // Header del DataView
  const header = () => (
    <div style={styles.dvHeader}>
      <div style={styles.dvHeaderLeft}>
        <span style={styles.dvTitle}>Monitoreo en Tiempo Real</span>
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
        {equipos?.length > 0 && <span style={styles.countBadge}>{equipos.length} equipos</span>}
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

  // Render skeleton mientras carga
  const skeletons = Array.from({ length: 8 });

  return (
    <div style={styles.pageWrapper}>
      {!account?.login ? (
        <NotLoggedView />
      ) : (
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
              itemTemplate={item => (layout === 'grid' ? gridItemTemplate(item) : listItemTemplate(item))}
              layout={layout}
              header={header()}
              paginator
              rows={40}
              rowsPerPageOptions={[20, 40, 100]}
              style={{ border: 'none', background: 'transparent' }}
            />
          )}
        </div>
      )}
    </div>
  );
};

// ─── Estilos ─────────────────────────────────────────────────

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
    animation: 'pulse 2s infinite',
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
    padding: '1rem 1rem 1rem 1.1rem',
    position: 'relative',
    transition: 'box-shadow 0.2s, transform 0.2s',
    cursor: 'default',
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
  },
  equipoNombre: {
    margin: 0,
    fontWeight: 700,
    fontSize: '0.92rem',
    color: '#0f172a',
    whiteSpace: 'nowrap',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    maxWidth: '90%',
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
  // ── No logueado ──
  notLoggedWrapper: {
    minHeight: '80vh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  notLoggedCard: {
    background: '#fff',
    borderRadius: '20px',
    padding: '3rem 2.5rem',
    textAlign: 'center',
    boxShadow: '0 4px 32px rgba(0,0,0,0.08)',
    maxWidth: '380px',
    width: '100%',
  },
  lockIcon: {
    fontSize: '3rem',
    marginBottom: '1rem',
  },
  notLoggedTitle: {
    fontWeight: 800,
    fontSize: '1.4rem',
    color: '#0f172a',
    margin: '0 0 0.5rem',
  },
  notLoggedSubtitle: {
    color: '#64748b',
    fontSize: '0.9rem',
    marginBottom: '1.5rem',
  },
  loginBtn: {
    display: 'inline-block',
    background: '#0f172a',
    color: '#fff',
    padding: '0.65rem 2rem',
    borderRadius: '10px',
    fontWeight: 700,
    fontSize: '0.9rem',
    textDecoration: 'none',
    transition: 'background 0.2s',
  },
  emptyMsg: {
    textAlign: 'center',
    padding: '3rem',
    color: '#94a3b8',
  },
};

export default Home;
