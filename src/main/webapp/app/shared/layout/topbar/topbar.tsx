import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  AppBar,
  Toolbar,
  IconButton,
  Box,
  InputBase,
  Badge,
  Avatar,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
  Typography,
  Tooltip,
  InputAdornment,
  Divider,
} from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import SearchIcon from '@mui/icons-material/Search';
import NotificationsIcon from '@mui/icons-material/Notifications';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import PersonIcon from '@mui/icons-material/Person';
import SettingsIcon from '@mui/icons-material/Settings';
import LogoutIcon from '@mui/icons-material/Logout';
import WarningIcon from '@mui/icons-material/Warning';
import InfoIcon from '@mui/icons-material/Info';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';

interface TopBarProps {
  isAuthenticated: boolean;
  onToggleSidebar: () => void;
}

const TopBar: React.FC<TopBarProps> = ({ isAuthenticated, onToggleSidebar }) => {
  const navigate = useNavigate();
  const [anchorElUser, setAnchorElUser] = useState<null | HTMLElement>(null);
  const [anchorElNotif, setAnchorElNotif] = useState<null | HTMLElement>(null);

  const handleOpenUserMenu = (event: React.MouseEvent<HTMLElement>) => setAnchorElUser(event.currentTarget);
  const handleCloseUserMenu = () => setAnchorElUser(null);
  const handleOpenNotifMenu = (event: React.MouseEvent<HTMLElement>) => setAnchorElNotif(event.currentTarget);
  const handleCloseNotifMenu = () => setAnchorElNotif(null);

  const mockNotificaciones = [
    { id: 1, tipo: 'warning', titulo: 'Alarma activa', mensaje: 'Equipo GEN-001 sin respuesta', tiempo: ' hace 5 min' },
    { id: 2, tipo: 'info', titulo: 'Actualización', mensaje: 'Firmware actualizado correctamente', tiempo: ' hace 1 hora' },
    { id: 3, tipo: 'success', titulo: 'Sistema OK', mensaje: 'Todos los equipos operativos', tiempo: ' hace 2 horas' },
  ];

  const getNotifIcon = (tipo: string) => {
    if (tipo === 'warning') return <WarningIcon sx={{ color: '#f59e0b', fontSize: 20 }} />;
    if (tipo === 'error') return <WarningIcon sx={{ color: '#dc2626', fontSize: 20 }} />;
    if (tipo === 'success') return <CheckCircleIcon sx={{ color: '#22c55e', fontSize: 20 }} />;
    return <InfoIcon sx={{ color: '#2563eb', fontSize: 20 }} />;
  };

  return (
    <AppBar
      position="fixed"
      elevation={0}
      sx={{
        zIndex: 1201,
        backgroundColor: '#ffffff',
        borderBottom: '1px solid #e2e8f0',
        color: '#0f172a',
      }}
    >
      <Toolbar>
        <IconButton edge="start" color="inherit" aria-label="menu" onClick={onToggleSidebar} sx={{ mr: 2 }}>
          <MenuIcon />
        </IconButton>

        <Box sx={{ flex: 1, maxWidth: 400, display: { xs: 'none', sm: 'block' } }}>
          <InputBase
            placeholder="Buscar..."
            startAdornment={
              <InputAdornment position="start">
                <SearchIcon sx={{ color: '#94a3b8' }} />
              </InputAdornment>
            }
            sx={{
              bgcolor: '#f1f5f9',
              borderRadius: 2,
              px: 2,
              py: 0.75,
              width: '100%',
              fontSize: '0.875rem',
              '&:focus-within': { bgcolor: '#e2e8f0' },
            }}
          />
        </Box>

        <Box sx={{ flexGrow: 1 }} />

        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
          {/* Notificaciones */}
          {isAuthenticated && (
            <>
              <Tooltip title="Notificaciones">
                <IconButton color="inherit" onClick={handleOpenNotifMenu}>
                  <Badge badgeContent={mockNotificaciones.length} color="error">
                    <NotificationsIcon />
                  </Badge>
                </IconButton>
              </Tooltip>
              <Menu
                anchorEl={anchorElNotif}
                open={Boolean(anchorElNotif)}
                onClose={handleCloseNotifMenu}
                PaperProps={{ sx: { width: 320, maxHeight: 400 } }}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                transformOrigin={{ vertical: 'top', horizontal: 'right' }}
              >
                <Box sx={{ px: 2, py: 1.5 }}>
                  <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                    Notificaciones
                  </Typography>
                </Box>
                <Divider />
                {mockNotificaciones.map(notif => (
                  <MenuItem key={notif.id} onClick={handleCloseNotifMenu} sx={{ py: 1.5 }}>
                    <ListItemIcon>{getNotifIcon(notif.tipo)}</ListItemIcon>
                    <Box>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {notif.titulo}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {notif.mensaje}
                      </Typography>
                      <Typography variant="caption" sx={{ display: 'block', color: '#94a3b8', fontSize: '0.7rem' }}>
                        {notif.tiempo}
                      </Typography>
                    </Box>
                  </MenuItem>
                ))}
                <Divider />
                <MenuItem onClick={handleCloseNotifMenu} sx={{ justifyContent: 'center' }}>
                  <Typography variant="body2" color="primary" sx={{ fontWeight: 500 }}>
                    Ver todas
                  </Typography>
                </MenuItem>
              </Menu>
            </>
          )}

          {/* Cuenta Usuario */}
          {isAuthenticated ? (
            <>
              <Tooltip title="Cuenta">
                <IconButton onClick={handleOpenUserMenu} color="inherit">
                  <Avatar sx={{ width: 36, height: 36, bgcolor: '#2563eb' }}>
                    <AccountCircleIcon />
                  </Avatar>
                </IconButton>
              </Tooltip>
              <Menu
                anchorEl={anchorElUser}
                open={Boolean(anchorElUser)}
                onClose={handleCloseUserMenu}
                PaperProps={{ sx: { width: 220 } }}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                transformOrigin={{ vertical: 'top', horizontal: 'right' }}
              >
                <Box sx={{ px: 2, py: 1.5, bgcolor: '#f8fafc' }}>
                  <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                    Usuario
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    usuario@etecsa.cu
                  </Typography>
                </Box>
                <Divider />
                <MenuItem
                  onClick={() => {
                    handleCloseUserMenu();
                    navigate('/account/settings');
                  }}
                >
                  <ListItemIcon>
                    <PersonIcon fontSize="small" />
                  </ListItemIcon>
                  <ListItemText>Mi Perfil</ListItemText>
                </MenuItem>
                <MenuItem
                  onClick={() => {
                    handleCloseUserMenu();
                    navigate('/account/password');
                  }}
                >
                  <ListItemIcon>
                    <SettingsIcon fontSize="small" />
                  </ListItemIcon>
                  <ListItemText>Cambiar Contraseña</ListItemText>
                </MenuItem>
                <Divider />
                <MenuItem
                  onClick={() => {
                    handleCloseUserMenu();
                    navigate('/logout');
                  }}
                >
                  <ListItemIcon>
                    <LogoutIcon fontSize="small" sx={{ color: '#dc2626' }} />
                  </ListItemIcon>
                  <ListItemText sx={{ color: '#dc2626' }}>Cerrar Sesión</ListItemText>
                </MenuItem>
              </Menu>
            </>
          ) : (
            <Tooltip title="Iniciar sesión">
              <IconButton color="inherit" onClick={() => navigate('/login')}>
                <AccountCircleIcon />
              </IconButton>
            </Tooltip>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default TopBar;
