import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Translate } from 'react-jhipster';
import { Box, Button, Paper, Typography } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import EditIcon from '@mui/icons-material/Edit';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './especialidad.reducer';

export const EspecialidadDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const especialidadEntity = useAppSelector(state => state.especialidad.entity);
  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h5" sx={{ mb: 2, fontWeight: 700 }}>
        <Translate contentKey="appsupervisorApp.especialidad.detail.title">Especialidad</Translate>
      </Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="global.field.id">ID</Translate>:
          </Typography>
          <Typography>{especialidadEntity.id}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.especialidad.nombre">Nombre</Translate>:
          </Typography>
          <Typography>{especialidadEntity.nombre}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.especialidad.codigo">Codigo</Translate>:
          </Typography>
          <Typography>{especialidadEntity.codigo}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.especialidad.descripcionTecnica">Descripcion Tecnica</Translate>:
          </Typography>
          <Typography>{especialidadEntity.descripcionTecnica}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.especialidad.equipos">Equipos</Translate>:
          </Typography>
          <Typography>
            {especialidadEntity.equipos
              ? especialidadEntity.equipos.map((val, i) => (
                  <span key={val.id}>
                    <a>{val.id}</a>
                    {especialidadEntity.equipos && i === especialidadEntity.equipos.length - 1 ? '' : ', '}
                  </span>
                ))
              : null}
          </Typography>
        </Box>
      </Box>
      <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
        <Button component={Link} to="/especialidad" replace color="info" startIcon={<ArrowBackIcon />} data-cy="entityDetailsBackButton">
          <Translate contentKey="entity.action.back">Back</Translate>
        </Button>
        <Button component={Link} to={`/especialidad/${especialidadEntity.id}/edit`} replace variant="contained" startIcon={<EditIcon />}>
          <Translate contentKey="entity.action.edit">Edit</Translate>
        </Button>
      </Box>
    </Paper>
  );
};

export default EspecialidadDetail;
