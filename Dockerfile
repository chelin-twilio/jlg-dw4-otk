FROM registry.twilio.com/library/java/jre-21-corretto-debian-slim:latest

WORKDIR /service
EXPOSE 9876 8765
ENTRYPOINT ["/service/bin/docker-entrypoint.sh"]

COPY --from=registry.twilio.com/twilio/distroless-utils:1.0.1 /bin/healthcheck /bin/healthcheck
HEALTHCHECK --interval=10s --timeout=3s \
  CMD [ "/bin/healthcheck", "--url", "http://localhost:8765/healthcheck" ]

USER twilio

COPY jlg-dw4-otk-server/scripts /service/bin/
COPY conf/service.yaml /service/conf/
COPY jlg-dw4-otk-server/target/jlg-dw4-otk-server.jar /service/bin/
