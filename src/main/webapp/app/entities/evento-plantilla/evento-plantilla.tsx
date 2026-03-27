import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { JhiPagination, Translate, getPaginationState, translate } from 'react-jhipster';
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
import ReadIcon from '@mui/icons-material/Visibility';
import ReadWriteIcon from '@mui/icons-material/EditNote';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import Spinner from '../loader/spinner';
import { getEntities, createEntity, updateEntity, deleteEntity } from './evento-plantilla.reducer';
import { getEntities as getEspecialidads } from 'app/entities/especialidad/especialidad.reducer';
import { IEventoPlantilla } from 'app/shared/model/evento-plantilla.model';

const FUNCION_LECTURA_OPCIONES = [
  { value: 'READ_HOLDING_REGISTERS', label: 'Leer Registros (03)' },
  { value: 'READ_INPUT_REGISTERS', label: 'Leer Entradas (04)' },
  { value: 'READ_COILS', label: 'Leer Bobinas (01)' },
  { value: 'READ_DISCRETE_INPUTS', label: 'Leer Discretas (02)' },
];

const FUNCION_ESCRITURA_OPCIONES = [
  { value: 'NONE', label: 'Solo Lectura' },
  { value: 'WRITE_SINGLE_REGISTER', label: 'Escribir Registro (06)' },
  { value: 'WRITE_MULTIPLE_REGISTERS', label: 'Escribir Múltiples (16)' },
  { value: 'WRITE_SINGLE_COIL', label: 'Escribir Bobina (05)' },
  { value: 'WRITE_MULTIPLE_COILS', label: 'Escribir Múltiples Bobinas (15)' },
];

const ALL_COLUMNS = [
  { key: 'id', label: 'Id', default: true },
  { key: 'nombre', label: 'Nombre', default: true },
  { key: 'descripcion', label: 'Descripción', default: true },
  { key: 'unidadMedida', label: 'Unidad', default: true },
  { key: 'scalingFactor', label: 'Factor Escala', default: false },
  { key: 'funcionLectura', label: 'Función Lectura', default: true },
  { key: 'funcionEscritura', label: 'Función Escritura', default: true },
  { key: 'especialidad', label: 'Especialidad', default: false },
];

