FROM  eclipse-temurin:25

ENV ARTIFACT log-receiver.jar
ENV DIR /opt/7-sol

RUN mkdir -p $DIR
# Hardcode source path for Skaffold dependency tracking
COPY target/log-receiver.jar $DIR/log-receiver.jar

WORKDIR $DIR

VOLUME /tmp

EXPOSE  8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60.0", "-XX:MaxMetaspaceSize=128m", "-jar", "/opt/7-sol/log-receiver.jar"]
