import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Translate } from 'react-jhipster';
import { Box, Button, Paper, Typography } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import EditIcon from '@mui/icons-material/Edit';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './evento-plantilla.reducer';

export const EventoPlantillaDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const eventoPlantillaEntity = useAppSelector(state => state.eventoPlantilla.entity);
  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h5" sx={{ mb: 2, fontWeight: 700 }}>
        <Translate contentKey="appsupervisorApp.eventoPlantilla.detail.title">EventoPlantilla</Translate>
      </Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="global.field.id">ID</Translate>:
          </Typography>
          <Typography>{eventoPlantillaEntity.id}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoPlantilla.nombre">Nombre</Translate>:
          </Typography>
          <Typography>{eventoPlantillaEntity.nombre}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoPlantilla.descripcion">Descripcion</Translate>:
          </Typography>
          <Typography>{eventoPlantillaEntity.descripcion}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoPlantilla.scalingFactor">Scaling Factor</Translate>:
          </Typography>
          <Typography>{eventoPlantillaEntity.scalingFactor}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoPlantilla.unidadMedida">Unidad Medida</Translate>:
          </Typography>
          <Typography>{eventoPlantillaEntity.unidadMedida}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoPlantilla.funcionLectura">Funcion Lectura</Translate>:
          </Typography>
          <Typography>{eventoPlantillaEntity.funcionLectura}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoPlantilla.funcionEscritura">Funcion Escritura</Translate>:
          </Typography>
          <Typography>{eventoPlantillaEntity.funcionEscritura}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoPlantilla.especialidad">Especialidad</Translate>:
          </Typography>
          <Typography>{eventoPlantillaEntity.especialidad ? eventoPlantillaEntity.especialidad.nombre : ''}</Typography>
        </Box>
      </Box>
      <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
        <Button
          component={Link}
          to="/evento-plantilla"
          replace
          color="info"
          startIcon={<ArrowBackIcon />}
          data-cy="entityDetailsBackButton"
        >
          <Translate contentKey="entity.action.back">Back</Translate>
        </Button>
        <Button
          component={Link}
          to={`/evento-plantilla/${eventoPlantillaEntity.id}/edit`}
          replace
          variant="contained"
          startIcon={<EditIcon />}
        >
          <Translate contentKey="entity.action.edit">Edit</Translate>
        </Button>
      </Box>
    </Paper>
  );
};

export default EventoPlantillaDetail;
