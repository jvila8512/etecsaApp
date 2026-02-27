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

import { getEntities } from './alarma.reducer';

export const Alarma = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const alarmaList = useAppSelector(state => state.alarma.entities);
  const loading = useAppSelector(state => state.alarma.loading);
  const totalItems = useAppSelector(state => state.alarma.totalItems);

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
      <h2 id="alarma-heading" data-cy="AlarmaHeading">
        <Translate contentKey="appsupervisorApp.alarma.home.title">Alarmas</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="appsupervisorApp.alarma.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/alarma/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="appsupervisorApp.alarma.home.createLabel">Create new Alarma</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {alarmaList && alarmaList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="appsupervisorApp.alarma.id">ID</Translate> <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('descripcion')}>
                  <Translate contentKey="appsupervisorApp.alarma.descripcion">Descripcion</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('descripcion')} />
                </th>
                <th className="hand" onClick={sort('activatedAt')}>
                  <Translate contentKey="appsupervisorApp.alarma.activatedAt">Activated At</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('activatedAt')} />
                </th>
                <th className="hand" onClick={sort('deactivatedAt')}>
                  <Translate contentKey="appsupervisorApp.alarma.deactivatedAt">Deactivated At</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('deactivatedAt')} />
                </th>
                <th className="hand" onClick={sort('severidad')}>
                  <Translate contentKey="appsupervisorApp.alarma.severidad">Severidad</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('severidad')} />
                </th>
                <th className="hand" onClick={sort('estado')}>
                  <Translate contentKey="appsupervisorApp.alarma.estado">Estado</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('estado')} />
                </th>
                <th className="hand" onClick={sort('mensajeUsuario')}>
                  <Translate contentKey="appsupervisorApp.alarma.mensajeUsuario">Mensaje Usuario</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('mensajeUsuario')} />
                </th>
                <th>
                  <Translate contentKey="appsupervisorApp.alarma.evento">Evento</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  <Translate contentKey="appsupervisorApp.alarma.acknowledgedBy">Acknowledged By</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {alarmaList.map((alarma, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/alarma/${alarma.id}`} color="link" size="sm">
                      {alarma.id}
                    </Button>
                  </td>
                  <td>{alarma.descripcion}</td>
                  <td>{alarma.activatedAt ? <TextFormat type="date" value={alarma.activatedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{alarma.deactivatedAt ? <TextFormat type="date" value={alarma.deactivatedAt} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>
                    <Translate contentKey={`appsupervisorApp.Severidad.${alarma.severidad}`} />
                  </td>
                  <td>
                    <Translate contentKey={`appsupervisorApp.EstadoAlarma.${alarma.estado}`} />
                  </td>
                  <td>{alarma.mensajeUsuario}</td>
                  <td>{alarma.evento ? <Link to={`/evento-equipo/${alarma.evento.id}`}>{alarma.evento.id}</Link> : ''}</td>
                  <td>{alarma.acknowledgedBy ? alarma.acknowledgedBy.login : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/alarma/${alarma.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/alarma/${alarma.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                          (window.location.href = `/alarma/${alarma.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
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
              <Translate contentKey="appsupervisorApp.alarma.home.notFound">No Alarmas found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={alarmaList && alarmaList.length > 0 ? '' : 'd-none'}>
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

export default Alarma;
