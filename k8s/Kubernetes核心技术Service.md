# Kubernetes核心技术Service

## Service存在的意义

### 防止Pod失联【服务发现】

因为Pod每次创建都对应一个IP地址，而这个IP地址是短暂的，每次随着Pod的更新都会变化，假设当我们的前端页面有多个Pod时候，同时后端也多个Pod，前端Pod访问后端Pod，就需要通过注册中心，拿到后端Pod的IP地址，然后去访问对应的Pod。

![image-20210924192235753](images/image-20210924192235753.png)

### 定义Pod访问策略【负载均衡】

页面前端的Pod访问到后端的Pod，中间会通过Service一层，而Service在这里还能做负载均衡，负载均衡的策略有很多种实现策略

![image-20210924192202743](images/image-20210924192202743.png)

## Pod和Service的关系

这里Pod 和 Service之间还是根据 label 和 selector 标签建立关联的 【和Controller一样】

![image-20210924192919678](images/image-20210924192919678.png)

我们在访问service的时候，其实也是需要有一个IP地址，这个IP肯定不是Pod的ip地址，而是虚拟IP。

## Service常用类型

Service常用类型有三种

- ClusterIP：集群内部访问（前端Pod访问后端Pod，集群中进行）
- NodePort：对外访问应用的时候使用（普通用户访问前端页面）
- LoadBalancer：对外访问应用使用，公有云

### 举例

当前Pod，service：

![image-20210924194701766](images/image-20210924194701766.png)

我们可以导出一个文件，包含service的配置信息

```bash
kubectl expose deployment web --port=80 --target-port=80 --dry-run -o yaml > service.yaml
```

service.yaml 如下所示

```yaml
apiVersion: v1
kind: Service
metadata:
  creationTimestamp: null
  labels:
    app: web
  name: web
spec:
  ports:
  - port: 80
    protocol: TCP
    targetPort: 80
  selector:
    app: web
status:
  loadBalancer: {}
```

如果我们没有做设置的话，默认使用的是第一种方式 ClusterIP，也就是只能在集群内部使用，我们可以添加一个type字段，用来设置我们的service类型

![image-20210924195224288](images/image-20210924195224288.png)

~~~yaml
apiVersion: v1
kind: Service
metadata:
  creationTimestamp: null
  labels:
    app: web
  name: web
spec:
  ports:
  - port: 80
    protocol: TCP
    targetPort: 80
  selector:
    app: web
  type: NodePort
status:
  loadBalancer: {}
~~~

![image-20210924195556437](images/image-20210924195556437.png)

node一般是在内网进行部署，而外网一般是不能访问到的，那么如何访问的呢？

- 找到一台可以通过外网访问机器，安装nginx，反向代理
- 手动把可以访问的节点添加到nginx中

如果我们使用LoadBalancer，就会有负载均衡的控制器，类似于nginx的功能，就不需要自己添加到nginx上

