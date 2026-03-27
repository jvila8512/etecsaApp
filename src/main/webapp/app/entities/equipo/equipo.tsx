import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import { JhiPagination, Translate, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
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
import { APP_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import Spinner from '../loader/spinner';
import { getEntities, createEntity, updateEntity, deleteEntity } from './equipo.reducer';
import { IEquipo } from 'app/shared/model/equipo.model';
import { EstadoEquipo } from 'app/shared/model/enumerations/estado-equipo.model';
import { getEntities as getSitios } from 'app/entities/sitio/sitio.reducer';

const ALL_COLUMNS = [
  { key: 'id', label: 'Id', default: true },
  { key: 'nombre', label: 'Nombre', default: true },
  { key: 'direccionIp', label: 'IP', default: true },
  { key: 'modelo', label: 'Modelo', default: false },
  { key: 'firmwareVersion', label: 'Firmware', default: false },
  { key: 'estado', label: 'Estado', default: true },
  { key: 'intervaloBase', label: 'Intervalo (seg)', default: true },
  { key: 'ultimoHeartbeat', label: 'Último Heartbeat', default: false },
  { key: 'sitio', label: 'Sitio', default: false },
];

export const Equipo = () => {
  const dispatch = useAppDispatch();
  const pageLocation = useLocation();
  const navigate = useNavigate();

  const equipoList = useAppSelector(state => state.equipo.entities);
  const loading = useAppSelector(state => state.equipo.loading);
  const updating = useAppSelector(state => state.equipo.updating);
  const updateSuccess = useAppSelector(state => state.equipo.updateSuccess);
  const totalItems = useAppSelector(state => state.equipo.totalItems);
  const sitios = useAppSelector(state => state.sitio.entities);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const [selectedEquipo, setSelectedEquipo] = useState<IEquipo | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [globalFilter, setGlobalFilterValue] = useState('');
  const [columnMenuAnchor, setColumnMenuAnchor] = useState<null | HTMLElement>(null);
  const [visibleColumns, setVisibleColumns] = useState<string[]>(() => {
    const saved = localStorage.getItem('equipo-columns');
    if (saved) return JSON.parse(saved);
    return ALL_COLUMNS.filter(c => c.default).map(c => c.key);
  });
  const [formKey, setFormKey] = useState(0);

  const [formValues, setFormValues] = useState({
    nombre: '',
    direccionIp: '',
    modbusSlaveId: '',
    modelo: '',
    firmwareVersion: '',
    estado: 'OPERATIVO',
    ultimoHeartbeat: '',
    intervaloBase: '10',
    sitio: '',
  });

  const estadoEquipoValues = Object.keys(EstadoEquipo);

  const saveColumns = (cols: string[]) => {
    setVisibleColumns(cols);
    localStorage.setItem('equipo-columns', JSON.stringify(cols));
  };

  const toggleColumn = (key: string) => {
    if (visibleColumns.includes(key)) {
      if (visibleColumns.length > 1) saveColumns(visibleColumns.filter(c => c !== key));
    } else {
      saveColumns([...visibleColumns, key]);
    }
  };

  const showColumn = (key: string) => visibleColumns.includes(key);

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
    dispatch(getSitios({}));
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({ ...paginationState, activePage: +page, sort: sortSplit[0], order: sortSplit[1] });
    }
  }, [pageLocation.search]);

  useEffect(() => {
    if (updateSuccess) {
      setIsOpen(false);
      setSelectedEquipo(null);
      setFormValues({
        nombre: '',
        direccionIp: '',
        modbusSlaveId: '',
        modelo: '',
        firmwareVersion: '',
        estado: 'OPERATIVO',
        ultimoHeartbeat: '',
        intervaloBase: '10',
        sitio: '',
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

  const handlePagination = (currentPage: number) => setPaginationState({ ...paginationState, activePage: currentPage });
  const handleSyncList = () => sortEntities();

  const onGlobalFilterChange = e => setGlobalFilterValue(e.target.value);

  const filteredList = equipoList?.filter(
    e => !globalFilter || e.nombre?.toLowerCase().includes(globalFilter.toLowerCase()) || e.direccionIp?.includes(globalFilter),
  );

  const verDialogNuevo = () => {
    setSelectedEquipo(null);
    setFormValues({
      nombre: '',
      direccionIp: '',
      modbusSlaveId: '',
      modelo: '',
      firmwareVersion: '',
      estado: 'OPERATIVO',
      ultimoHeartbeat: '',
      intervaloBase: '10',
      sitio: '',
    });
    setFormKey(prev => prev + 1);
    setIsOpen(true);
  };

  const actualizar = (rowData: IEquipo) => {
    setSelectedEquipo(rowData);
    setFormValues({
      nombre: rowData.nombre || '',
      direccionIp: rowData.direccionIp || '',
      modbusSlaveId: rowData.modbusSlaveId?.toString() || '',
      modelo: rowData.modelo || '',
      firmwareVersion: rowData.firmwareVersion || '',
      estado: rowData.estado || 'OPERATIVO',
      ultimoHeartbeat: rowData.ultimoHeartbeat ? new Date(rowData.ultimoHeartbeat as any).toISOString().slice(0, 16) : '',
      intervaloBase: rowData.intervaloBase?.toString() || '10',
      sitio: rowData.sitio?.id?.toString() || '',
    });
    setFormKey(prev => prev + 1);
    setIsOpen(true);
  };

  const hideDialogNuevo = () => {
    setIsOpen(false);
    setSelectedEquipo(null);
    setFormValues({
      nombre: '',
      direccionIp: '',
      modbusSlaveId: '',
      modelo: '',
      firmwareVersion: '',
      estado: 'OPERATIVO',
      ultimoHeartbeat: '',
      intervaloBase: '10',
      sitio: '',
    });
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormValues(prev => ({ ...prev, [name]: value }));
  };

  const guardar = () => {
    const entity: any = {
      nombre: formValues.nombre,
      direccionIp: formValues.direccionIp,
      modbusSlaveId: formValues.modbusSlaveId ? parseInt(formValues.modbusSlaveId, 10) : null,
      modelo: formValues.modelo || null,
      firmwareVersion: formValues.firmwareVersion || null,
      estado: formValues.estado,
      ultimoHeartbeat: formValues.ultimoHeartbeat ? dayjs(formValues.ultimoHeartbeat).toDate() : null,
      intervaloBase: formValues.intervaloBase ? parseInt(formValues.intervaloBase, 10) : 10,
    };

    if (selectedEquipo?.id) {
      entity.id = selectedEquipo.id;
    }

    if (formValues.sitio) {
      entity.sitio = sitios?.find(s => String(s.id) === String(formValues.sitio));
    }

    if (selectedEquipo?.id) {
      dispatch(updateEntity(entity));
    } else {
      dispatch(createEntity(entity));
    }
  };

  const verEliminar = (rowData: IEquipo) => {
    setSelectedEquipo(rowData);
    setIsDeleting(true);
  };

  const deleteEquipo = () => {
    if (!selectedEquipo?.id) return;
    dispatch(deleteEntity(selectedEquipo.id));
    setIsDeleting(false);
    setSelectedEquipo(null);
  };

  const hideDeleteDialog = () => {
    setIsDeleting(false);
    setSelectedEquipo(null);
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Toolbar sx={{ justifyContent: 'space-between', mb: 2, px: 0 }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Equipos
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button variant="outlined" startIcon={<ViewColumnIcon />} onClick={e => setColumnMenuAnchor(e.currentTarget)}>
            Columnas
          </Button>
          <Menu anchorEl={columnMenuAnchor} open={Boolean(columnMenuAnchor)} onClose={() => setColumnMenuAnchor(null)}>
            {ALL_COLUMNS.map(col => (
              <MenuItem key={col.key} onClick={() => toggleColumn(col.key)}>
                <ListItemIcon>
                  <Checkbox checked={visibleColumns.includes(col.key)} size="small" />
                </ListItemIcon>
                <ListItemText>{col.label}</ListItemText>
              </MenuItem>
            ))}
          </Menu>
          <Button variant="outlined" startIcon={<RefreshIcon />} onClick={handleSyncList} disabled={loading}>
            Actualizar
          </Button>
          <Button variant="contained" startIcon={<AddIcon />} onClick={verDialogNuevo}>
            Nuevo Equipo
          </Button>
        </Box>
      </Toolbar>

      <TextField
        size="small"
        placeholder="Buscar..."
        value={globalFilter}
        onChange={onGlobalFilterChange}
        InputProps={{ startAdornment: <SearchIcon sx={{ color: 'text.secondary', mr: 1 }} /> }}
        sx={{ mb: 2, width: 300 }}
      />

      <TableContainer sx={{ overflowX: 'auto' }}>
        <Table sx={{ minWidth: 800 }}>
          <TableHead>
            <TableRow sx={{ bgcolor: '#f8fafc' }}>
              {showColumn('id') && <TableCell sx={{ fontWeight: 600 }}>Id</TableCell>}
              {showColumn('nombre') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('nombre')}>
                  Nombre
                </TableCell>
              )}
              {showColumn('direccionIp') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('direccionIp')}>
                  IP
                </TableCell>
              )}
              {showColumn('modelo') && <TableCell sx={{ fontWeight: 600 }}>Modelo</TableCell>}
              {showColumn('firmwareVersion') && <TableCell sx={{ fontWeight: 600 }}>Firmware</TableCell>}
              {showColumn('estado') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('estado')}>
                  Estado
                </TableCell>
              )}
              {showColumn('intervaloBase') && <TableCell sx={{ fontWeight: 600 }}>Int. (seg)</TableCell>}
              {showColumn('ultimoHeartbeat') && <TableCell sx={{ fontWeight: 600 }}>Último Heartbeat</TableCell>}
              {showColumn('sitio') && <TableCell sx={{ fontWeight: 600 }}>Sitio</TableCell>}
              <TableCell sx={{ fontWeight: 600 }}>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredList?.map((e: IEquipo) => (
              <TableRow key={e.id} hover>
                {showColumn('id') && <TableCell>{e.id}</TableCell>}
                {showColumn('nombre') && <TableCell>{e.nombre}</TableCell>}
                {showColumn('direccionIp') && <TableCell sx={{ fontFamily: 'monospace' }}>{e.direccionIp}</TableCell>}
                {showColumn('modelo') && <TableCell>{e.modelo}</TableCell>}
                {showColumn('firmwareVersion') && <TableCell>{e.firmwareVersion}</TableCell>}
                {showColumn('estado') && <TableCell>{e.estado}</TableCell>}
                {showColumn('intervaloBase') && (
                  <TableCell>
                    <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                      {e.intervaloBase ?? 10}
                    </Typography>
                  </TableCell>
                )}
                {showColumn('ultimoHeartbeat') && (
                  <TableCell>{e.ultimoHeartbeat ? <span>{dayjs(e.ultimoHeartbeat).format(APP_DATE_FORMAT)}</span> : null}</TableCell>
                )}
                {showColumn('sitio') && <TableCell>{e.sitio?.nombre}</TableCell>}
                <TableCell>
                  <Box sx={{ display: 'flex', gap: 0.5 }}>
                    <IconButton size="small" color="warning" onClick={() => actualizar(e)}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton size="small" color="error" onClick={() => verEliminar(e)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {totalItems && equipoList && equipoList.length > 0 ? (
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

      <Dialog open={isOpen} onClose={() => {}} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedEquipo ? 'Editar Equipo' : 'Nuevo Equipo'}</DialogTitle>
        <DialogContent>
          {loading ? (
            <Spinner />
          ) : (
            <form key={formKey}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                <TextField
                  label="Nombre"
                  id="equipo-nombre"
                  name="nombre"
                  value={formValues.nombre}
                  onChange={handleInputChange}
                  fullWidth
                  required
                  error={!formValues.nombre}
                  helperText={!formValues.nombre ? 'Requerido' : ''}
                />
                <TextField
                  label="Dirección IP"
                  id="equipo-direccionIp"
                  name="direccionIp"
                  value={formValues.direccionIp}
                  onChange={handleInputChange}
                  fullWidth
                  required
                  error={!formValues.direccionIp}
                  helperText={!formValues.direccionIp ? 'Requerido' : ''}
                />
                <TextField
                  label="Modbus Slave ID"
                  id="equipo-modbusSlaveId"
                  name="modbusSlaveId"
                  type="number"
                  value={formValues.modbusSlaveId}
                  onChange={handleInputChange}
                  fullWidth
                />
                <TextField
                  label="Modelo"
                  id="equipo-modelo"
                  name="modelo"
                  value={formValues.modelo}
                  onChange={handleInputChange}
                  fullWidth
                />
                <TextField
                  label="Firmware Version"
                  id="equipo-firmwareVersion"
                  name="firmwareVersion"
                  value={formValues.firmwareVersion}
                  onChange={handleInputChange}
                  fullWidth
                />
                <TextField
                  select
                  label="Estado"
                  id="equipo-estado"
                  name="estado"
                  value={formValues.estado}
                  onChange={handleInputChange}
                  fullWidth
                  SelectProps={{ native: true }}
                >
                  {estadoEquipoValues.map(estado => (
                    <option value={estado} key={estado}>
                      {estado}
                    </option>
                  ))}
                </TextField>
                <TextField
                  label="Último Heartbeat"
                  id="equipo-ultimoHeartbeat"
                  name="ultimoHeartbeat"
                  type="datetime-local"
                  value={formValues.ultimoHeartbeat}
                  onChange={handleInputChange}
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                />
                <TextField
                  select
                  label="Sitio"
                  id="equipo-sitio"
                  name="sitio"
                  value={formValues.sitio}
                  onChange={handleInputChange}
                  fullWidth
                  SelectProps={{ native: true }}
                >
                  <option value="">-- Seleccionar --</option>
                  {sitios?.map(s => (
                    <option value={s.id} key={s.id}>
                      {s.nombre}
                    </option>
                  ))}
                </TextField>
                <TextField
                  label="Intervalo Base de Lectura (segundos)"
                  id="equipo-intervaloBase"
                  name="intervaloBase"
                  type="number"
                  value={formValues.intervaloBase}
                  onChange={handleInputChange}
                  fullWidth
                  helperText="Intervalo de polling para este equipo. Valor por defecto: 10 segundos. Use 5-300."
                  inputProps={{ min: 5, max: 300 }}
                />
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 2 }}>
                  <Button onClick={hideDialogNuevo}>Cancelar</Button>
                  <Button variant="contained" onClick={guardar} disabled={updating || !formValues.nombre || !formValues.direccionIp}>
                    <FontAwesomeIcon icon="save" />
                    &nbsp;Guardar
                  </Button>
                </Box>
              </Box>
            </form>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={isDeleting} onClose={hideDeleteDialog} maxWidth="xs" fullWidth>
        <DialogTitle>Confirmar</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 1 }}>
            <WarningAmberIcon sx={{ fontSize: 40, color: '#f59e0b' }} />
            {selectedEquipo && (
              <Typography>
                ¿Seguro que quiere eliminar el Equipo: <strong>{selectedEquipo.nombre}</strong>?
              </Typography>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={hideDeleteDialog}>No</Button>
          <Button onClick={deleteEquipo} color="error" variant="contained">
            Sí
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default Equipo;
