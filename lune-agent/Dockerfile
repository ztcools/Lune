FROM eclipse-temurin:17-jre-alpine AS runtime

RUN apk add --no-cache curl && \
    addgroup -g 1000 lune && adduser -u 1000 -G lune -s /bin/sh -D lune

WORKDIR /app
COPY target/lune-agent.jar /app/lune-agent.jar
RUN chown -R lune:lune /app

USER lune:lune

ENV JAVA_OPTS="-Xms128m -Xmx256m \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=50.0 \
  -XX:+UseG1GC"

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=30s \
    CMD curl -f http://localhost:8082/api/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/lune-agent.jar"]
