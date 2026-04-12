import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import {
  Box,
  Button,
  Checkbox,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  IconButton,
  InputLabel,
  ListItemIcon,
  ListItemText,
  Menu,
  MenuItem,
  Paper,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Toolbar,
  Typography,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import RefreshIcon from '@mui/icons-material/Refresh';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import SearchIcon from '@mui/icons-material/Search';
import ViewColumnIcon from '@mui/icons-material/ViewColumn';
import WarningAmberIcon from '@mui/icons-material/WarningAmber';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import BlockIcon from '@mui/icons-material/Block';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getPaginationState, TextFormat, translate } from 'react-jhipster';

import Spinner from 'app/entities/loader/spinner';
import { getUsersAsAdmin, createUser, updateUser, deleteUser, getRoles } from './user-management.reducer';
import { IUser, defaultValue } from 'app/shared/model/user.model';

const ALL_COLUMNS = [
  { key: 'id', label: 'Id', default: true },
  { key: 'login', label: 'Usuario', default: true },
  { key: 'firstName', label: 'Nombre', default: true },
  { key: 'lastName', label: 'Apellidos', default: true },
  { key: 'email', label: 'Email', default: true },
  { key: 'activated', label: 'Activo', default: true },
  { key: 'langKey', label: 'Idioma', default: false },
  { key: 'authorities', label: 'Roles', default: true },
  { key: 'createdDate', label: 'Fecha Creación', default: false },
];

