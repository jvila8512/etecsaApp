import React from 'react';

import { ProgressSpinner } from 'primereact/progressspinner';

const SpinnerCar = () => {
  return <ProgressSpinner className="flex " style={{ width: '50px', height: '50px' }} animationDuration=".5s" />;
};

export default SpinnerCar;
