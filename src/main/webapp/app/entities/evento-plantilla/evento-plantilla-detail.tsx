import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

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
    <Row>
      <Col md="8">
        <h2 data-cy="eventoPlantillaDetailsHeading">
          <Translate contentKey="appsupervisorApp.eventoPlantilla.detail.title">EventoPlantilla</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{eventoPlantillaEntity.id}</dd>
          <dt>
            <span id="nombre">
              <Translate contentKey="appsupervisorApp.eventoPlantilla.nombre">Nombre</Translate>
            </span>
          </dt>
          <dd>{eventoPlantillaEntity.nombre}</dd>
          <dt>
            <span id="descripcion">
              <Translate contentKey="appsupervisorApp.eventoPlantilla.descripcion">Descripcion</Translate>
            </span>
          </dt>
          <dd>{eventoPlantillaEntity.descripcion}</dd>
          <dt>
            <span id="scalingFactor">
              <Translate contentKey="appsupervisorApp.eventoPlantilla.scalingFactor">Scaling Factor</Translate>
            </span>
          </dt>
          <dd>{eventoPlantillaEntity.scalingFactor}</dd>
          <dt>
            <span id="unidadMedida">
              <Translate contentKey="appsupervisorApp.eventoPlantilla.unidadMedida">Unidad Medida</Translate>
            </span>
          </dt>
          <dd>{eventoPlantillaEntity.unidadMedida}</dd>
          <dt>
            <span id="funcionLectura">
              <Translate contentKey="appsupervisorApp.eventoPlantilla.funcionLectura">Funcion Lectura</Translate>
            </span>
          </dt>
          <dd>{eventoPlantillaEntity.funcionLectura}</dd>
          <dt>
            <span id="funcionEscritura">
              <Translate contentKey="appsupervisorApp.eventoPlantilla.funcionEscritura">Funcion Escritura</Translate>
            </span>
          </dt>
          <dd>{eventoPlantillaEntity.funcionEscritura}</dd>
          <dt>
            <Translate contentKey="appsupervisorApp.eventoPlantilla.especialidad">Especialidad</Translate>
          </dt>
          <dd>{eventoPlantillaEntity.especialidad ? eventoPlantillaEntity.especialidad.nombre : ''}</dd>
        </dl>
        <Button tag={Link} to="/evento-plantilla" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/evento-plantilla/${eventoPlantillaEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default EventoPlantillaDetail;
