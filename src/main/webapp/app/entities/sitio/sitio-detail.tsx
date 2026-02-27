import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './sitio.reducer';

export const SitioDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const sitioEntity = useAppSelector(state => state.sitio.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="sitioDetailsHeading">
          <Translate contentKey="appsupervisorApp.sitio.detail.title">Sitio</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{sitioEntity.id}</dd>
          <dt>
            <span id="nombre">
              <Translate contentKey="appsupervisorApp.sitio.nombre">Nombre</Translate>
            </span>
          </dt>
          <dd>{sitioEntity.nombre}</dd>
          <dt>
            <span id="codigo">
              <Translate contentKey="appsupervisorApp.sitio.codigo">Codigo</Translate>
            </span>
          </dt>
          <dd>{sitioEntity.codigo}</dd>
          <dt>
            <span id="ubicacion">
              <Translate contentKey="appsupervisorApp.sitio.ubicacion">Ubicacion</Translate>
            </span>
          </dt>
          <dd>{sitioEntity.ubicacion}</dd>
          <dt>
            <span id="fechaRegistro">
              <Translate contentKey="appsupervisorApp.sitio.fechaRegistro">Fecha Registro</Translate>
            </span>
          </dt>
          <dd>
            {sitioEntity.fechaRegistro ? <TextFormat value={sitioEntity.fechaRegistro} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
        </dl>
        <Button tag={Link} to="/sitio" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/sitio/${sitioEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default SitioDetail;
