import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Translate, ValidatedField, ValidatedForm, isNumber, translate } from 'react-jhipster';
import { Box, Button, Grid, Paper, Typography } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getEquipos } from 'app/entities/equipo/equipo.reducer';
import { getEntities as getEventoPlantillas } from 'app/entities/evento-plantilla/evento-plantilla.reducer';
import { TipoRegistro } from 'app/shared/model/enumerations/tipo-registro.model';
import { TipoDato } from 'app/shared/model/enumerations/tipo-dato.model';
import { createEntity, getEntity, reset, updateEntity } from './evento-equipo.reducer';

export const EventoEquipoUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const equipos = useAppSelector(state => state.equipo.entities);
  const eventoPlantillas = useAppSelector(state => state.eventoPlantilla.entities);
  const eventoEquipoEntity = useAppSelector(state => state.eventoEquipo.entity);
  const loading = useAppSelector(state => state.eventoEquipo.loading);
  const updating = useAppSelector(state => state.eventoEquipo.updating);
  const updateSuccess = useAppSelector(state => state.eventoEquipo.updateSuccess);
  const tipoRegistroValues = Object.keys(TipoRegistro);
  const tipoDatoValues = Object.keys(TipoDato);

  const handleClose = () => {
    navigate(`/evento-equipo${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getEquipos({}));
    dispatch(getEventoPlantillas({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }
    if (values.direccionModbus !== undefined && typeof values.direccionModbus !== 'number') {
      values.direccionModbus = Number(values.direccionModbus);
    }
    if (values.valorNumerico !== undefined && typeof values.valorNumerico !== 'number') {
      values.valorNumerico = Number(values.valorNumerico);
    }
    values.timestampActualizacion = convertDateTimeToServer(values.timestampActualizacion);
    if (values.intervaloLectura !== undefined && typeof values.intervaloLectura !== 'number') {
      values.intervaloLectura = Number(values.intervaloLectura);
    }
    if (values.umbralAlerta !== undefined && typeof values.umbralAlerta !== 'number') {
      values.umbralAlerta = Number(values.umbralAlerta);
    }

    const entity = {
      ...eventoEquipoEntity,
      ...values,
      equipo: equipos.find(it => it.id.toString() === values.equipo?.toString()),
      plantilla: eventoPlantillas.find(it => it.id.toString() === values.plantilla?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {
          timestampActualizacion: displayDefaultDateTime(),
        }
      : {
          tipoRegistro: 'BIT_LOGICO_M',
          tipoDato: 'BOOLEAN',
          ...eventoEquipoEntity,
          timestampActualizacion: convertDateTimeFromServer(eventoEquipoEntity.timestampActualizacion),
          equipo: eventoEquipoEntity?.equipo?.id,
          plantilla: eventoEquipoEntity?.plantilla?.id,
        };

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 700 }}>
        <Translate contentKey="appsupervisorApp.eventoEquipo.home.createOrEditLabel">Create or edit a EventoEquipo</Translate>
      </Typography>
      {loading ? (
        <Typography>Loading...</Typography>
      ) : (
        <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
          <Grid container spacing={2}>
            {!isNew ? (
              <Grid size={{ xs: 12 }}>
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="evento-equipo-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              </Grid>
            ) : null}
            <Grid size={{ xs: 12 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoEquipo.nombreVariable')}
                id="evento-equipo-nombreVariable"
                name="nombreVariable"
                data-cy="nombreVariable"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoEquipo.direccionModbus')}
                id="evento-equipo-direccionModbus"
                name="direccionModbus"
                data-cy="direccionModbus"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                  min: { value: 0, message: translate('entity.validation.min', { min: 0 }) },
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoEquipo.tipoRegistro')}
                id="evento-equipo-tipoRegistro"
                name="tipoRegistro"
                data-cy="tipoRegistro"
                type="select"
              >
                {tipoRegistroValues.map(tipoRegistro => (
                  <option value={tipoRegistro} key={tipoRegistro}>
                    {translate(`appsupervisorApp.TipoRegistro.${tipoRegistro}`)}
                  </option>
                ))}
              </ValidatedField>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoEquipo.tipoDato')}
                id="evento-equipo-tipoDato"
                name="tipoDato"
                data-cy="tipoDato"
                type="select"
              >
                {tipoDatoValues.map(tipoDato => (
                  <option value={tipoDato} key={tipoDato}>
                    {translate(`appsupervisorApp.TipoDato.${tipoDato}`)}
                  </option>
                ))}
              </ValidatedField>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoEquipo.esEscribible')}
                id="evento-equipo-esEscribible"
                name="esEscribible"
                data-cy="esEscribible"
                type="checkbox"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoEquipo.valorNumerico')}
                id="evento-equipo-valorNumerico"
                name="valorNumerico"
                data-cy="valorNumerico"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoEquipo.valorBooleano')}
                id="evento-equipo-valorBooleano"
                name="valorBooleano"
                data-cy="valorBooleano"
                type="checkbox"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoEquipo.timestampActualizacion')}
                id="evento-equipo-timestampActualizacion"
                name="timestampActualizacion"
                data-cy="timestampActualizacion"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoEquipo.intervaloLectura')}
                id="evento-equipo-intervaloLectura"
                name="intervaloLectura"
                data-cy="intervaloLectura"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoEquipo.umbralAlerta')}
                id="evento-equipo-umbralAlerta"
                name="umbralAlerta"
                data-cy="umbralAlerta"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                id="evento-equipo-equipo"
                name="equipo"
                data-cy="equipo"
                label={translate('appsupervisorApp.eventoEquipo.equipo')}
                type="select"
              >
                <option value="" key="0" />
                {equipos
                  ? equipos.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.nombre}
                      </option>
                    ))
                  : null}
              </ValidatedField>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                id="evento-equipo-plantilla"
                name="plantilla"
                data-cy="plantilla"
                label={translate('appsupervisorApp.eventoEquipo.plantilla')}
                type="select"
              >
                <option value="" key="0" />
                {eventoPlantillas
                  ? eventoPlantillas.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.nombre}
                      </option>
                    ))
                  : null}
              </ValidatedField>
            </Grid>
            <Grid size={{ xs: 12 }}>
              <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
                <Button
                  component={Link}
                  to="/evento-equipo"
                  replace
                  color="info"
                  startIcon={<ArrowBackIcon />}
                  data-cy="entityCreateCancelButton"
                >
                  <Translate contentKey="entity.action.back">Back</Translate>
                </Button>
                <Button
                  variant="contained"
                  color="primary"
                  type="submit"
                  disabled={updating}
                  startIcon={<SaveIcon />}
                  data-cy="entityCreateSaveButton"
                >
                  <Translate contentKey="entity.action.save">Save</Translate>
                </Button>
              </Box>
            </Grid>
          </Grid>
        </ValidatedForm>
      )}
    </Paper>
  );
};

export default EventoEquipoUpdate;
