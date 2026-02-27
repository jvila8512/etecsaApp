import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

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
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="appsupervisorApp.especialidad.home.createOrEditLabel" data-cy="EspecialidadCreateUpdateHeading">
            <Translate contentKey="appsupervisorApp.especialidad.home.createOrEditLabel">Create or edit a Especialidad</Translate>
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
                  id="especialidad-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
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
              <ValidatedField
                label={translate('appsupervisorApp.especialidad.descripcionTecnica')}
                id="especialidad-descripcionTecnica"
                name="descripcionTecnica"
                data-cy="descripcionTecnica"
                type="text"
              />
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/especialidad" replace color="info">
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

export default EspecialidadUpdate;
