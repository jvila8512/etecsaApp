import './footer.scss';

import React from 'react';
import { Box, Container, Typography } from '@mui/material';

const Footer = () => (
  <Box
    component="footer"
    sx={{
      mt: 'auto',
      py: 2,
      px: 2,
      bgcolor: '#ffffff',
      borderTop: '1px solid #e2e8f0',
    }}
  >
    <Container maxWidth="lg">
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 3 }}>
        {/* Columna 1: Logo más pequeño */}
        <Box component="img" src="content/images/logotipo-etecsa.jpg" alt="ETECSA" sx={{ height: 35, objectFit: 'contain' }} />

        {/* Columna 2: Textos centrados y más pequeños */}
        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="caption" sx={{ color: '#64748b', display: 'block', fontSize: '0.7rem' }}>
            Actualización: 23 de Febrero, 2026
          </Typography>
          <Typography variant="caption" sx={{ color: '#94a3b8', display: 'block', fontSize: '0.65rem', fontStyle: 'italic' }}>
            Desarrollado por Javier Vila Labrada
          </Typography>
        </Box>
      </Box>
    </Container>
  </Box>
);

export default Footer;
