FROM postgres

USER postgres
WORKDIR /app

COPY /src/sql/create-schema.sql /docker-entrypoint-initdb.d/1_create.sql
COPY /src/sql/insert-test-data.sql /docker-entrypoint-initdb.d/2_insert.sql

COPY --chown=postgres:postgres ./src/docker/scripts/wait-for-postgres.sh ./bin/wait-for-postgres.sh
RUN chmod +x ./bin/wait-for-postgres.sh

EXPOSE 5432