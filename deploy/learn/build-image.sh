#!/usr/bin/env bash

ver=${1}
if [ "${ver}" = "" ]; then
ver=latest
fi
cd rex-starter-learn

cp -r ../rex-ui rex-ui
docker rmi repo.zmcsoft.com:5000/rex-learn:${ver}

docker build -t repo.zmcsoft.com:5000/rex-learn:${ver} .
docker push repo.zmcsoft.com:5000/rex-learn:${ver}
rm -rf rex-ui