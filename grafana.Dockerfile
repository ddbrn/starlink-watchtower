FROM grafana/grafana:latest

COPY grafana-provisioning/scripts/generate-datasources.sh /scripts/generate-datasources.sh

RUN chmod +x /scripts/generate-datasources.sh