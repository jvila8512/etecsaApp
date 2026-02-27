import sitio from 'app/entities/sitio/sitio.reducer';
import equipo from 'app/entities/equipo/equipo.reducer';
import especialidad from 'app/entities/especialidad/especialidad.reducer';
import eventoEquipo from 'app/entities/evento-equipo/evento-equipo.reducer';
import eventoPlantilla from 'app/entities/evento-plantilla/evento-plantilla.reducer';
import alarma from 'app/entities/alarma/alarma.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  sitio,
  equipo,
  especialidad,
  eventoEquipo,
  eventoPlantilla,
  alarma,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
