# A5: imagen de copia de seguridad independiente del backend.
# Usa las mismas herramientas de PostgreSQL 18.6 que el servidor del Compose.
FROM postgres:18.6
COPY backup-postgres.sh /usr/local/bin/backup-postgres.sh
# El archivo puede estar en CRLF si se extrajo mediante Windows/Git.
RUN sed -i 's/\r$//' /usr/local/bin/backup-postgres.sh \
    && chmod 0755 /usr/local/bin/backup-postgres.sh
USER postgres
ENV TZ=America/Bogota
ENTRYPOINT ["/bin/sh", "/usr/local/bin/backup-postgres.sh"]
CMD ["schedule"]
