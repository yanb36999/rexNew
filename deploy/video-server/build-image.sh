#!/usr/bin/env bash

ver=${1}
if [ "${ver}" = "" ]; then
ver=latest
fi
cd rex-starter-video
docker rmi repo.zmcsoft.com:5000/rex-starter-video:${ver}

docker build -t repo.zmcsoft.com:5000/rex-starter-video:${ver} .
docker push repo.zmcsoft.com:5000/rex-starter-video:${ver}