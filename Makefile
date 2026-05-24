.PHONY: up down test verify logs clean

up:
	@./scripts/dev-up.sh

down:
	@./scripts/dev-down.sh

test:
	@./scripts/test.sh

verify:
	@./scripts/verify.sh

logs:
	@docker compose logs -f

clean:
	@mvn clean
	@./scripts/dev-down.sh
