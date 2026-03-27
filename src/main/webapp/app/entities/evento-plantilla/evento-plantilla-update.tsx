import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Box, Button, Grid, Paper, Typography } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getEspecialidads } from 'app/entities/especialidad/especialidad.reducer';
import { createEntity, getEntity, reset, updateEntity } from './evento-plantilla.reducer';

export const EventoPlantillaUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const especialidads = useAppSelector(state => state.especialidad.entities);
  const eventoPlantillaEntity = useAppSelector(state => state.eventoPlantilla.entity);
  const loading = useAppSelector(state => state.eventoPlantilla.loading);
  const updating = useAppSelector(state => state.eventoPlantilla.updating);
  const updateSuccess = useAppSelector(state => state.eventoPlantilla.updateSuccess);

  const handleClose = () => {
    navigate(`/evento-plantilla${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

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
    if (values.scalingFactor !== undefined && typeof values.scalingFactor !== 'number') {
      values.scalingFactor = Number(values.scalingFactor);
    }

    const entity = {
      ...eventoPlantillaEntity,
      ...values,
      especialidad: especialidads.find(it => it.id.toString() === values.especialidad?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...eventoPlantillaEntity,
          especialidad: eventoPlantillaEntity?.especialidad?.id,
        };

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 700 }}>
        <Translate contentKey="appsupervisorApp.eventoPlantilla.home.createOrEditLabel">Create or edit a EventoPlantilla</Translate>
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
                  id="evento-plantilla-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              </Grid>
            ) : null}
            <Grid size={{ xs: 12 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoPlantilla.nombre')}
                id="evento-plantilla-nombre"
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
                label={translate('appsupervisorApp.eventoPlantilla.descripcion')}
                id="evento-plantilla-descripcion"
                name="descripcion"
                data-cy="descripcion"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoPlantilla.scalingFactor')}
                id="evento-plantilla-scalingFactor"
                name="scalingFactor"
                data-cy="scalingFactor"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoPlantilla.unidadMedida')}
                id="evento-plantilla-unidadMedida"
                name="unidadMedida"
                data-cy="unidadMedida"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoPlantilla.funcionLectura')}
                id="evento-plantilla-funcionLectura"
                name="funcionLectura"
                data-cy="funcionLectura"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12, sm: 6 }}>
              <ValidatedField
                label={translate('appsupervisorApp.eventoPlantilla.funcionEscritura')}
                id="evento-plantilla-funcionEscritura"
                name="funcionEscritura"
                data-cy="funcionEscritura"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <ValidatedField
                id="evento-plantilla-especialidad"
                name="especialidad"
                data-cy="especialidad"
                label={translate('appsupervisorApp.eventoPlantilla.especialidad')}
                type="select"
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
                <Button
                  component={Link}
                  to="/evento-plantilla"
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

export default EventoPlantillaUpdate;
