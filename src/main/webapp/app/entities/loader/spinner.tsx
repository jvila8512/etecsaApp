import React from 'react';
import { Box, CircularProgress } from '@mui/material';

const SpinnerCar = () => {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 2 }}>
      <CircularProgress size={50} sx={{ color: '#2563eb' }} />
    </Box>
  );
};

export default SpinnerCar;
