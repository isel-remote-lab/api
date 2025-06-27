FROM postgres

USER postgres
WORKDIR /app

COPY ./db/schema.sql /docker-entrypoint-initdb.d/1_create.sql

COPY --chown=postgres:postgres ./api/repository-jdbi/docker/scripts/wait-for-postgres.sh ./bin/wait-for-postgres.sh
RUN chmod +x ./bin/wait-for-postgres.sh

EXPOSE 5432