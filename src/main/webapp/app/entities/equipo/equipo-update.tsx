import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Box, Button, Grid, Paper, Typography } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getSitios } from 'app/entities/sitio/sitio.reducer';
import { getEntities as getEspecialidads } from 'app/entities/especialidad/especialidad.reducer';
import { EstadoEquipo } from 'app/shared/model/enumerations/estado-equipo.model';
import { createEntity, getEntity, reset, updateEntity } from './equipo.reducer';

export const EquipoUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const sitios = useAppSelector(state => state.sitio.entities);
  const especialidads = useAppSelector(state => state.especialidad.entities);
  const equipoEntity = useAppSelector(state => state.equipo.entity);
  const loading = useAppSelector(state => state.equipo.loading);
  const updating = useAppSelector(state => state.equipo.updating);
  const updateSuccess = useAppSelector(state => state.equipo.updateSuccess);
  const estadoEquipoValues = Object.keys(EstadoEquipo);

  const handleClose = () => {
    navigate(`/equipo${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getSitios({}));
    dispatch(getEspecialidads({}));
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
    if (values.modbusSlaveId !== undefined && typeof values.modbusSlaveId !== 'number') {
      values.modbusSlaveId = Number(values.modbusSlaveId);
    }
    values.ultimoHeartbeat = convertDateTimeToServer(values.ultimoHeartbeat);

    const entity = {
      ...equipoEntity,
      ...values,
      sitio: sitios.find(it => it.id.toString() === values.sitio?.toString()),
      especialidades: mapIdList(values.especialidades),
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
          ultimoHeartbeat: displayDefaultDateTime(),
        }
      : {
          estado: 'OPERATIVO',
          ...equipoEntity,
          ultimoHeartbeat: convertDateTimeFromServer(equipoEntity.ultimoHeartbeat),
          sitio: equipoEntity?.sitio?.id,
          especialidades: equipoEntity?.especialidades?.map(e => e.id.toString()),
        };

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 700 }}>
        <Translate contentKey="appsupervisorApp.equipo.home.createOrEditLabel">Create or edit a Equipo</Translate>
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
                  id="equipo-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              </Grid>
            ) : null}
            <Grid size={{ xs: 12 }}>
              <ValidatedField
                label={translate('appsupervisorApp.equipo.nombre')}
                id="equipo-nombre"
                name="nombre"
                data-cy="nombre"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <ValidatedField
                label={translate('appsupervisorApp.equipo.direccionIp')}
                id="equipo-direccionIp"
                name="direccionIp"
                data-cy="direccionIp"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.equipo.modbusSlaveId')}
                id="equipo-modbusSlaveId"
                name="modbusSlaveId"
                data-cy="modbusSlaveId"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.equipo.modelo')}
                id="equipo-modelo"
                name="modelo"
                data-cy="modelo"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.equipo.firmwareVersion')}
                id="equipo-firmwareVersion"
                name="firmwareVersion"
                data-cy="firmwareVersion"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.equipo.estado')}
                id="equipo-estado"
                name="estado"
                data-cy="estado"
                type="select"
              >
                {estadoEquipoValues.map(estadoEquipo => (
                  <option value={estadoEquipo} key={estadoEquipo}>
                    {translate(`appsupervisorApp.EstadoEquipo.${estadoEquipo}`)}
                  </option>
                ))}
              </ValidatedField>
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.equipo.ultimoHeartbeat')}
                id="equipo-ultimoHeartbeat"
                name="ultimoHeartbeat"
                data-cy="ultimoHeartbeat"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                id="equipo-sitio"
                name="sitio"
                data-cy="sitio"
                label={translate('appsupervisorApp.equipo.sitio')}
                type="select"
              >
                <option value="" key="0" />
                {sitios
                  ? sitios.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.nombre}
                      </option>
                    ))
                  : null}
              </ValidatedField>
            </Grid>
            <Grid size={{ xs: 12 }}>
              <ValidatedField
                label={translate('appsupervisorApp.equipo.especialidades')}
                id="equipo-especialidades"
                data-cy="especialidades"
                type="select"
                multiple
                name="especialidades"
              >
                <option value="" key="0" />
                {especialidads
                  ? especialidads.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.nombre}
                      </option>
                    ))
                  : null}
              </ValidatedField>
            </Grid>
            <Grid size={{ xs: 12 }}>
              <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
                <Button component={Link} to="/equipo" replace color="info" startIcon={<ArrowBackIcon />} data-cy="entityCreateCancelButton">
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

export default EquipoUpdate;
