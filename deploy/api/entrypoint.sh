#!/bin/sh
set -eu

# API y worker comparten imagen. --worker desactiva HTTP en MarketplaceApplication.
for argument in "$@"; do
    if [ "$argument" = '--worker' ]; then
        echo 'Activa SPRING_PROFILES_ACTIVE=docker,worker para conservar la sonda HTTP.' >&2
        exit 2
    fi
done

: "${SPRING_DATASOURCE_URL:?Configura SPRING_DATASOURCE_URL en .env.docker}"
: "${SPRING_DATASOURCE_USERNAME:?Configura SPRING_DATASOURCE_USERNAME en .env.docker}"
: "${SPRING_DATASOURCE_PASSWORD?Configura SPRING_DATASOURCE_PASSWORD en .env.docker}"
: "${JWT_SECRET:?Configura JWT_SECRET en .env.docker}"
: "${DOCUMENTO_CLAVE_CIFRADO:?Conserva DOCUMENTO_CLAVE_CIFRADO de tu configuracion local}"
: "${DOCUMENTO_CLAVE_HMAC:?Conserva DOCUMENTO_CLAVE_HMAC de tu configuracion local}"

# exec permite que Java reciba SIGTERM y complete su cierre ordenado.
exec java -jar /app/marketplace.war "$@"
