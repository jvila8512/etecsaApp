import 'react-toastify/dist/ReactToastify.css';
import 'app/config/dayjs';

import React, { useEffect, useState } from 'react';
import { BrowserRouter, Routes, Route, useLocation, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import { ThemeProvider } from '@mui/material/styles';
import { Box, CssBaseline } from '@mui/material';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getSession } from 'app/shared/reducers/authentication';
import { getProfile } from 'app/shared/reducers/application-profile';
import Header from 'app/shared/layout/header/header';
import Footer from 'app/shared/layout/footer/footer';
import Sidebar from 'app/shared/layout/sidebar/sidebar';
import TopBar from 'app/shared/layout/topbar/topbar';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import ErrorBoundary from 'app/shared/error/error-boundary';
import { AUTHORITIES } from 'app/config/constants';
import AppRoutes from 'app/routes';
import theme from 'app/shared/layout/theme/mui-theme';
import { setLocale } from 'app/shared/reducers/locale';
import { Storage } from 'react-jhipster';
import Login from 'app/modules/login/login';
import Home from 'app/modules/home/home';
import GeneradoresDashboard from 'app/modules/generadores/GeneradoresDashboard';
import Dashboard from 'app/modules/generadores';

const baseHref = document.querySelector('base').getAttribute('href').replace(/\/$/, '');

// Componente interno que tiene acceso a useLocation
const AppContent = () => {
  const dispatch = useAppDispatch();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(true);

  // Cerrar sidebar cuando esté en monitor de alarmas
  useEffect(() => {
    if (location.pathname === '/alarma/monitor') {
      setSidebarOpen(false);
    } else {
      setSidebarOpen(true);
    }
  }, [location.pathname]);

  useEffect(() => {
    dispatch(getSession());
    dispatch(getProfile());
  }, [dispatch]);

  const currentLocale = useAppSelector(state => state.locale.currentLocale);
  const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
  const account = useAppSelector(state => state.authentication.account);
  const isAdmin = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [AUTHORITIES.ADMIN]));
  const ribbonEnv = useAppSelector(state => state.applicationProfile.ribbonEnv);
  const isInProduction = useAppSelector(state => state.applicationProfile.inProduction);
  const isOpenAPIEnabled = useAppSelector(state => state.applicationProfile.isOpenAPIEnabled);

  const handleLocaleChange = (langKey: string) => {
    Storage.session.set('locale', langKey);
    dispatch(setLocale(langKey));
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: 'flex', minHeight: '100vh' }}>
        <ToastContainer position="top-left" className="toastify-container" toastClassName="toastify-toast" />

        {isAuthenticated && (
          <Sidebar
            isAuthenticated={isAuthenticated}
            isAdmin={isAdmin}
            currentLocale={currentLocale}
            onLocaleChange={handleLocaleChange}
            open={sidebarOpen}
            onToggle={() => setSidebarOpen(!sidebarOpen)}
          />
        )}

        <Box
          component="main"
          sx={{
            flexGrow: 1,
            minHeight: '100vh',
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: '#f8fafc',
            transition: 'margin-left 0.3s ease',
          }}
        >
          <TopBar isAuthenticated={isAuthenticated} onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} account={account} />

          <Box sx={{ height: 64 }} />

          <Box sx={{ p: 3, flex: 1, display: 'flex', flexDirection: 'column' }}>
            <Box sx={{ flex: 1 }}>
              <ErrorBoundary>
                <AppRoutes />
              </ErrorBoundary>
            </Box>
            <Footer />
          </Box>
        </Box>
      </Box>

      <Header
        isAuthenticated={isAuthenticated}
        isAdmin={isAdmin}
        currentLocale={currentLocale}
        ribbonEnv={ribbonEnv}
        isInProduction={isInProduction}
        isOpenAPIEnabled={isOpenAPIEnabled}
      />
    </ThemeProvider>
  );
};

// Componente principal con BrowserRouter
const App = () => {
  return (
    <BrowserRouter basename={baseHref}>
      <AppContent />
    </BrowserRouter>
  );
};

export default App;
