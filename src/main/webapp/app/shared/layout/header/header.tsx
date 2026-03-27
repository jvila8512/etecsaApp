import React from 'react';

export interface IHeaderProps {
  isAuthenticated: boolean;
  isAdmin: boolean;
  ribbonEnv: string;
  isInProduction: boolean;
  isOpenAPIEnabled: boolean;
  currentLocale: string;
}

/**
 * Header legacy deshabilitado.
 * Reemplazado por Sidebar + TopBar del nuevo layout MUI.
 */
const Header = (_props: IHeaderProps) => {
  return null;
};

export default Header;
