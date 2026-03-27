import './footer.scss';

import React from 'react';
import { Box, Container, Typography } from '@mui/material';

const Footer = () => (
  <Box
    component="footer"
    sx={{
      mt: 'auto',
      py: 3,
      px: 2,
      bgcolor: '#ffffff',
      borderTop: '1px solid #e2e8f0',
    }}
  >
    <Container maxWidth="lg">
      <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1.5 }}>
        <Box component="img" src="content/images/logotipo-etecsa.jpg" alt="ETECSA" sx={{ height: 80, objectFit: 'contain' }} />
        <Typography variant="caption" sx={{ color: '#64748b', textAlign: 'center' }}>
          Actualización: 23 de Febrero, 2026
        </Typography>
        <Typography variant="caption" sx={{ color: '#94a3b8', textAlign: 'center', fontStyle: 'italic' }}>
          Desarrollado por Javier Vila Labrada
        </Typography>
      </Box>
    </Container>
  </Box>
);

export default Footer;
