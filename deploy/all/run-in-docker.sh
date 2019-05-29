#!/usr/bin/env bash

ui_path=${PWD}/rex-ui
profile=${1}
if [ "${profile}" = "" ]; then
profile=dev
fi
container_name=rex-all
image_name=repo.zmcsoft.com:5000/rex-all
server_port=8080
echo use profile ${profile}
cd rex-starter-all
cp -r ../rex-ui rex-ui
if [ -f "target/rex-starter-all.jar" ]; then
        container_id=$(docker ps -a | grep "${container_name}" | awk '{print $1}')
        if [ "${container_id}" != "" ];then
            docker stop ${container_name}
            docker rm ${container_name}
            docker rmi  ${image_name}
        fi
          docker build -t ${image_name} .
          docker run -d -p ${server_port}:8080 \
          --add-host icbc-server:10.190.220.120 \
          --add-host file.rex.hsweb.me:178.25.1.39 \
          --add-host api.rex.hsweb.me:178.25.1.39 \
          --add-host cdjj.chegonggong.com:178.25.1.36 \
          --add-host cdjjjf.chegonggong.com:178.25.1.36 \
          --add-host api.test.rex.hsweb.me:178.25.1.40 \
          -e TZ="Asia/Shanghai" \
          -e spring.profiles.active=${profile} \
          --net host \
          -v /etc/localtime:/etc/localtime \
          -v ${ui_path}:/rex-ui \
          -v /opt/rex-api/data/:/data/ \
          -v /opt/rex-api/logs/:/logs/ \
          --name ${container_name} ${image_name} \
          /app.jar
    else
        echo "build error!"
        exit -1
fi