FROM eclipse-temurin:17-noble
RUN apt-get update && apt-get install -y git && git config --global --add safe.directory /
