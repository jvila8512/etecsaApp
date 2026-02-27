import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate, ValidatedField, ValidatedForm, translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

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
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="appsupervisorApp.eventoPlantilla.home.createOrEditLabel" data-cy="EventoPlantillaCreateUpdateHeading">
            <Translate contentKey="appsupervisorApp.eventoPlantilla.home.createOrEditLabel">Create or edit a EventoPlantilla</Translate>
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
                  id="evento-plantilla-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
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
              <ValidatedField
                label={translate('appsupervisorApp.eventoPlantilla.descripcion')}
                id="evento-plantilla-descripcion"
                name="descripcion"
                data-cy="descripcion"
                type="text"
              />
              <ValidatedField
                label={translate('appsupervisorApp.eventoPlantilla.scalingFactor')}
                id="evento-plantilla-scalingFactor"
                name="scalingFactor"
                data-cy="scalingFactor"
                type="text"
              />
              <ValidatedField
                label={translate('appsupervisorApp.eventoPlantilla.unidadMedida')}
                id="evento-plantilla-unidadMedida"
                name="unidadMedida"
                data-cy="unidadMedida"
                type="text"
              />
              <ValidatedField
                label={translate('appsupervisorApp.eventoPlantilla.funcionLectura')}
                id="evento-plantilla-funcionLectura"
                name="funcionLectura"
                data-cy="funcionLectura"
                type="text"
              />
              <ValidatedField
                label={translate('appsupervisorApp.eventoPlantilla.funcionEscritura')}
                id="evento-plantilla-funcionEscritura"
                name="funcionEscritura"
                data-cy="funcionEscritura"
                type="text"
              />
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/evento-plantilla" replace color="info">
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

export default EventoPlantillaUpdate;
