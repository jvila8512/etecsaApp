import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { TextFormat, Translate } from 'react-jhipster';
import { Box, Button, Paper, Typography } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import EditIcon from '@mui/icons-material/Edit';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './evento-equipo.reducer';

export const EventoEquipoDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const eventoEquipoEntity = useAppSelector(state => state.eventoEquipo.entity);
  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h5" sx={{ mb: 2, fontWeight: 700 }}>
        <Translate contentKey="appsupervisorApp.eventoEquipo.detail.title">EventoEquipo</Translate>
      </Typography>
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="global.field.id">ID</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.id}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.nombreVariable">Nombre Variable</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.nombreVariable}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.direccionModbus">Direccion Modbus</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.direccionModbus}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.tipoRegistro">Tipo Registro</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.tipoRegistro}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.tipoDato">Tipo Dato</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.tipoDato}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.esEscribible">Es Escribible</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.esEscribible ? 'true' : 'false'}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.valorNumerico">Valor Numerico</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.valorNumerico}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.valorBooleano">Valor Booleano</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.valorBooleano ? 'true' : 'false'}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.timestampActualizacion">Timestamp Actualizacion</Translate>:
          </Typography>
          <Typography>
            {eventoEquipoEntity.timestampActualizacion ? (
              <TextFormat value={eventoEquipoEntity.timestampActualizacion} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.intervaloLectura">Intervalo Lectura</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.intervaloLectura}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.umbralAlerta">Umbral Alerta</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.umbralAlerta}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.equipo">Equipo</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.equipo ? eventoEquipoEntity.equipo.nombre : ''}</Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
            <Translate contentKey="appsupervisorApp.eventoEquipo.plantilla">Plantilla</Translate>:
          </Typography>
          <Typography>{eventoEquipoEntity.plantilla ? eventoEquipoEntity.plantilla.nombre : ''}</Typography>
        </Box>
      </Box>
      <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
        <Button component={Link} to="/evento-equipo" replace color="info" startIcon={<ArrowBackIcon />} data-cy="entityDetailsBackButton">
          <Translate contentKey="entity.action.back">Back</Translate>
        </Button>
        <Button component={Link} to={`/evento-equipo/${eventoEquipoEntity.id}/edit`} replace variant="contained" startIcon={<EditIcon />}>
          <Translate contentKey="entity.action.edit">Edit</Translate>
        </Button>
      </Box>
    </Paper>
  );
};

export default EventoEquipoDetail;
