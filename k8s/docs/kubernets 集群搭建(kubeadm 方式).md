# kubernetes 集群搭建(kubeadm 方式)

## kubeadm 部署方式介绍

kubeadm 是官方社区推出的一个用于快速部署 kubernets 集群的工具，这个工具能通过两条指令完成一个 kubernets 集群的部署：

~~~shell
# 创建一个 Master 节点
$ kubeadm init

# 将一个 Node 节点加入到当前集群中
$ kubeadm join <Master节点的IP和端口 >
~~~

## 安装要求

在开始之前，部署 Kubernets 集群机器需要满足以下几个条件：

- 一台或多台机器，操作系统 CentOS7.x-86_x64
- 硬件配置：2GB或更多RAM，2个CPU或更多CPU，硬盘30GB或更多
- 集群中所有机器之间网络互通
- 可以访问外网，需要拉取镜像，如果服务器不能上网，需要提前下载镜像并导入节点
- 禁止swap分区

## 准备环境

| 角色   | IP             |
| ------ | -------------- |
| master | 192.168.126.10 |
| node1  | 192.168.126.11 |
| node2  | 192.168.126.12 |

~~~shell
# 关闭防火墙
systemctl stop firewalld
systemctl disable firewalld

# 关闭selinux
sed -i 's/enforcing/disabled/' /etc/selinux/config  # 永久
setenforce 0  # 临时

# 关闭swap
swapoff -a  # 临时
sed -ri 's/.*swap.*/#&/' /etc/fstab    # 永久

# 根据规划设置主机名
hostnamectl set-hostname <hostname>

# 在master添加hosts
cat >> /etc/hosts << EOF
192.168.126.10 k8s-master
192.168.126.11 k8s-node1
192.168.126.12 k8s-node2
EOF

# 将桥接的IPv4流量传递到iptables的链
cat > /etc/sysctl.d/k8s.conf << EOF
net.bridge.bridge-nf-call-ip6tables = 1
net.bridge.bridge-nf-call-iptables = 1
EOF
sysctl --system  # 生效

# 时间同步
yum install ntpdate -y
ntpdate time.windows.com
~~~

## 安装Docker/kubeadm/kubelet/kubectl

Kubernetes默认CRI（容器运行时）为Docker，因此先安装Docker。

### 安装Docker

~~~shell
$ yum -y install docker-ce-18.06.1.ce-3.el7
$ systemctl enable docker && systemctl start docker
$ docker --version
Docker version 18.06.1-ce, build e68fc7a
~~~

~~~shell
$ cat > /etc/docker/daemon.json << EOF
{
  "registry-mirrors": ["https://b9pmyelo.mirror.aliyuncs.com"]
}
EOF
~~~

### 添加阿里云YUM软件源

~~~shell
$ cat > /etc/yum.repos.d/kubernetes.repo << EOF
[kubernetes]
name=Kubernetes
baseurl=https://mirrors.aliyun.com/kubernetes/yum/repos/kubernetes-el7-x86_64
enabled=1
gpgcheck=0
repo_gpgcheck=0
gpgkey=https://mirrors.aliyun.com/kubernetes/yum/doc/yum-key.gpg https://mirrors.aliyun.com/kubernetes/yum/doc/rpm-package-key.gpg
EOF
~~~

### 安装kubeadm，kubelet和kubectl

由于版本更新频繁，这里指定版本号部署：

~~~shell
$ yum install -y kubelet-1.18.0 kubeadm-1.18.0 kubectl-1.18.0
$ systemctl enable kubelet
~~~

## 部署Kubernetes Master

在192.168.126.10（Master）执行。

~~~shell
$ kubeadm init \
  --apiserver-advertise-address=192.168.126.10 \
  --image-repository registry.aliyuncs.com/google_containers \
  --kubernetes-version v1.18.0 \
  --service-cidr=10.96.0.0/12 \
  --pod-network-cidr=10.244.0.0/16
~~~

由于默认拉取镜像地址k8s.gcr.io国内无法访问，这里指定阿里云镜像仓库地址。

使用kubectl工具：

~~~shell
mkdir -p $HOME/.kube
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config
sudo chown $(id -u):$(id -g) $HOME/.kube/config
$ kubectl get nodes
~~~

## 加入Kubernetes Node

在192.168.126.11（Node）执行。

向集群添加新节点，执行在kubeadm init输出的kubeadm join命令：

~~~shell
kubeadm join 192.168.126.10:6443 --token i7qs20.qbnzr8e9wo02w16g \
    --discovery-token-ca-cert-hash sha256:6e04674bd0e5358bdef1023c2f0c510f9a77836ab6f20fa4261aa2fc63e0c4ff
~~~

默认token有效期为24小时，当过期之后，该token就不可用了。这时就需要重新创建token，操作如下：

~~~shell
kubeadm token create --print-join-command
~~~

~~~shell
kubectl get node
~~~

## 部署CNI网络插件

~~~shell
wget https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml
~~~

默认镜像地址无法访问，sed命令修改为docker hub镜像仓库。

~~~shell
kubectl apply -f https://raw.githubusercontent.com/coreos/flannel/master/Documentation/kube-flannel.yml

kubectl get pods -n kube-system
NAME                          READY   STATUS    RESTARTS   AGE
kube-flannel-ds-amd64-2pc95   1/1     Running   0          72s

[root@k8s-master ~]# kubectl get nodes
NAME         STATUS   ROLES    AGE     VERSION
k8s-master   Ready    master   15m     v1.18.0
k8s-node1    Ready    <none>   9m50s   v1.18.0
k8s-node2    Ready    <none>   9m45s   v1.18.0
~~~

## 测试kubernetes集群

在Kubernetes集群中创建一个pod，验证是否正常运行：

~~~shell
$ kubectl create deployment nginx --image=nginx
$ kubectl expose deployment nginx --port=80 --type=NodePort
$ kubectl get pod,svc
~~~

~~~shell
[root@k8s-master ~]# kubectl get pod,svc
NAME                        READY   STATUS    RESTARTS   AGE
pod/nginx-f89759699-d2s7r   1/1     Running   0          68s

NAME                 TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)        AGE
service/kubernetes   ClusterIP   10.96.0.1       <none>        443/TCP        16m
service/nginx        NodePort    10.110.237.61   <none>        80:30518/TCP   14s
~~~

访问地址：http://NodeIP:Port  

~~~shell
http://192.168.126.11:30518
~~~

