import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { JhiPagination, Translate, getPaginationState, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
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
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import Spinner from '../loader/spinner';
import { getEntities, createEntity, updateEntity, deleteEntity } from './especialidad.reducer';
import { IEspecialidad } from 'app/shared/model/especialidad.model';

export const Especialidad = () => {
  const dispatch = useAppDispatch();
  const pageLocation = useLocation();
  const navigate = useNavigate();

  const especialidadList = useAppSelector(state => state.especialidad.entities);
  const loading = useAppSelector(state => state.especialidad.loading);
  const updating = useAppSelector(state => state.especialidad.updating);
  const updateSuccess = useAppSelector(state => state.especialidad.updateSuccess);
  const totalItems = useAppSelector(state => state.especialidad.totalItems);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const [selectedEspecialidad, setSelectedEspecialidad] = useState<IEspecialidad | null>(null);
  const [especialidadDialog, setEspecialidadDialog] = useState(false);
  const [deleteEspecialidadDialog, setDeleteEspecialidadDialog] = useState(false);
  const [globalFilter, setGlobalFilterValue] = useState('');
  const [formKey, setFormKey] = useState(0);

  const [formValues, setFormValues] = useState({
    nombre: '',
    codigo: '',
    descripcionTecnica: '',
  });

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
      setEspecialidadDialog(false);
      setSelectedEspecialidad(null);
      setFormValues({ nombre: '', codigo: '', descripcionTecnica: '' });
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

  const filteredList = especialidadList?.filter(
    e =>
      !globalFilter ||
      e.nombre?.toLowerCase().includes(globalFilter.toLowerCase()) ||
      e.codigo?.toLowerCase().includes(globalFilter.toLowerCase()),
  );

  const verDialogNuevo = () => {
    setSelectedEspecialidad(null);
    setFormValues({ nombre: '', codigo: '', descripcionTecnica: '' });
    setFormKey(prev => prev + 1);
    setEspecialidadDialog(true);
  };

  const actualizar = (rowData: IEspecialidad) => {
    setSelectedEspecialidad(rowData);
    setFormValues({
      nombre: rowData.nombre || '',
      codigo: rowData.codigo || '',
      descripcionTecnica: rowData.descripcionTecnica || '',
    });
    setFormKey(prev => prev + 1);
    setEspecialidadDialog(true);
  };

  const hideDialogNuevo = () => {
    setEspecialidadDialog(false);
    setSelectedEspecialidad(null);
    setFormValues({ nombre: '', codigo: '', descripcionTecnica: '' });
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormValues(prev => ({ ...prev, [name]: value }));
  };

  const guardar = () => {
    const entity: any = {
      nombre: formValues.nombre,
      codigo: formValues.codigo,
      descripcionTecnica: formValues.descripcionTecnica || null,
    };

    if (selectedEspecialidad?.id) {
      entity.id = selectedEspecialidad.id;
      dispatch(updateEntity(entity));
    } else {
      dispatch(createEntity(entity));
    }
  };

  const verEliminar = (rowData: IEspecialidad) => {
    setSelectedEspecialidad(rowData);
    setDeleteEspecialidadDialog(true);
  };

  const deleteEspecialidad = () => {
    if (!selectedEspecialidad?.id) return;
    dispatch(deleteEntity(selectedEspecialidad.id));
    setDeleteEspecialidadDialog(false);
    setSelectedEspecialidad(null);
  };

  const hideDeleteDialog = () => {
    setDeleteEspecialidadDialog(false);
    setSelectedEspecialidad(null);
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Toolbar sx={{ justifyContent: 'space-between', mb: 2, px: 0 }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Especialidades
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button variant="outlined" startIcon={<RefreshIcon />} onClick={handleSyncList} disabled={loading}>
            Actualizar
          </Button>
          <Button variant="contained" startIcon={<AddIcon />} onClick={verDialogNuevo}>
            Nueva Especialidad
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
        <Table sx={{ minWidth: 900 }}>
          <TableHead>
            <TableRow sx={{ bgcolor: '#f8fafc' }}>
              <TableCell sx={{ fontWeight: 600 }}>Id</TableCell>
              <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('nombre')}>
                Nombre
              </TableCell>
              <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('codigo')}>
                Código
              </TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Descripción Técnica</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredList?.map((e: IEspecialidad) => (
              <TableRow key={e.id} hover>
                <TableCell>{e.id}</TableCell>
                <TableCell sx={{ fontWeight: 500 }}>{e.nombre}</TableCell>
                <TableCell>{e.codigo}</TableCell>
                <TableCell>{e.descripcionTecnica}</TableCell>
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

      {totalItems && especialidadList && especialidadList.length > 0 ? (
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

      <Dialog open={especialidadDialog} onClose={() => {}} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedEspecialidad ? 'Editar Especialidad' : 'Nueva Especialidad'}</DialogTitle>
        <DialogContent>
          {loading ? (
            <Spinner />
          ) : (
            <form key={formKey}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                <TextField
                  label={translate('appsupervisorApp.especialidad.nombre')}
                  id="especialidad-nombre"
                  name="nombre"
                  value={formValues.nombre}
                  onChange={handleInputChange}
                  fullWidth
                  required
                  error={!formValues.nombre}
                  helperText={!formValues.nombre ? translate('entity.validation.required') : ''}
                />
                <TextField
                  label={translate('appsupervisorApp.especialidad.codigo')}
                  id="especialidad-codigo"
                  name="codigo"
                  value={formValues.codigo}
                  onChange={handleInputChange}
                  fullWidth
                  required
                  error={!formValues.codigo}
                  helperText={!formValues.codigo ? translate('entity.validation.required') : ''}
                />
                <TextField
                  label={translate('appsupervisorApp.especialidad.descripcionTecnica')}
                  id="especialidad-descripcionTecnica"
                  name="descripcionTecnica"
                  value={formValues.descripcionTecnica}
                  onChange={handleInputChange}
                  fullWidth
                  multiline
                  rows={3}
                />
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 2 }}>
                  <Button onClick={hideDialogNuevo}>Cancelar</Button>
                  <Button variant="contained" onClick={guardar} disabled={updating || !formValues.nombre || !formValues.codigo}>
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

      <Dialog open={deleteEspecialidadDialog} onClose={hideDeleteDialog} maxWidth="xs" fullWidth>
        <DialogTitle>Confirmar</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 1 }}>
            <WarningAmberIcon sx={{ fontSize: 40, color: '#f59e0b' }} />
            {selectedEspecialidad && (
              <Typography>
                ¿Seguro que quiere eliminar la Especialidad: <strong>{selectedEspecialidad.nombre}</strong>?
              </Typography>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={hideDeleteDialog}>No</Button>
          <Button onClick={deleteEspecialidad} color="error" variant="contained">
            Sí
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default Especialidad;
