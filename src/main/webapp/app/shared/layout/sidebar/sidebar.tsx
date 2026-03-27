import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import {
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Collapse,
  Avatar,
  IconButton,
  Divider,
  Box,
  Typography,
  Tooltip,
} from '@mui/material';
import HomeIcon from '@mui/icons-material/Home';
import CategoryIcon from '@mui/icons-material/Category';
import AdminPanelSettingsIcon from '@mui/icons-material/AdminPanelSettings';
import AccountCircleIcon from '@mui/icons-material/AccountCircle';
import LogoutIcon from '@mui/icons-material/Logout';
import ExpandLess from '@mui/icons-material/ExpandLess';
import ExpandMore from '@mui/icons-material/ExpandMore';
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import TranslateIcon from '@mui/icons-material/Translate';
import BoltIcon from '@mui/icons-material/Bolt';
import NotificationsActiveIcon from '@mui/icons-material/NotificationsActive';

const DRAWER_WIDTH = 260;
const DRAWER_COLLAPSED_WIDTH = 72;

interface SidebarProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  currentLocale: string;
  onLocaleChange: (langKey: string) => void;
  open: boolean;
  onToggle: () => void;
}

const Sidebar: React.FC<SidebarProps> = ({ isAuthenticated, isAdmin, currentLocale, onLocaleChange, open, onToggle }) => {
  const [entitiesOpen, setEntitiesOpen] = useState(false);
  const [monitoreoOpen, setMonitoreoOpen] = useState(true);
  const [adminOpen, setAdminOpen] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const subItem = (path: string, label: string) => {
    const selected = location.pathname === path || location.pathname.startsWith(path + '/');
    return (
      <ListItemButton
        key={path}
        sx={{
          pl: 4,
          borderRadius: 2,
          mx: 1,
          mb: 0.5,
          '&.Mui-selected': { bgcolor: '#eff6ff', '&:hover': { bgcolor: '#dbeafe' } },
          '&:hover': { bgcolor: '#f1f5f9' },
        }}
        selected={selected}
        onClick={() => navigate(path)}
      >
        <ListItemIcon sx={{ minWidth: 40 }}>
          <Box sx={{ width: 6, height: 6, borderRadius: '50%', bgcolor: selected ? '#2563eb' : '#94a3b8' }} />
        </ListItemIcon>
        <ListItemText primary={label} primaryTypographyProps={{ fontSize: '0.8125rem', fontWeight: selected ? 600 : 400 }} />
      </ListItemButton>
    );
  };

  const navItem = (path: string, icon: React.ReactNode, label: string) => {
    const selected = location.pathname === path || location.pathname.startsWith(path + '/');
    return (
      <ListItemButton
        key={path}
        selected={selected}
        onClick={() => navigate(path)}
        sx={{
          borderRadius: 2,
          mx: 1,
          mb: 0.5,
          '&.Mui-selected': { bgcolor: '#eff6ff', '&:hover': { bgcolor: '#dbeafe' } },
          '&:hover': { bgcolor: '#f1f5f9' },
        }}
      >
        <ListItemIcon sx={{ minWidth: open ? 40 : 'auto', justifyContent: 'center' }}>{icon}</ListItemIcon>
        {open && <ListItemText primary={label} primaryTypographyProps={{ fontSize: '0.875rem', fontWeight: selected ? 600 : 400 }} />}
      </ListItemButton>
    );
  };

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: open ? DRAWER_WIDTH : DRAWER_COLLAPSED_WIDTH,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: open ? DRAWER_WIDTH : DRAWER_COLLAPSED_WIDTH,
          boxSizing: 'border-box',
          borderRight: '1px solid #e2e8f0',
          backgroundColor: '#ffffff',
          transition: 'width 0.3s ease',
          overflowX: 'hidden',
        },
      }}
    >
      {/* Brand Header */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          p: 2,
          borderBottom: '1px solid #e2e8f0',
          minHeight: 64,
        }}
      >
        {open && (
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <BoltIcon sx={{ color: '#2563eb', fontSize: 28 }} />
            <Typography variant="subtitle1" sx={{ fontWeight: 700, color: '#0f172a', lineHeight: 1.2 }}>
              ETECSA
              <br />
              <Typography component="span" variant="caption" sx={{ color: '#64748b', fontWeight: 400 }}>
                Solar Monitoring
              </Typography>
            </Typography>
          </Box>
        )}
        {!open && (
          <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%' }}>
            <BoltIcon sx={{ color: '#2563eb', fontSize: 24 }} />
          </Box>
        )}
        <IconButton onClick={onToggle} size="small" sx={{ position: open ? 'static' : 'absolute', right: 8 }}>
          <ChevronLeftIcon sx={{ transition: 'transform 0.3s', transform: open ? 'none' : 'rotate(180deg)' }} />
        </IconButton>
      </Box>

      {/* User Profile */}
      {isAuthenticated && (
        <Box
          sx={{
            background: 'linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%)',
            color: '#fff',
            p: open ? 2 : 1.5,
            display: 'flex',
            flexDirection: open ? 'row' : 'column',
            alignItems: 'center',
            gap: open ? 1.5 : 1,
          }}
        >
          <Avatar sx={{ bgcolor: 'rgba(255,255,255,0.2)', width: 40, height: 40 }}>
            <AccountCircleIcon />
          </Avatar>
          {open && (
            <Box>
              <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                Usuario
              </Typography>
              <Typography variant="caption" sx={{ opacity: 0.8 }}>
                Conectado
              </Typography>
            </Box>
          )}
        </Box>
      )}

      {/* Navigation */}
      <Box sx={{ py: 1, overflowY: 'auto', flex: 1 }}>
        {navItem('/', <HomeIcon />, 'Inicio')}

        {isAuthenticated && (
          <>
            {/* Entidades */}
            <Divider sx={{ my: 1, mx: 2 }} />
            <ListItemButton
              onClick={() => setEntitiesOpen(!entitiesOpen)}
              sx={{ borderRadius: 2, mx: 1, mb: 0.5, '&:hover': { bgcolor: '#f1f5f9' } }}
            >
              <ListItemIcon sx={{ minWidth: open ? 40 : 'auto', justifyContent: 'center' }}>
                <CategoryIcon />
              </ListItemIcon>
              {open && <ListItemText primary="Entidades" primaryTypographyProps={{ fontSize: '0.875rem' }} />}
              {open && (entitiesOpen ? <ExpandLess /> : <ExpandMore />)}
            </ListItemButton>
            <Collapse in={entitiesOpen && open} timeout="auto" unmountOnExit>
              <List component="div" disablePadding>
                {subItem('/sitio', 'Sitios')}
                {subItem('/equipo', 'Equipos')}
                {subItem('/evento-plantilla', 'Eventos Plantilla')}
                {subItem('/evento-equipo', 'Eventos Equipo')}
                {subItem('/especialidad', 'Especialidades')}
                {subItem('/generadores', 'Generadores')}
              </List>
            </Collapse>

            {/* Monitoreo */}
            <Divider sx={{ my: 1, mx: 2 }} />
            <ListItemButton
              onClick={() => setMonitoreoOpen(!monitoreoOpen)}
              sx={{ borderRadius: 2, mx: 1, mb: 0.5, '&:hover': { bgcolor: '#f1f5f9' } }}
            >
              <ListItemIcon sx={{ minWidth: open ? 40 : 'auto', justifyContent: 'center' }}>
                <NotificationsActiveIcon />
              </ListItemIcon>
              {open && <ListItemText primary="Monitoreo" primaryTypographyProps={{ fontSize: '0.875rem' }} />}
              {open && (monitoreoOpen ? <ExpandLess /> : <ExpandMore />)}
            </ListItemButton>
            <Collapse in={monitoreoOpen && open} timeout="auto" unmountOnExit>
              <List component="div" disablePadding>
                {subItem('/alarma/monitor', 'Monitor Alarmas')}
                {subItem('/alarma', 'Administrar Alarmas')}
              </List>
            </Collapse>

            {/* Admin */}
            {isAdmin && (
              <>
                <ListItemButton
                  onClick={() => setAdminOpen(!adminOpen)}
                  sx={{ borderRadius: 2, mx: 1, mb: 0.5, '&:hover': { bgcolor: '#f1f5f9' } }}
                >
                  <ListItemIcon sx={{ minWidth: open ? 40 : 'auto', justifyContent: 'center' }}>
                    <AdminPanelSettingsIcon />
                  </ListItemIcon>
                  {open && <ListItemText primary="Admin" primaryTypographyProps={{ fontSize: '0.875rem' }} />}
                  {open && (adminOpen ? <ExpandLess /> : <ExpandMore />)}
                </ListItemButton>
                <Collapse in={adminOpen && open} timeout="auto" unmountOnExit>
                  <List component="div" disablePadding>
                    {subItem('/admin/user-management', 'Usuarios')}
                    {subItem('/admin/metrics', 'Métricas')}
                    {subItem('/admin/health', 'Health Checks')}
                    {subItem('/admin/configuration', 'Configuración')}
                    {subItem('/admin/logs', 'Logs')}
                    {subItem('/admin/tracker', 'Tracker')}
                    {subItem('/admin/docs', 'API Docs')}
                  </List>
                </Collapse>
              </>
            )}
          </>
        )}

        <Divider sx={{ my: 1, mx: 2 }} />
        {navItem('/account/settings', <AccountCircleIcon />, 'Mi Cuenta')}

        {isAuthenticated && (
          <ListItemButton onClick={() => navigate('/logout')} sx={{ borderRadius: 2, mx: 1, mb: 0.5, '&:hover': { bgcolor: '#fef2f2' } }}>
            <ListItemIcon sx={{ minWidth: open ? 40 : 'auto', justifyContent: 'center' }}>
              <LogoutIcon sx={{ color: '#dc2626' }} />
            </ListItemIcon>
            {open && <ListItemText primary="Cerrar Sesión" primaryTypographyProps={{ fontSize: '0.875rem', color: '#dc2626' }} />}
          </ListItemButton>
        )}
      </Box>

      {/* Language Toggle */}
      <Box sx={{ p: 1, borderTop: '1px solid #e2e8f0' }}>
        <ListItemButton
          onClick={() => onLocaleChange(currentLocale === 'es' ? 'en' : 'es')}
          sx={{ borderRadius: 2, '&:hover': { bgcolor: '#f1f5f9' } }}
        >
          <ListItemIcon sx={{ minWidth: open ? 40 : 'auto', justifyContent: 'center' }}>
            <TranslateIcon />
          </ListItemIcon>
          {open && (
            <ListItemText primary={currentLocale === 'es' ? 'Español' : 'English'} primaryTypographyProps={{ fontSize: '0.875rem' }} />
          )}
        </ListItemButton>
      </Box>
    </Drawer>
  );
};

export default Sidebar;
