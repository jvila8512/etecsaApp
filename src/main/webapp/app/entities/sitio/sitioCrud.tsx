import React, { useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import {
  JhiItemCount,
  JhiPagination,
  TextFormat,
  Translate,
  getPaginationState,
  translate,
  ValidatedField,
  ValidatedForm,
} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Row } from 'reactstrap';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { IconField } from 'primereact/iconfield';
import { InputIcon } from 'primereact/inputicon';
import { Dialog } from 'primereact/dialog';
import { DataTable, DataTableFilterMeta } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Toolbar } from 'primereact/toolbar';
import { FilterMatchMode, FilterOperator } from 'primereact/api';

import Spinner from '../loader/spinner';
import { getEntities, createEntity, updateEntity, deleteEntity, reset } from './sitio.reducer';
import { ISitio } from 'app/shared/model/sitio.model';

export const Sitio = () => {
  const dispatch = useAppDispatch();
  const pageLocation = useLocation();
  const navigate = useNavigate();

  // ─── Redux state ──────────────────────────────────────────────────────────────

  const sitioList = useAppSelector(state => state.sitio.entities);
  const loading = useAppSelector(state => state.sitio.loading);
  const updating = useAppSelector(state => state.sitio.updating);
  const updateSuccess = useAppSelector(state => state.sitio.updateSuccess);
  const totalItems = useAppSelector(state => state.sitio.totalItems);

  // ─── Pagination state ─────────────────────────────────────────────────────────

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  // ─── Local state ──────────────────────────────────────────────────────────────

  const dt = useRef(null);
  const [isNew, setIsNew] = useState(true);
  const [sitio, setSitio] = useState<ISitio | null>(null);
  const [selectedSitio, setSelectedSitio] = useState<ISitio | null>(null);
  const [sitioDialog, setSitioDialog] = useState(false);
  const [deleteSitioDialog, setDeleteSitioDialog] = useState(false);
  const [globalFilter, setGlobalFilterValue] = useState('');

  const [filters, setFilters] = useState<DataTableFilterMeta>({
    global: { value: null, matchMode: FilterMatchMode.CONTAINS },
    nombre: { operator: FilterOperator.AND, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
    codigo: { operator: FilterOperator.AND, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
    ubicacion: { operator: FilterOperator.AND, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
  });

  // ─── Effects ──────────────────────────────────────────────────────────────────

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

  // Cerrar dialog al guardar exitosamente — igual que el ejemplo
  useEffect(() => {
    if (updateSuccess && isNew) {
      setSitioDialog(false);
      setSelectedSitio(null);
    }
    if (updateSuccess && !isNew) {
      setSitioDialog(false);
    }
  }, [updateSuccess]);

  // ─── Sorting ──────────────────────────────────────────────────────────────────

  const sort = (field: string) => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: field,
    });
  };

  // ─── Pagination ───────────────────────────────────────────────────────────────

  const handlePagination = (currentPage: number) => setPaginationState({ ...paginationState, activePage: currentPage });

  const handleSyncList = () => sortEntities();

  // ─── Global filter ────────────────────────────────────────────────────────────

  const onGlobalFilterChange = e => {
    const value = e.target.value;
    const _filters = { ...filters };
    (_filters as any)['global'].value = value;
    setFilters(_filters);
    setGlobalFilterValue(value);
  };

  // ─── CRUD ─────────────────────────────────────────────────────────────────────

  const verDialogNuevo = () => {
    setSitio(null);
    setIsNew(true);
    setSitioDialog(true);
  };

  const actualizar = (rowData: ISitio) => {
    setSitio({ ...rowData });
    setIsNew(false);
    setSitioDialog(true);
  };

  const hideDialogNuevo = () => {
    setSitioDialog(false);
  };

  const saveEntity = values => {
    const entity: ISitio = {
      ...values,
      fechaRegistro: values.fechaRegistro ? dayjs(values.fechaRegistro) : undefined,
    };
    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...sitio,
          fechaRegistro: sitio?.fechaRegistro ? dayjs(sitio.fechaRegistro).format('YYYY-MM-DDTHH:mm') : '',
        };

  const verEliminar = (rowData: ISitio) => {
    setSelectedSitio(rowData);
    setDeleteSitioDialog(true);
  };

  const deleteSitio = () => {
    if (!selectedSitio?.id) return;
    dispatch(deleteEntity(selectedSitio.id));
    setDeleteSitioDialog(false);
    setSelectedSitio(null);
  };

  const hideDeleteDialog = () => {
    setDeleteSitioDialog(false);
    setSelectedSitio(null);
  };

  // ─── Toolbar ──────────────────────────────────────────────────────────────────

  const leftToolbarTemplate = () => (
    <React.Fragment>
      <div className="my-2">
        <Button label="Actualizar" icon="pi pi-refresh" className="p-button-secondary mr-2" onClick={handleSyncList} disabled={loading} />
      </div>
    </React.Fragment>
  );

  const rightToolbarTemplate = () => (
    <React.Fragment>
      {/* Azul = p-button-info, igual que el ejemplo */}
      <Button label="Nuevo Sitio" icon="pi pi-plus" className="p-button-info" onClick={verDialogNuevo} />
    </React.Fragment>
  );

  // ─── Table header — buscar a la derecha ───────────────────────────────────────

  const header = (
    <div className="d-flex flex-wrap gap-2 align-items-center justify-content-between">
      <h4 className="m-0">Sitios</h4>
      <IconField iconPosition="left">
        <InputIcon className="pi pi-search" />
        <InputText type="search" onInput={onGlobalFilterChange} placeholder="Buscar..." />
      </IconField>
    </div>
  );

  const actionBodyTemplate = (rowData: ISitio) => (
    <>
      <Button icon="pi pi-trash" rounded className="p-button-danger ml-2 mb-1" onClick={() => verEliminar(rowData)} />
      <Button icon="pi pi-pencil" className="p-button-rounded p-button-warning ml-2 mb-1" onClick={() => actualizar(rowData)} />
      <Button icon="pi pi-times" rounded text raised severity="danger" aria-label="Cancel" />
    </>
  );

  // ─── Fecha column ─────────────────────────────────────────────────────────────

  const fechaBodyTemplate = (rowData: ISitio) =>
    rowData.fechaRegistro ? <TextFormat type="date" value={rowData.fechaRegistro as unknown as string} format={APP_DATE_FORMAT} /> : null;

  // ─── Delete footer ────────────────────────────────────────────────────────────

  const deleteDialogFooter = (
    <>
      <Button label="No" icon="pi pi-times" className="p-button-text" onClick={hideDeleteDialog} />
      <Button label="Sí" icon="pi pi-check" className="p-button-text" onClick={deleteSitio} />
    </>
  );

  // ─── Render ───────────────────────────────────────────────────────────────────

  return (
    <div className="grid crud-demo mt-3 mb-4">
      <div className="col-12">
        <div className="card">
          <Toolbar className="mb-4" left={leftToolbarTemplate} right={rightToolbarTemplate} />

          <DataTable
            ref={dt}
            value={sitioList}
            selection={selectedSitio}
            onSelectionChange={e => setSelectedSitio(e.value as ISitio)}
            dataKey="id"
            paginator={false}
            className="datatable-responsive"
            globalFilter={globalFilter}
            loading={loading}
            emptyMessage="No hay Sitios..."
            header={header}
            onSort={e => sort(e.sortField)}
            sortField={paginationState.sort}
            sortOrder={paginationState.order === ASC ? 1 : -1}
            showGridlines
          >
            <Column field="id" header="Id" hidden />
            <Column field="nombre" header="Nombre" sortable headerStyle={{ minWidth: '15rem' }} />
            <Column field="codigo" header="Código" sortable headerStyle={{ minWidth: '10rem' }} />
            <Column field="ubicacion" header="Ubicación" sortable headerStyle={{ minWidth: '15rem' }} />
            <Column field="fechaRegistro" header="Fecha Registro" body={fechaBodyTemplate} sortable headerStyle={{ minWidth: '12rem' }} />
            <Column body={actionBodyTemplate} headerStyle={{ minWidth: '8rem' }} />
          </DataTable>

          {/* ── Paginador server-side (JHipster) ── */}
          {totalItems && sitioList && sitioList.length > 0 ? (
            <div className="mt-3">
              <div className="justify-content-center d-flex">
                <JhiItemCount
                  page={paginationState.activePage}
                  total={totalItems}
                  itemsPerPage={paginationState.itemsPerPage}
                  i18nEnabled
                />
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
          ) : null}

          {/* ── Dialog Crear / Editar — igual al patrón del ejemplo ── */}
          <Dialog
            visible={sitioDialog}
            style={{ width: '450px' }}
            header={isNew ? 'Nuevo Sitio' : 'Editar Sitio'}
            modal
            className="p-fluid"
            onHide={hideDialogNuevo}
          >
            <Row className="justify-content-center">
              {loading ? (
                <Spinner />
              ) : (
                <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
                  {!isNew && (
                    <ValidatedField
                      name="id"
                      required
                      readOnly
                      hidden
                      id="sitio-id"
                      label={translate('global.field.id')}
                      validate={{ required: true }}
                    />
                  )}
                  <ValidatedField
                    label={translate('appsupervisorApp.sitio.nombre')}
                    id="sitio-nombre"
                    name="nombre"
                    data-cy="nombre"
                    type="text"
                    validate={{
                      required: { value: true, message: translate('entity.validation.required') },
                    }}
                  />
                  <ValidatedField
                    label={translate('appsupervisorApp.sitio.codigo')}
                    id="sitio-codigo"
                    name="codigo"
                    data-cy="codigo"
                    type="text"
                    validate={{
                      required: { value: true, message: translate('entity.validation.required') },
                    }}
                  />
                  <ValidatedField
                    label={translate('appsupervisorApp.sitio.ubicacion')}
                    id="sitio-ubicacion"
                    name="ubicacion"
                    data-cy="ubicacion"
                    type="text"
                  />
                  <ValidatedField
                    label={translate('appsupervisorApp.sitio.fechaRegistro')}
                    id="sitio-fechaRegistro"
                    name="fechaRegistro"
                    data-cy="fechaRegistro"
                    type="datetime-local"
                    placeholder="YYYY-MM-DD HH:mm"
                  />
                  &nbsp;
                  <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                    <span className="m-auto">
                      <FontAwesomeIcon icon="save" />
                      &nbsp;
                      <Translate contentKey="entity.action.save">Guardar</Translate>
                    </span>
                  </Button>
                </ValidatedForm>
              )}
            </Row>
          </Dialog>

          {/* ── Dialog Confirmar Eliminación ── */}
          <Dialog
            visible={deleteSitioDialog}
            style={{ width: '450px' }}
            header="Confirmar"
            modal
            footer={deleteDialogFooter}
            onHide={hideDeleteDialog}
          >
            <div className="flex align-items-center justify-content-center">
              <i className="pi pi-exclamation-triangle mr-3" style={{ fontSize: '2rem' }} />
              {selectedSitio && (
                <span>
                  ¿Seguro que quiere eliminar el Sitio: <b>{selectedSitio.nombre}</b>?
                </span>
              )}
            </div>
          </Dialog>
        </div>
      </div>
    </div>
  );
};

export default Sitio;
