import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

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
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="appsupervisorApp.equipo.home.createOrEditLabel" data-cy="EquipoCreateUpdateHeading">
            <Translate contentKey="appsupervisorApp.equipo.home.createOrEditLabel">Create or edit a Equipo</Translate>
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
                  id="equipo-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
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
              <ValidatedField
                label={translate('appsupervisorApp.equipo.modbusSlaveId')}
                id="equipo-modbusSlaveId"
                name="modbusSlaveId"
                data-cy="modbusSlaveId"
                type="text"
              />
              <ValidatedField
                label={translate('appsupervisorApp.equipo.modelo')}
                id="equipo-modelo"
                name="modelo"
                data-cy="modelo"
                type="text"
              />
              <ValidatedField
                label={translate('appsupervisorApp.equipo.firmwareVersion')}
                id="equipo-firmwareVersion"
                name="firmwareVersion"
                data-cy="firmwareVersion"
                type="text"
              />
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
              <ValidatedField
                label={translate('appsupervisorApp.equipo.ultimoHeartbeat')}
                id="equipo-ultimoHeartbeat"
                name="ultimoHeartbeat"
                data-cy="ultimoHeartbeat"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
              />
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/equipo" replace color="info">
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

export default EquipoUpdate;
