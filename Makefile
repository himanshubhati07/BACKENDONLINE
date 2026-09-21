.PHONY: install run test seed docker-up docker-down

install:
	pip install -r requirements.txt

run:
	PORT=25518 bash start.sh

seed:
	python3 seed.py

test:
	pytest tests/ -v --tb=short

docker-up:
	docker-compose up --build

docker-down:
	docker-compose down
