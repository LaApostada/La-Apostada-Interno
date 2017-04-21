# Servicio Interno de La Apostada

Servicio Interno de aplicación [La Apostada](https://github.com/arubioVK/La-Apostada)

## Comunicación con La Apostada Web

**API REST mediante JSON**

El servicio interno se comunica con la web mediante una API REST en este servicio (mediante JSON) y Se utiliza para añadir una apuesta a la base de datos.

| Método | Endpoint | Parametro | Descripción
|---|---|---|---|
| POST | `/apuesta` | Objeto `Apuesta`| Crear apuesta
