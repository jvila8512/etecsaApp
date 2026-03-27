import React, { useEffect, useState, useRef } from 'react';
import {
  Box,
  Card,
  CardContent,
  CardActions,
  Chip,
  Grid,
  InputAdornment,
  Skeleton,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ViewModuleIcon from '@mui/icons-material/ViewModule';
import ViewListIcon from '@mui/icons-material/ViewList';
import ArrowForwardIcon from '@mui/icons-material/ArrowForward';
import BlockIcon from '@mui/icons-material/Block';
import WifiIcon from '@mui/icons-material/Wifi';
import WifiOffIcon from '@mui/icons-material/WifiOff';
import { useAppSelector, useAppDispatch } from 'app/config/store';
import {
  getDashboardEquipos,
  getDashboardConnected,
  dashboardDataReceived,
  dashboardConnected,
  getDashboardHasReceivedData,
} from 'app/shared/reducers/dashboard-reducer';

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
const getEstadoColor = (estado: string) => {
  if (estado === 'OPERATIVO') return '#22c55e';
  if (estado === 'DESCONECTADO') return '#ef4444';
  if (estado === 'ERROR') return '#f59e0b';
  return '#94a3b8';
};

const getChipColor = (estado: string) => {
  if (estado === 'OPERATIVO') return 'success' as const;
  if (estado === 'DESCONECTADO') return 'error' as const;
  if (estado === 'ERROR') return 'warning' as const;
  return 'default' as const;
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

  const loading = !hasReceivedData && equipos.length === 0;

  // ── WebSocket - El middleware ya suscribe, no necesitamos duplicar ──
  useEffect(() => {
    if (!hasReceivedData && !timeoutRef.current) {
      timeoutRef.current = setTimeout(() => {}, 3000);
    }

    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
        timeoutRef.current = undefined;
      }
    };
  }, [hasReceivedData]);

  const equiposFiltrados = globalFilter
    ? equipos?.filter(
        (e: EquipoDTO) =>
          e.nombre?.toLowerCase().includes(globalFilter.toLowerCase()) ||
          e.direccionIp?.includes(globalFilter) ||
          e.estado?.toLowerCase().includes(globalFilter.toLowerCase()),
      )
    : equipos;

  const operativos = equipos?.filter(e => e.estado === 'OPERATIVO').length ?? 0;
  const desconectados = equipos?.filter(e => e.estado === 'DESCONECTADO').length ?? 0;
  const errores = equipos?.filter(e => e.estado === 'ERROR').length ?? 0;

  const skeletons = Array.from({ length: 8 });

  return (
    <Box>
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 2,
          mb: 2,
          p: 2,
          bgcolor: '#ffffff',
          borderRadius: 2,
          border: '1px solid #e2e8f0',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5, flexWrap: 'wrap' }}>
          <Typography variant="h6" sx={{ fontWeight: 700 }}>
            Monitoreo en Tiempo Real
          </Typography>
          <Chip
            icon={isConnected ? <WifiIcon sx={{ fontSize: 14 }} /> : <WifiOffIcon sx={{ fontSize: 14 }} />}
            label={isConnected ? 'WebSocket activo' : 'Sin conexión'}
            color={isConnected ? 'success' : 'error'}
            size="small"
            variant="outlined"
          />
          {equipos?.length > 0 && (
            <>
              <Chip label={`${equipos.length} equipos`} size="small" sx={{ bgcolor: '#e2e8f0', color: '#475569' }} />
              {operativos > 0 && <Chip label={`${operativos} operativos`} size="small" sx={{ bgcolor: '#dcfce7', color: '#166534' }} />}
              {errores > 0 && <Chip label={`${errores} errores`} size="small" sx={{ bgcolor: '#fef9c3', color: '#854d0e' }} />}
              {desconectados > 0 && (
                <Chip label={`${desconectados} desconect.`} size="small" sx={{ bgcolor: '#fee2e2', color: '#991b1b' }} />
              )}
            </>
          )}
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <TextField
            size="small"
            placeholder="Buscar equipo, IP o estado..."
            value={globalFilter}
            onChange={e => setGlobalFilter(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon sx={{ color: '#94a3b8' }} />
                </InputAdornment>
              ),
            }}
            sx={{ width: 260 }}
          />
          <ToggleButtonGroup value={layout} exclusive onChange={(_, v) => v && setLayout(v)} size="small">
            <ToggleButton value="grid">
              <ViewModuleIcon />
            </ToggleButton>
            <ToggleButton value="list">
              <ViewListIcon />
            </ToggleButton>
          </ToggleButtonGroup>
        </Box>
      </Box>

      {/* Content */}
      {loading ? (
        <Grid container spacing={2}>
          {skeletons.map((_, i) => (
            <Grid size={{ xs: 12, sm: 6, md: 4, xl: 3 }} key={i}>
              <Card sx={{ p: 2 }}>
                <Skeleton width="60%" height={24} />
                <Skeleton width="40%" height={20} sx={{ mt: 1 }} />
                <Skeleton width="30%" height={32} sx={{ mt: 1 }} />
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : layout === 'grid' ? (
        <Grid container spacing={2}>
          {equiposFiltrados?.map((equipo: EquipoDTO) => {
            const activo = equipo.estado === 'OPERATIVO';
            return (
              <Grid size={{ xs: 12, sm: 6, md: 4, xl: 3 }} key={equipo.id}>
                <Card
                  sx={{
                    borderLeft: `4px solid ${getEstadoColor(equipo.estado)}`,
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    transition: 'box-shadow 0.2s, transform 0.2s',
                    '&:hover': { boxShadow: '0 4px 20px rgba(0,0,0,0.1)' },
                  }}
                >
                  <CardContent sx={{ flex: 1 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                      <Typography
                        variant="subtitle1"
                        sx={{ fontWeight: 700, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                      >
                        {equipo.nombre}
                      </Typography>
                      <Box
                        sx={{
                          width: 10,
                          height: 10,
                          borderRadius: '50%',
                          bgcolor: getEstadoColor(equipo.estado),
                          boxShadow: activo ? `0 0 0 4px ${getEstadoColor(equipo.estado)}33` : 'none',
                        }}
                      />
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 1 }}>
                      <Typography variant="body2" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
                        {equipo.direccionIp}
                      </Typography>
                    </Box>
                    <Chip
                      label={equipo.estado}
                      color={getChipColor(equipo.estado)}
                      size="small"
                      sx={{ fontWeight: 700, fontSize: '0.7rem' }}
                    />
                  </CardContent>
                  <CardActions sx={{ px: 2, pb: 2 }}>
                    <Box
                      onClick={() => activo && onEntrar(equipo)}
                      sx={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        width: '100%',
                        py: 0.75,
                        borderRadius: 1,
                        bgcolor: activo ? '#2563eb' : '#e2e8f0',
                        color: activo ? '#fff' : '#94a3b8',
                        cursor: activo ? 'pointer' : 'default',
                        fontWeight: 600,
                        fontSize: '0.85rem',
                        transition: 'background 0.2s',
                        '&:hover': activo ? { bgcolor: '#1d4ed8' } : {},
                      }}
                    >
                      {activo ? <ArrowForwardIcon sx={{ fontSize: 18, mr: 0.5 }} /> : <BlockIcon sx={{ fontSize: 18, mr: 0.5 }} />}
                      {activo ? 'Entrar' : equipo.estado.toLowerCase()}
                    </Box>
                  </CardActions>
                </Card>
              </Grid>
            );
          })}
        </Grid>
      ) : (
        <Card>
          {equiposFiltrados?.map((equipo: EquipoDTO) => {
            const activo = equipo.estado === 'OPERATIVO';
            return (
              <Box
                key={equipo.id}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 2,
                  p: 1.5,
                  borderBottom: '1px solid #f1f5f9',
                  borderLeft: `3px solid ${getEstadoColor(equipo.estado)}`,
                  '&:hover': { bgcolor: '#f8fafc' },
                }}
              >
                <Box sx={{ width: 10, height: 10, borderRadius: '50%', bgcolor: getEstadoColor(equipo.estado) }} />
                <Typography sx={{ fontWeight: 600, flex: 2 }}>{equipo.nombre}</Typography>
                <Typography sx={{ fontFamily: 'monospace', color: 'text.secondary', flex: 1 }}>{equipo.direccionIp}</Typography>
                <Chip label={equipo.estado} color={getChipColor(equipo.estado)} size="small" sx={{ fontWeight: 700, fontSize: '0.7rem' }} />
                <Box
                  onClick={() => activo && onEntrar(equipo)}
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 0.5,
                    px: 2,
                    py: 0.5,
                    borderRadius: 1,
                    bgcolor: activo ? '#eff6ff' : '#f1f5f9',
                    color: activo ? '#2563eb' : '#94a3b8',
                    cursor: activo ? 'pointer' : 'default',
                    fontSize: '0.8rem',
                    fontWeight: 600,
                  }}
                >
                  {activo ? <ArrowForwardIcon sx={{ fontSize: 16 }} /> : <BlockIcon sx={{ fontSize: 16 }} />}
                </Box>
              </Box>
            );
          })}
        </Card>
      )}
    </Box>
  );
};

export default DashboardHome;
