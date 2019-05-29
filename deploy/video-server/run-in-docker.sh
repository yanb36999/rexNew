#!/usr/bin/env bash

ui_path=${PWD}/rex-ui
profile=${1}
if [ "${profile}" = "" ]; then
profile=dev
fi
container_name=rex-starter-video
image_name=repo.zmcsoft.com:5000/rex-starter-video
server_port=8898
echo use profile ${profile}
cd rex-starter-video
if [ -f "target/rex-starter-video.jar" ]; then
        container_id=$(docker ps -a | grep "${container_name}" | awk '{print $1}')
        if [ "${container_id}" != "" ];then
            docker stop ${container_name}
            docker rm ${container_name}
            docker rmi  ${image_name}
        fi
          docker build -t ${image_name} .
          docker run -d -p ${server_port}:8898 \
          -e TZ="Asia/Shanghai" \
          -e spring.profiles.active=${profile} \
          --net host \
          -v /etc/localtime:/etc/localtime \
          -v /opt/file-input/:/opt/file-input/ \
          -v /opt/file-output/:/opt/file-output/ \
          -v /opt/video-server/data/:/data/ \
          -v /opt/video-server/logs/:/logs/ \
          -v "/var/run/docker.sock:/var/run/docker.sock" \
          --name ${container_name} ${image_name} \
          /app.jar
    else
        echo "build error!"
        exit -1
fi