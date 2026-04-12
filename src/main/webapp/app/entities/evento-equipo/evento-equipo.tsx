import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { JhiPagination, Translate, getPaginationState, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  Alert,
  AlertTitle,
  Box,
  Button,
  CircularProgress,
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
  Tooltip,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import RefreshIcon from '@mui/icons-material/Refresh';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import SearchIcon from '@mui/icons-material/Search';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import HelpOutlineIcon from '@mui/icons-material/HelpOutline';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import Spinner from '../loader/spinner';
import { getEntities, createEntity, updateEntity, deleteEntity } from './evento-equipo.reducer';
import { getEntities as getEquipos } from 'app/entities/equipo/equipo.reducer';
import { getEntities as getPlantillas } from 'app/entities/evento-plantilla/evento-plantilla.reducer';
import { IEventoEquipo } from 'app/shared/model/evento-equipo.model';
import { TipoRegistro } from 'app/shared/model/enumerations/tipo-registro.model';
import { TipoDato } from 'app/shared/model/enumerations/tipo-dato.model';
import { Severidad } from 'app/shared/model/enumerations/severidad.model';

export const EventoEquipo = () => {
  const dispatch = useAppDispatch();
  const pageLocation = useLocation();
  const navigate = useNavigate();

  const eventoEquipoList = useAppSelector(state => state.eventoEquipo.entities);
  const loading = useAppSelector(state => state.eventoEquipo.loading);
  const updating = useAppSelector(state => state.eventoEquipo.updating);
  const updateSuccess = useAppSelector(state => state.eventoEquipo.updateSuccess);
  const totalItems = useAppSelector(state => state.eventoEquipo.totalItems);
  const equipos = useAppSelector(state => state.equipo.entities);
  const plantillas = useAppSelector(state => state.eventoPlantilla.entities);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const [eventoEquipo, setEventoEquipo] = useState<IEventoEquipo | null>(null);
  const [selectedEventoEquipo, setSelectedEventoEquipo] = useState<IEventoEquipo | null>(null);
  const [eventoEquipoDialog, setEventoEquipoDialog] = useState(false);
  const [deleteEventoEquipoDialog, setDeleteEventoEquipoDialog] = useState(false);
  const [helpDialogOpen, setHelpDialogOpen] = useState(false);
  const [globalFilter, setGlobalFilterValue] = useState('');
  const [formKey, setFormKey] = useState(0);

  const [formValues, setFormValues] = useState({
    nombreVariable: '',
    direccionModbus: '',
    tipoRegistro: 'BIT_LOGICO_M',
    tipoDato: 'BOOLEAN',
    esEscribible: false,
    equipo: '',
    plantilla: '',
    intervaloLectura: '',
    umbralAlerta: '',
    severidadAlerta: '',
    habilitarAlarma: false,
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
    dispatch(getEquipos({}));
    dispatch(getPlantillas({}));
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
      setEventoEquipoDialog(false);
      setSelectedEventoEquipo(null);
      setEventoEquipo(null);
      resetFormValues();
    }
  }, [updateSuccess]);

  const resetFormValues = () => {
    setFormValues({
      nombreVariable: '',
      direccionModbus: '',
      tipoRegistro: 'BIT_LOGICO_M',
      tipoDato: 'BOOLEAN',
      esEscribible: false,
      equipo: '',
      plantilla: '',
      intervaloLectura: '',
      umbralAlerta: '',
      severidadAlerta: '',
      habilitarAlarma: false,
    });
  };

  const sort = (field: string) => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === DESC ? ASC : DESC,
      sort: field,
    });
  };

  const handlePagination = (currentPage: number) => setPaginationState({ ...paginationState, activePage: currentPage });
  const handleSyncList = () => sortEntities();

  const onGlobalFilterChange = e => {
    setGlobalFilterValue(e.target.value);
  };

  const filteredList = eventoEquipoList?.filter(
    ee =>
      !globalFilter ||
      ee.nombreVariable?.toLowerCase().includes(globalFilter.toLowerCase()) ||
      ee.direccionModbus?.toString().includes(globalFilter) ||
      ee.tipoRegistro?.toLowerCase().includes(globalFilter.toLowerCase()) ||
      ee.tipoDato?.toLowerCase().includes(globalFilter.toLowerCase()),
  );

  const verDialogNuevo = () => {
    setEventoEquipo(null);
    resetFormValues();
    setFormKey(prev => prev + 1);
    setEventoEquipoDialog(true);
  };

  const actualizar = (rowData: IEventoEquipo) => {
    setEventoEquipo(rowData);
    setFormValues({
      nombreVariable: rowData.nombreVariable || '',
      direccionModbus: rowData.direccionModbus?.toString() || '',
      tipoRegistro: rowData.tipoRegistro || 'BIT_LOGICO_M',
      tipoDato: rowData.tipoDato || 'BOOLEAN',
      esEscribible: rowData.esEscribible || false,
      equipo: rowData.equipo?.id?.toString() || '',
      plantilla: rowData.plantilla?.id?.toString() || '',
      intervaloLectura: rowData.intervaloLectura?.toString() || '',
      umbralAlerta: rowData.umbralAlerta?.toString() || '',
      severidadAlerta: rowData.severidadAlerta || '',
      habilitarAlarma: rowData.habilitarAlarma || false,
    });
    setFormKey(prev => prev + 1);
    setEventoEquipoDialog(true);
  };

  const hideDialogNuevo = () => {
    setEventoEquipoDialog(false);
    setSelectedEventoEquipo(null);
    setEventoEquipo(null);
    resetFormValues();
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type } = e.target;
    if (type === 'checkbox') {
      const checked = (e.target as HTMLInputElement).checked;
      setFormValues(prev => {
        const newValues = { ...prev, [name]: checked };
        // Si marca "Escribible", desmarca y desactiva "Habilitar Alarma"
        if (name === 'esEscribible' && checked) {
          newValues.habilitarAlarma = false;
        }
        // Si marca "Habilitar Alarma", desmarca y desactiva "Escribible"
        if (name === 'habilitarAlarma' && checked) {
          newValues.esEscribible = false;
        }
        return newValues;
      });
    } else {
      setFormValues(prev => ({ ...prev, [name]: value }));
    }
  };

  const tipoRegistroValues = Object.keys(TipoRegistro);
  const tipoDatoValues = Object.keys(TipoDato);
  const severidadValues = Object.keys(Severidad);

  const tipoRegistroToTipoDato: Record<string, string> = {
    BIT_LOGICO_M: 'BOOLEAN',
    PALABRA_MW: 'INT16',
    PALABRA_DOBLE_MD: 'INT32',
  };

  const handleTipoRegistroChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormValues(prev => {
      const newValues = { ...prev, [name]: value };
      const autoTipoDato = tipoRegistroToTipoDato[value];
      if (autoTipoDato) {
        newValues.tipoDato = autoTipoDato;
      }
      return newValues;
    });
  };

  const guardar = () => {
    const entity: any = {
      ...eventoEquipo,
      nombreVariable: formValues.nombreVariable,
      direccionModbus: formValues.direccionModbus ? Number(formValues.direccionModbus) : null,
      tipoRegistro: formValues.tipoRegistro,
      tipoDato: formValues.tipoDato,
      esEscribible: formValues.esEscribible,
      habilitarAlarma: formValues.habilitarAlarma,
      intervaloLectura: formValues.intervaloLectura ? Number(formValues.intervaloLectura) : null,
      umbralAlerta: formValues.umbralAlerta ? Number(formValues.umbralAlerta) : null,
      severidadAlerta: formValues.severidadAlerta || null,
    };

    if (formValues.equipo) {
      entity.equipo = equipos?.find(it => it.id.toString() === formValues.equipo.toString());
    }
    if (formValues.plantilla) {
      entity.plantilla = plantillas?.find(it => it.id.toString() === formValues.plantilla.toString());
    }

    if (eventoEquipo?.id) {
      dispatch(updateEntity(entity));
    } else {
      dispatch(createEntity(entity));
    }
  };

  const verEliminar = (rowData: IEventoEquipo) => {
    setSelectedEventoEquipo(rowData);
    setDeleteEventoEquipoDialog(true);
  };

  const deleteEventoEquipo = () => {
    if (!selectedEventoEquipo?.id) return;
    dispatch(deleteEntity(selectedEventoEquipo.id));
    setDeleteEventoEquipoDialog(false);
    setSelectedEventoEquipo(null);
  };

  const hideDeleteDialog = () => {
    setDeleteEventoEquipoDialog(false);
    setSelectedEventoEquipo(null);
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Toolbar sx={{ justifyContent: 'space-between', mb: 2, px: 0 }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Eventos de Equipo
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title="Ayuda sobre Alarmas">
            <IconButton onClick={() => setHelpDialogOpen(true)} color="primary">
              <HelpOutlineIcon />
            </IconButton>
          </Tooltip>
          <Button variant="outlined" startIcon={<RefreshIcon />} onClick={handleSyncList} disabled={loading}>
            Actualizar
          </Button>
          <Button variant="contained" startIcon={<AddIcon />} onClick={verDialogNuevo}>
            Nuevo Evento
          </Button>
        </Box>
      </Toolbar>

      {/* Loading indicator */}
      {loading && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
          <CircularProgress size={40} sx={{ color: '#2563eb' }} />
        </Box>
      )}

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
              <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('nombreVariable')}>
                Variable
              </TableCell>
              <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('direccionModbus')}>
                Dir Modbus
              </TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Tipo</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Umbral</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Equipo</TableCell>
              <TableCell sx={{ fontWeight: 600 }}>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredList?.map((ee: IEventoEquipo) => (
              <TableRow key={ee.id} hover>
                <TableCell>{ee.id}</TableCell>
                <TableCell sx={{ fontWeight: 500 }}>{ee.nombreVariable}</TableCell>
                <TableCell>{ee.direccionModbus}</TableCell>
                <TableCell>
                  <Translate contentKey={`appsupervisorApp.TipoRegistro.${ee.tipoRegistro}`} />
                </TableCell>
                <TableCell>
                  {ee.umbralAlerta != null ? (
                    <Typography variant="body2" color={ee.umbralAlerta > 0 ? 'error.main' : 'text.secondary'}>
                      {ee.umbralAlerta}
                    </Typography>
                  ) : (
                    <Typography variant="body2" color="text.disabled">
                      Sin alarma
                    </Typography>
                  )}
                </TableCell>
                <TableCell>{ee.equipo ? ee.equipo.nombre : ''}</TableCell>
                <TableCell>
                  <Box sx={{ display: 'flex', gap: 0.5 }}>
                    <IconButton size="small" color="warning" onClick={() => actualizar(ee)}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton size="small" color="error" onClick={() => verEliminar(ee)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {totalItems && eventoEquipoList && eventoEquipoList.length > 0 ? (
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

      <Dialog open={eventoEquipoDialog} onClose={() => {}} maxWidth="md" fullWidth>
        <DialogTitle>{eventoEquipo ? 'Editar Evento de Equipo' : 'Nuevo Evento de Equipo'}</DialogTitle>
        <DialogContent>
          {loading ? (
            <Spinner />
          ) : (
            <form key={formKey}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                {/* Nombre Variable + Dirección Modbus juntos */}
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <TextField
                    label={translate('appsupervisorApp.eventoEquipo.nombreVariable')}
                    id="evento-equipo-nombreVariable"
                    name="nombreVariable"
                    value={formValues.nombreVariable}
                    onChange={handleInputChange}
                    fullWidth
                    required
                    error={!formValues.nombreVariable}
                    helperText={!formValues.nombreVariable ? translate('entity.validation.required') : 'Nombre de la variable'}
                    InputLabelProps={{ shrink: true }}
                  />
                  <TextField
                    label="Dirección Modbus"
                    id="evento-equipo-direccionModbus"
                    name="direccionModbus"
                    type="number"
                    value={formValues.direccionModbus}
                    onChange={handleInputChange}
                    fullWidth
                    required
                    error={!formValues.direccionModbus}
                    InputLabelProps={{ shrink: true }}
                  />
                </Box>
                {/* Tipo Registro + Tipo Dato */}
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <TextField
                    select
                    label={translate(`appsupervisorApp.eventoEquipo.tipoRegistro`)}
                    id="evento-equipo-tipoRegistro"
                    name="tipoRegistro"
                    value={formValues.tipoRegistro}
                    onChange={handleTipoRegistroChange}
                    fullWidth
                    variant="outlined"
                    InputLabelProps={{ shrink: true }}
                    SelectProps={{ native: true }}
                  >
                    {tipoRegistroValues.map(tipoRegistro => (
                      <option value={tipoRegistro} key={tipoRegistro}>
                        {translate(`appsupervisorApp.TipoRegistro.${tipoRegistro}`)}
                      </option>
                    ))}
                  </TextField>
                  <TextField
                    select
                    label={translate(`appsupervisorApp.eventoEquipo.tipoDato`)}
                    id="evento-equipo-tipoDato"
                    name="tipoDato"
                    value={formValues.tipoDato}
                    fullWidth
                    variant="outlined"
                    disabled
                    InputLabelProps={{ shrink: true }}
                    SelectProps={{ native: true }}
                    helperText="Se completa automáticamente según Tipo de Registro"
                  >
                    {tipoDatoValues.map(tipoDato => (
                      <option value={tipoDato} key={tipoDato}>
                        {translate(`appsupervisorApp.TipoDato.${tipoDato}`)}
                      </option>
                    ))}
                  </TextField>
                </Box>
                {/* Equipo + Plantilla */}
                <Box sx={{ display: 'flex', gap: 2 }}>
                  <TextField
                    select
                    label="Equipo"
                    id="evento-equipo-equipo"
                    name="equipo"
                    value={formValues.equipo}
                    onChange={handleInputChange}
                    fullWidth
                    variant="outlined"
                    InputLabelProps={{ shrink: true }}
                    SelectProps={{ native: true }}
                  >
                    <option value="">-- Seleccionar --</option>
                    {equipos?.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.nombre}
                      </option>
                    ))}
                  </TextField>
                  <TextField
                    select
                    label="Plantilla"
                    id="evento-equipo-plantilla"
                    name="plantilla"
                    value={formValues.plantilla}
                    onChange={handleInputChange}
                    fullWidth
                    variant="outlined"
                    InputLabelProps={{ shrink: true }}
                    SelectProps={{ native: true }}
                  >
                    <option value="">-- Seleccionar --</option>
                    {plantillas?.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.nombre}
                      </option>
                    ))}
                  </TextField>
                </Box>
                {/* Alerta de configuración */}
                <Alert severity="warning" sx={{ mt: 1 }}>
                  <AlertTitle>Configuración de Alarmas</AlertTitle>
                  <Typography variant="body2" component="div">
                    <ul style={{ margin: '4px 0', paddingLeft: 20 }}>
                      <li>
                        <strong>Alarma Numérica:</strong> Ingrese el umbral (ej: 80). Se dispara cuando valor &gt; umbral.
                      </li>
                      <li>
                        <strong>Alarma Booleana:</strong> Ingrese <strong>1</strong>. Se dispara cuando valor = true.
                      </li>
                      <li>
                        <strong>Sin Alarma:</strong> Deje <strong>vacío</strong> este campo.
                      </li>
                    </ul>
                  </Typography>
                </Alert>
                {/* Umbral de Alerta */}
                <TextField
                  label="Umbral de Alerta"
                  id="evento-equipo-umbralAlerta"
                  name="umbralAlerta"
                  type="number"
                  value={formValues.umbralAlerta}
                  onChange={handleInputChange}
                  fullWidth
                  placeholder="Vacío = sin alarma. 1 = booleano. Número = umbral numérico"
                  helperText="Configure para activar alarmas"
                  InputLabelProps={{ shrink: true }}
                  sx={{ mb: 2 }}
                />
                {/* Habilitar Alarma - Checkbox destacado */}
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1,
                    mb: 2,
                    p: 1.5,
                    bgcolor: '#fff3cd',
                    borderRadius: 1,
                    opacity: formValues.esEscribible ? 0.5 : 1,
                  }}
                >
                  <input
                    type="checkbox"
                    id="evento-equipo-habilitarAlarma"
                    name="habilitarAlarma"
                    checked={formValues.habilitarAlarma}
                    onChange={handleInputChange}
                    disabled={formValues.esEscribible}
                    style={{ width: 18, height: 18 }}
                  />
                  <label htmlFor="evento-equipo-habilitarAlarma" style={{ fontWeight: 600 }}>
                    Habilitar Alarma Automática {formValues.esEscribible && '(bloqueado - es escribible)'}
                  </label>
                </Box>
                {/* Severidad de Alerta */}
                <TextField
                  select
                  label="Severidad de Alerta"
                  id="evento-equipo-severidadAlerta"
                  name="severidadAlerta"
                  value={formValues.severidadAlerta}
                  onChange={handleInputChange}
                  fullWidth
                  variant="outlined"
                  SelectProps={{ native: true }}
                >
                  <option value="">-- Por defecto --</option>
                  {severidadValues.map(sev => (
                    <option value={sev} key={sev}>
                      {sev}
                    </option>
                  ))}
                </TextField>
                {/* Escribible */}
                <Box
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: 1,
                    mt: formValues.habilitarAlarma ? 0 : 2,
                    opacity: formValues.habilitarAlarma ? 0.5 : 1,
                  }}
                >
                  <input
                    type="checkbox"
                    id="evento-equipo-esEscribible"
                    name="esEscribible"
                    checked={formValues.esEscribible}
                    onChange={handleInputChange}
                    disabled={formValues.habilitarAlarma}
                    style={{ width: 18, height: 18 }}
                  />
                  <label htmlFor="evento-equipo-esEscribible">
                    Escribible {formValues.habilitarAlarma && '(bloqueado - alarma es solo lectura)'}
                  </label>
                </Box>
                {/* Intervalo de lectura - oculto al final con ayuda */}
                <input type="hidden" name="intervaloLectura" value={formValues.intervaloLectura} />
                <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1, mt: 2 }}>
                  <Button onClick={hideDialogNuevo}>Cancelar</Button>
                  <Button
                    variant="contained"
                    onClick={guardar}
                    disabled={updating || !formValues.nombreVariable || !formValues.direccionModbus}
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

      <Dialog open={deleteEventoEquipoDialog} onClose={hideDeleteDialog} maxWidth="xs" fullWidth>
        <DialogTitle>Confirmar</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 1 }}>
            <WarningAmberIcon sx={{ fontSize: 40, color: '#f59e0b' }} />
            {selectedEventoEquipo && (
              <Typography>
                ¿Seguro que quiere eliminar el Evento: <strong>{selectedEventoEquipo.nombreVariable}</strong>?
              </Typography>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={hideDeleteDialog}>No</Button>
          <Button onClick={deleteEventoEquipo} color="error" variant="contained">
            Sí
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={helpDialogOpen} onClose={() => setHelpDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>Ayuda: Configuración de Alarmas</DialogTitle>
        <DialogContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            <AlertTitle>¿Cómo funcionan las Alarmas?</AlertTitle>
            El sistema monitorea las variables de los equipos y genera alarmas cuando se cumplen ciertas condiciones.
          </Alert>

          <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
            Tipos de Alarmas
          </Typography>

          <Box sx={{ mb: 3 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600, color: 'error.main' }}>
              Alarma Numérica
            </Typography>
            <Typography variant="body2" sx={{ mb: 1 }}>
              Se dispara cuando el valor de la variable supera el umbral configurado.
            </Typography>
            <Box sx={{ bgcolor: '#f5f5f5', p: 2, borderRadius: 1, fontFamily: 'monospace' }}>
              <div>
                <strong>Ejemplo:</strong> Temperatura Motor
              </div>
              <div>Umbral Alerta = 80</div>
              <div>Resultado: Se dispara cuando temperatura &gt; 80°C</div>
            </Box>
          </Box>

          <Box sx={{ mb: 3 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600, color: 'warning.main' }}>
              Alarma Booleana
            </Typography>
            <Typography variant="body2" sx={{ mb: 1 }}>
              Se dispara cuando el valor booleano es TRUE (activado).
            </Typography>
            <Box sx={{ bgcolor: '#f5f5f5', p: 2, borderRadius: 1, fontFamily: 'monospace' }}>
              <div>
                <strong>Ejemplo:</strong> Puerta de Acceso
              </div>
              <div>Umbral Alerta = 1</div>
              <div>Resultado: Se dispara cuando puerta = ABIERTA (true)</div>
            </Box>
          </Box>

          <Box sx={{ mb: 3 }}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600, color: 'text.disabled' }}>
              Sin Alarma
            </Typography>
            <Typography variant="body2" sx={{ mb: 1 }}>
              No se genera ninguna alarma para esta variable.
            </Typography>
            <Box sx={{ bgcolor: '#f5f5f5', p: 2, borderRadius: 1, fontFamily: 'monospace' }}>
              <div>
                <strong>Configuración:</strong>
              </div>
              <div>Resultado: La variable se monitorea pero no genera alarmas</div>
            </Box>
          </Box>

          <Typography variant="h6" sx={{ mt: 3, mb: 1 }}>
            Estados de Alarma
          </Typography>
          <Box sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', mb: 2 }}>
            <Box sx={{ px: 2, py: 1, bgcolor: 'error.main', color: 'white', borderRadius: 1 }}>ACTIVA</Box>
            <Box sx={{ px: 2, py: 1, bgcolor: 'warning.main', color: 'white', borderRadius: 1 }}>RECONOCIDA</Box>
            <Box sx={{ px: 2, py: 1, bgcolor: 'success.main', color: 'white', borderRadius: 1 }}>FINALIZADA</Box>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setHelpDialogOpen(false)}>Cerrar</Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default EventoEquipo;
