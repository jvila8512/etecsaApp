import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import { JhiPagination, TextFormat, Translate, getPaginationState, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  Box,
  Button,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Menu,
  MenuItem,
  Checkbox,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Toolbar,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import RefreshIcon from '@mui/icons-material/Refresh';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import SearchIcon from '@mui/icons-material/Search';
import ViewColumnIcon from '@mui/icons-material/ViewColumn';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import Spinner from '../loader/spinner';
import { getEntities, createEntity, updateEntity, deleteEntity } from './alarma.reducer';
import { IAlarma } from 'app/shared/model/alarma.model';
import { Severidad } from 'app/shared/model/enumerations/severidad.model';
import { EstadoAlarma } from 'app/shared/model/enumerations/estado-alarma.model';
import { getEntities as getEventoEquipos } from 'app/entities/evento-equipo/evento-equipo.reducer';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';

const ALL_COLUMNS = [
  { key: 'id', label: 'Id', default: true },
  { key: 'descripcion', label: 'Descripción', default: true },
  { key: 'mensajeUsuario', label: 'Mensaje', default: true },
  { key: 'severidad', label: 'Severidad', default: true },
  { key: 'estado', label: 'Estado', default: true },
  { key: 'evento', label: 'Variable', default: false },
  { key: 'activatedAt', label: 'Activada', default: true },
  { key: 'acknowledgedBy', label: 'Reconocida Por', default: false },
];

export const getSeveridadColor = (severidad: string): 'error' | 'warning' | 'info' | 'default' => {
  switch (severidad) {
    case 'CRITICA':
      return 'error';
    case 'ALTA':
      return 'warning';
    case 'MEDIA':
      return 'info';
    case 'BAJA':
      return 'default';
    default:
      return 'default';
  }
};

export const getEstadoColor = (estado: string): 'error' | 'warning' | 'success' => {
  switch (estado) {
    case 'ACTIVA':
      return 'error';
    case 'RECONOCIDA':
      return 'warning';
    case 'FINALIZADA':
      return 'success';
    default:
      return 'error';
  }
};

export const getEstadoLabel = (estado: string): string => {
  switch (estado) {
    case 'ACTIVA':
      return 'Activa';
    case 'RECONOCIDA':
      return 'Reconocida';
    case 'FINALIZADA':
      return 'Finalizada';
    default:
      return estado;
  }
};

export const formatValorAlarma = (valor: number | boolean, umbral: number | null, tipo: 'BOOLEAN' | 'NUMERICO'): string => {
  if (tipo === 'BOOLEAN') {
    return `Estado: ${valor ? 'ACTIVADO' : 'DESACTIVADO'}`;
  }
  const numValor = valor as number;
  if (umbral != null) {
    return `Valor: ${numValor.toFixed(2)} > Umbral: ${umbral.toFixed(2)}`;
  }
  return `Valor: ${numValor.toFixed(2)}`;
};

export const Alarma = () => {
  const dispatch = useAppDispatch();
  const pageLocation = useLocation();
  const navigate = useNavigate();

  const alarmaList = useAppSelector(state => state.alarma.entities);
  const loading = useAppSelector(state => state.alarma.loading);
  const updating = useAppSelector(state => state.alarma.updating);
  const updateSuccess = useAppSelector(state => state.alarma.updateSuccess);
  const totalItems = useAppSelector(state => state.alarma.totalItems);
  const eventoEquipos = useAppSelector(state => state.eventoEquipo.entities);
  const users = useAppSelector(state => state.userManagement.users);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'activatedAt'), pageLocation.search),
  );

  const formatDate = (dateValue: string | Date): string => {
    if (!dateValue) return '-';
    const date = typeof dateValue === 'string' ? new Date(dateValue) : dateValue;
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    return `${day}/${month}/${year} ${hours}:${minutes}`;
  };

  const [alarma, setAlarma] = useState<IAlarma | null>(null);
  const [selectedAlarma, setSelectedAlarma] = useState<IAlarma | null>(null);
  const [alarmaDialog, setAlarmaDialog] = useState(false);
  const [deleteAlarmaDialog, setDeleteAlarmaDialog] = useState(false);
  const [globalFilter, setGlobalFilterValue] = useState('');
  const [formKey, setFormKey] = useState(0);

  const [formValues, setFormValues] = useState({
    descripcion: '',
    severidad: 'BAJA',
    estado: 'ACTIVA',
    mensajeUsuario: '',
    activatedAt: '',
    deactivatedAt: '',
    evento: '',
    acknowledgedBy: '',
  });

  const severidadValues = Object.keys(Severidad);
  const estadoAlarmaValues = Object.keys(EstadoAlarma);

  const [columnMenuAnchor, setColumnMenuAnchor] = useState<null | HTMLElement>(null);
  const [visibleColumns, setVisibleColumns] = useState<string[]>(() => {
    const saved = localStorage.getItem('alarma-columns');
    if (saved) return JSON.parse(saved);
    return ALL_COLUMNS.filter(c => c.default).map(c => c.key);
  });
  const [dateFilter, setDateFilter] = useState<'all' | 'today' | 'week' | 'month'>('all');

  const saveColumns = (cols: string[]) => {
    setVisibleColumns(cols);
    localStorage.setItem('alarma-columns', JSON.stringify(cols));
  };

  const toggleColumn = (key: string) => {
    if (visibleColumns.includes(key)) {
      if (visibleColumns.length > 1) saveColumns(visibleColumns.filter(c => c !== key));
    } else {
      saveColumns([...visibleColumns, key]);
    }
  };

  const showColumn = (key: string) => visibleColumns.includes(key);

  const getDateFilterStart = (): Date | null => {
    const now = new Date();
    switch (dateFilter) {
      case 'today':
        return new Date(now.getFullYear(), now.getMonth(), now.getDate());
      case 'week':
        return new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
      case 'month':
        return new Date(now.getFullYear(), now.getMonth(), 1);
      default:
        return null;
    }
  };

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
    dispatch(getEventoEquipos({}));
    dispatch(getUsers({}));
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  useEffect(() => {
    if (updateSuccess) {
      setAlarmaDialog(false);
      setSelectedAlarma(null);
      setAlarma(null);
      setFormValues({
        descripcion: '',
        severidad: 'BAJA',
        estado: 'ACTIVA',
        mensajeUsuario: '',
        activatedAt: '',
        deactivatedAt: '',
        evento: '',
        acknowledgedBy: '',
      });
    }
  }, [updateSuccess]);

  const sort = (field: string) => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: field,
    });
  };

  useEffect(() => {
    if (!pageLocation.search) {
      setPaginationState(prev => ({ ...prev, sort: 'activatedAt', order: DESC }));
    }
  }, []);

  const handlePagination = (currentPage: number) => setPaginationState({ ...paginationState, activePage: currentPage });
  const handleSyncList = () => sortEntities();

  const onGlobalFilterChange = e => {
    setGlobalFilterValue(e.target.value);
  };

  const filteredList = alarmaList?.filter(a => {
    const matchesGlobalFilter =
      !globalFilter ||
      a.descripcion?.toLowerCase().includes(globalFilter.toLowerCase()) ||
      a.mensajeUsuario?.toLowerCase().includes(globalFilter.toLowerCase());

    const dateStart = getDateFilterStart();
    const matchesDateFilter = !dateStart || (a.activatedAt && new Date(a.activatedAt as unknown as string) >= dateStart);

    return matchesGlobalFilter && matchesDateFilter;
  });

  const verDialogNuevo = () => {
    setAlarma(null);
    setFormValues({
      descripcion: '',
      severidad: 'BAJA',
      estado: 'ACTIVA',
      mensajeUsuario: '',
      activatedAt: new Date().toISOString().slice(0, 16),
      deactivatedAt: '',
      evento: '',
      acknowledgedBy: '',
    });
    setFormKey(prev => prev + 1);
    setAlarmaDialog(true);
  };

  const actualizar = (rowData: IAlarma) => {
    setAlarma(rowData);
    setFormValues({
      descripcion: rowData.descripcion || '',
      severidad: rowData.severidad || 'BAJA',
      estado: rowData.estado || 'ACTIVA',
      mensajeUsuario: rowData.mensajeUsuario || '',
      activatedAt: rowData.activatedAt ? new Date(rowData.activatedAt as any).toISOString().slice(0, 16) : '',
      deactivatedAt: rowData.deactivatedAt ? new Date(rowData.deactivatedAt as any).toISOString().slice(0, 16) : '',
      evento: rowData.evento?.id?.toString() || '',
      acknowledgedBy: rowData.acknowledgedBy?.id?.toString() || '',
    });
    setFormKey(prev => prev + 1);
    setAlarmaDialog(true);
  };

  const hideDialogNuevo = () => {
    setAlarmaDialog(false);
    setSelectedAlarma(null);
    setAlarma(null);
    setFormValues({
      descripcion: '',
      severidad: 'BAJA',
      estado: 'ACTIVA',
      mensajeUsuario: '',
      activatedAt: '',
      deactivatedAt: '',
      evento: '',
      acknowledgedBy: '',
    });
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormValues(prev => ({ ...prev, [name]: value }));
  };

  const generarDescripcion = (): string => {
    const eventoSeleccionado = eventoEquipos?.find(e => String(e.id) === String(formValues.evento));
    if (eventoSeleccionado) {
      const nombreVar = eventoSeleccionado.nombreVariable || 'Variable';
      const umbral = eventoSeleccionado.umbralAlerta;
      const severidad = formValues.severidad;

      if (umbral != null) {
        return `${severidad}: ${nombreVar} > ${umbral}`;
      }
      return `${severidad}: ${nombreVar}`;
    }
    return `${formValues.severidad}: Alarma`;
  };

  const guardar = () => {
    const descripcionAuto = generarDescripcion();
    const entity: any = {
      descripcion: descripcionAuto,
      severidad: formValues.severidad,
      estado: formValues.estado,
      mensajeUsuario: formValues.mensajeUsuario || null,
      activatedAt: formValues.activatedAt ? new Date(formValues.activatedAt).toISOString() : null,
      deactivatedAt: formValues.deactivatedAt ? new Date(formValues.deactivatedAt).toISOString() : null,
    };

    if (alarma?.id) {
      entity.id = alarma.id;
    }

    if (formValues.evento) {
      entity.evento = eventoEquipos?.find(e => String(e.id) === String(formValues.evento));
    }
    if (formValues.acknowledgedBy) {
      entity.acknowledgedBy = users?.find(u => String(u.id) === String(formValues.acknowledgedBy));
    }

    if (alarma?.id) {
      dispatch(updateEntity(entity));
    } else {
      dispatch(createEntity(entity));
    }
  };

  const verEliminar = (rowData: IAlarma) => {
    setSelectedAlarma(rowData);
    setDeleteAlarmaDialog(true);
  };

  const deleteAlarma = () => {
    if (!selectedAlarma?.id) return;
    dispatch(deleteEntity(selectedAlarma.id));
    setDeleteAlarmaDialog(false);
    setSelectedAlarma(null);
  };

  const hideDeleteDialog = () => {
    setDeleteAlarmaDialog(false);
    setSelectedAlarma(null);
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Toolbar sx={{ justifyContent: 'space-between', mb: 2, px: 0 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Typography variant="h5" sx={{ fontWeight: 700 }}>
            <Translate contentKey="appsupervisorApp.alarma.home.title">Alarmas</Translate>
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button variant="outlined" startIcon={<RefreshIcon />} onClick={handleSyncList} disabled={loading}>
            Actualizar
          </Button>
          <Button variant="outlined" startIcon={<ViewColumnIcon />} onClick={e => setColumnMenuAnchor(e.currentTarget)}>
            Columnas
          </Button>
          <Menu anchorEl={columnMenuAnchor} open={Boolean(columnMenuAnchor)} onClose={() => setColumnMenuAnchor(null)}>
            {ALL_COLUMNS.map(col => (
              <MenuItem key={col.key} onClick={() => toggleColumn(col.key)}>
                <Checkbox checked={visibleColumns.includes(col.key)} size="small" />
                {col.label}
              </MenuItem>
            ))}
          </Menu>
          <Button variant="contained" startIcon={<AddIcon />} onClick={verDialogNuevo}>
            Nueva Alarma
          </Button>
        </Box>
      </Toolbar>

      <Box sx={{ display: 'flex', gap: 2, mb: 2 }}>
        <TextField
          size="small"
          placeholder="Buscar..."
          value={globalFilter}
          onChange={onGlobalFilterChange}
          InputProps={{
            startAdornment: <SearchIcon sx={{ color: 'text.secondary', mr: 1 }} />,
          }}
          sx={{ width: 300 }}
        />
        <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
          <Typography variant="body2" color="text.secondary">
            Fecha:
          </Typography>
          {(['all', 'today', 'week', 'month'] as const).map(opt => (
            <Chip
              key={opt}
              label={opt === 'all' ? 'Todas' : opt === 'today' ? 'Hoy' : opt === 'week' ? 'Semana' : 'Mes'}
              size="small"
              variant={dateFilter === opt ? 'filled' : 'outlined'}
              color={dateFilter === opt ? 'primary' : 'default'}
              onClick={() => setDateFilter(opt)}
              sx={{ cursor: 'pointer' }}
            />
          ))}
        </Box>
      </Box>

      <TableContainer sx={{ overflowX: 'auto' }}>
        <Table sx={{ minWidth: 900 }}>
          <TableHead>
            <TableRow sx={{ bgcolor: '#f8fafc' }}>
              {showColumn('id') && <TableCell sx={{ fontWeight: 600 }}>Id</TableCell>}
              {showColumn('descripcion') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('descripcion')}>
                  Descripción
                </TableCell>
              )}
              {showColumn('mensajeUsuario') && <TableCell sx={{ fontWeight: 600 }}>Mensaje</TableCell>}
              {showColumn('severidad') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('severidad')}>
                  Severidad
                </TableCell>
              )}
              {showColumn('estado') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('estado')}>
                  Estado
                </TableCell>
              )}
              {showColumn('evento') && <TableCell sx={{ fontWeight: 600 }}>Variable</TableCell>}
              {showColumn('activatedAt') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('activatedAt')}>
                  Activada
                </TableCell>
              )}
              {showColumn('acknowledgedBy') && <TableCell sx={{ fontWeight: 600 }}>Reconocida Por</TableCell>}
              <TableCell sx={{ fontWeight: 600 }}>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredList?.map((a: IAlarma) => (
              <TableRow
                key={a.id}
                hover
                sx={{
                  bgcolor:
                    a.estado === 'ACTIVA' ? 'rgba(239, 68, 68, 0.05)' : a.estado === 'RECONOCIDA' ? 'rgba(245, 158, 11, 0.05)' : 'inherit',
                }}
              >
                {showColumn('id') && <TableCell>{a.id}</TableCell>}
                {showColumn('descripcion') && <TableCell sx={{ fontWeight: 500 }}>{a.descripcion}</TableCell>}
                {showColumn('mensajeUsuario') && (
                  <TableCell>
                    <Typography variant="body2" color={a.mensajeUsuario ? 'text.primary' : 'text.disabled'}>
                      {a.mensajeUsuario || '-'}
                    </Typography>
                  </TableCell>
                )}
                {showColumn('severidad') && (
                  <TableCell>
                    <Chip label={a.severidad} color={getSeveridadColor(a.severidad || 'BAJA')} size="small" variant="outlined" />
                  </TableCell>
                )}
                {showColumn('estado') && (
                  <TableCell>
                    <Chip label={getEstadoLabel(a.estado || 'ACTIVA')} color={getEstadoColor(a.estado || 'ACTIVA')} size="small" />
                  </TableCell>
                )}
                {showColumn('evento') && (
                  <TableCell>
                    <Typography variant="body2">{a.evento?.nombreVariable || '-'}</Typography>
                  </TableCell>
                )}
                {showColumn('activatedAt') && <TableCell>{a.activatedAt ? formatDate(a.activatedAt as unknown as string) : '-'}</TableCell>}
                {showColumn('acknowledgedBy') && (
                  <TableCell>
                    <Typography variant="body2">{a.acknowledgedBy?.login || '-'}</Typography>
                  </TableCell>
                )}
                <TableCell>
                  <Box sx={{ display: 'flex', gap: 0.5 }}>
                    <IconButton size="small" color="warning" onClick={() => actualizar(a)}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton size="small" color="error" onClick={() => verEliminar(a)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {totalItems && alarmaList && alarmaList.length > 0 ? (
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'center' }}>
          <JhiPagination
            activePage={paginationState.activePage}
            onSelect={handlePagination}
            maxButtons={5}
            itemsPerPage={paginationState.itemsPerPage}
            totalItems={totalItems}
          />
        </Box>
      ) : null}

      <Dialog open={alarmaDialog} onClose={() => {}} maxWidth="sm" fullWidth>
        <DialogTitle>{alarma ? 'Editar Alarma' : 'Nueva Alarma'}</DialogTitle>
        <DialogContent>
          {loading ? (
            <Spinner />
          ) : (
            <form key={formKey}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                <TextField
                  label={translate('appsupervisorApp.alarma.descripcion')}
                  id="alarma-descripcion"
                  name="descripcion"
                  value={generarDescripcion()}
                  fullWidth
                  InputProps={{ readOnly: true }}
                  helperText="Se genera automáticamente según variable y severidad"
                />
                <TextField
                  select
                  label={translate('appsupervisorApp.alarma.evento')}
                  id="alarma-evento"
                  name="evento"
                  value={formValues.evento}
                  onChange={handleInputChange}
                  fullWidth
                  SelectProps={{ native: true }}
                >
                  <option value="">-- Seleccionar Variable --</option>
                  {eventoEquipos?.map(e => (
                    <option value={e.id} key={e.id}>
                      {e.nombreVariable} {e.equipo?.nombre ? `(${e.equipo.nombre})` : ''}
                    </option>
                  ))}
                </TextField>
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <TextField
                    select
                    label={translate('appsupervisorApp.alarma.severidad')}
                    id="alarma-severidad"
                    name="severidad"
                    value={formValues.severidad}
                    onChange={handleInputChange}
                    fullWidth
                    sx={{ flex: 1 }}
                    SelectProps={{ native: true }}
                  >
                    {severidadValues.map(s => (
                      <option value={s} key={s}>
                        {s}
                      </option>
                    ))}
                  </TextField>
                  <TextField
                    select
                    label={translate('appsupervisorApp.alarma.estado')}
                    id="alarma-estado"
                    name="estado"
                    value={formValues.estado}
                    onChange={handleInputChange}
                    fullWidth
                    sx={{ flex: 1 }}
                    SelectProps={{ native: true }}
                  >
                    {estadoAlarmaValues.map(e => (
                      <option value={e} key={e}>
                        {e}
                      </option>
                    ))}
                  </TextField>
                </Box>
                <TextField
                  label={translate('appsupervisorApp.alarma.mensajeUsuario')}
                  id="alarma-mensajeUsuario"
                  name="mensajeUsuario"
                  value={formValues.mensajeUsuario}
                  onChange={handleInputChange}
                  fullWidth
                />
                <TextField
                  label={translate('appsupervisorApp.alarma.activatedAt')}
                  id="alarma-activatedAt"
                  name="activatedAt"
                  type="datetime-local"
                  value={formValues.activatedAt}
                  onChange={handleInputChange}
                  fullWidth
                  required
                  error={!formValues.activatedAt}
                  helperText={!formValues.activatedAt ? translate('entity.validation.required') : ''}
                  InputLabelProps={{ shrink: true }}
                />
                <TextField
                  label={translate('appsupervisorApp.alarma.deactivatedAt')}
                  id="alarma-deactivatedAt"
                  name="deactivatedAt"
                  type="datetime-local"
                  value={formValues.deactivatedAt}
                  onChange={handleInputChange}
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                />
                <TextField
                  select
                  label={translate('appsupervisorApp.alarma.acknowledgedBy')}
                  id="alarma-acknowledgedBy"
                  name="acknowledgedBy"
                  value={formValues.acknowledgedBy}
                  onChange={handleInputChange}
                  fullWidth
                  SelectProps={{ native: true }}
                >
                  <option value="">--</option>
                  {users?.map(u => (
                    <option value={u.id} key={u.id}>
                      {u.login}
                    </option>
                  ))}
                </TextField>
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 2 }}>
                  <Button onClick={hideDialogNuevo}>Cancelar</Button>
                  <Button variant="contained" onClick={guardar} disabled={updating || !formValues.activatedAt}>
                    <FontAwesomeIcon icon="save" />
                    &nbsp;Guardar
                  </Button>
                </Box>
              </Box>
            </form>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={deleteAlarmaDialog} onClose={hideDeleteDialog} maxWidth="xs" fullWidth>
        <DialogTitle>Confirmar</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 1 }}>
            <WarningAmberIcon sx={{ fontSize: 40, color: '#f59e0b' }} />
            {selectedAlarma && (
              <Typography>
                ¿Seguro que quiere eliminar la Alarma: <strong>{selectedAlarma.descripcion}</strong>?
              </Typography>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={hideDeleteDialog}>No</Button>
          <Button onClick={deleteAlarma} color="error" variant="contained">
            Sí
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default Alarma;
