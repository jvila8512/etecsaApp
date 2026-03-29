import React, { useEffect, useState, useRef, useCallback, useMemo } from 'react';
import dayjs from 'dayjs';
import {
  Alert,
  AlertTitle,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Paper,
  Snackbar,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Toolbar,
  Tooltip,
  Typography,
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import DoneAllIcon from '@mui/icons-material/DoneAll';
import VolumeUpIcon from '@mui/icons-material/VolumeUp';
import VolumeOffIcon from '@mui/icons-material/VolumeOff';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';
import CheckIcon from '@mui/icons-material/Check';
import ErrorOutlineIcon from '@mui/icons-material/ErrorOutline';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { receiveDashboardActualizaciones } from 'app/websocket/dashboard-websocket';
import {
  connectAlarmas,
  disconnectAlarmas,
  receiveNuevaAlarma,
  receiveAlarmaReconocida,
  receiveAlarmaResuelta,
} from 'app/websocket/alarma-websocket';
import { getDashboardEquipos } from 'app/shared/reducers/dashboard-reducer';
import { getEntities as getAlarmas, reconocerAlarma, finalizarAlarma, procesarDeteccion, AlarmaDeteccion } from './alarma.reducer';
import { IAlarma } from 'app/shared/model/alarma.model';
import AlarmSound from '../alarm-sound/alarm-sound';

interface VariableData {
  id: number;
  nombreVariable: string;
  valorNumerico: number | null;
  valorBooleano: boolean | null;
  umbralAlerta: number | null;
  unidadMedida: string | null;
  habilitarAlarma: boolean | null;
}

interface EquipoData {
  id: number;
  nombre: string;
  estado: string;
  variables: VariableData[];
}

export const AlarmaMonitor = () => {
  const dispatch = useAppDispatch();
  const account = useAppSelector(state => state.authentication.account);
  const updating = useAppSelector(state => state.alarma.updating);

  const alarmaEntities = useAppSelector(state => state.alarma.entities);
  const dashboardData = useAppSelector(getDashboardEquipos) as EquipoData[];

  const [nuevasAlarmas, setNuevasAlarmas] = useState<IAlarma[]>([]);
  const [notificationOpen, setNotificationOpen] = useState(false);
  const [lastNotification, setLastNotification] = useState<IAlarma | null>(null);
  const [ackDialogOpen, setAckDialogOpen] = useState(false);
  const [resolveDialogOpen, setResolveDialogOpen] = useState(false);
  const [helpDialogOpen, setHelpDialogOpen] = useState(false);
  const [selectedAlarm, setSelectedAlarm] = useState<IAlarma | null>(null);
  const [resolveMessage, setResolveMessage] = useState('');
  const [showSuccessMsg, setShowSuccessMsg] = useState(false);
  const [successMsg, setSuccessMsg] = useState('');
  const [soundEnabled, setSoundEnabled] = useState(true);

  const procesamientoRef = useRef<boolean>(false);
  const lastProcessedRef = useRef<Map<string, number>>(new Map());
  const [refreshKey, setRefreshKey] = useState(0);
  const pendingAlarmsRef = useRef<Set<number>>(new Set());

  // Combinar alarmas del backend con las nuevas que aún no están en el estado Redux
  const todasLasAlarmas = useMemo(() => {
    const deRedux = alarmaEntities?.filter(a => a.estado === 'ACTIVA' || a.estado === 'RECONOCIDA') || [];
    // Filtrar las nuevas alarmas que no están en Redux
    const nuevasFiltradas = nuevasAlarmas.filter(nueva => !deRedux.some(r => r.id === nueva.id));
    return [...deRedux, ...nuevasFiltradas];
  }, [alarmaEntities, nuevasAlarmas]);

  const alarmasActivas = todasLasAlarmas;
  const hasUnacknowledged = alarmasActivas.some(a => a.estado === 'ACTIVA');

  // Crear mapa de valores actuales: eventoId -> boolean (esta en alarma?)
  // Solo considera variables con habilitarAlarma = true
  const valoresActualesMap = useMemo(() => {
    const map = new Map<number, boolean>();
    dashboardData.forEach(equipo => {
      if (equipo.estado !== 'OPERATIVO') return;
      equipo.variables?.forEach(variable => {
        // Solo mostrar estado de alarma si tiene habilitarAlarma
        if (!variable.habilitarAlarma) {
          return;
        }
        let isAlarm = false;
        if (variable.valorBooleano === true && variable.umbralAlerta != null) {
          isAlarm = true;
        } else if (variable.valorNumerico != null && variable.umbralAlerta != null && variable.valorNumerico > variable.umbralAlerta) {
          isAlarm = true;
        }
        map.set(variable.id, isAlarm);
      });
    });
    return map;
  }, [dashboardData]);

  const loadAlarmas = useCallback(() => {
    dispatch(getAlarmas({ page: 0, size: 1000, sort: 'activatedAt,desc' }));
  }, [dispatch]);

  // Cargar alarmas solo una vez al montar
  useEffect(() => {
    loadAlarmas();
  }, []);

  const procesarDeteccionAlarma = useCallback(
    async (equipo: EquipoData, variable: VariableData, tipo: 'BOOLEAN' | 'NUMERICO') => {
      const alarmKey = `${variable.id}`;
      const now = Date.now();
      const lastProcessed = lastProcessedRef.current.get(alarmKey) || 0;

      if (now - lastProcessed < 500) {
        return;
      }

      // Verificar si ya existe una alarma activa para este evento
      const yaExiste = alarmasActivas.some(a => a.evento?.id === variable.id);
      if (yaExiste) {
        return;
      }

      // Verificar si ya hay una llamada pendiente para este evento
      if (pendingAlarmsRef.current.has(variable.id)) {
        return;
      }

      lastProcessedRef.current.set(alarmKey, now);
      pendingAlarmsRef.current.add(variable.id);

      const deteccion: AlarmaDeteccion = {
        eventoId: variable.id,
        esAlarma: true,
        valorBooleano: tipo === 'BOOLEAN' ? (variable.valorBooleano ?? false) : undefined,
        valorActual: tipo === 'NUMERICO' ? (variable.valorNumerico ?? 0) : undefined,
        umbral: variable.umbralAlerta ?? undefined,
        severidad: 'ALTA',
        mensajeUsuario:
          tipo === 'BOOLEAN'
            ? `${variable.nombreVariable} en ${equipo.nombre} activado`
            : `${variable.nombreVariable} en ${equipo.nombre} superó el umbral de ${variable.umbralAlerta}`,
      };

      try {
        const result = await dispatch(procesarDeteccion(deteccion)).unwrap();
        if (result) {
          // Agregar inmediatamente al estado local
          setNuevasAlarmas(prev => [result, ...prev]);
          setLastNotification(result);
          setNotificationOpen(true);
          setRefreshKey(k => k + 1);
          // Recargar en background para sincronizar con el estado Redux
          loadAlarmas();
        }
      } catch (error) {
        console.error('Error procesando detección:', error);
      } finally {
        pendingAlarmsRef.current.delete(variable.id);
      }
    },
    [dispatch, loadAlarmas],
  );

  useEffect(() => {
    let mounted = true;

    // Suscripción a actualizaciones rápidas (solo para detectar alarmas)
    const subActualizaciones = receiveDashboardActualizaciones().subscribe({
      next({ equipos }) {
        if (!mounted) return;
        if (!equipos || procesamientoRef.current) return;

        procesamientoRef.current = true;

        const processVariables = async () => {
          for (const equipo of equipos) {
            if (!mounted || equipo.estado !== 'OPERATIVO') continue;

            for (const variable of equipo.variables || []) {
              if (!mounted) break;

              let isAlarm = false;
              let tipo: 'BOOLEAN' | 'NUMERICO' = 'NUMERICO';

              if (variable.valorBooleano != null && variable.valorBooleano === true && variable.umbralAlerta != null) {
                isAlarm = true;
                tipo = 'BOOLEAN';
              } else if (
                variable.valorNumerico != null &&
                variable.umbralAlerta != null &&
                variable.valorNumerico > variable.umbralAlerta
              ) {
                isAlarm = true;
                tipo = 'NUMERICO';
              }

              if (isAlarm) {
                await procesarDeteccionAlarma(equipo, variable, tipo);
              }
            }
          }
          procesamientoRef.current = false;
        };

        processVariables();
      },
      error(err) {
        console.error('Error WebSocket:', err);
        procesamientoRef.current = false;
      },
    });

    return () => {
      mounted = false;
      subActualizaciones.unsubscribe();
    };
  }, [procesarDeteccionAlarma]);

  // Suscripción a notificaciones WebSocket de alarmas
  useEffect(() => {
    connectAlarmas();

    const subNueva = receiveNuevaAlarma().subscribe({
      next(alarma: IAlarma) {
        // Agregar al estado local inmediatamente
        setNuevasAlarmas(prev => {
          if (prev.some(a => a.id === alarma.id)) return prev;
          return [alarma, ...prev];
        });
        setLastNotification(alarma);
        setNotificationOpen(true);
        setRefreshKey(k => k + 1);
        // Recargar en background
        loadAlarmas();
      },
    });

    const subReconocida = receiveAlarmaReconocida().subscribe({
      next(alarma: IAlarma) {
        setSuccessMsg(`Alarma reconocida: ${alarma.descripcion || alarma.evento?.nombreVariable}`);
        setShowSuccessMsg(true);
        loadAlarmas();
      },
    });

    const subResuelta = receiveAlarmaResuelta().subscribe({
      next(alarma: IAlarma) {
        // Remover de las alarmas locales
        setNuevasAlarmas(prev => prev.filter(a => a.id !== alarma.id));
        setSuccessMsg(`Alarma resuelta: ${alarma.descripcion || alarma.evento?.nombreVariable}`);
        setShowSuccessMsg(true);
        loadAlarmas();
      },
    });

    return () => {
      subNueva.unsubscribe();
      subReconocida.unsubscribe();
      subResuelta.unsubscribe();
      disconnectAlarmas();
    };
  }, [loadAlarmas]);

  const handleAcknowledge = (alarma: IAlarma) => {
    if (alarma.estado !== 'ACTIVA') return;
    setSelectedAlarm(alarma);
    setAckDialogOpen(true);
  };

  const confirmAcknowledge = async () => {
    if (!selectedAlarm?.id) return;

    try {
      await dispatch(reconocerAlarma(selectedAlarm.id)).unwrap();
      setSuccessMsg(`Alarma reconocida: ${selectedAlarm.descripcion || selectedAlarm.evento?.nombreVariable}`);
      setShowSuccessMsg(true);
      loadAlarmas();
    } catch (error) {
      console.error('Error reconociendo alarma:', error);
    }

    setAckDialogOpen(false);
    setSelectedAlarm(null);
  };

  const handleResolve = (alarma: IAlarma) => {
    if (alarma.estado !== 'RECONOCIDA') return;
    setSelectedAlarm(alarma);
    setResolveMessage('');
    setResolveDialogOpen(true);
  };

  const confirmResolve = async () => {
    if (!selectedAlarm?.id) return;

    try {
      await dispatch(finalizarAlarma(selectedAlarm.id)).unwrap();
      setSuccessMsg(`Alarma resuelta: ${selectedAlarm.descripcion || selectedAlarm.evento?.nombreVariable}`);
      setShowSuccessMsg(true);
      loadAlarmas();
    } catch (error) {
      console.error('Error resolviendo alarma:', error);
    }

    setResolveDialogOpen(false);
    setSelectedAlarm(null);
    setResolveMessage('');
  };

  const handleIgnore = (alarma: IAlarma) => {
    if (alarma.estado !== 'RECONOCIDA') return;
    setSuccessMsg(`Alarma ignorada: ${alarma.descripcion || alarma.evento?.nombreVariable}`);
    setShowSuccessMsg(true);
  };

  const activasCount = alarmasActivas.filter(a => a.estado === 'ACTIVA').length;
  const reconocidasCount = alarmasActivas.filter(a => a.estado === 'RECONOCIDA').length;

  return (
    <Paper sx={{ p: 3 }} key={refreshKey}>
      <AlarmSound active={soundEnabled && hasUnacknowledged} />

      <Toolbar sx={{ justifyContent: 'space-between', mb: 2, px: 0 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>
            Monitor de Alarmas
          </Typography>
          {activasCount > 0 && (
            <Chip icon={<WarningAmberIcon />} label={`${activasCount} activa${activasCount > 1 ? 's' : ''}`} color="error" size="medium" />
          )}
          {reconocidasCount > 0 && (
            <Chip label={`${reconocidasCount} reconocida${reconocidasCount > 1 ? 's' : ''}`} color="warning" size="small" />
          )}
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title={soundEnabled ? 'Silenciar alarma' : 'Activar alarma'}>
            <IconButton onClick={() => setSoundEnabled(!soundEnabled)} color={soundEnabled ? 'primary' : 'default'}>
              {soundEnabled ? <VolumeUpIcon /> : <VolumeOffIcon />}
            </IconButton>
          </Tooltip>
          <Tooltip title="Recargar">
            <IconButton onClick={loadAlarmas}>
              <RefreshIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title="Ayuda">
            <IconButton onClick={() => setHelpDialogOpen(true)}>
              <HelpOutlineIcon />
            </IconButton>
          </Tooltip>
        </Box>
      </Toolbar>

      {alarmasActivas.length === 0 ? (
        <Alert severity="success" sx={{ mb: 2 }}>
          <AlertTitle>Sistema Normal</AlertTitle>
          No hay alarmas activas. El sistema está operando correctamente.
        </Alert>
      ) : (
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ bgcolor: '#f8fafc' }}>
                <TableCell sx={{ fontWeight: 600, width: 50 }}>Estado</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Fecha/Hora</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Descripción</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Severidad</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Estado Alarma</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Variable</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Acciones</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {alarmasActivas.map(alarma => {
                const eventoId = alarma.evento?.id;
                const valorEnAlarma = eventoId ? valoresActualesMap.get(eventoId) : undefined;
                const tieneDatos = valorEnAlarma !== undefined;
                const valorNormalizado = valorEnAlarma === false;

                return (
                  <TableRow
                    key={alarma.id}
                    hover
                    sx={{
                      bgcolor: alarma.estado === 'ACTIVA' ? 'rgba(239, 68, 68, 0.1)' : 'rgba(245, 158, 11, 0.1)',
                    }}
                  >
                    <TableCell>
                      <Tooltip
                        title={
                          !tieneDatos
                            ? 'Sin datos del sitio'
                            : valorNormalizado
                              ? 'Valor volvió a la normalidad'
                              : 'Valor en condición de alarma'
                        }
                      >
                        {!tieneDatos ? (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'grey.500' }}>
                            <HelpOutlineIcon fontSize="small" />
                            <Typography variant="caption">?</Typography>
                          </Box>
                        ) : valorNormalizado ? (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'success.main' }}>
                            <CheckIcon fontSize="small" />
                            <Typography variant="caption">OK</Typography>
                          </Box>
                        ) : (
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'error.main' }}>
                            <ErrorOutlineIcon fontSize="small" />
                            <Typography variant="caption">ALARMA</Typography>
                          </Box>
                        )}
                      </Tooltip>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                        {alarma.activatedAt ? dayjs(alarma.activatedAt as unknown as string).format('DD/MM/YYYY HH:mm:ss') : '-'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {alarma.descripcion || '-'}
                      </Typography>
                      {alarma.mensajeUsuario && (
                        <Typography variant="caption" color="text.secondary">
                          {alarma.mensajeUsuario}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={alarma.severidad || 'BAJA'}
                        color={alarma.severidad === 'CRITICA' ? 'error' : alarma.severidad === 'ALTA' ? 'warning' : 'info'}
                        size="small"
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={alarma.estado === 'ACTIVA' ? 'ACTIVA' : 'RECONOCIDA'}
                        color={alarma.estado === 'ACTIVA' ? 'error' : 'warning'}
                        size="small"
                        variant={alarma.estado === 'ACTIVA' ? 'filled' : 'outlined'}
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2">{alarma.evento?.nombreVariable || '-'}</Typography>
                      {alarma.acknowledgedBy && (
                        <Typography variant="caption" color="text.secondary">
                          Por: {alarma.acknowledgedBy.login}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', gap: 1 }}>
                        {alarma.estado === 'ACTIVA' && (
                          <Tooltip title="Reconocer">
                            <Button
                              size="small"
                              variant="outlined"
                              color="warning"
                              startIcon={<CheckCircleIcon />}
                              onClick={() => handleAcknowledge(alarma)}
                              disabled={updating}
                            >
                              ACK
                            </Button>
                          </Tooltip>
                        )}
                        {alarma.estado === 'RECONOCIDA' && (
                          <>
                            <Tooltip title="Resolver">
                              <Button
                                size="small"
                                variant="contained"
                                color="success"
                                startIcon={<DoneAllIcon />}
                                onClick={() => handleResolve(alarma)}
                                disabled={updating}
                              >
                                Resolver
                              </Button>
                            </Tooltip>
                            <Tooltip title="Ignorar">
                              <IconButton size="small" onClick={() => handleIgnore(alarma)}>
                                <VisibilityOffIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          </>
                        )}
                      </Box>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <Dialog open={ackDialogOpen} onClose={() => setAckDialogOpen(false)}>
        <DialogTitle>Reconocer Alarma</DialogTitle>
        <DialogContent>
          {selectedAlarm && (
            <Typography>
              ¿Reconocer la alarma: <strong>{selectedAlarm.descripcion || selectedAlarm.evento?.nombreVariable}</strong>?
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAckDialogOpen(false)}>Cancelar</Button>
          <Button onClick={confirmAcknowledge} color="warning" variant="contained" disabled={updating}>
            Reconocer
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={resolveDialogOpen} onClose={() => setResolveDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Resolver Alarma</DialogTitle>
        <DialogContent>
          {selectedAlarm && (
            <>
              <Typography sx={{ mb: 2 }}>
                Resolver la alarma: <strong>{selectedAlarm.descripcion || selectedAlarm.evento?.nombreVariable}</strong>?
              </Typography>
              <TextField
                label="Mensaje de resolución"
                value={resolveMessage}
                onChange={e => setResolveMessage(e.target.value)}
                fullWidth
                multiline
                rows={3}
                placeholder="Ej: Puerta cerrada y verificada."
              />
            </>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setResolveDialogOpen(false)}>Cancelar</Button>
          <Button onClick={confirmResolve} color="success" variant="contained" disabled={updating}>
            Resolver
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={helpDialogOpen} onClose={() => setHelpDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Ayuda - Monitor de Alarmas</DialogTitle>
        <DialogContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            <AlertTitle>Flujo de Alarmas</AlertTitle>
            ACTIVA → RECONOCIDA → FINALIZADA
          </Alert>
          <Typography variant="body2" sx={{ mb: 2 }}>
            <strong>Columna Estado:</strong>
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <ErrorOutlineIcon color="error" />
              <Typography variant="body2">ALARMA: El valor está en condición de alarma</Typography>
            </Box>
          </Box>
          <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <CheckIcon color="success" />
              <Typography variant="body2">OK: El valor volvió a la normalidad</Typography>
            </Box>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setHelpDialogOpen(false)}>Cerrar</Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={notificationOpen}
        autoHideDuration={5000}
        onClose={() => setNotificationOpen(false)}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert onClose={() => setNotificationOpen(false)} severity="error" variant="filled" icon={<NotificationsActiveIcon />}>
          {lastNotification && (
            <>
              <strong>¡Nueva Alarma!</strong>
              <br />
              {lastNotification.descripcion}
            </>
          )}
        </Alert>
      </Snackbar>

      <Snackbar
        open={showSuccessMsg}
        autoHideDuration={3000}
        onClose={() => setShowSuccessMsg(false)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'left' }}
      >
        <Alert severity="success" variant="filled">
          {successMsg}
        </Alert>
      </Snackbar>
    </Paper>
  );
};

export default AlarmaMonitor;
