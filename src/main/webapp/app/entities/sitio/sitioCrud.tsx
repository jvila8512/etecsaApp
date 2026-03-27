import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import {
  JhiItemCount,
  JhiPagination,
  TextFormat,
  Translate,
  getPaginationState,
  translate,
  ValidatedField,
  ValidatedForm,
} from 'react-jhipster';
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
import { getEntities, createEntity, updateEntity, deleteEntity } from './sitio.reducer';
import { ISitio } from 'app/shared/model/sitio.model';

const ALL_COLUMNS = [
  { key: 'id', label: 'Id', default: true },
  { key: 'nombre', label: 'Nombre', default: true },
  { key: 'codigo', label: 'Código', default: true },
  { key: 'ubicacion', label: 'Ubicación', default: false },
  { key: 'fechaRegistro', label: 'Fecha Registro', default: false },
];

export const Sitio = () => {
  const dispatch = useAppDispatch();
  const pageLocation = useLocation();
  const navigate = useNavigate();

  const sitioList = useAppSelector(state => state.sitio.entities);
  const loading = useAppSelector(state => state.sitio.loading);
  const updating = useAppSelector(state => state.sitio.updating);
  const updateSuccess = useAppSelector(state => state.sitio.updateSuccess);
  const totalItems = useAppSelector(state => state.sitio.totalItems);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const [sitioDialog, setSitioDialog] = useState(false);
  const [deleteSitioDialog, setDeleteSitioDialog] = useState(false);
  const [globalFilter, setGlobalFilterValue] = useState('');
  const [columnMenuAnchor, setColumnMenuAnchor] = useState<null | HTMLElement>(null);
  const [selectedSitio, setSelectedSitio] = useState<ISitio | null>(null);
  const [formKey, setFormKey] = useState(0);

  const [formValues, setFormValues] = useState({
    nombre: '',
    codigo: '',
    ubicacion: '',
    fechaRegistro: '',
  });

  const [visibleColumns, setVisibleColumns] = useState<string[]>(() => {
    const saved = localStorage.getItem('sitio-columns');
    if (saved) return JSON.parse(saved);
    return ALL_COLUMNS.filter(c => c.default).map(c => c.key);
  });

  const saveColumns = (cols: string[]) => {
    setVisibleColumns(cols);
    localStorage.setItem('sitio-columns', JSON.stringify(cols));
  };

  const toggleColumn = (key: string) => {
    if (visibleColumns.includes(key)) {
      if (visibleColumns.length > 1) saveColumns(visibleColumns.filter(c => c !== key));
    } else {
      saveColumns([...visibleColumns, key]);
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
      setSitioDialog(false);
      setSelectedSitio(null);
      setFormValues({ nombre: '', codigo: '', ubicacion: '', fechaRegistro: '' });
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

  const onGlobalFilterChange = e => {
    setGlobalFilterValue(e.target.value);
  };

  const filteredList = sitioList?.filter(
    s =>
      !globalFilter ||
      s.nombre?.toLowerCase().includes(globalFilter.toLowerCase()) ||
      s.codigo?.toLowerCase().includes(globalFilter.toLowerCase()) ||
      s.ubicacion?.toLowerCase().includes(globalFilter.toLowerCase()),
  );

  const verDialogNuevo = () => {
    setSelectedSitio(null);
    setFormValues({ nombre: '', codigo: '', ubicacion: '', fechaRegistro: '' });
    setFormKey(prev => prev + 1);
    setSitioDialog(true);
  };

  const actualizar = (rowData: ISitio) => {
    setSelectedSitio(rowData);
    setFormValues({
      nombre: rowData.nombre || '',
      codigo: rowData.codigo || '',
      ubicacion: rowData.ubicacion || '',
      fechaRegistro: rowData.fechaRegistro ? dayjs(rowData.fechaRegistro).format('YYYY-MM-DDTHH:mm') : '',
    });
    setFormKey(prev => prev + 1);
    setSitioDialog(true);
  };

  const hideDialogNuevo = () => {
    setSitioDialog(false);
    setSelectedSitio(null);
    setFormValues({ nombre: '', codigo: '', ubicacion: '', fechaRegistro: '' });
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormValues(prev => ({ ...prev, [name]: value }));
  };

  const guardar = (values: any) => {
    const entity: ISitio = {
      ...values,
      fechaRegistro: values.fechaRegistro ? dayjs(values.fechaRegistro) : undefined,
    };
    if (selectedSitio?.id) {
      entity.id = selectedSitio.id;
      dispatch(updateEntity(entity));
    } else {
      dispatch(createEntity(entity));
    }
  };

  const verEliminar = (rowData: ISitio) => {
    setSelectedSitio(rowData);
    setDeleteSitioDialog(true);
  };

  const deleteSitio = () => {
    if (!selectedSitio?.id) return;
    dispatch(deleteEntity(selectedSitio.id));
    setDeleteSitioDialog(false);
    setSelectedSitio(null);
  };

  const hideDeleteDialog = () => {
    setDeleteSitioDialog(false);
    setSelectedSitio(null);
  };

  const showColumn = (key: string) => visibleColumns.includes(key);

  return (
    <Paper sx={{ p: 3 }}>
      <Toolbar sx={{ justifyContent: 'space-between', mb: 2, px: 0 }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Sitios
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
            Nuevo Sitio
          </Button>
        </Box>
      </Toolbar>

      <TextField
        size="small"
        placeholder="Buscar..."
        value={globalFilter}
        onChange={onGlobalFilterChange}
        InputProps={{
          startAdornment: <SearchIcon sx={{ color: 'text.secondary', mr: 1 }} />,
        }}
        sx={{ mb: 2, width: 300 }}
      />

      <TableContainer sx={{ overflowX: 'auto' }}>
        <Table sx={{ minWidth: 600 }}>
          <TableHead>
            <TableRow sx={{ bgcolor: '#f8fafc' }}>
              {showColumn('id') && <TableCell sx={{ fontWeight: 600 }}>Id</TableCell>}
              {showColumn('nombre') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('nombre')}>
                  Nombre
                </TableCell>
              )}
              {showColumn('codigo') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('codigo')}>
                  Código
                </TableCell>
              )}
              {showColumn('ubicacion') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('ubicacion')}>
                  Ubicación
                </TableCell>
              )}
              {showColumn('fechaRegistro') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('fechaRegistro')}>
                  Fecha Registro
                </TableCell>
              )}
              <TableCell sx={{ fontWeight: 600 }}>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredList?.map((s: ISitio) => (
              <TableRow key={s.id} hover>
                {showColumn('id') && <TableCell>{s.id}</TableCell>}
                {showColumn('nombre') && <TableCell>{s.nombre}</TableCell>}
                {showColumn('codigo') && <TableCell>{s.codigo}</TableCell>}
                {showColumn('ubicacion') && <TableCell>{s.ubicacion}</TableCell>}
                {showColumn('fechaRegistro') && (
                  <TableCell>
                    {s.fechaRegistro ? (
                      <TextFormat type="date" value={s.fechaRegistro as unknown as string} format={APP_DATE_FORMAT} />
                    ) : null}
                  </TableCell>
                )}
                <TableCell>
                  <Box sx={{ display: 'flex', gap: 0.5 }}>
                    <IconButton size="small" color="warning" onClick={() => actualizar(s)}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton size="small" color="error" onClick={() => verEliminar(s)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {totalItems && sitioList && sitioList.length > 0 ? (
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

      <Dialog open={sitioDialog} onClose={() => {}} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedSitio ? 'Editar Sitio' : 'Nuevo Sitio'}</DialogTitle>
        <DialogContent>
          {loading ? (
            <Spinner />
          ) : (
            <form key={formKey}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                <TextField
                  label={translate('appsupervisorApp.sitio.nombre')}
                  id="sitio-nombre"
                  name="nombre"
                  value={formValues.nombre}
                  onChange={handleInputChange}
                  fullWidth
                  required
                  error={!formValues.nombre}
                  helperText={!formValues.nombre ? 'Requerido' : ''}
                />
                <TextField
                  label={translate('appsupervisorApp.sitio.codigo')}
                  id="sitio-codigo"
                  name="codigo"
                  value={formValues.codigo}
                  onChange={handleInputChange}
                  fullWidth
                  required
                  error={!formValues.codigo}
                  helperText={!formValues.codigo ? 'Requerido' : ''}
                />
                <TextField
                  label={translate('appsupervisorApp.sitio.ubicacion')}
                  id="sitio-ubicacion"
                  name="ubicacion"
                  value={formValues.ubicacion}
                  onChange={handleInputChange}
                  fullWidth
                />
                <TextField
                  label={translate('appsupervisorApp.sitio.fechaRegistro')}
                  id="sitio-fechaRegistro"
                  name="fechaRegistro"
                  type="datetime-local"
                  value={formValues.fechaRegistro}
                  onChange={handleInputChange}
                  fullWidth
                  InputLabelProps={{ shrink: true }}
                />
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 2 }}>
                  <Button onClick={hideDialogNuevo}>Cancelar</Button>
                  <Button
                    variant="contained"
                    onClick={() => guardar(formValues)}
                    disabled={updating || !formValues.nombre || !formValues.codigo}
                  >
                    <FontAwesomeIcon icon="save" />
                    &nbsp;
                    <Translate contentKey="entity.action.save">Guardar</Translate>
                  </Button>
                </Box>
              </Box>
            </form>
          )}
        </DialogContent>
      </Dialog>

      <Dialog open={deleteSitioDialog} onClose={hideDeleteDialog} maxWidth="xs" fullWidth>
        <DialogTitle>Confirmar</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 1 }}>
            <WarningAmberIcon sx={{ fontSize: 40, color: '#f59e0b' }} />
            {selectedSitio && (
              <Typography>
                ¿Seguro que quiere eliminar el Sitio: <strong>{selectedSitio.nombre}</strong>?
              </Typography>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={hideDeleteDialog}>No</Button>
          <Button onClick={deleteSitio} color="error" variant="contained">
            Sí
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default Sitio;
