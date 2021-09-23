# Kubernetes 集群搭建(二进制方式)

## 步骤

- 创建多台虚拟机，安装Linux操作系统
- 操作系统初始化
- 为etcd和apiserver自签证书
- 部署etc集群
- 部署master组件
- 部署node组件
- 部署集群网络

## 安装要求

- 一台或多台机器，操作系统CentOS 7.x
- 硬件配置：2GB ，2个CPU，硬盘30GB
- 集群中所有机器之间网络互通
- 可以访问外网，需要拉取镜像，如果服务器不能上网，需要提前下载镜像导入节点
- 禁止swap分区

## 准备环境

| 角色      | IP             | 组件                                                         |
| --------- | -------------- | ------------------------------------------------------------ |
| k8smaster | 192.168.126.20 | kube-apiserver，kube-controler-manger，kube-scheduler，etcd，docker |
| k8snode1  | 192.168.126.21 | kubelet，kube-proxy，docker，etcd                            |
| k8snode2  | 192.168.126.22 | kubelet，kube-proxy，docker，etcd                            |

##  操作系统的初始化

~~~shell
# 关闭防火墙
systemctl stop firewalld
systemctl disable firewalld

# 关闭selinux
# 永久关闭
sed -i 's/enforcing/disabled/' /etc/selinux/config  
# 临时关闭
setenforce 0  

# 关闭swap
# 临时
swapoff -a 
# 永久关闭
sed -ri 's/.*swap.*/#&/' /etc/fstab

# 根据规划设置主机名【master节点上操作】
hostnamectl set-hostname k8smaster
# 根据规划设置主机名【node1节点操作】
hostnamectl set-hostname k8snode1
# 根据规划设置主机名【node2节点操作】
hostnamectl set-hostname k8snode2

# 在master添加hosts
cat >> /etc/hosts << EOF
192.168.126.20 k8smaster
192.168.126.21 k8snode1
192.168.126.22 k8snode2
EOF


# 将桥接的IPv4流量传递到iptables的链
cat > /etc/sysctl.d/k8s.conf << EOF
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
# 生效
sysctl --system  

# 时间同步
yum install ntpdate -y
ntpdate time.windows.com
~~~

## 为etcd和API server自签证书

在k8s集群中，也是有证书的，如果不带证书，那么访问就会受限

![](E:\doc\k8s\images\二进制搭建集群中集群内部证书问题.png)

集群外部也需要签发证书

![](E:\doc\k8s\images\二进制搭建集群中集群外部访问证书问题.png)

自签证书：一般门禁卡有两种，一个是内部员工的门禁卡，和外部访客门禁卡。这两种门禁卡的权限可能不同，员工的门禁卡可以进入公司的任何地方，而访客的门禁卡是受限的，这个门禁卡其实就是自签证书。

![](E:\doc\k8s\images\自签证书.png)

### 准备 cfssl 证书生成工具

cfsl 是一个开源的证书管理工具，使用 json 文件生成证书，相比 opensl 更方便使用。找任意一台服务器操作，这里用 Master 节点。

~~~shell
wget https://pkg.cfssl.org/R1.2/cfssl_linux-amd64
wget https://pkg.cfssl.org/R1.2/cfssljson_linux-amd64
wget https://pkg.cfssl.org/R1.2/cfssl-certinfo_linux-amd64
chmod +x cfssl_linux-amd64 cfssljson_linux-amd64 cfssl-certinfo_linux-amd64
mv cfssl_linux-amd64 /usr/local/bin/cfssl
mv cfssljson_linux-amd64 /usr/local/bin/cfssljson
mv cfssl-certinfo_linux-amd64 /usr/bin/cfssl-certinfo
chmod +x /usr/bin/cfssl*
~~~

###  生成 Etcd 证书

#### 自签证书颁发机构（CA）

创建工作目录：

~~~shell
mkdir -p ~/TLS/{etcd,k8s}
cd TLS/etc
~~~

自签 CA：

~~~shell
cat > ca-config.json << EOF
{
  "signing": {
    "default": {
      "expiry": "87600h"
    },
    "profiles": {
      "www": {
         "expiry": "87600h",
         "usages": [
            "signing",
            "key encipherment",
            "server auth",
            "client auth"
        ]
      }
    }
  }
}
EOF
~~~

~~~shell
cat > ca-csr.json << EOF
{
    "CN": "etcd CA",
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [
        {
            "C": "CN",
            "L": "Beijing",
            "ST": "Beijing"
        }
    ]
}
EOF
~~~

