# 搭建K8S集群

## 搭建k8s环境平台规划

### 单master集群

一个master节点管理多个node节点

![](E:\doc\k8s\images\单master集群.png)

### 多master集群

多个master节点管理多个node节点，中间多了一个负载均衡的过程

![](E:\doc\k8s\images\多master集群.png)

## 服务器硬件配置要求

### 测试环境

master：2核 4G 20G

node： 4核 8G 40G

### 生产环境

更高要求

## 集群搭建方式

### kubeadm

Kubeadm 是一个 K8s 部署工具，提供 kubeadm int 和 kubeadm join，用于快速部署 Kubernets 集群。

官方地址：[kubeadm](https://kubernetes.io/zh/docs/setup/production-environment/tools/kubeadm/install-kubeadm/)

### 二进制包

从 github 下载发行版的二进制包，手动部署每个组件，组成 Kubernets 集群。

Kubeadm 降低部署门槛，但屏蔽了很多细节，遇到问题很难排查。如果想更容易可控，推荐使用二进制包部署 Kubernets 集群，虽然手动部署麻烦点，期间可以学习很多工作原理，也利于后期维护。