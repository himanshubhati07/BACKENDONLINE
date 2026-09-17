.PHONY: build test run package docker-build docker-up docker-down clean

build:
	./mvnw compile -q

test:
	./mvnw test -q

package:
	./mvnw package -DskipTests -q

run: package
	bash ./start.sh

docker-build:
	docker compose build

docker-up:
	docker compose up -d

docker-down:
	docker compose down

clean:
	./mvnw clean -q
