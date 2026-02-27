import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getEventoEquipos } from 'app/entities/evento-equipo/evento-equipo.reducer';
import { getUsers } from 'app/modules/administration/user-management/user-management.reducer';
import { Severidad } from 'app/shared/model/enumerations/severidad.model';
import { EstadoAlarma } from 'app/shared/model/enumerations/estado-alarma.model';
import { createEntity, getEntity, reset, updateEntity } from './alarma.reducer';

export const AlarmaUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const eventoEquipos = useAppSelector(state => state.eventoEquipo.entities);
  const users = useAppSelector(state => state.userManagement.users);
  const alarmaEntity = useAppSelector(state => state.alarma.entity);
  const loading = useAppSelector(state => state.alarma.loading);
  const updating = useAppSelector(state => state.alarma.updating);
  const updateSuccess = useAppSelector(state => state.alarma.updateSuccess);
  const severidadValues = Object.keys(Severidad);
  const estadoAlarmaValues = Object.keys(EstadoAlarma);

  const handleClose = () => {
    navigate(`/alarma${location.search}`);
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getEventoEquipos({}));
    dispatch(getUsers({}));
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
    values.activatedAt = convertDateTimeToServer(values.activatedAt);
    values.deactivatedAt = convertDateTimeToServer(values.deactivatedAt);

    const entity = {
      ...alarmaEntity,
      ...values,
      evento: eventoEquipos.find(it => it.id.toString() === values.evento?.toString()),
      acknowledgedBy: users.find(it => it.id.toString() === values.acknowledgedBy?.toString()),
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
          activatedAt: displayDefaultDateTime(),
          deactivatedAt: displayDefaultDateTime(),
        }
      : {
          severidad: 'BAJA',
          estado: 'ACTIVA',
          ...alarmaEntity,
          activatedAt: convertDateTimeFromServer(alarmaEntity.activatedAt),
          deactivatedAt: convertDateTimeFromServer(alarmaEntity.deactivatedAt),
          evento: alarmaEntity?.evento?.id,
          acknowledgedBy: alarmaEntity?.acknowledgedBy?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="appsupervisorApp.alarma.home.createOrEditLabel" data-cy="AlarmaCreateUpdateHeading">
            <Translate contentKey="appsupervisorApp.alarma.home.createOrEditLabel">Create or edit a Alarma</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="alarma-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('appsupervisorApp.alarma.descripcion')}
                id="alarma-descripcion"
                name="descripcion"
                data-cy="descripcion"
                type="text"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('appsupervisorApp.alarma.activatedAt')}
                id="alarma-activatedAt"
                name="activatedAt"
                data-cy="activatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: translate('entity.validation.required') },
                }}
              />
              <ValidatedField
                label={translate('appsupervisorApp.alarma.deactivatedAt')}
                id="alarma-deactivatedAt"
                name="deactivatedAt"
                data-cy="deactivatedAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
              <ValidatedField
                label={translate('appsupervisorApp.alarma.severidad')}
                id="alarma-severidad"
                name="severidad"
                data-cy="severidad"
                type="select"
              >
                {severidadValues.map(severidad => (
                  <option value={severidad} key={severidad}>
                    {translate(`appsupervisorApp.Severidad.${severidad}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('appsupervisorApp.alarma.estado')}
                id="alarma-estado"
                name="estado"
                data-cy="estado"
                type="select"
              >
                {estadoAlarmaValues.map(estadoAlarma => (
                  <option value={estadoAlarma} key={estadoAlarma}>
                    {translate(`appsupervisorApp.EstadoAlarma.${estadoAlarma}`)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('appsupervisorApp.alarma.mensajeUsuario')}
                id="alarma-mensajeUsuario"
                name="mensajeUsuario"
                data-cy="mensajeUsuario"
                type="text"
              />
              <ValidatedField
                id="alarma-evento"
                name="evento"
                data-cy="evento"
                label={translate('appsupervisorApp.alarma.evento')}
                type="select"
              >
                <option value="" key="0" />
                {eventoEquipos
                  ? eventoEquipos.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="alarma-acknowledgedBy"
                name="acknowledgedBy"
                data-cy="acknowledgedBy"
                label={translate('appsupervisorApp.alarma.acknowledgedBy')}
                type="select"
              >
                <option value="" key="0" />
                {users
                  ? users.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.login}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/alarma" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default AlarmaUpdate;
