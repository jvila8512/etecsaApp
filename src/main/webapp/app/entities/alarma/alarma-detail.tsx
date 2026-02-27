import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './alarma.reducer';

export const AlarmaDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const alarmaEntity = useAppSelector(state => state.alarma.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="alarmaDetailsHeading">
          <Translate contentKey="appsupervisorApp.alarma.detail.title">Alarma</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{alarmaEntity.id}</dd>
          <dt>
            <span id="descripcion">
              <Translate contentKey="appsupervisorApp.alarma.descripcion">Descripcion</Translate>
            </span>
          </dt>
          <dd>{alarmaEntity.descripcion}</dd>
          <dt>
            <span id="activatedAt">
              <Translate contentKey="appsupervisorApp.alarma.activatedAt">Activated At</Translate>
            </span>
          </dt>
          <dd>{alarmaEntity.activatedAt ? <TextFormat value={alarmaEntity.activatedAt} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="deactivatedAt">
              <Translate contentKey="appsupervisorApp.alarma.deactivatedAt">Deactivated At</Translate>
            </span>
          </dt>
          <dd>
            {alarmaEntity.deactivatedAt ? <TextFormat value={alarmaEntity.deactivatedAt} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <span id="severidad">
              <Translate contentKey="appsupervisorApp.alarma.severidad">Severidad</Translate>
            </span>
          </dt>
          <dd>{alarmaEntity.severidad}</dd>
          <dt>
            <span id="estado">
              <Translate contentKey="appsupervisorApp.alarma.estado">Estado</Translate>
            </span>
          </dt>
          <dd>{alarmaEntity.estado}</dd>
          <dt>
            <span id="mensajeUsuario">
              <Translate contentKey="appsupervisorApp.alarma.mensajeUsuario">Mensaje Usuario</Translate>
            </span>
          </dt>
          <dd>{alarmaEntity.mensajeUsuario}</dd>
          <dt>
            <Translate contentKey="appsupervisorApp.alarma.evento">Evento</Translate>
          </dt>
          <dd>{alarmaEntity.evento ? alarmaEntity.evento.id : ''}</dd>
          <dt>
            <Translate contentKey="appsupervisorApp.alarma.acknowledgedBy">Acknowledged By</Translate>
          </dt>
          <dd>{alarmaEntity.acknowledgedBy ? alarmaEntity.acknowledgedBy.login : ''}</dd>
        </dl>
        <Button tag={Link} to="/alarma" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/alarma/${alarmaEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default AlarmaDetail;
