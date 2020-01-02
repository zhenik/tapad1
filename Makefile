MAVEN := './mvnw'

.PHONY: all
all: build run-infrastructure wait create-topics run-application

.PHONY: build
build: build-src build-docker-images
.PHONY: build-src
build-src:
	${MAVEN} clean install
.PHONY: build-docker-images
build-docker-images:
	docker-compose build

.PHONY: wait
wait:
	sleep 25
## status
.PHONY: status
status:
	docker-compose ps
## stop
.PHONY: stop
stop:
	docker-compose down

## infrastructure: zookeeper kafka redis
.PHONY: run-infrastructure
run-infrastructure:
	docker-compose up -d zookeeper kafka redis
## required topics
.PHONY: create-topics
create-topics:
	docker-compose exec kafka kafka-topics --create --if-not-exists --zookeeper zookeeper:2181 --partitions 10 --replication-factor 1 --topic timestamp-user-action-v1
	docker-compose exec kafka kafka-topics --create --if-not-exists --zookeeper zookeeper:2181 --partitions 10 --replication-factor 1 --topic analytics-v1
## application
.PHONY: run-application
run-application:
	docker-compose up -d command processor materializer query
## scale example
.PHONY: scale-streams
scale-streams:
	docker-compose up --scale processor=3 -d

## curl example
.PHONY: curl-post
curl-post:
	curl -X POST "http://localhost:8081/analytics?timestamp=123&user=kek&click"
.PHONY: curl-get
curl-get:
	curl "http://localhost:8082/analytics?timestamp=123"