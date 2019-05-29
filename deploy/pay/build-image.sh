#!/usr/bin/env bash

ver=${1}
if [ "${ver}" = "" ]; then
ver=latest
fi
cd rex-starter-pay
docker rmi repo.zmcsoft.com:5000/rex-pay:${ver}

docker build -t repo.zmcsoft.com:5000/rex-pay:${ver} .
docker push repo.zmcsoft.com:5000/rex-pay:${ver}