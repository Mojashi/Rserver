FROM gradle:jdk11-alpine

RUN apk update
RUN apk add g++
RUN mkdir /app
WORKDIR /app
COPY app .

RUN cd ./solver; g++ solver_g.cpp -O3 -o solver_g
# RUN gradle run
CMD ["gradle", "run"]
