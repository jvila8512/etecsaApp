import React, { useState, useRef, useCallback, useEffect } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Divider,
  Grid,
  IconButton,
  LinearProgress,
  Snackbar,
  Alert,
  Switch,
  TextField,
  Toolbar,
  Typography,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  CircularProgress,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import SendIcon from '@mui/icons-material/Send';
import ToggleOnIcon from '@mui/icons-material/ToggleOn';
import ToggleOffIcon from '@mui/icons-material/ToggleOff';
import CircleIcon from '@mui/icons-material/Circle';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { useAppDispatch } from 'app/config/store';
import { dashboardVariableWritten } from 'app/shared/reducers/dashboard-reducer';
import { useAppSelector } from 'app/config/store';
import { getDashboardEquipos, getDashboardConnected } from 'app/shared/reducers/dashboard-reducer';
import { EquipoDTO } from './types';
import { useWriteVariable } from './hooks/useWriteVariable';
import { connectGeneradores } from 'app/websocket/generadores-websocket';

// ══════════════════════════════════════════════════════════
//  TIPOS WebSocket
// ══════════════════════════════════════════════════════════
interface WsVariable {
  nombreVariable: string;
  dir: number;
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
const estadoColor = (estado: string) => {
  if (estado === 'OPERATIVO') return 'success' as const;
  if (estado === 'ERROR') return 'error' as const;
  return 'default' as const;
};

const fmtNumerico = (v: number | null | undefined): string => {
  if (v === null || v === undefined) return '---';
  return Math.round(v).toLocaleString('es-ES');
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

  return (
    <Card
      sx={{
        borderLeft: `4px solid ${isOn ? '#22c55e' : '#cbd5e1'}`,
        transition: 'border-color 0.2s',
      }}
    >
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
          <Box>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {variable.nombreVariable}
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mt: 0.25 }}>
              <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
                BOOLEAN
              </Typography>
              {escribible && <Chip label="WRITE" size="small" color="info" sx={{ height: 18, fontSize: '0.65rem' }} />}
            </Box>
          </Box>
          <Chip
            icon={isOn ? <CheckCircleIcon sx={{ fontSize: 14 }} /> : <CircleIcon sx={{ fontSize: 14 }} />}
            label={isOn ? 'ON' : 'OFF'}
            color={isOn ? 'success' : 'error'}
            size="small"
            sx={{ fontWeight: 600 }}
          />
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 1 }}>
          <Typography variant="caption" color="text.secondary">
            {escribible ? 'Accionable' : 'Solo lectura'}
          </Typography>
          <Switch
            checked={isOn}
            disabled={!escribible || isWriting}
            onChange={e => escribible && onToggle(variable.nombreVariable, variable.dir, e.target.checked)}
            size="small"
          />
        </Box>
      </CardContent>
    </Card>
  );
};

