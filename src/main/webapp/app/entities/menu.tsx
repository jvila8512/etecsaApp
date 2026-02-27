import React from 'react';
import { Translate } from 'react-jhipster';

import MenuItem from 'app/shared/layout/menus/menu-item';

const EntitiesMenu = () => {
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/sitio">
        <Translate contentKey="global.menu.entities.sitio" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/equipo">
        <Translate contentKey="global.menu.entities.equipo" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/especialidad">
        <Translate contentKey="global.menu.entities.especialidad" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/evento-equipo">
        <Translate contentKey="global.menu.entities.eventoEquipo" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/evento-plantilla">
        <Translate contentKey="global.menu.entities.eventoPlantilla" />
      </MenuItem>
      <MenuItem icon="asterisk" to="/alarma">
        <Translate contentKey="global.menu.entities.alarma" />
      </MenuItem>
      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
