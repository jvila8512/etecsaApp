import React, { useEffect } from 'react';
import { useGeneradores } from 'app/shared/hooks/useGeneradores';

export const GeneradoresDashboard = () => {
  const { connected, datosTiempoReal, grupos, connectToGenerador, writeCoil } = useGeneradores();

  if (!connected) {
    return <div className="alert alert-info">🔌 Conectando al servicio de generadores...</div>;
  }

  return (
    <div className="generadores-dashboard">
      <h2>🏭 Monitor de Generadores</h2>

      {datosTiempoReal && (
        <div className="alert alert-secondary">
          <strong>
            📊 {datosTiempoReal.gruposConectados || 0}/{datosTiempoReal.totalGrupos || 0}
          </strong>{' '}
          grupos conectados
        </div>
      )}

      <div className="row">
        {Object.entries(grupos).map(([grupoId, datos]: [string, any]) => (
          <div key={grupoId} className="col-md-6 col-lg-3 mb-3">
            <div className={`card ${datos.conectado ? 'border-success' : 'border-secondary'}`}>
              <div className="card-body">
                <h5 className="card-title">{grupoId.toUpperCase()}</h5>
                <p className="card-text">
                  <small className="text-muted">{datos.ip}</small>
                  <br />
                  <strong>Estado:</strong> {datos.estado || 'DESCONOCIDO'}
                </p>

                {datos.conectado && datos.datosValidos && (
                  <div className="datos-grupo">
                    <p>
                      <strong>Voltaje:</strong> {datos.voltaje || 'N/A'}
                    </p>
                    <p>
                      <strong>Frecuencia:</strong> {datos.frecuencia || 'N/A'}
                    </p>

                    <div className="btn-group w-100">
                      <button onClick={() => writeCoil(grupoId, 0, true)} className="btn btn-success btn-sm">
                        🟢 Encender
                      </button>
                      <button onClick={() => writeCoil(grupoId, 0, false)} className="btn btn-danger btn-sm">
                        🔴 Apagar
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default GeneradoresDashboard;
