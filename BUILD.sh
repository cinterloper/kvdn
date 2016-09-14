#/usr/bin/env bash

export GH_TOKEN=$(cat ~/.ghtoken )
export GH_USER=cinterloper
export VENDOR=$GH_USER
#building builder
docker run -t -i -v $(which docker):$(which docker)\
		 -v $(realpath ~/.m2):/root/.m2\
		 -v $(realpath ~/.aws):/root/.aws\
		 -v /var/run/docker.sock:/var/run/docker.sock\
		 -v $(realpath ~/.docker/):/root/.docker\
		 -e BASEIMAGE=builder\
		 -e VENDOR\
		 -e BUILDARGS\
		 -e GH_USER\
		 -e GH_TOKEN\
		 -e GH_BRANCH\
		 -e BUILD_EXTENSIONS\
		 -e GH_REPO=cinterloper/kvdn\
		 cinterloper/builder
