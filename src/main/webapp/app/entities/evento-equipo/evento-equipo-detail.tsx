import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './evento-equipo.reducer';

export const EventoEquipoDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const eventoEquipoEntity = useAppSelector(state => state.eventoEquipo.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="eventoEquipoDetailsHeading">
          <Translate contentKey="appsupervisorApp.eventoEquipo.detail.title">EventoEquipo</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{eventoEquipoEntity.id}</dd>
          <dt>
            <span id="nombreVariable">
              <Translate contentKey="appsupervisorApp.eventoEquipo.nombreVariable">Nombre Variable</Translate>
            </span>
          </dt>
          <dd>{eventoEquipoEntity.nombreVariable}</dd>
          <dt>
            <span id="direccionModbus">
              <Translate contentKey="appsupervisorApp.eventoEquipo.direccionModbus">Direccion Modbus</Translate>
            </span>
          </dt>
          <dd>{eventoEquipoEntity.direccionModbus}</dd>
          <dt>
            <span id="tipoRegistro">
              <Translate contentKey="appsupervisorApp.eventoEquipo.tipoRegistro">Tipo Registro</Translate>
            </span>
          </dt>
          <dd>{eventoEquipoEntity.tipoRegistro}</dd>
          <dt>
            <span id="tipoDato">
              <Translate contentKey="appsupervisorApp.eventoEquipo.tipoDato">Tipo Dato</Translate>
            </span>
          </dt>
          <dd>{eventoEquipoEntity.tipoDato}</dd>
          <dt>
            <span id="esEscribible">
              <Translate contentKey="appsupervisorApp.eventoEquipo.esEscribible">Es Escribible</Translate>
            </span>
          </dt>
          <dd>{eventoEquipoEntity.esEscribible ? 'true' : 'false'}</dd>
          <dt>
            <span id="valorNumerico">
              <Translate contentKey="appsupervisorApp.eventoEquipo.valorNumerico">Valor Numerico</Translate>
            </span>
          </dt>
          <dd>{eventoEquipoEntity.valorNumerico}</dd>
          <dt>
            <span id="valorBooleano">
              <Translate contentKey="appsupervisorApp.eventoEquipo.valorBooleano">Valor Booleano</Translate>
            </span>
          </dt>
          <dd>{eventoEquipoEntity.valorBooleano ? 'true' : 'false'}</dd>
          <dt>
            <span id="timestampActualizacion">
              <Translate contentKey="appsupervisorApp.eventoEquipo.timestampActualizacion">Timestamp Actualizacion</Translate>
            </span>
          </dt>
          <dd>
            {eventoEquipoEntity.timestampActualizacion ? (
              <TextFormat value={eventoEquipoEntity.timestampActualizacion} type="date" format={APP_DATE_FORMAT} />
            ) : null}
          </dd>
          <dt>
            <span id="intervaloLectura">
              <Translate contentKey="appsupervisorApp.eventoEquipo.intervaloLectura">Intervalo Lectura</Translate>
            </span>
          </dt>
          <dd>{eventoEquipoEntity.intervaloLectura}</dd>
          <dt>
            <span id="umbralAlerta">
              <Translate contentKey="appsupervisorApp.eventoEquipo.umbralAlerta">Umbral Alerta</Translate>
            </span>
          </dt>
          <dd>{eventoEquipoEntity.umbralAlerta}</dd>
          <dt>
            <Translate contentKey="appsupervisorApp.eventoEquipo.equipo">Equipo</Translate>
          </dt>
          <dd>{eventoEquipoEntity.equipo ? eventoEquipoEntity.equipo.nombre : ''}</dd>
          <dt>
            <Translate contentKey="appsupervisorApp.eventoEquipo.plantilla">Plantilla</Translate>
          </dt>
          <dd>{eventoEquipoEntity.plantilla ? eventoEquipoEntity.plantilla.nombre : ''}</dd>
        </dl>
        <Button tag={Link} to="/evento-equipo" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/evento-equipo/${eventoEquipoEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default EventoEquipoDetail;
