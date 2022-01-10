FROM gradle:jdk11-alpine

RUN apk update
RUN apk add g++ make
RUN mkdir /app
WORKDIR /app
COPY app .

RUN cd ./solver; make solver_g
# RUN gradle run
CMD ["gradle", "run"]
