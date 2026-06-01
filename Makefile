REPO=northamerica-northeast1-docker.pkg.dev/servers-7-sol/docker-repo-7-sol

build-beta:
	skaffold build -p beta --default-repo=$(REPO) --file-output=artifacts.json

deploy-beta:
	skaffold deploy -a artifacts.json -p beta

build-prod:
	skaffold build -p prod --default-repo=$(REPO) --file-output=artifacts.json

deploy-prod:
	skaffold deploy -a artifacts.json -p prod
