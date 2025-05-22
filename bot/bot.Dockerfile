FROM ubuntu:latest
LABEL authors="ilasa"

ENTRYPOINT ["top", "-b"]
