# Kubernetes集群资源监控

## 概述

### 监控指标

一个好的系统，主要监控以下内容

- 集群监控
  - 节点资源利用率
  - 节点数
  - 运行Pods
- Pod监控
  - 容器指标
  - 应用程序【程序占用多少CPU、内存】

### 监控平台

使用普罗米修斯【prometheus】 + Grafana 搭建监控平台

- prometheus【定时搜索被监控服务的状态】
  - 开源的
  - 监控、报警、数据库
  - 以HTTP协议周期性抓取被监控组件状态
  - 不需要复杂的集成过程，使用http接口接入即可
- Grafana
  - 开源的数据分析和可视化工具
  - 支持多种数据源

![image-20210925204051803](images/image-20210925204051803.png)

## 搭建监控平台

### 部署prometheus

首先需要部署一个守护进程

![image-20210925205542933](images/image-20210925205542933.png)

node-exporter.yaml

~~~yaml
---
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: node-exporter
  namespace: kube-system
  labels:
    k8s-app: node-exporter
spec:
  selector:
    matchLabels:
      k8s-app: node-exporter
  template:
    metadata:
      labels:
        k8s-app: node-exporter
    spec:
      containers:
      - image: prom/node-exporter
        name: node-exporter
        ports:
        - containerPort: 9100
          protocol: TCP
          name: http
---
apiVersion: v1
kind: Service
metadata:
  labels:
    k8s-app: node-exporter
  name: node-exporter
  namespace: kube-system
spec:
  ports:
  - name: http
    port: 9100
    nodePort: 31672
    protocol: TCP
  type: NodePort
  selector:
    k8s-app: node-exporter
~~~

```shell
kubectl create -f node-exporter.yaml
```

部署其他yaml

- configmap：定义一个configmap：存储一些配置文件【不加密】
- prometheus.deploy.yaml：部署一个deployment【包括端口号，资源限制】
- prometheus.svc.yaml：对外暴露的端口
- rbac-setup.yaml：分配一些角色的权限

下面我们进入目录下，首先部署 rbac-setup.yaml

```bash
kubectl create -f rbac-setup.yaml
```

![image-20210925210532280](images/image-20210925210532280.png)

然后分别部署

```bash
# 部署configmap
kubectl create -f configmap.yaml
# 部署deployment
kubectl create -f prometheus.deploy.yml
# 部署svc
kubectl create -f prometheus.svc.yml
```

部署完成后，我们使用下面命令查看

```bash
kubectl get pods -n kube-system
```

![image-20210925210803496](images/image-20210925210803496.png)

### 部署grafana

![image-20210925211017428](images/image-20210925211017428.png)

~~~shell
kubectl create -f grafana-deploy.yaml
~~~

然后执行完后，发现下面的问题

```bash
error: unable to recognize "grafana-deploy.yaml": no matches for kind "Deployment" in version "extensions/v1beta1"
```

我们需要修改如下内容

```bash
# 修改
apiVersion: apps/v1

# 添加selector
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
      component: core
```

修改完成后，我们继续执行上述代码

```bash
# 创建deployment
kubectl create -f grafana-deploy.yaml
# 创建svc
kubectl create -f grafana-svc.yaml
# 创建 ing
kubectl create -f grafana-ing.yaml
```

我们能看到，我们的grafana创建完成

![image-20210925211622103](images/image-20210925211622103.png)

### 配置数据源

下面我们需要开始打开 Grafana，然后配置数据源，导入数据显示模板

```bash
kubectl get svc -n kube-system
```

![image-20210925211735072](images/image-20210925211735072.png)

我们可以通过 ip + 30431 访问我们的 grafana 图形化页面，账户密码admin

进入后，我们就需要配置 prometheus 的数据源

![image-20210925212156709](images/image-20210925212156709.png)

和 对应的IP【这里IP是我们的ClusterIP】

![image-20210925212229776](images/image-20210925212229776.png)

### 设置显示数据的模板

选择Dashboard，导入我们的模板

![image-20210925212400531](images/image-20210925212400531.png)

然后输入 315 号模板

![image-20210925212516803](images/image-20210925212516803.png)

导入后的效果如下所示

![image-20210925212605991](images/image-20210925212605991.png)