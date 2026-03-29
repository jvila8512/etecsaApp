import './home.scss';

import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Chip,
  Grid,
  InputAdornment,
  Skeleton,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import ViewModuleIcon from '@mui/icons-material/ViewModule';
import ViewListIcon from '@mui/icons-material/ViewList';
import ComputerIcon from '@mui/icons-material/Computer';
import WifiIcon from '@mui/icons-material/Wifi';
import WifiOffIcon from '@mui/icons-material/WifiOff';
import LockIcon from '@mui/icons-material/Lock';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import { useAppSelector, useAppDispatch } from 'app/config/store';

import {
  getDashboardEquipos,
  getDashboardConnected,
  getDashboardHasReceivedData,
  dashboardDataReceived,
  dashboardConnected,
  dashboardDisconnected,
} from 'app/shared/reducers/dashboard-reducer';

// ─── Helpers ────────────────────────────────────────────────

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

// ─── Vista NO logueado ───────────────────────────────────────

const NotLoggedView = () => (
  <Box
    sx={{
      minHeight: '80vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
    }}
  >
    <Card sx={{ maxWidth: 380, textAlign: 'center', p: 3, borderRadius: 4 }}>
      <LockIcon sx={{ fontSize: 48, color: '#94a3b8', mb: 2 }} />
      <Typography variant="h5" sx={{ fontWeight: 800, mb: 1 }}>
        Acceso Restringido
      </Typography>
      <Typography color="text.secondary" sx={{ mb: 3 }}>
        Debes iniciar sesión para ver el monitoreo en tiempo real de los equipos.
      </Typography>
      <Box
        component={Link}
        to="/login"
        sx={{
          display: 'inline-block',
          bgcolor: '#2563eb',
          color: '#fff',
          px: 4,
          py: 1.5,
          borderRadius: 2,
          fontWeight: 700,
          textDecoration: 'none',
          '&:hover': { bgcolor: '#1d4ed8' },
        }}
      >
        Iniciar Sesión
      </Box>
    </Card>
  </Box>
);

// ─── Componente principal ────────────────────────────────────

export const Home = () => {
  const dispatch = useAppDispatch();
  const account = useAppSelector(state => state.authentication.account);
  const equipos = useAppSelector(getDashboardEquipos);
  const isConnected = useAppSelector(getDashboardConnected);

  const hasReceivedData = useAppSelector(getDashboardHasReceivedData);
  const [loading, setLoading] = useState(true);
  const [layout, setLayout] = useState<'grid' | 'list'>('grid');
  const [globalFilter, setGlobalFilter] = useState('');
  const [helpOpen, setHelpOpen] = useState(false);

  useEffect(() => {
    if (hasReceivedData) {
      setLoading(false);
    } else {
      const timeout = setTimeout(() => setLoading(false), 8000);
      return () => clearTimeout(timeout);
    }
  }, [hasReceivedData]);

  useEffect(() => {
    if (!account?.login) return;

    if (window['stompClient']?.connected) {
      dispatch(dashboardConnected());
    }

    const checkConnection = setInterval(() => {
      if (window['stompClient']?.connected) {
        dispatch(dashboardConnected());
        clearInterval(checkConnection);
      }
    }, 1000);

    return () => {
      clearInterval(checkConnection);
    };
  }, [account?.login, dispatch]);

  const equiposFiltrados = globalFilter
    ? equipos?.filter(
        (e: any) =>
          e.nombre?.toLowerCase().includes(globalFilter.toLowerCase()) ||
          e.direccionIp?.includes(globalFilter) ||
          e.estado?.toLowerCase().includes(globalFilter.toLowerCase()),
      )
    : equipos;

  if (!account?.login) {
    return <NotLoggedView />;
  }

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
          {equipos?.length > 0 && <Chip label={`${equipos.length} equipos`} size="small" sx={{ bgcolor: '#e2e8f0', color: '#475569' }} />}
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
          <IconButton onClick={() => setHelpOpen(true)} color="primary" title="Ayuda sobre intervalos">
            <HelpOutlineIcon />
          </IconButton>
        </Box>
      </Box>

      {/* Dialogo de Ayuda */}
      <Dialog open={helpOpen} onClose={() => setHelpOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Sistema de Monitoreo - Intervalos de Lectura</DialogTitle>
        <DialogContent>
          <Typography variant="body2" sx={{ mb: 2 }}>
            El sistema optimiza automáticamente los intervalos de lectura según el tipo de variable y equipo:
          </Typography>
          <Table size="small">
            <TableHead>
              <TableRow sx={{ bgcolor: '#f1f5f9' }}>
                <TableCell>
                  <strong>Tipo</strong>
                </TableCell>
                <TableCell>
                  <strong>Intervalo</strong>
                </TableCell>
                <TableCell>
                  <strong>Descripción</strong>
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                <TableCell>
                  <strong>Booleana con Umbral</strong>
                </TableCell>
                <TableCell>
                  <Chip label="500ms" color="error" size="small" />
                </TableCell>
                <TableCell>Variables tipo BIT con umbral configurado (ej: puerta abierta, alarma)</TableCell>
              </TableRow>
              <TableRow>
                <TableCell>
                  <strong>Numérica con Umbral</strong>
                </TableCell>
                <TableCell>
                  <Chip label="2s" color="warning" size="small" />
                </TableCell>
                <TableCell>Variables numéricas con umbral (ej: presion, temperatura)</TableCell>
              </TableRow>
              <TableRow>
                <TableCell>
                  <strong>Equipo Critico</strong>
                </TableCell>
                <TableCell>
                  <Chip label="3s" color="default" size="small" />
                </TableCell>
                <TableCell>Equipos marcados como criticos en su configuracion</TableCell>
              </TableRow>
              <TableRow>
                <TableCell>
                  <strong>Normal</strong>
                </TableCell>
                <TableCell>
                  <Chip label="10s" size="small" />
                </TableCell>
                <TableCell>Otras variables sin configuracion especial</TableCell>
              </TableRow>
            </TableBody>
          </Table>
          <Typography variant="body2" sx={{ mt: 3, bgcolor: '#fef3c7', p: 2, borderRadius: 1 }}>
            <strong>Tip:</strong> Para que una alarma sea instantanea, marca el equipo como critico y configura un umbral de alerta en la
            variable.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setHelpOpen(false)} variant="contained">
            Entendido
          </Button>
        </DialogActions>
      </Dialog>

      {/* Grid / List */}
      {loading ? (
        <Grid container spacing={2}>
          {Array.from({ length: 8 }).map((_, i) => (
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
          {equiposFiltrados?.map((equipo: any) => (
            <Grid size={{ xs: 12, sm: 6, md: 4, xl: 3 }} key={equipo.id}>
              <Card
                sx={{
                  borderLeft: `4px solid ${getEstadoColor(equipo.estado)}`,
                  height: '100%',
                  transition: 'box-shadow 0.2s, transform 0.2s',
                  '&:hover': { boxShadow: '0 4px 20px rgba(0,0,0,0.1)' },
                }}
              >
                <CardContent>
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
                        boxShadow: equipo.estado === 'OPERATIVO' ? `0 0 0 4px ${getEstadoColor(equipo.estado)}33` : 'none',
                      }}
                    />
                  </Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 1 }}>
                    <ComputerIcon sx={{ fontSize: 14, color: '#94a3b8' }} />
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
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Card>
          {equiposFiltrados?.map((equipo: any) => (
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
            </Box>
          ))}
        </Card>
      )}
    </Box>
  );
};

export default Home;