export const EventoPlantilla = () => {
  const dispatch = useAppDispatch();
  const pageLocation = useLocation();
  const navigate = useNavigate();

  const eventoPlantillaList = useAppSelector(state => state.eventoPlantilla.entities);
  const loading = useAppSelector(state => state.eventoPlantilla.loading);
  const updating = useAppSelector(state => state.eventoPlantilla.updating);
  const updateSuccess = useAppSelector(state => state.eventoPlantilla.updateSuccess);
  const totalItems = useAppSelector(state => state.eventoPlantilla.totalItems);
  const especialidads = useAppSelector(state => state.especialidad.entities);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const [currentId, setCurrentId] = useState<number | null>(null);
  const [eventoPlantillaDialog, setEventoPlantillaDialog] = useState(false);
  const [deleteEventoPlantillaDialog, setDeleteEventoPlantillaDialog] = useState(false);
  const [selectedEventoPlantilla, setSelectedEventoPlantilla] = useState<IEventoPlantilla | null>(null);
  const [globalFilter, setGlobalFilterValue] = useState('');
  const [columnMenuAnchor, setColumnMenuAnchor] = useState<null | HTMLElement>(null);
  const [visibleColumns, setVisibleColumns] = useState<string[]>(() => {
    const saved = localStorage.getItem('evento-plantilla-columns');
    if (saved) return JSON.parse(saved);
    return ALL_COLUMNS.filter(c => c.default).map(c => c.key);
  });
  const [formKey, setFormKey] = useState(0);

  const [formValues, setFormValues] = useState({
    nombre: '',
    descripcion: '',
    scalingFactor: '',
    unidadMedida: '',
    funcionLectura: 'READ_HOLDING_REGISTERS',
    funcionEscritura: 'NONE',
    especialidad: '',
  });

  const saveColumns = (cols: string[]) => {
    setVisibleColumns(cols);
    localStorage.setItem('evento-plantilla-columns', JSON.stringify(cols));
  };

  const toggleColumn = (key: string) => {
    if (visibleColumns.includes(key)) {
      if (visibleColumns.length > 1) saveColumns(visibleColumns.filter(c => c !== key));
    } else {
      saveColumns([...visibleColumns, key]);
    }
  };

  const showColumn = (key: string) => visibleColumns.includes(key);

  const getFuncionLabel = (value: string | null | undefined) => {
    if (!value) return '-';
    const all = [...FUNCION_LECTURA_OPCIONES, ...FUNCION_ESCRITURA_OPCIONES];
    const found = all.find(o => o.value === value);
    return found ? found.label : value;
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
    dispatch(getEspecialidads({}));
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
      setEventoPlantillaDialog(false);
      setSelectedEventoPlantilla(null);
      setCurrentId(null);
      setFormValues({
        nombre: '',
        descripcion: '',
        scalingFactor: '',
        unidadMedida: '',
        funcionLectura: 'READ_HOLDING_REGISTERS',
        funcionEscritura: 'NONE',
        especialidad: '',
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

  const filteredList = eventoPlantillaList?.filter(
    ep =>
      !globalFilter ||
      ep.nombre?.toLowerCase().includes(globalFilter.toLowerCase()) ||
      ep.descripcion?.toLowerCase().includes(globalFilter.toLowerCase()),
  );

  const abrirDialogNuevo = () => {
    setCurrentId(null);
    setSelectedEventoPlantilla(null);
    setFormValues({
      nombre: '',
      descripcion: '',
      scalingFactor: '',
      unidadMedida: '',
      funcionLectura: 'READ_HOLDING_REGISTERS',
      funcionEscritura: 'NONE',
      especialidad: '',
    });
    setFormKey(prev => prev + 1);
    setEventoPlantillaDialog(true);
  };

  const abrirDialogEditar = (rowData: IEventoPlantilla) => {
    setCurrentId(rowData.id ?? null);
    setSelectedEventoPlantilla(rowData);
    setFormValues({
      nombre: rowData.nombre || '',
      descripcion: rowData.descripcion || '',
      scalingFactor: rowData.scalingFactor?.toString() || '',
      unidadMedida: rowData.unidadMedida || '',
      funcionLectura: rowData.funcionLectura || 'READ_HOLDING_REGISTERS',
      funcionEscritura: rowData.funcionEscritura || 'NONE',
      especialidad: rowData.especialidad?.id?.toString() || '',
    });
    setFormKey(prev => prev + 1);
    setEventoPlantillaDialog(true);
  };

  const cerrarDialog = () => {
    setEventoPlantillaDialog(false);
    setSelectedEventoPlantilla(null);
    setCurrentId(null);
    setFormValues({
      nombre: '',
      descripcion: '',
      scalingFactor: '',
      unidadMedida: '',
      funcionLectura: 'READ_HOLDING_REGISTERS',
      funcionEscritura: 'NONE',
      especialidad: '',
    });
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormValues(prev => ({ ...prev, [name]: value }));
  };

  const guardar = () => {
    const entity: any = {
      nombre: formValues.nombre,
      descripcion: formValues.descripcion || null,
      scalingFactor: formValues.scalingFactor ? parseFloat(formValues.scalingFactor) : null,
      unidadMedida: formValues.unidadMedida || null,
      funcionLectura: formValues.funcionLectura || null,
      funcionEscritura: formValues.funcionEscritura || null,
    };

    if (currentId) {
      entity.id = currentId;
    }

    if (formValues.especialidad) {
      entity.especialidad = especialidads?.find(e => String(e.id) === String(formValues.especialidad));
    }

    if (currentId) {
      dispatch(updateEntity(entity));
    } else {
      dispatch(createEntity(entity));
    }
  };

  const abrirEliminar = (rowData: IEventoPlantilla) => {
    setSelectedEventoPlantilla(rowData);
    setDeleteEventoPlantillaDialog(true);
  };

  const confirmarEliminar = () => {
    if (!selectedEventoPlantilla?.id) return;
    dispatch(deleteEntity(selectedEventoPlantilla.id));
    setDeleteEventoPlantillaDialog(false);
    setSelectedEventoPlantilla(null);
  };

  const cerrarEliminar = () => {
    setDeleteEventoPlantillaDialog(false);
    setSelectedEventoPlantilla(null);
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Toolbar sx={{ justifyContent: 'space-between', mb: 2, px: 0 }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Plantillas de Evento
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
          <Button variant="contained" startIcon={<AddIcon />} onClick={abrirDialogNuevo}>
            Nueva Plantilla
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
        <Table sx={{ minWidth: 1000 }}>
          <TableHead>
            <TableRow sx={{ bgcolor: '#f8fafc' }}>
              {showColumn('id') && <TableCell sx={{ fontWeight: 600 }}>Id</TableCell>}
              {showColumn('nombre') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('nombre')}>
                  Nombre
                </TableCell>
              )}
              {showColumn('descripcion') && <TableCell sx={{ fontWeight: 600 }}>Descripción</TableCell>}
              {showColumn('unidadMedida') && <TableCell sx={{ fontWeight: 600 }}>Unidad</TableCell>}
              {showColumn('scalingFactor') && <TableCell sx={{ fontWeight: 600 }}>Factor Escala</TableCell>}
              {showColumn('funcionLectura') && <TableCell sx={{ fontWeight: 600 }}>Función Lectura</TableCell>}
              {showColumn('funcionEscritura') && <TableCell sx={{ fontWeight: 600 }}>Función Escritura</TableCell>}
              {showColumn('especialidad') && <TableCell sx={{ fontWeight: 600 }}>Especialidad</TableCell>}
              <TableCell sx={{ fontWeight: 600 }}>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredList?.map((ep: IEventoPlantilla) => (
              <TableRow key={ep.id} hover>
                {showColumn('id') && <TableCell>{ep.id}</TableCell>}
                {showColumn('nombre') && <TableCell sx={{ fontWeight: 500 }}>{ep.nombre}</TableCell>}
                {showColumn('descripcion') && <TableCell>{ep.descripcion}</TableCell>}
                {showColumn('unidadMedida') && (
                  <TableCell>
                    <Chip label={ep.unidadMedida || '-'} size="small" variant="outlined" />
                  </TableCell>
                )}
                {showColumn('scalingFactor') && <TableCell>{ep.scalingFactor ?? '-'}</TableCell>}
                {showColumn('funcionLectura') && (
                  <TableCell>
                    <Chip
                      icon={<ReadIcon sx={{ fontSize: 14 }} />}
                      label={getFuncionLabel(ep.funcionLectura)}
                      size="small"
                      sx={{ bgcolor: '#dbeafe', color: '#1e40af' }}
                    />
                  </TableCell>
                )}
                {showColumn('funcionEscritura') && (
                  <TableCell>
                    <Chip
                      icon={ep.funcionEscritura === 'NONE' ? <ReadIcon sx={{ fontSize: 14 }} /> : <ReadWriteIcon sx={{ fontSize: 14 }} />}
                      label={getFuncionLabel(ep.funcionEscritura)}
                      size="small"
                      sx={{
                        bgcolor: ep.funcionEscritura === 'NONE' ? '#f1f5f9' : '#dcfce7',
                        color: ep.funcionEscritura === 'NONE' ? '#64748b' : '#166534',
                      }}
                    />
                  </TableCell>
                )}
                {showColumn('especialidad') && <TableCell>{ep.especialidad?.nombre || '-'}</TableCell>}
                <TableCell>
                  <Box sx={{ display: 'flex', gap: 0.5 }}>
                    <IconButton size="small" color="warning" onClick={() => abrirDialogEditar(ep)}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton size="small" color="error" onClick={() => abrirEliminar(ep)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {totalItems && eventoPlantillaList && eventoPlantillaList.length > 0 ? (
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

      <Dialog open={eventoPlantillaDialog} onClose={() => {}} maxWidth="sm" fullWidth>
        <DialogTitle>{currentId ? 'Editar Plantilla' : 'Nueva Plantilla'}</DialogTitle>
        <DialogContent>
          {loading ? (
            <Spinner />
          ) : (
            <form key={formKey}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                <TextField
                  label={translate('appsupervisorApp.eventoPlantilla.nombre')}
                  id="evento-plantilla-nombre"
                  name="nombre"
                  value={formValues.nombre}
                  onChange={handleInputChange}
                  fullWidth
                  required
                  error={!formValues.nombre}
                  helperText={!formValues.nombre ? translate('entity.validation.required') : ''}
                />
                <TextField
                  label={translate('appsupervisorApp.eventoPlantilla.descripcion')}
                  id="evento-plantilla-descripcion"
                  name="descripcion"
                  value={formValues.descripcion}
                  onChange={handleInputChange}
                  fullWidth
                />
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <TextField
                    label="Factor Escala"
                    id="evento-plantilla-scalingFactor"
                    name="scalingFactor"
                    type="number"
                    value={formValues.scalingFactor}
                    onChange={handleInputChange}
                    fullWidth
                    sx={{ flex: 1 }}
                  />
                  <TextField
                    label="Unidad de Medida"
                    id="evento-plantilla-unidadMedida"
                    name="unidadMedida"
                    value={formValues.unidadMedida}
                    onChange={handleInputChange}
                    fullWidth
                    sx={{ flex: 1 }}
                    placeholder="°C, kPa, %"
                  />
                </Box>

                <Box sx={{ bgcolor: '#f8fafc', p: 2, borderRadius: 2, border: '1px solid #e2e8f0' }}>
                  <Typography variant="subtitle2" sx={{ mb: 1.5, fontWeight: 600 }}>
                    Configuración Modbus
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 2 }}>
                    <TextField
                      select
                      label="Función de Lectura"
                      id="evento-plantilla-funcionLectura"
                      name="funcionLectura"
                      value={formValues.funcionLectura}
                      onChange={handleInputChange}
                      fullWidth
                      sx={{ flex: 1 }}
                      SelectProps={{ native: true }}
                    >
                      <option value="">-- Seleccionar --</option>
                      {FUNCION_LECTURA_OPCIONES.map(opt => (
                        <option value={opt.value} key={opt.value}>
                          {opt.label}
                        </option>
                      ))}
                    </TextField>
                    <TextField
                      select
                      label="Función de Escritura"
                      id="evento-plantilla-funcionEscritura"
                      name="funcionEscritura"
                      value={formValues.funcionEscritura}
                      onChange={handleInputChange}
                      fullWidth
                      sx={{ flex: 1 }}
                      SelectProps={{ native: true }}
                    >
                      <option value="">-- Seleccionar --</option>
                      {FUNCION_ESCRITURA_OPCIONES.map(opt => (
                        <option value={opt.value} key={opt.value}>
                          {opt.label}
                        </option>
                      ))}
                    </TextField>
                  </Box>
                </Box>

                <TextField
                  select
                  label="Especialidad"
                  id="evento-plantilla-especialidad"
                  name="especialidad"
                  value={formValues.especialidad}
                  onChange={handleInputChange}
                  fullWidth
                  SelectProps={{ native: true }}
                >
                  <option value="">-- Sin especialidad --</option>
                  {especialidads?.map(e => (
                    <option value={e.id} key={e.id}>
                      {e.nombre}
                    </option>
                  ))}
                </TextField>

                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 2 }}>
                  <Button onClick={cerrarDialog}>Cancelar</Button>
                  <Button variant="contained" onClick={guardar} disabled={updating || !formValues.nombre}>
                    <FontAwesomeIcon icon="save" />
                    &nbsp;Guardar
                  </Button>
                </Box>
              </Box>
            </form>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={deleteEventoPlantillaDialog} onClose={cerrarEliminar} maxWidth="xs" fullWidth>
        <DialogTitle>Confirmar</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 1 }}>
            <WarningAmberIcon sx={{ fontSize: 40, color: '#f59e0b' }} />
            {selectedEventoPlantilla && (
              <Typography>
                ¿Seguro que quiere eliminar: <strong>{selectedEventoPlantilla.nombre}</strong>?
              </Typography>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={cerrarEliminar}>No</Button>
          <Button onClick={confirmarEliminar} color="error" variant="contained">
            Sí
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default EventoPlantilla;
