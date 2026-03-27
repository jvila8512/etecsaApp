import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Translate } from 'react-jhipster';
import { Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Typography } from '@mui/material';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { deleteEntity, getEntity } from './evento-equipo.reducer';

export const EventoEquipoDeleteDialog = () => {
  const dispatch = useAppDispatch();
  const pageLocation = useLocation();
  const navigate = useNavigate();
  const { id } = useParams<'id'>();

  const [loadModal, setLoadModal] = useState(false);

  useEffect(() => {
    dispatch(getEntity(id));
    setLoadModal(true);
  }, []);

  const eventoEquipoEntity = useAppSelector(state => state.eventoEquipo.entity);
  const updateSuccess = useAppSelector(state => state.eventoEquipo.updateSuccess);

  const handleClose = () => {
    navigate(`/evento-equipo${pageLocation.search}`);
  };

  useEffect(() => {
    if (updateSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [updateSuccess]);

  const confirmDelete = () => {
    dispatch(deleteEntity(eventoEquipoEntity.id));
  };

  return (
    <Dialog open onClose={handleClose} maxWidth="xs" fullWidth>
      <DialogTitle>
        <Translate contentKey="entity.delete.title">Confirm delete operation</Translate>
      </DialogTitle>
      <DialogContent>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 1 }}>
          <WarningAmberIcon sx={{ fontSize: 40, color: '#f59e0b' }} />
          <Typography id="appsupervisorApp.eventoEquipo.delete.question">
            <Translate contentKey="appsupervisorApp.eventoEquipo.delete.question" interpolate={{ id: eventoEquipoEntity.id }}>
              Are you sure you want to delete this EventoEquipo?
            </Translate>
          </Typography>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} color="inherit">
          <Translate contentKey="entity.action.cancel">Cancel</Translate>
        </Button>
        <Button
          id="jhi-confirm-delete-eventoEquipo"
          data-cy="entityConfirmDeleteButton"
          color="error"
          variant="contained"
          onClick={confirmDelete}
        >
          <Translate contentKey="entity.action.delete">Delete</Translate>
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default EventoEquipoDeleteDialog;
