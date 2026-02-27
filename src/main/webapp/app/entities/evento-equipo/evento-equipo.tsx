import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { JhiItemCount, JhiPagination, TextFormat, Translate, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities } from './evento-equipo.reducer';

export const EventoEquipo = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const eventoEquipoList = useAppSelector(state => state.eventoEquipo.entities);
  const loading = useAppSelector(state => state.eventoEquipo.loading);
  const totalItems = useAppSelector(state => state.eventoEquipo.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = paginationState.sort;
    const order = paginationState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="evento-equipo-heading" data-cy="EventoEquipoHeading">
        <Translate contentKey="appsupervisorApp.eventoEquipo.home.title">Evento Equipos</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="appsupervisorApp.eventoEquipo.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/evento-equipo/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="appsupervisorApp.eventoEquipo.home.createLabel">Create new Evento Equipo</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {eventoEquipoList && eventoEquipoList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.id">ID</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('nombreVariable')}>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.nombreVariable">Nombre Variable</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('nombreVariable')} />
                </th>
                <th className="hand" onClick={sort('direccionModbus')}>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.direccionModbus">Direccion Modbus</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('direccionModbus')} />
                </th>
                <th className="hand" onClick={sort('tipoRegistro')}>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.tipoRegistro">Tipo Registro</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('tipoRegistro')} />
                </th>
                <th className="hand" onClick={sort('tipoDato')}>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.tipoDato">Tipo Dato</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('tipoDato')} />
                </th>
                <th className="hand" onClick={sort('esEscribible')}>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.esEscribible">Es Escribible</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('esEscribible')} />
                </th>
                <th className="hand" onClick={sort('valorNumerico')}>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.valorNumerico">Valor Numerico</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('valorNumerico')} />
                </th>
                <th className="hand" onClick={sort('valorBooleano')}>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.valorBooleano">Valor Booleano</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('valorBooleano')} />
                </th>
                <th className="hand" onClick={sort('timestampActualizacion')}>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.timestampActualizacion">Timestamp Actualizacion</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('timestampActualizacion')} />
                </th>
                <th className="hand" onClick={sort('intervaloLectura')}>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.intervaloLectura">Intervalo Lectura</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('intervaloLectura')} />
                </th>
                <th className="hand" onClick={sort('umbralAlerta')}>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.umbralAlerta">Umbral Alerta</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('umbralAlerta')} />
                </th>
                <th>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.equipo">Equipo</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  <Translate contentKey="appsupervisorApp.eventoEquipo.plantilla">Plantilla</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {eventoEquipoList.map((eventoEquipo, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/evento-equipo/${eventoEquipo.id}`} color="link" size="sm">
                      {eventoEquipo.id}
                    </Button>
                  </td>
                  <td>{eventoEquipo.nombreVariable}</td>
                  <td>{eventoEquipo.direccionModbus}</td>
                  <td>
                    <Translate contentKey={`appsupervisorApp.TipoRegistro.${eventoEquipo.tipoRegistro}`} />
                  </td>
                  <td>
                    <Translate contentKey={`appsupervisorApp.TipoDato.${eventoEquipo.tipoDato}`} />
                  </td>
                  <td>{eventoEquipo.esEscribible ? 'true' : 'false'}</td>
                  <td>{eventoEquipo.valorNumerico}</td>
                  <td>{eventoEquipo.valorBooleano ? 'true' : 'false'}</td>
                  <td>
                    {eventoEquipo.timestampActualizacion ? (
                      <TextFormat type="date" value={eventoEquipo.timestampActualizacion} format={APP_DATE_FORMAT} />
                    ) : null}
                  </td>
                  <td>{eventoEquipo.intervaloLectura}</td>
                  <td>{eventoEquipo.umbralAlerta}</td>
                  <td>{eventoEquipo.equipo ? <Link to={`/equipo/${eventoEquipo.equipo.id}`}>{eventoEquipo.equipo.nombre}</Link> : ''}</td>
                  <td>
                    {eventoEquipo.plantilla ? (
                      <Link to={`/evento-plantilla/${eventoEquipo.plantilla.id}`}>{eventoEquipo.plantilla.nombre}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/evento-equipo/${eventoEquipo.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/evento-equipo/${eventoEquipo.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        onClick={() =>
                          (window.location.href = `/evento-equipo/${eventoEquipo.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="appsupervisorApp.eventoEquipo.home.notFound">No Evento Equipos found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={eventoEquipoList && eventoEquipoList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} i18nEnabled />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default EventoEquipo;
