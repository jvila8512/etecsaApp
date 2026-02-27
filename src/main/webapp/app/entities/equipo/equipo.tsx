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

import { getEntities } from './equipo.reducer';

export const Equipo = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const equipoList = useAppSelector(state => state.equipo.entities);
  const loading = useAppSelector(state => state.equipo.loading);
  const totalItems = useAppSelector(state => state.equipo.totalItems);

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
      <h2 id="equipo-heading" data-cy="EquipoHeading">
        <Translate contentKey="appsupervisorApp.equipo.home.title">Equipos</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="appsupervisorApp.equipo.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/equipo/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="appsupervisorApp.equipo.home.createLabel">Create new Equipo</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {equipoList && equipoList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="appsupervisorApp.equipo.id">ID</Translate> <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('nombre')}>
                  <Translate contentKey="appsupervisorApp.equipo.nombre">Nombre</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('nombre')} />
                </th>
                <th className="hand" onClick={sort('direccionIp')}>
                  <Translate contentKey="appsupervisorApp.equipo.direccionIp">Direccion Ip</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('direccionIp')} />
                </th>
                <th className="hand" onClick={sort('modbusSlaveId')}>
                  <Translate contentKey="appsupervisorApp.equipo.modbusSlaveId">Modbus Slave Id</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('modbusSlaveId')} />
                </th>
                <th className="hand" onClick={sort('modelo')}>
                  <Translate contentKey="appsupervisorApp.equipo.modelo">Modelo</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('modelo')} />
                </th>
                <th className="hand" onClick={sort('firmwareVersion')}>
                  <Translate contentKey="appsupervisorApp.equipo.firmwareVersion">Firmware Version</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('firmwareVersion')} />
                </th>
                <th className="hand" onClick={sort('estado')}>
                  <Translate contentKey="appsupervisorApp.equipo.estado">Estado</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('estado')} />
                </th>
                <th className="hand" onClick={sort('ultimoHeartbeat')}>
                  <Translate contentKey="appsupervisorApp.equipo.ultimoHeartbeat">Ultimo Heartbeat</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('ultimoHeartbeat')} />
                </th>
                <th>
                  <Translate contentKey="appsupervisorApp.equipo.sitio">Sitio</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {equipoList.map((equipo, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/equipo/${equipo.id}`} color="link" size="sm">
                      {equipo.id}
                    </Button>
                  </td>
                  <td>{equipo.nombre}</td>
                  <td>{equipo.direccionIp}</td>
                  <td>{equipo.modbusSlaveId}</td>
                  <td>{equipo.modelo}</td>
                  <td>{equipo.firmwareVersion}</td>
                  <td>
                    <Translate contentKey={`appsupervisorApp.EstadoEquipo.${equipo.estado}`} />
                  </td>
                  <td>
                    {equipo.ultimoHeartbeat ? <TextFormat type="date" value={equipo.ultimoHeartbeat} format={APP_DATE_FORMAT} /> : null}
                  </td>
                  <td>{equipo.sitio ? <Link to={`/sitio/${equipo.sitio.id}`}>{equipo.sitio.nombre}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/equipo/${equipo.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/equipo/${equipo.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                          (window.location.href = `/equipo/${equipo.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
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
              <Translate contentKey="appsupervisorApp.equipo.home.notFound">No Equipos found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={equipoList && equipoList.length > 0 ? '' : 'd-none'}>
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

export default Equipo;