export const UserManagement = () => {
  const dispatch = useAppDispatch();
  const pageLocation = useLocation();
  const navigate = useNavigate();

  const userList = useAppSelector(state => state.userManagement.users);
  const loading = useAppSelector(state => state.userManagement.loading);
  const updating = useAppSelector(state => state.userManagement.updating);
  const updateSuccess = useAppSelector(state => state.userManagement.updateSuccess);
  const totalItems = useAppSelector(state => state.userManagement.totalItems);
  const authorities = useAppSelector(state => state.userManagement.authorities);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const [userDialog, setUserDialog] = useState(false);
  const [deleteUserDialog, setDeleteUserDialog] = useState(false);
  const [globalFilter, setGlobalFilterValue] = useState('');
  const [columnMenuAnchor, setColumnMenuAnchor] = useState<null | HTMLElement>(null);
  const [selectedUser, setSelectedUser] = useState<IUser | null>(null);
  const [formKey, setFormKey] = useState(0);

  const [formValues, setFormValues] = useState<IUser>({
    login: '',
    firstName: '',
    lastName: '',
    email: '',
    activated: true,
    langKey: 'es',
    authorities: [],
    password: '',
  });

  const [visibleColumns, setVisibleColumns] = useState<string[]>(() => {
    const saved = localStorage.getItem('user-columns');
    if (saved) return JSON.parse(saved);
    return ALL_COLUMNS.filter(c => c.default).map(c => c.key);
  });

  useEffect(() => {
    dispatch(getRoles());
  }, [dispatch]);

  const saveColumns = (cols: string[]) => {
    setVisibleColumns(cols);
    localStorage.setItem('user-columns', JSON.stringify(cols));
  };

  const toggleColumn = (key: string) => {
    if (visibleColumns.includes(key)) {
      if (visibleColumns.length > 1) saveColumns(visibleColumns.filter(c => c !== key));
    } else {
      saveColumns([...visibleColumns, key]);
    }
  };

  const getAllEntities = () => {
    dispatch(
      getUsersAsAdmin({
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

  useEffect(() => {
    if (updateSuccess) {
      setUserDialog(false);
      setSelectedUser(null);
      setFormValues({ login: '', firstName: '', lastName: '', email: '', activated: true, langKey: 'es', authorities: [], password: '' });
    }
  }, [updateSuccess]);

  const sort = (field: string) => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: field,
    });
  };

  const handlePagination = (currentPage: number) => setPaginationState({ ...paginationState, activePage: currentPage });
  const handleSyncList = () => sortEntities();

  const onGlobalFilterChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setGlobalFilterValue(e.target.value);
  };

  const filteredList = userList?.filter(
    u =>
      !globalFilter ||
      u.login?.toLowerCase().includes(globalFilter.toLowerCase()) ||
      u.firstName?.toLowerCase().includes(globalFilter.toLowerCase()) ||
      u.lastName?.toLowerCase().includes(globalFilter.toLowerCase()) ||
      u.email?.toLowerCase().includes(globalFilter.toLowerCase()),
  );

  const showColumn = (key: string) => visibleColumns.includes(key);

  const verDialogNuevo = () => {
    setSelectedUser(null);
    setFormValues({ login: '', firstName: '', lastName: '', email: '', activated: true, langKey: 'es', authorities: [], password: '' });
    setFormKey(prev => prev + 1);
    setUserDialog(true);
  };

  const actualizar = (user: IUser) => {
    setSelectedUser(user);
    setFormValues({
      login: user.login || '',
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      email: user.email || '',
      activated: user.activated ?? true,
      langKey: user.langKey || 'es',
      authorities: user.authorities || [],
      password: '',
    });
    setFormKey(prev => prev + 1);
    setUserDialog(true);
  };

  const hideDialogNuevo = () => {
    setUserDialog(false);
    setSelectedUser(null);
    setFormValues({ login: '', firstName: '', lastName: '', email: '', activated: true, langKey: 'es', authorities: [], password: '' });
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormValues(prev => ({ ...prev, [name]: value }));
  };

  const handleSelectChange = (e: any) => {
    const { name, value } = e.target;
    setFormValues(prev => ({ ...prev, [name]: value }));
  };

  const handleCheckboxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, checked } = e.target;
    setFormValues(prev => ({ ...prev, [name]: checked }));
  };

  const guardar = (values: IUser) => {
    const entity: IUser = { ...values };
    if (selectedUser?.id) {
      entity.id = selectedUser.id;
      dispatch(updateUser(entity));
    } else {
      dispatch(createUser(entity));
    }
  };

  const verEliminar = (user: IUser) => {
    setSelectedUser(user);
    setDeleteUserDialog(true);
  };

  const deleteUserFn = () => {
    if (!selectedUser?.login) return;
    dispatch(deleteUser(selectedUser.login));
    setDeleteUserDialog(false);
    setSelectedUser(null);
  };

  const hideDeleteDialog = () => {
    setDeleteUserDialog(false);
    setSelectedUser(null);
  };

  const toggleActive = (user: IUser) => {
    dispatch(updateUser({ ...user, activated: !user.activated }));
  };

  return (
    <Paper sx={{ p: 3 }}>
      <Toolbar sx={{ justifyContent: 'space-between', mb: 2, px: 0 }}>
        <Typography variant="h5" sx={{ fontWeight: 700 }}>
          Usuarios
        </Typography>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button variant="outlined" startIcon={<ViewColumnIcon />} onClick={e => setColumnMenuAnchor(e.currentTarget)}>
            Columnas
          </Button>
          <Menu anchorEl={columnMenuAnchor} open={Boolean(columnMenuAnchor)} onClose={() => setColumnMenuAnchor(null)}>
            {ALL_COLUMNS.map(col => (
              <MenuItem key={col.key} onClick={() => toggleColumn(col.key)}>
                <ListItemIcon>
                  <Checkbox checked={visibleColumns.includes(col.key)} size="small" />
                </ListItemIcon>
                <ListItemText>{col.label}</ListItemText>
              </MenuItem>
            ))}
          </Menu>
          <Button variant="outlined" startIcon={<RefreshIcon />} onClick={handleSyncList} disabled={loading}>
            Actualizar
          </Button>
          <Button variant="contained" startIcon={<PersonAddIcon />} onClick={verDialogNuevo}>
            Nuevo Usuario
          </Button>
        </Box>
      </Toolbar>

      <TextField
        size="small"
        placeholder="Buscar..."
        value={globalFilter}
        onChange={onGlobalFilterChange}
        InputProps={{
          startAdornment: <SearchIcon sx={{ color: 'text.secondary', mr: 1 }} />,
        }}
        sx={{ mb: 2, width: 300 }}
      />

      <TableContainer sx={{ overflowX: 'auto' }}>
        <Table sx={{ minWidth: 800 }}>
          <TableHead>
            <TableRow sx={{ bgcolor: '#f8fafc' }}>
              {showColumn('id') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('id')}>
                  Id
                </TableCell>
              )}
              {showColumn('login') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('login')}>
                  Usuario
                </TableCell>
              )}
              {showColumn('firstName') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('firstName')}>
                  Nombre
                </TableCell>
              )}
              {showColumn('lastName') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('lastName')}>
                  Apellidos
                </TableCell>
              )}
              {showColumn('email') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('email')}>
                  Email
                </TableCell>
              )}
              {showColumn('activated') && <TableCell sx={{ fontWeight: 600 }}>Activo</TableCell>}
              {showColumn('authorities') && <TableCell sx={{ fontWeight: 600 }}>Roles</TableCell>}
              {showColumn('createdDate') && (
                <TableCell sx={{ fontWeight: 600, cursor: 'pointer' }} onClick={() => sort('createdDate')}>
                  Fecha Creación
                </TableCell>
              )}
              <TableCell sx={{ fontWeight: 600 }}>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredList?.map((user: IUser) => (
              <TableRow key={user.id} hover>
                {showColumn('id') && <TableCell>{user.id}</TableCell>}
                {showColumn('login') && <TableCell>{user.login}</TableCell>}
                {showColumn('firstName') && <TableCell>{user.firstName}</TableCell>}
                {showColumn('lastName') && <TableCell>{user.lastName}</TableCell>}
                {showColumn('email') && <TableCell>{user.email}</TableCell>}
                {showColumn('activated') && (
                  <TableCell>
                    <IconButton size="small" onClick={() => toggleActive(user)} color={user.activated ? 'success' : 'default'}>
                      {user.activated ? <CheckCircleIcon /> : <BlockIcon />}
                    </IconButton>
                  </TableCell>
                )}
                {showColumn('authorities') && (
                  <TableCell>
                    {user.authorities?.map((auth, i) => (
                      <Typography
                        key={i}
                        variant="caption"
                        sx={{
                          bgcolor: auth === 'ROLE_ADMIN' ? '#dc2626' : auth === 'ROLE_USER' ? '#16a34a' : '#64748b',
                          color: 'white',
                          px: 1,
                          borderRadius: 1,
                          mr: 0.5,
                          display: 'inline-block',
                          fontWeight: 'bold',
                        }}
                      >
                        {auth === 'ROLE_ADMIN' ? 'ADMIN' : auth === 'ROLE_USER' ? 'USUARIO' : auth}
                      </Typography>
                    ))}
                  </TableCell>
                )}
                {showColumn('createdDate') && (
                  <TableCell>
                    {user.createdDate ? (
                      <TextFormat type="date" value={user.createdDate as unknown as string} format={APP_DATE_FORMAT} />
                    ) : null}
                  </TableCell>
                )}
                <TableCell>
                  <Box sx={{ display: 'flex', gap: 0.5 }}>
                    <IconButton size="small" color="warning" onClick={() => actualizar(user)}>
                      <EditIcon fontSize="small" />
                    </IconButton>
                    <IconButton size="small" color="error" onClick={() => verEliminar(user)}>
                      <DeleteIcon fontSize="small" />
                    </IconButton>
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {totalItems && userList && userList.length > 0 && (
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'center' }}>{/* JhiPagination would go here */}</Box>
      )}

      {/* Dialog Crear/Editar Usuario */}
      <Dialog open={userDialog} onClose={hideDialogNuevo} maxWidth="sm" fullWidth>
        <DialogTitle>{selectedUser ? 'Editar Usuario' : 'Nuevo Usuario'}</DialogTitle>
        <DialogContent>
          {loading ? (
            <Spinner />
          ) : (
            <form key={formKey}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
                <TextField
                  label="Usuario"
                  id="user-login"
                  name="login"
                  value={formValues.login}
                  onChange={handleInputChange}
                  fullWidth
                  required
                  disabled={!!selectedUser}
                  error={!formValues.login}
                  helperText={!formValues.login ? 'Requerido' : ''}
                />

                {!selectedUser && (
                  <TextField
                    label="Contraseña"
                    id="user-password"
                    name="password"
                    type="password"
                    value={formValues.password}
                    onChange={handleInputChange}
                    fullWidth
                    required={!selectedUser}
                    error={!selectedUser && !formValues.password}
                    helperText={!selectedUser && !formValues.password ? 'Requerida' : ''}
                  />
                )}

                <TextField
                  label="Nombre"
                  id="user-firstName"
                  name="firstName"
                  value={formValues.firstName}
                  onChange={handleInputChange}
                  fullWidth
                />
                <TextField
                  label="Apellidos"
                  id="user-lastName"
                  name="lastName"
                  value={formValues.lastName}
                  onChange={handleInputChange}
                  fullWidth
                />
                <TextField
                  label="Email"
                  id="user-email"
                  name="email"
                  type="email"
                  value={formValues.email}
                  onChange={handleInputChange}
                  fullWidth
                  required
                  error={!formValues.email}
                  helperText={!formValues.email ? 'Requerido' : ''}
                />
                <FormControl fullWidth>
                  <InputLabel id="user-langKey-label">Idioma</InputLabel>
                  <Select
                    labelId="user-langKey-label"
                    id="user-langKey"
                    name="langKey"
                    value={formValues.langKey}
                    label="Idioma"
                    onChange={handleSelectChange}
                  >
                    <MenuItem value="es">Español</MenuItem>
                    <MenuItem value="en">English</MenuItem>
                  </Select>
                </FormControl>
                <FormControl fullWidth>
                  <InputLabel id="user-authorities-label">Roles</InputLabel>
                  <Select
                    labelId="user-authorities-label"
                    id="user-authorities"
                    name="authorities"
                    multiple
                    value={formValues.authorities || []}
                    label="Roles"
                    onChange={handleSelectChange}
                  >
                    {authorities.map((auth: any) => (
                      <MenuItem key={auth} value={auth}>
                        {auth}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                {selectedUser && (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Checkbox
                      id="user-activated"
                      name="activated"
                      checked={formValues.activated ?? false}
                      onChange={handleCheckboxChange}
                    />
                    <Typography>Usuario activo</Typography>
                  </Box>
                )}
              </Box>
            </form>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={hideDialogNuevo}>Cancelar</Button>
          <Button
            variant="contained"
            onClick={() => guardar(formValues)}
            disabled={updating || !formValues.login || !formValues.email || (!selectedUser && !formValues.password)}
          >
            Guardar
          </Button>
        </DialogActions>
      </Dialog>

      {/* Dialog Eliminar */}
      <Dialog open={deleteUserDialog} onClose={hideDeleteDialog} maxWidth="xs" fullWidth>
        <DialogTitle>Confirmar</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 1 }}>
            <WarningAmberIcon sx={{ fontSize: 40, color: '#f59e0b' }} />
            {selectedUser && (
              <Typography>
                ¿Seguro que desea eliminar el usuario: <strong>{selectedUser.login}</strong>?
              </Typography>
            )}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={hideDeleteDialog}>No</Button>
          <Button onClick={deleteUserFn} color="error" variant="contained">
            Sí
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default UserManagement;
