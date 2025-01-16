# code-sandbox
## 代码沙箱容器构建
```shell
docker build . -t codesandbox:latest
```

## docker快速删除所有容器
```shell
sudo docker rm $(docker ps -aq)
```
```shell
sudo docker stop $(docker ps -q) & docker rm $(docker ps -aq)
```

