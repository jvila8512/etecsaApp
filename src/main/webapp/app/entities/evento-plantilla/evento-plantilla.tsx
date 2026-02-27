import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { JhiItemCount, JhiPagination, Translate, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities } from './evento-plantilla.reducer';

export const EventoPlantilla = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const eventoPlantillaList = useAppSelector(state => state.eventoPlantilla.entities);
  const loading = useAppSelector(state => state.eventoPlantilla.loading);
  const totalItems = useAppSelector(state => state.eventoPlantilla.totalItems);

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
      <h2 id="evento-plantilla-heading" data-cy="EventoPlantillaHeading">
        <Translate contentKey="appsupervisorApp.eventoPlantilla.home.title">Evento Plantillas</Translate>
        <div className="d-flex justify-content-end">
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="appsupervisorApp.eventoPlantilla.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/evento-plantilla/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="appsupervisorApp.eventoPlantilla.home.createLabel">Create new Evento Plantilla</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {eventoPlantillaList && eventoPlantillaList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="appsupervisorApp.eventoPlantilla.id">ID</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('nombre')}>
                  <Translate contentKey="appsupervisorApp.eventoPlantilla.nombre">Nombre</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('nombre')} />
                </th>
                <th className="hand" onClick={sort('descripcion')}>
                  <Translate contentKey="appsupervisorApp.eventoPlantilla.descripcion">Descripcion</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('descripcion')} />
                </th>
                <th className="hand" onClick={sort('scalingFactor')}>
                  <Translate contentKey="appsupervisorApp.eventoPlantilla.scalingFactor">Scaling Factor</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('scalingFactor')} />
                </th>
                <th className="hand" onClick={sort('unidadMedida')}>
                  <Translate contentKey="appsupervisorApp.eventoPlantilla.unidadMedida">Unidad Medida</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('unidadMedida')} />
                </th>
                <th className="hand" onClick={sort('funcionLectura')}>
                  <Translate contentKey="appsupervisorApp.eventoPlantilla.funcionLectura">Funcion Lectura</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('funcionLectura')} />
                </th>
                <th className="hand" onClick={sort('funcionEscritura')}>
                  <Translate contentKey="appsupervisorApp.eventoPlantilla.funcionEscritura">Funcion Escritura</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('funcionEscritura')} />
                </th>
                <th>
                  <Translate contentKey="appsupervisorApp.eventoPlantilla.especialidad">Especialidad</Translate>{' '}
                  <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {eventoPlantillaList.map((eventoPlantilla, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/evento-plantilla/${eventoPlantilla.id}`} color="link" size="sm">
                      {eventoPlantilla.id}
                    </Button>
                  </td>
                  <td>{eventoPlantilla.nombre}</td>
                  <td>{eventoPlantilla.descripcion}</td>
                  <td>{eventoPlantilla.scalingFactor}</td>
                  <td>{eventoPlantilla.unidadMedida}</td>
                  <td>{eventoPlantilla.funcionLectura}</td>
                  <td>{eventoPlantilla.funcionEscritura}</td>
                  <td>
                    {eventoPlantilla.especialidad ? (
                      <Link to={`/especialidad/${eventoPlantilla.especialidad.id}`}>{eventoPlantilla.especialidad.nombre}</Link>
                    ) : (
                      ''
                    )}
                  </td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/evento-plantilla/${eventoPlantilla.id}`}
                        color="info"
                        size="sm"
                        data-cy="entityDetailsButton"
                      >
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/evento-plantilla/${eventoPlantilla.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
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
                          (window.location.href = `/evento-plantilla/${eventoPlantilla.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
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
              <Translate contentKey="appsupervisorApp.eventoPlantilla.home.notFound">No Evento Plantillas found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={eventoPlantillaList && eventoPlantillaList.length > 0 ? '' : 'd-none'}>
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

export default EventoPlantilla;
