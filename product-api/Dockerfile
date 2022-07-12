FROM gradle:7.0.0-jdk11
COPY --chown=gradle:gradle . .
WORKDIR .
RUN /bin/sh -c gradle build -x test --no-daemon
#/bin/sh -c
EXPOSE 8081
CMD ["gradle", "bootRun"]
