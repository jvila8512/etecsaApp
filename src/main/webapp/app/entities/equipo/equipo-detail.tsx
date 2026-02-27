import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './equipo.reducer';

export const EquipoDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const equipoEntity = useAppSelector(state => state.equipo.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="equipoDetailsHeading">
          <Translate contentKey="appsupervisorApp.equipo.detail.title">Equipo</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{equipoEntity.id}</dd>
          <dt>
            <span id="nombre">
              <Translate contentKey="appsupervisorApp.equipo.nombre">Nombre</Translate>
            </span>
          </dt>
          <dd>{equipoEntity.nombre}</dd>
          <dt>
            <span id="direccionIp">
              <Translate contentKey="appsupervisorApp.equipo.direccionIp">Direccion Ip</Translate>
            </span>
          </dt>
          <dd>{equipoEntity.direccionIp}</dd>
          <dt>
            <span id="modbusSlaveId">
              <Translate contentKey="appsupervisorApp.equipo.modbusSlaveId">Modbus Slave Id</Translate>
            </span>
          </dt>
          <dd>{equipoEntity.modbusSlaveId}</dd>
          <dt>
            <span id="modelo">
              <Translate contentKey="appsupervisorApp.equipo.modelo">Modelo</Translate>
            </span>
          </dt>
          <dd>{equipoEntity.modelo}</dd>
          <dt>
            <span id="firmwareVersion">
              <Translate contentKey="appsupervisorApp.equipo.firmwareVersion">Firmware Version</Translate>
            </span>
          </dt>
          <dd>{equipoEntity.firmwareVersion}</dd>
          <dt>
            <span id="estado">
              <Translate contentKey="appsupervisorApp.equipo.estado">Estado</Translate>
            </span>
          </dt>
          <dd>{equipoEntity.estado}</dd>
          <dt>
            <span id="ultimoHeartbeat">
              <Translate contentKey="appsupervisorApp.equipo.ultimoHeartbeat">Ultimo Heartbeat</Translate>
            </span>
          </dt>
          <dd>
            {equipoEntity.ultimoHeartbeat ? <TextFormat value={equipoEntity.ultimoHeartbeat} type="date" format={APP_DATE_FORMAT} /> : null}
          </dd>
          <dt>
            <Translate contentKey="appsupervisorApp.equipo.sitio">Sitio</Translate>
          </dt>
          <dd>{equipoEntity.sitio ? equipoEntity.sitio.nombre : ''}</dd>
          <dt>
            <Translate contentKey="appsupervisorApp.equipo.especialidades">Especialidades</Translate>
          </dt>
          <dd>
            {equipoEntity.especialidades
              ? equipoEntity.especialidades.map((val, i) => (
                  <span key={val.id}>
                    <a>{val.nombre}</a>
                    {equipoEntity.especialidades && i === equipoEntity.especialidades.length - 1 ? '' : ', '}
                  </span>
                ))
              : null}
          </dd>
        </dl>
        <Button tag={Link} to="/equipo" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/equipo/${equipoEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default EquipoDetail;
