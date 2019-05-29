#!/usr/bin/env bash

ver=${1}
if [ "${ver}" = "" ]; then
ver=latest
fi
cd rex-starter-all

cp -r ../rex-ui rex-ui
docker rmi repo.zmcsoft.com:5000/rex-all:${ver}

docker build -t repo.zmcsoft.com:5000/rex-all:${ver} .
docker push repo.zmcsoft.com:5000/rex-all:${ver}
rm -rf rex-ui