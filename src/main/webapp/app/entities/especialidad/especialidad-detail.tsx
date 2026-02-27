import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

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
    <Row>
      <Col md="8">
        <h2 data-cy="especialidadDetailsHeading">
          <Translate contentKey="appsupervisorApp.especialidad.detail.title">Especialidad</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{especialidadEntity.id}</dd>
          <dt>
            <span id="nombre">
              <Translate contentKey="appsupervisorApp.especialidad.nombre">Nombre</Translate>
            </span>
          </dt>
          <dd>{especialidadEntity.nombre}</dd>
          <dt>
            <span id="codigo">
              <Translate contentKey="appsupervisorApp.especialidad.codigo">Codigo</Translate>
            </span>
          </dt>
          <dd>{especialidadEntity.codigo}</dd>
          <dt>
            <span id="descripcionTecnica">
              <Translate contentKey="appsupervisorApp.especialidad.descripcionTecnica">Descripcion Tecnica</Translate>
            </span>
          </dt>
          <dd>{especialidadEntity.descripcionTecnica}</dd>
          <dt>
            <Translate contentKey="appsupervisorApp.especialidad.equipos">Equipos</Translate>
          </dt>
          <dd>
            {especialidadEntity.equipos
              ? especialidadEntity.equipos.map((val, i) => (
                  <span key={val.id}>
                    <a>{val.id}</a>
                    {especialidadEntity.equipos && i === especialidadEntity.equipos.length - 1 ? '' : ', '}
                  </span>
                ))
              : null}
          </dd>
        </dl>
        <Button tag={Link} to="/especialidad" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/especialidad/${especialidadEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default EspecialidadDetail;
