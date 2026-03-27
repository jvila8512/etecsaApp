import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Translate } from 'react-jhipster';
import { Box, Button, Paper, Typography } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import EditIcon from '@mui/icons-material/Edit';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './alarma.reducer';

const formatDate = (dateValue: string | Date): string => {
  if (!dateValue) return '-';
  const date = typeof dateValue === 'string' ? new Date(dateValue) : dateValue;
  const day = date.getDate().toString().padStart(2, '0');
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const year = date.getFullYear();
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${day}/${month}/${year} ${hours}:${minutes}`;
};

export const AlarmaDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const alarmaEntity = useAppSelector(state => state.alarma.entity);
  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h5" sx={{ mb: 2, fontWeight: 700 }}>
        <Translate contentKey="appsupervisorApp.alarma.detail.title">Alarma</Translate>
      </Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="global.field.id">ID</Translate>:
          </Typography>
          <Typography>{alarmaEntity.id}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.alarma.descripcion">Descripcion</Translate>:
          </Typography>
          <Typography>{alarmaEntity.descripcion}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.alarma.activatedAt">Activated At</Translate>:
          </Typography>
          <Typography>{formatDate(alarmaEntity.activatedAt)}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.alarma.deactivatedAt">Deactivated At</Translate>:
          </Typography>
          <Typography>{formatDate(alarmaEntity.deactivatedAt)}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.alarma.severidad">Severidad</Translate>:
          </Typography>
          <Typography>{alarmaEntity.severidad}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.alarma.estado">Estado</Translate>:
          </Typography>
          <Typography>{alarmaEntity.estado}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.alarma.mensajeUsuario">Mensaje Usuario</Translate>:
          </Typography>
          <Typography>{alarmaEntity.mensajeUsuario}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.alarma.evento">Evento</Translate>:
          </Typography>
          <Typography>{alarmaEntity.evento ? alarmaEntity.evento.id : ''}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.alarma.acknowledgedBy">Acknowledged By</Translate>:
          </Typography>
          <Typography>{alarmaEntity.acknowledgedBy ? alarmaEntity.acknowledgedBy.login : ''}</Typography>
        </Box>
      </Box>
      <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
        <Button component={Link} to="/alarma" replace color="info" startIcon={<ArrowBackIcon />} data-cy="entityDetailsBackButton">
          <Translate contentKey="entity.action.back">Back</Translate>
        </Button>
        <Button component={Link} to={`/alarma/${alarmaEntity.id}/edit`} replace variant="contained" startIcon={<EditIcon />}>
          <Translate contentKey="entity.action.edit">Edit</Translate>
        </Button>
      </Box>
    </Paper>
  );
};

export default AlarmaDetail;
