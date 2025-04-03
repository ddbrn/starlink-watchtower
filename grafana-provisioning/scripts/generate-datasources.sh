#!/bin/bash

if [ -z "$DB_NAME" ] || [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ]; then
  echo "Fehler: Umgebungsvariablen DB_NAME, DB_USER oder DB_PASSWORD fehlen!"
  exit 1
fi

# provisioning files
DATASOURCES_DIR="/etc/grafana/provisioning/datasources"
DATASOURCES_FILE="$DATASOURCES_DIR/datasources.yaml"

mkdir -p "$DATASOURCES_DIR"

cat <<EOF > "$DATASOURCES_FILE"
apiVersion: 1

datasources:
  - name: WatchtowerDB
    type: postgres
    url: db:5432
    database: $DB_NAME
    user: $DB_USER
    password: $DB_PASSWORD
    jsonData:
      sslmode: disable
    editable: true
EOF

echo "datasources.yaml wurde erfolgreich generiert unter $DATASOURCES_FILE"
cat "$DATASOURCES_FILE"