#### 生成证书

~~~shell
cfssl gencert -initca ca-csr.json | cfssljson -bare ca -

ls *pem
~~~

#### 使用自签 CA 签发 Etcd HTTPS 证书

~~~shell
cat > server-csr.json << EOF
{
    "CN": "etcd",
    "hosts": [
    "192.168.126.20",
    "192.168.126.21",
    "192.168.126.22"
    ],
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [
        {
            "C": "CN",
            "L": "BeiJing",
            "ST": "BeiJing"
        }
    ]
}
EOF
~~~

> 上述文件 hosts 字段中 IP 为所有 etcd 节点的集群内部通信 IP，一个都不能少！为了 方便后期扩容可以多写几个预留的 IP

#### 生成证书

~~~shell
cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=www server-csr.json | cfssljson -bare server ls server*pem
~~~

~~~shell
[root@k8smaster etcd]# cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=www server-csr.json | cfssljson -bare server ls server*pem
2021/09/17 21:16:30 [INFO] generate received request
2021/09/17 21:16:30 [INFO] received CSR
2021/09/17 21:16:30 [INFO] generating key: rsa-2048
2021/09/17 21:16:31 [INFO] encoded CSR
2021/09/17 21:16:31 [INFO] signed certificate with serial number 239938705096264136163703154375106181822363744769
2021/09/17 21:16:31 [WARNING] This certificate lacks a "hosts" field. This makes it unsuitable for
websites. For more information see the Baseline Requirements for the Issuance and Management
of Publicly-Trusted Certificates, v.1.1.6, from the CA/Browser Forum (https://cabforum.org);
specifically, section 10.2.3 ("Information Requirements").
~~~

查看证书

~~~shell
ls server*pem
~~~

### 从Github上下载二进制文件

下载地址：[点我下载](https://github.com/etcd-io/etcd/releases/download/v3.4.9/etcd-v3.4.9-%20linux-amd64.tar.gz)

## 部署 Etcd 集群

**以下在节点 1 上操作，为简化操作，待会将节点 1 生成的所有文件拷贝到节点 2 和节点 3**

### 创建工作目录并解压二进制包 

~~~shell
mkdir /opt/etcd/{bin,cfg,ssl} –p 
tar zxvf etcd-v3.4.9-linux-amd64.tar.gz 
mv etcd-v3.4.9-linux-amd64/{etcd,etcdctl} /opt/etcd/bin/
~~~

### 创建 etcd 配置文件

~~~shell
cat > /opt/etcd/cfg/etcd.conf << EOF
#[Member]
ETCD_NAME="etcd-1"
ETCD_DATA_DIR="/var/lib/etcd/default.etcd"
ETCD_LISTEN_PEER_URLS="https://192.168.126.20:2380"
ETCD_LISTEN_CLIENT_URLS="https://192.168.126.20:2379"
#[Clustering]
ETCD_INITIAL_ADVERTISE_PEER_URLS="https://192.168.126.20:2380"
ETCD_ADVERTISE_CLIENT_URLS="https://192.168.126.20:2379"
ETCD_INITIAL_CLUSTER="etcd-1=https://192.168.126.20:2380,etcd-2=https://192.168.126.21:2380,etcd-3=https://192.168.126.22:2380"
ETCD_INITIAL_CLUSTER_TOKEN="etcd-cluster"
ETCD_INITIAL_CLUSTER_STATE="new"
EOF
~~~

- ETCD_NAME：节点名称，集群中唯一
- ETCD_DATA_DIR：数据目录
- ETCD_LISTEN_PEER_URLS：集群通信监听地址
- ETCD_LISTEN_CLIENT_URLS：客户端访问监听地址
- ETCD_INITIAL_ADVERTISE_PEER_URLS：集群通告地址
- ETCD_ADVERTISE_CLIENT_URLS：客户端通告地址
- ETCD_INITIAL_CLUSTER：集群节点地址
- ETCD_INITIAL_CLUSTER_TOKEN：集群 Token
- ETCD_INITIAL_CLUSTER_STATE：加入集群的当前状态，new 是新集群，existing 表示加入 已有集群

### systemd管理etcd 

~~~shell
cat > /usr/lib/systemd/system/etcd.service << EOF
[Unit]
Description=Etcd Server
After=network.target
After=network-online.target
Wants=network-online.target
[Service]
Type=notify
EnvironmentFile=/opt/etcd/cfg/etcd.conf
ExecStart=/opt/etcd/bin/etcd \
--cert-file=/opt/etcd/ssl/server.pem \
--key-file=/opt/etcd/ssl/server-key.pem \
--peer-cert-file=/opt/etcd/ssl/server.pem \
--peer-key-file=/opt/etcd/ssl/server-key.pem \
--trusted-ca-file=/opt/etcd/ssl/ca.pem \
--peer-trusted-ca-file=/opt/etcd/ssl/ca.pem \
--logger=zap
Restart=on-failure
LimitNOFILE=65536
[Install]
WantedBy=multi-user.target
EOF
~~~

### 拷贝刚才生成的证书

把刚才生成的证书拷贝到配置文件中的路径：

~~~shell
cp ~/TLS/etcd/ca*pem ~/TLS/etcd/server*pem /opt/etcd/ssl/
~~~

### 将上面节点 1 所有生成的文件拷贝到节点 2 和节点 3

~~~shell
scp -r /opt/etcd/ root@192.168.126.21:/opt/

scp /usr/lib/systemd/system/etcd.service root@192.168.126.21:/usr/lib/systemd/system/

scp -r /opt/etcd/ root@192.168.126.22:/opt/

scp /usr/lib/systemd/system/etcd.service root@192.168.126.22:/usr/lib/systemd/system/
~~~

然后在节点 2 和节点 3 分别修改 etcd.conf 配置文件中的节点名称和当前服务器 IP

~~~shell
vi /opt/etcd/cfg/etcd.conf
~~~

~~~shell
#[Member]
ETCD_NAME="etcd-2"
ETCD_DATA_DIR="/var/lib/etcd/default.etcd"
ETCD_LISTEN_PEER_URLS="https://192.168.126.21:2380"
ETCD_LISTEN_CLIENT_URLS="https://192.168.126.21:2379"
#[Clustering]
ETCD_INITIAL_ADVERTISE_PEER_URLS="https://192.168.126.21:2380"
ETCD_ADVERTISE_CLIENT_URLS="https://192.168.126.21:2379"
ETCD_INITIAL_CLUSTER="etcd-1=https://192.168.126.20:2380,etcd-2=https://192.168.126.21:2380,etcd-3=https://192.168.126.22:2380"
ETCD_INITIAL_CLUSTER_TOKEN="etcd-cluster"
ETCD_INITIAL_CLUSTER_STATE="new"
~~~

~~~shell
#[Member]
ETCD_NAME="etcd-3"
ETCD_DATA_DIR="/var/lib/etcd/default.etcd"
ETCD_LISTEN_PEER_URLS="https://192.168.126.22:2380"
ETCD_LISTEN_CLIENT_URLS="https://192.168.126.22:2379"
#[Clustering]
ETCD_INITIAL_ADVERTISE_PEER_URLS="https://192.168.126.22:2380"
ETCD_ADVERTISE_CLIENT_URLS="https://192.168.126.22:2379"
ETCD_INITIAL_CLUSTER="etcd-1=https://192.168.126.20:2380,etcd-2=https://192.168.126.21:2380,etcd-3=https://192.168.126.22:2380"
ETCD_INITIAL_CLUSTER_TOKEN="etcd-cluster"
ETCD_INITIAL_CLUSTER_STATE="new"
~~~

### 启动并设置开机启动

~~~shell
systemctl daemon-reload 
systemctl start etcd #启动后会卡住因为需要和node节点一起执行命令
systemctl enable etcd
~~~

### 查看集群状态

~~~shell
[root@k8smaster ~]# /opt/etcd/bin/etcdctl --cacert=/opt/etcd/ssl/ca.pem --cert=/opt/etcd/ssl/server.pem --key=/opt/etcd/ssl/server-key.pem --endpoints="https://192.168.126.20:2379,https://192.168.126.21:2379,https://192.168.126.22:2379" endpoint status --write-out=table
+-----------------------------+------------------+---------+---------+-----------+------------+-----------+------------+--------------------+--------+
|          ENDPOINT           |        ID        | VERSION | DB SIZE | IS LEADER | IS LEARNER | RAFT TERM | RAFT INDEX | RAFT APPLIED INDEX | ERRORS |
+-----------------------------+------------------+---------+---------+-----------+------------+-----------+------------+--------------------+--------+
| https://192.168.126.20:2379 | 5b8bf9271093fa26 |   3.4.9 |   20 kB |     false |      false |         2 |          8 |                  8 |        |
| https://192.168.126.21:2379 | 53cebd1eedd492c0 |   3.4.9 |   20 kB |     false |      false |         2 |          8 |                  8 |        |
| https://192.168.126.22:2379 | e9299ee3ba217abe |   3.4.9 |   25 kB |      true |      false |         2 |          8 |                  8 |        |
+-----------------------------+------------------+---------+---------+-----------+------------+-----------+------------+--------------------+--------+

~~~

~~~shell
[root@k8smaster ~]# ETCDCTL_API=3 /opt/etcd/bin/etcdctl --cacert=/opt/etcd/ssl/ca.pem --cert=/opt/etcd/ssl/server.pem --key=/opt/etcd/ssl/server-key.pem --endpoints="https://192.168.126.20:2379,https://192.168.126.21:2379,https://192.168.126.22:2379" endpoint health
https://192.168.126.20:2379 is healthy: successfully committed proposal: took = 17.416822ms
https://192.168.126.21:2379 is healthy: successfully committed proposal: took = 18.444082ms
https://192.168.126.22:2379 is healthy: successfully committed proposal: took = 19.923066ms
~~~

如果输出上面信息，就说明集群部署成功。如果有问题第一步先看日志： 

~~~shell
/var/log/message 或 journalctl -u etcd
~~~

## **部署 Master Node** 

### 为kube-apiserver自签证书

#### 自签证书颁发机构（CA）

方式：

- 添加可信任的ip列表
- 携带ca证书发送

~~~shell
cd TLS/k8s
~~~

~~~shell
cat > ca-config.json << EOF
{
  "signing": {
    "default": {
      "expiry": "87600h"
    },
    "profiles": {
      "kubernetes": {
         "expiry": "87600h",
         "usages": [
            "signing",
            "key encipherment",
            "server auth",
            "client auth"
        ]
      }
    }
  }
}
EOF
~~~

~~~shell
cat > ca-csr.json << EOF
{
    "CN": "kubernetes",
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [
        {
            "C": "CN",
            "L": "Beijing",
            "ST": "Beijing",
            "O": "k8s",
            "OU": "System"
        }
    ]
}
EOF
~~~

#### 生成证书

~~~shell
cfssl gencert -initca ca-csr.json | cfssljson -bare ca -
ls *pem
~~~

#### 使用自签CA签发kube-apiserver HTTPS 证书 

创建证书申请文件：

~~~shell
cat > server-csr.json << EOF
{
    "CN": "kubernetes",
    "hosts": [
      "10.0.0.1",
      "127.0.0.1",
      "192.168.126.10",
      "192.168.126.11",
      "192.168.126.12",
      "192.168.126.20",
      "192.168.126.21",
      "192.168.126.22",
      "192.168.126.88",
      "kubernetes",
      "kubernetes.default",
      "kubernetes.default.svc",
      "kubernetes.default.svc.cluster",
      "kubernetes.default.svc.cluster.local"
    ],
    "key": {
        "algo": "rsa",
        "size": 2048
    },
    "names": [
        {
            "C": "CN",
            "L": "BeiJing",
            "ST": "BeiJing",
            "O": "k8s",
            "OU": "System"
        }
    ]
}
EOF
~~~

生成证书：

~~~shell
cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=kubernetes server-csr.json | cfssljson -bare server

ls server*pem
~~~

### 从 Github 下载二进制文件

~~~shell
https://github.com/kubernetes/kubernetes/blob/master/CHANGELOG/CHANGELOG-1.18.md#v11820
~~~

> 打开链接你会发现里面有很多包，下载一个 server 包就够了，包含了 Master 和 Worker Node 二进制文件。

### 解压二进制包 

~~~shell
mkdir -p /opt/kubernetes/{bin,cfg,ssl,logs}
tar zxvf kubernetes-server-linux-amd64.tar.gz
cd kubernetes/server/bin
cp kube-apiserver kube-scheduler kube-controller-manager /opt/kubernetes/bin
cp kubectl /usr/bin/
~~~

### 部署 kube-apiserver

#### 拷贝刚才生成的证书 

~~~shell
cp ~/TLS/k8s/ca*pem ~/TLS/k8s/server*pem /opt/kubernetes/ssl/
~~~

#### 创建配置文件 

~~~shell
cat > /opt/kubernetes/cfg/kube-apiserver.conf << EOF
KUBE_APISERVER_OPTS="--logtostderr=false \\
--v=2 \\
--log-dir=/opt/kubernetes/logs \\
--etcd-servers=https://192.168.126.20:2379,https://192.168.126.21:2379,https://192.168.126.22:2379 \\
--bind-address=192.168.126.20 \\
--secure-port=6443 \\
--advertise-address=192.168.126.20 \\
--allow-privileged=true \\
--service-cluster-ip-range=10.0.0.0/24 \\
--enable-admission-plugins=NamespaceLifecycle,LimitRanger,ServiceAccount,ResourceQuota,NodeRestriction \\
--authorization-mode=RBAC,Node \\
--enable-bootstrap-token-auth=true \\
--token-auth-file=/opt/kubernetes/cfg/token.csv \\
--service-node-port-range=30000-32767 \\
--kubelet-client-certificate=/opt/kubernetes/ssl/server.pem \\
--kubelet-client-key=/opt/kubernetes/ssl/server-key.pem \\
--tls-cert-file=/opt/kubernetes/ssl/server.pem  \\
--tls-private-key-file=/opt/kubernetes/ssl/server-key.pem \\
--client-ca-file=/opt/kubernetes/ssl/ca.pem \\
--service-account-key-file=/opt/kubernetes/ssl/ca-key.pem \\
--etcd-cafile=/opt/etcd/ssl/ca.pem \\
--etcd-certfile=/opt/etcd/ssl/server.pem \\
--etcd-keyfile=/opt/etcd/ssl/server-key.pem \\
--audit-log-maxage=30 \\
--audit-log-maxbackup=3 \\
--audit-log-maxsize=100 \\
--audit-log-path=/opt/kubernetes/logs/k8s-audit.log"
EOF
~~~

> 注：上面两个\ \ 第一个是转义符，第二个是换行符，使用转义符是为了使用 EOF 保留换行符。

–logtostderr：启用日志
—v：日志等级
–log-dir：日志目录
–etcd-servers：etcd 集群地址
–bind-address：监听地址
–secure-port：https 安全端口
–advertise-address：集群通告地址
–allow-privileged：启用授权
–service-cluster-ip-range：Service 虚拟 IP 地址段
–enable-admission-plugins：准入控制模块
–authorization-mode：认证授权，启用 RBAC 授权和节点自管理
–enable-bootstrap-token-auth：启用 TLS bootstrap 机制
–token-auth-file：bootstrap token 文件
–service-node-port-range：Service nodeport 类型默认分配端口范围
–kubelet-client-xxx：apiserver 访问 kubelet 客户端证书
–tls-xxx-file：apiserver https 证书
–etcd-xxxfile：连接 Etcd 集群证书
–audit-log-xxx：审计日志

#### 启用 TLS Bootstrapping 机制 

TLS Bootstraping：Master apiserver 启用 TLS 认证后，Node 节点 kubelet 和 kube-proxy 要与 kube-apiserver进行通信，必须使用 CA 签发的有效证书才可以，当 Node 节点很多时，这种客户端证书颁发需要大量工作，同样也会增加集群扩展复杂度。为了 简化流程，Kubernetes 引入了 TLS bootstraping 机制来自动颁发客户端证书，kubelet 会以一个低权限用户自动向 apiserver 申请证书，kubelet 的证书由 apiserver 动态签署。

所以强烈建议在 Node 上使用这种方式，目前主要用于 kubelet，kube-proxy 还是由我 们统一颁发一个证书。 

TLS bootstraping 工作流程：

![](E:\doc\k8s\images\TLSbotsraping工作流程.jpg)

创建上述配置文件中token文件：

~~~shell
cat > /opt/kubernetes/cfg/token.csv << EOF
6050c9947dc6a7d21a220169974ea49c,kubelet-bootstrap,10001,"system:node-bootstrapper"
EOF
~~~

格式：token，用户名，UID，用户组 

token 也可自行生成替换：

~~~shell
head -c 16 /dev/urandom | od -An -t x | tr -d ' '
~~~

#### systemd 管理 apiserver

~~~shell
cat > /usr/lib/systemd/system/kube-apiserver.service << EOF
[Unit]
Description=Kubernetes API Server
Documentation=https://github.com/kubernetes/kubernetes
[Service]
EnvironmentFile=/opt/kubernetes/cfg/kube-apiserver.conf
ExecStart=/opt/kubernetes/bin/kube-apiserver \$KUBE_APISERVER_OPTS
Restart=on-failure
[Install]
WantedBy=multi-user.target
EOF
~~~

#### 启动并设置开机启动

~~~shell
systemctl daemon-reload
systemctl start kube-apiserver
systemctl enable kube-apiserver
systemctl status kube-apiserver 
~~~

#### 授权 kubelet-bootstrap 用户允许请求证书

~~~shell
kubectl create clusterrolebinding kubelet-bootstrap \
--clusterrole=system:node-bootstrapper \
--user=kubelet-bootstrap
~~~

### 部署 kube-controler-manger

#### 创建配置文件

~~~shell
cat > /opt/kubernetes/cfg/kube-controller-manager.conf << EOF
KUBE_CONTROLLER_MANAGER_OPTS="--logtostderr=false \\
--v=2 \\
--log-dir=/opt/kubernetes/logs \\
--leader-elect=true \\
--master=127.0.0.1:8080 \\
--bind-address=127.0.0.1 \\
--allocate-node-cidrs=true \\
--cluster-cidr=10.244.0.0/16 \\
--service-cluster-ip-range=10.0.0.0/24 \\
--cluster-signing-cert-file=/opt/kubernetes/ssl/ca.pem \\
--cluster-signing-key-file=/opt/kubernetes/ssl/ca-key.pem  \\
--root-ca-file=/opt/kubernetes/ssl/ca.pem \\
--service-account-private-key-file=/opt/kubernetes/ssl/ca-key.pem \\
--experimental-cluster-signing-duration=87600h0m0s"
EOF
~~~

–master：通过本地非安全本地端口 8080 连接 apiserver。

–leader-elect：当该组件启动多个时，自动选举（HA）

–cluster-signing-cert-file/–cluster-signing-key-file：自动为 kubelet 颁发证书

的 CA，与 apiserver 保持一致

#### sytemd 管理 controler-manger

~~~shell
cat > /usr/lib/systemd/system/kube-controller-manager.service << EOF
[Unit]
Description=Kubernetes Controller Manager
Documentation=https://github.com/kubernetes/kubernetes
[Service]
EnvironmentFile=/opt/kubernetes/cfg/kube-controller-manager.conf
ExecStart=/opt/kubernetes/bin/kube-controller-manager \$KUBE_CONTROLLER_MANAGER_OPTS
Restart=on-failure
[Install]
WantedBy=multi-user.target
EOF
~~~

#### 启动并设置开机启动

~~~shell
systemctl daemon-reload
systemctl start kube-controller-manager
systemctl enable kube-controller-manager
systemctl status kube-controller-manager
~~~

### 部署 kube-scheduler

#### 创建配置文件 

~~~shell
cat > /opt/kubernetes/cfg/kube-scheduler.conf << EOF
KUBE_SCHEDULER_OPTS="--logtostderr=false \
--v=2 \
--log-dir=/opt/kubernetes/logs \
--leader-elect \
--master=127.0.0.1:8080 \
--bind-address=127.0.0.1"
EOF
~~~

–master：通过本地非安全本地端口 8080 连接 apiserver。 

–leader-elect：当该组件启动多个时，自动选举（HA）

#### systemd 管理 scheduler 

~~~shell
cat > /usr/lib/systemd/system/kube-scheduler.service << EOF
[Unit]
Description=Kubernetes Scheduler
Documentation=https://github.com/kubernetes/kubernetes
[Service]
EnvironmentFile=/opt/kubernetes/cfg/kube-scheduler.conf
ExecStart=/opt/kubernetes/bin/kube-scheduler \$KUBE_SCHEDULER_OPTS
Restart=on-failure
[Install]
WantedBy=multi-user.target
EOF
~~~

#### 启动并设置开机启动

~~~shell
systemctl daemon-reload
systemctl start kube-scheduler
systemctl enable kube-scheduler
systemctl status kube-scheduler
~~~

### 查看集群状态 

所有组件都已经启动成功，通过 kubectl 工具查看当前集群组件状态：

~~~shell
[root@k8smaster server]# kubectl get cs
NAME                 STATUS    MESSAGE             ERROR
scheduler            Healthy   ok                  
controller-manager   Healthy   ok                  
etcd-2               Healthy   {"health":"true"}   
etcd-1               Healthy   {"health":"true"}   
etcd-0               Healthy   {"health":"true"}
~~~

## 安装Docker

下载地址：[下载docker](https://download.docker.com/linux/static/stable/x86_64/docker-20.10.3.tgz)

以下在所有节点操作。这里采用二进制安装，用 yum 安装也一样。

~~~shell
tar zxvf docker-20.10.3.tgz 
mv docker/* /usr/bin
~~~

### systemd 管理 docker

~~~shell
cat > /usr/lib/systemd/system/docker.service << EOF
[Unit]
Description=Docker Application Container Engine
Documentation=https://docs.docker.com
After=network-online.target firewalld.service
Wants=network-online.target
[Service]
Type=notify
ExecStart=/usr/bin/dockerd
ExecReload=/bin/kill -s HUP $MAINPID
LimitNOFILE=infinity
LimitNPROC=infinity
LimitCORE=infinity
TimeoutStartSec=0
Delegate=yes
KillMode=process
Restart=on-failure
StartLimitBurst=3
StartLimitInterval=60s
[Install]
WantedBy=multi-user.target
EOF
~~~

### 配置阿里云加速

~~~shell
mkdir /etc/docker
cat > /etc/docker/daemon.json << EOF
{
  "registry-mirrors": ["https://b9pmyelo.mirror.aliyuncs.com"]
}
EOF
~~~

### 启动并设置开机启动 

~~~shell
systemctl daemon-reload

systemctl start docker

systemctl enable docker
~~~

~~~shell
[root@k8snode1 ~]# docker -v
Docker version 20.10.3, build 48d30b5
~~~



(下面未完成，有错误)

## **部署 Worker Node**

### 创建工作目录并拷贝二进制文件 

在所有 worker node 创建工作目录：

~~~shell
mkdir -p /opt/kubernetes/{bin,cfg,ssl,logs}
tar zxvf kubernetes-server-linux-amd64.tar.gz
cd kubernetes/server/bin

cp kubelet kube-proxy /opt/kubernetes/bin

cp kubectl /usr/bin/
~~~

### 部署 kubelet

#### 创建配置文件 

~~~shell
cat > /opt/kubernetes/cfg/kubelet.conf << EOF
KUBELET_OPTS="--logtostderr=false \\
--v=2 \\
--log-dir=/opt/kubernetes/logs \\
--hostname-override=k8snode2 \\
--network-plugin=cni \\
--kubeconfig=/opt/kubernetes/cfg/kubelet.kubeconfig \\
--bootstrap-kubeconfig=/opt/kubernetes/cfg/bootstrap.kubeconfig \\
--config=/opt/kubernetes/cfg/kubelet-config.yml \\
--cert-dir=/opt/kubernetes/ssl \\
--pod-infra-container-image=lizhenliang/pause-amd64:3.0"
EOF
~~~

–hostname-override：显示名称，集群中唯一
–network-plugin：启用CNI
–kubeconfig：空路径，会自动生成，后面用于连接apiserver
–bootstrap-kubeconfig：首次启动向apiserver申请证书
–config：配置参数文件
–cert-dir：kubelet证书生成目录
–pod-infra-container-image：管理Pod网络容器的镜像

#### 配置参数文件 

~~~shell
cat > /opt/kubernetes/cfg/kubelet-config.yml << EOF
kind: KubeletConfiguration
apiVersion: kubelet.config.k8s.io/v1beta1
address: 0.0.0.0
port: 10250
readOnlyPort: 10255
cgroupDriver: cgroupfs
clusterDNS:
- 10.0.0.2
clusterDomain: cluster.local 
failSwapOn: false
authentication:
  anonymous:
    enabled: false
  webhook:
    cacheTTL: 2m0s
    enabled: true
  x509:
    clientCAFile: /opt/kubernetes/ssl/ca.pem 
authorization:
  mode: Webhook
  webhook:
    cacheAuthorizedTTL: 5m0s
    cacheUnauthorizedTTL: 30s
evictionHard:
  imagefs.available: 15%
  memory.available: 100Mi
  nodefs.available: 10%
  nodefs.inodesFree: 5%
maxOpenFiles: 1000000
maxPods: 110
EOF
~~~

#### 将master一些配置文件拷贝到node节点上

~~~shell
scp -r /opt/kubernetes/ssl root@192.168.126.21:/opt/kubernetes
scp -r /opt/kubernetes/ssl root@192.168.126.22:/opt/kubernetes
~~~

#### 生成 bootstrap.kubeconfig 文件 

~~~shell
cat > /opt/kubernetes/cfg/bootstrap.kubeconfig << EOF
KUBE_APISERVER="https://192.168.126.20:6443" # apiserver IP:PORT
TOKEN="6050c9947dc6a7d21a220169974ea49c" # 与token.csv里保持一致
kubectl config set-cluster kubernetes \
  --certificate-authority=/opt/kubernetes/ssl/ca.pem \
  --embed-certs=true \
  --server=${KUBE_APISERVER} \
  --kubeconfig=bootstrap.kubeconfig
kubectl config set-credentials "kubelet-bootstrap" \
  --token=${TOKEN} \
  --kubeconfig=bootstrap.kubeconfig
kubectl config set-context default \
  --cluster=kubernetes \
  --user="kubelet-bootstrap" \
  --kubeconfig=bootstrap.kubeconfig
kubectl config use-context default --kubeconfig=bootstrap.kubeconfig
EOF
~~~

#### systemd管理kubelet

~~~shell
cat > /usr/lib/systemd/system/kubelet.service << EOF
[Unit]
Description=Kubernetes Kubelet
After=docker.service
[Service]
EnvironmentFile=/opt/kubernetes/cfg/kubelet.conf
ExecStart=/opt/kubernetes/bin/kubelet \$KUBELET_OPTS
Restart=on-failure
LimitNOFILE=65536
[Install]
WantedBy=multi-user.target
EOF
~~~

#### 启动并设置开机启动

~~~shell
systemctl daemon-reload 
systemctl start kubelet 
systemctl enable kubelet
~~~

### 部署 kube-proxy

#### 创建配置文件

~~~shell
cat > /opt/kubernetes/cfg/kube-proxy.conf << EOF
KUBE_PROXY_OPTS="--logtostderr=false \\
--v=2 \\
--log-dir=/opt/kubernetes/logs \\
--config=/opt/kubernetes/cfg/kube-proxy-config.yml"
EOF
~~~

#### 配置参数文件 

~~~shell
cat > /opt/kubernetes/cfg/kube-proxy-config.yml << EOF
kind: KubeProxyConfiguration
apiVersion: kubeproxy.config.k8s.io/v1alpha1
bindAddress: 0.0.0.0
metricsBindAddress: 0.0.0.0:10249
clientConnection:
  kubeconfig: /opt/kubernetes/cfg/kube-proxy.kubeconfig
hostnameOverride: k8smaster
clusterCIDR: 10.0.0.0/24
EOF
~~~

#### 生成kube-proxy.kubeconfig文件(master生成在传到node)

~~~shell
# 切换工作目录
cd TLS/k8s
~~~

~~~shell
# 创建证书请求文件
cat > kube-proxy-csr.json << EOF
{
  "CN": "system:kube-proxy",
  "hosts": [],
  "key": {
    "algo": "rsa",
    "size": 2048
  },
  "names": [
    {
      "C": "CN",
      "L": "BeiJing",
      "ST": "BeiJing",
      "O": "k8s",
      "OU": "System"
    }
  ]
}
EOF
~~~

~~~shell
# 生成证书
cfssl gencert -ca=ca.pem -ca-key=ca-key.pem -config=ca-config.json -profile=kubernetes kube-proxy-csr.json | cfssljson -bare kube-proxy
~~~

~~~shell
scp -r /root/TLS/k8s root@192.168.126.21:/opt/TLS/
scp -r /root/TLS/k8s root@192.168.126.22:/opt/TLS/
~~~

#### 生成kube-proxy.kubeconfig文件

~~~shell
cat > /opt/kubernetes/cfg/kube-proxy.kubeconfig << EOF
KUBE_APISERVER="https://192.168.126.20:6443"
kubectl config set-cluster kubernetes \
  --certificate-authority=/opt/kubernetes/ssl/ca.pem \
  --embed-certs=true \
  --server=${KUBE_APISERVER} \
  --kubeconfig=kube-proxy.kubeconfig
kubectl config set-credentials kube-proxy \
  --client-certificate=./kube-proxy.pem \
  --client-key=./kube-proxy-key.pem \
  --embed-certs=true \
  --kubeconfig=kube-proxy.kubeconfig
kubectl config set-context default \
  --cluster=kubernetes \
  --user=kube-proxy \
  --kubeconfig=kube-proxy.kubeconfig
kubectl config use-context default --kubeconfig=kube-proxy.kubeconfig
EOF
~~~

#### systemd管理kube-proxy

~~~shell
cat > /usr/lib/systemd/system/kube-proxy.service << EOF
[Unit]
Description=Kubernetes Proxy
After=network.target
[Service]
EnvironmentFile=/opt/kubernetes/cfg/kube-proxy.conf
ExecStart=/opt/kubernetes/bin/kube-proxy \$KUBE_PROXY_OPTS
Restart=on-failure
LimitNOFILE=65536
[Install]
WantedBy=multi-user.target
EOF
~~~

#### 启动并设置开机启动

~~~shell
systemctl daemon-reload

systemctl start kube-proxy

systemctl enable kube-proxy

systemctl status kube-proxy

~~~