// ══════════════════════════════════════════════════════════
//  TARJETA REGISTRO NUMÉRICO
// ══════════════════════════════════════════════════════════
const RegCard: React.FC<{ variable: WsVariable }> = ({ variable }) => {
  const val = variable.valorNumerico;
  const pct = Math.round(calcPct(val));
  const showKnob = val !== null && val !== undefined && variable.unidadMedida === '%';

  return (
    <Card sx={{ borderLeft: '4px solid #3b82f6' }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
          <Box>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {variable.nombreVariable}
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
              NUMÉRICO
            </Typography>
          </Box>
          {showKnob && (
            <Box sx={{ position: 'relative', width: 48, height: 48 }}>
              <CircularProgress variant="determinate" value={pct} size={48} thickness={4} sx={{ color: '#3b82f6' }} />
              <Box
                sx={{
                  position: 'absolute',
                  top: 0,
                  left: 0,
                  bottom: 0,
                  right: 0,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
              >
                <Typography variant="caption" sx={{ fontWeight: 600, fontSize: '0.65rem' }}>
                  {pct}%
                </Typography>
              </Box>
            </Box>
          )}
        </Box>
        <Box sx={{ display: 'flex', alignItems: 'baseline', gap: 0.5, mb: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 700, fontFamily: 'monospace', lineHeight: 1 }}>
            {fmtNumerico(val)}
          </Typography>
          {variable.unidadMedida && (
            <Typography variant="body2" color="text.secondary">
              {variable.unidadMedida}
            </Typography>
          )}
        </Box>
        <LinearProgress
          variant="determinate"
          value={pct}
          sx={{ height: 4, borderRadius: 2, bgcolor: '#e2e8f0', '& .MuiLinearProgress-bar': { bgcolor: '#3b82f6' } }}
        />
      </CardContent>
    </Card>
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
    <Card
      sx={{
        borderLeft: `4px solid ${isLoading ? '#f59e0b' : '#3b82f6'}`,
        opacity: isLoading ? 0.7 : 1,
        transition: 'opacity 0.2s',
      }}
    >
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Typography variant="body2" sx={{ fontWeight: 600, mb: 0.5 }}>
          {variable.nombreVariable}
        </Typography>
        <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
          ESCRITURA · {variable.unidadMedida ?? '---'}
        </Typography>
        <Box sx={{ display: 'flex', gap: 1, mt: 1.5 }}>
          <TextField
            size="small"
            type="number"
            value={inputVal}
            onChange={e => !isLoading && setInputVal(Number(e.target.value))}
            disabled={isLoading}
            placeholder="Valor..."
            sx={{ flex: 1, '& input': { fontFamily: 'monospace', fontSize: '0.85rem' } }}
          />
          <Button
            variant="contained"
            size="small"
            disabled={isLoading}
            onClick={() => onWrite(variable.nombreVariable, variable.dir, inputVal)}
            sx={{ minWidth: 40 }}
          >
            {isLoading ? <CircularProgress size={18} color="inherit" /> : <SendIcon fontSize="small" />}
          </Button>
        </Box>
      </CardContent>
    </Card>
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
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: 'success' | 'error' }>({
    open: false,
    message: '',
    severity: 'success',
  });

  const todosLosEquipos = useAppSelector(getDashboardEquipos) as WsEquipo[];
  const isConnected = useAppSelector(getDashboardConnected);
  const [writingAddress, setWritingAddress] = useState<number | null>(null);

  const { writeBoolean, writeNumeric } = useWriteVariable(equipo.id, {
    onSuccess(result) {
      setWritingAddress(null);
      setSnackbar({ open: true, message: result.message || 'Enviado al PLC', severity: 'success' });
    },
    onError(error) {
      setWritingAddress(null);
      setSnackbar({ open: true, message: error?.message || 'No se pudo enviar el comando', severity: 'error' });
    },
  });

  const wsEquipo = todosLosEquipos?.find(e => e.id === equipo.id);
  const variables = wsEquipo?.variables ?? [];
  const estado = wsEquipo?.estado ?? equipo.estado;

  const bits = variables.filter(v => v.valorBooleano !== null && v.esLectura).sort((a, b) => a.dir - b.dir);
  const registros = variables
    .filter(v => v.valorNumerico !== null && v.valorBooleano === null && v.esLectura)
    .sort((a, b) => a.dir - b.dir);
  const escritura = variables.filter(v => !v.esLectura).sort((a, b) => a.dir - b.dir);

  const [lastUpdate, setLastUpdate] = useState<string>('--:--:--');

  // Conectar al WebSocket de generadores para poder escribir
  useEffect(() => {
    connectGeneradores();
  }, []);

  useEffect(() => {
    if (wsEquipo) {
      setLastUpdate(new Date().toLocaleTimeString('es-ES'));
    }
  }, [wsEquipo]);

  const dispatch = useAppDispatch();

  const handleToggleBit = useCallback(
    (nombre: string, address: number, val: boolean) => {
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

  const sectionHeader = (title: string, count: number, color: string) => (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
      <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
        {title}
      </Typography>
      <Chip label={count} size="small" sx={{ bgcolor: color + '22', color, fontWeight: 600 }} />
    </Box>
  );

  return (
    <Box sx={{ bgcolor: '#f8fafc', minHeight: '100vh', pb: 4 }}>
      <Snackbar open={snackbar.open} autoHideDuration={3000} onClose={() => setSnackbar(s => ({ ...s, open: false }))}>
        <Alert severity={snackbar.severity} onClose={() => setSnackbar(s => ({ ...s, open: false }))}>
          {snackbar.message}
        </Alert>
      </Snackbar>

      {/* Toolbar */}
      <Toolbar
        sx={{
          bgcolor: '#fff',
          borderBottom: '1px solid #e2e8f0',
          mb: 3,
          px: { xs: 2, md: 3 },
          display: 'flex',
          justifyContent: 'space-between',
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Button startIcon={<ArrowBackIcon />} onClick={onVolver} size="small">
            Volver
          </Button>
          <Divider orientation="vertical" flexItem />
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              {equipo.nombre}
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
              ID: {equipo.id} · {equipo.direccionIp}
            </Typography>
          </Box>
        </Box>

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Chip
            icon={isConnected ? <CircleIcon sx={{ fontSize: 10 }} /> : <CircleIcon sx={{ fontSize: 10 }} />}
            label={isConnected ? 'En vivo' : 'Sin señal'}
            color={isConnected ? 'success' : 'error'}
            size="small"
            variant="outlined"
          />
          <Typography variant="caption" color="text.secondary" sx={{ fontFamily: 'monospace' }}>
            {lastUpdate}
          </Typography>
          <Chip label={`${bits.length} bits`} size="small" sx={{ bgcolor: '#f0fdf4', color: '#166534' }} />
          <Chip label={`${registros.length} registros`} size="small" sx={{ bgcolor: '#eff6ff', color: '#1e40af' }} />
          <Chip label={estado} color={estadoColor(estado)} size="small" />
        </Box>
      </Toolbar>

      <Box sx={{ px: { xs: 2, md: 3 } }}>
        {/* Sin datos */}
        {variables.length === 0 && (
          <Alert severity={isConnected ? 'info' : 'warning'} sx={{ mb: 2 }}>
            {isConnected ? 'Esperando datos del Sitio...' : 'WebSocket no conectado. Verifica la conexión.'}
          </Alert>
        )}

        {/* BITS BOOLEANOS */}
        {bits.length > 0 && (
          <Accordion defaultExpanded sx={{ mb: 2, borderRadius: 2, '&:before': { display: 'none' } }}>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              {sectionHeader('Variables Booleanas', bits.length, '#22c55e')}
            </AccordionSummary>
            <AccordionDetails>
              <Grid container spacing={1.5}>
                {bits.map(v => (
                  <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={`bit-${v.dir}-${v.nombreVariable}`}>
                    <BitCard
                      variable={v}
                      escribible={!v.esLectura}
                      isWriting={writingAddress === v.dir}
                      onToggle={(name, _address, val) => handleToggleBit(name, v.dir, val)}
                    />
                  </Grid>
                ))}
              </Grid>
            </AccordionDetails>
          </Accordion>
        )}

        {/* REGISTROS NUMÉRICOS */}
        {registros.length > 0 && (
          <Accordion defaultExpanded sx={{ mb: 2, borderRadius: 2, '&:before': { display: 'none' } }}>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              {sectionHeader('Registros Numéricos', registros.length, '#3b82f6')}
            </AccordionSummary>
            <AccordionDetails>
              <Grid container spacing={1.5}>
                {registros.map(v => (
                  <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={`reg-${v.dir}-${v.nombreVariable}`}>
                    <RegCard variable={v} />
                  </Grid>
                ))}
              </Grid>
            </AccordionDetails>
          </Accordion>
        )}

        {/* ESCRITURA */}
        {escritura.length > 0 && (
          <Accordion defaultExpanded sx={{ borderRadius: 2, '&:before': { display: 'none' } }}>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              {sectionHeader('Variables de Escritura', escritura.length, '#f59e0b')}
            </AccordionSummary>
            <AccordionDetails>
              <Grid container spacing={1.5}>
                {escritura.map(v =>
                  v.valorBooleano !== null ? (
                    <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={`wr-bit-${v.dir}-${v.nombreVariable}`}>
                      <BitCard
                        variable={v}
                        escribible={true}
                        isWriting={writingAddress === v.dir}
                        onToggle={(name, _address, val) => handleToggleBit(name, v.dir, val)}
                      />
                    </Grid>
                  ) : (
                    <Grid size={{ xs: 12, sm: 6, md: 4, lg: 3 }} key={`wr-num-${v.dir}-${v.nombreVariable}`}>
                      <WriteCard
                        variable={v}
                        isLoading={writingAddress === v.dir}
                        onWrite={(name, _address, val) => handleWrite(name, v.dir, val)}
                      />
                    </Grid>
                  ),
                )}
              </Grid>
            </AccordionDetails>
          </Accordion>
        )}
      </Box>
    </Box>
  );
};

export default EquipoDetalle;
