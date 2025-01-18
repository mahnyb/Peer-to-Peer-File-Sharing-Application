FROM openjdk:11-jdk-slim

WORKDIR /Project
COPY ./src/*.java /Project/
COPY ./SharedFiles /SharedFiles
COPY ./DownloadedFiles /DownloadedFiles
RUN javac Main.java OverlayNetwork.java FileChunk.java FileRequestHandler.java
CMD ["java", "Main"]
