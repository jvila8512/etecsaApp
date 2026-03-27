import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { Box, Button, Grid, Paper, Typography } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';

import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getEquipos } from 'app/entities/equipo/equipo.reducer';
import { createEntity, getEntity, reset, updateEntity } from './especialidad.reducer';

export const EspecialidadUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const equipos = useAppSelector(state => state.equipo.entities);
  const especialidadEntity = useAppSelector(state => state.especialidad.entity);
  const loading = useAppSelector(state => state.especialidad.loading);
  const updating = useAppSelector(state => state.especialidad.updating);
  const updateSuccess = useAppSelector(state => state.especialidad.updateSuccess);

  const handleClose = () => {
    navigate(`/especialidad${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getEquipos({}));
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

    const entity = {
      ...especialidadEntity,
      ...values,
      equipos: mapIdList(values.equipos),
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
          ...especialidadEntity,
          equipos: especialidadEntity?.equipos?.map(e => e.id.toString()),
        };

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h5" sx={{ mb: 3, fontWeight: 700 }}>
        <Translate contentKey="appsupervisorApp.especialidad.home.createOrEditLabel">Create or edit a Especialidad</Translate>
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
                  id="especialidad-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              </Grid>
            ) : null}
            <Grid size={{ xs: 12 }}>
              <ValidatedField
                label={translate('appsupervisorApp.especialidad.nombre')}
                id="especialidad-nombre"
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
                label={translate('appsupervisorApp.especialidad.codigo')}
                id="especialidad-codigo"
                name="codigo"
                data-cy="codigo"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <ValidatedField
                label={translate('appsupervisorApp.especialidad.descripcionTecnica')}
                id="especialidad-descripcionTecnica"
                name="descripcionTecnica"
                data-cy="descripcionTecnica"
                type="text"
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <ValidatedField
                label={translate('appsupervisorApp.especialidad.equipos')}
                id="especialidad-equipos"
                data-cy="equipos"
                type="select"
                multiple
                name="equipos"
              >
                <option value="" key="0" />
                {equipos
                  ? equipos.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
            </Grid>
            <Grid size={{ xs: 12 }}>
              <Box sx={{ display: 'flex', gap: 2, mt: 2 }}>
                <Button
                  component={Link}
                  to="/especialidad"
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

export default EspecialidadUpdate;
