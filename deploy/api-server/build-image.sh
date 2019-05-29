#!/usr/bin/env bash

ver=${1}
if [ "${ver}" = "" ]; then
ver=latest
fi
cd rex-starter-tmb-api
docker rmi repo.zmcsoft.com:5000/rex-api-server:${ver}

docker build -t repo.zmcsoft.com:5000/rex-api-server:${ver} .
docker push repo.zmcsoft.com:5000/rex-api-server:${ver}