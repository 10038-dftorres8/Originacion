-- Script para agregar la columna score_interno a la tabla clientes_prospectos
-- Ejecutar en la base de datos de Originación

ALTER TABLE originacion.clientes_prospectos 
ADD COLUMN score_interno DECIMAL(10,2);

-- Comentario para la columna
COMMENT ON COLUMN originacion.clientes_prospectos.score_interno IS 'Score interno del cliente obtenido del Core Bancario'; 