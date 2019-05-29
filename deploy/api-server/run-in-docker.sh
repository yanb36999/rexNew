#!/usr/bin/env bash

ui_path=${PWD}/rex-ui
profile=${1}
if [ "${profile}" = "" ]; then
profile=dev
fi
container_name=rex-api-server
image_name=repo.zmcsoft.com:5000/rex-api-server
server_port=8089
echo use profile ${profile}
cd rex-starter-tmb-api
if [ -f "target/rex-starter-tmb-api.jar" ]; then
        container_id=$(docker ps -a | grep "${container_name}" | awk '{print $1}')
        if [ "${container_id}" != "" ];then
            docker stop ${container_name}
            docker rm ${container_name}
            docker rmi  ${image_name}
        fi
          docker build -t ${image_name} .
          docker run -d -p ${server_port}:8089 \
          -e TZ="Asia/Shanghai" \
          -e spring.profiles.active=${profile} \
          --net host \
          -v /etc/localtime:/etc/localtime \
          -v ${ui_path}:/rex-ui \
          -v /opt/tmb-api/data/:/data/ \
          -v /opt/tmb-api/logs/:/logs/ \
          --name ${container_name} ${image_name} \
          /app.jar
    else
        echo "build error!"
        exit -1
fi