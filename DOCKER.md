# NovaSalud en Docker

## Requisitos
Docker Desktop (o Docker Engine + Compose v2).

## Levantar todo el sistema
```bash
docker compose up -d --build
```
La primera vez MySQL ejecuta los scripts de `databaseMysql/` (tarda 1–2 minutos).

| Servicio | URL en tu navegador |
| --- | --- |
| Frontend Citas | http://localhost:8080/novasalud-citas-web |
| Frontend Farmacia | http://localhost:8081/novasalud-farmacia-web |
| Atención API | http://localhost:8082/novasalud-atencion-api/api |
| Orquestador | http://localhost:8083/novasalud-orchestrator/api |
| Citas API (Swagger) | http://localhost:8084/novasalud-citas-api/swagger/ |
| Farmacia API (Swagger) | http://localhost:8085/novasalud-farmacia-api/swagger/ |
| MySQL | localhost:3307 (root / root) |

Dentro de la red de Docker todos los servicios escuchan en el puerto 8080 y se
ubican por nombre (`citas-api`, `atencion-api`, `farmacia-api`, `orchestrator`, `mysql`).

## Comandos útiles
```bash
docker compose ps                    # estado de los contenedores
docker compose logs -f orchestrator  # ver logs de un servicio
docker compose up -d --build citas-api   # reconstruir solo un servicio
docker compose down                  # detener
docker compose down -v               # detener y borrar la BD (reejecuta los scripts)
```

## Construir un solo módulo
```bash
docker build --build-arg MODULE=novasalud-orchestrator -t novasalud-orchestrator .
```
