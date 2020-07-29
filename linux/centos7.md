# 设置静态IP

## 虚拟机网络配置

![]([虚拟机网络编辑器](https://github.com/jackhusky/doc/blob/master/linux/images/虚拟机网络编辑器.png))

![]([NAT设置](https://github.com/jackhusky/doc/blob/master/linux/images/NAT设置.png))

```shell
# 查看MAC地址
[root@localhost ~]# ip addr
1: lo: <LOOPBACK,UP,LOWER_UP> mtu 65536 qdisc noqueue state UNKNOWN group default qlen 1000
    link/loopback 00:00:00:00:00:00 brd 00:00:00:00:00:00
    inet 127.0.0.1/8 scope host lo
       valid_lft forever preferred_lft forever
    inet6 ::1/128 scope host 
       valid_lft forever preferred_lft forever
2: ens33: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc pfifo_fast state UP group default qlen 1000
    link/ether 00:0c:29:6c:73:ee brd ff:ff:ff:ff:ff:ff
    inet 192.168.44.139/24 brd 192.168.44.255 scope global ens33
       valid_lft forever preferred_lft forever
    inet6 fe80::20c:29ff:fe6c:73ee/64 scope link 
       valid_lft forever preferred_lft forever
3: virbr0: <NO-CARRIER,BROADCAST,MULTICAST,UP> mtu 1500 qdisc noqueue state DOWN group default qlen 1000
    link/ether 52:54:00:a4:d3:9a brd ff:ff:ff:ff:ff:ff
    inet 192.168.122.1/24 brd 192.168.122.255 scope global virbr0
       valid_lft forever preferred_lft forever
4: virbr0-nic: <BROADCAST,MULTICAST> mtu 1500 qdisc pfifo_fast master virbr0 state DOWN group default qlen 1000
    link/ether 52:54:00:a4:d3:9a brd ff:ff:ff:ff:ff:ff
    
[root@localhost network-scripts]# cd /etc/sysconfig/network-scripts/
[root@localhost network-scripts]# vim ifcfg-ens33
TYPE="Ethernet"
PROXY_METHOD="none"
BROWSER_ONLY="no"
BOOTPROTO="static" # 修改为static
DEFROUTE="yes"
IPV4_FAILURE_FATAL="no"
IPV6INIT="yes"
IPV6_AUTOCONF="yes"
IPV6_DEFROUTE="yes"
IPV6_FAILURE_FATAL="no"
IPV6_ADDR_GEN_MODE="stable-privacy"	
NAME="ens33"
UUID="92633ab9-a8da-4976-84e4-73db715a026f"
# DEVICE="ens33"
ONBOOT="yes" # 修改为yes
ZONE=public
IPADDR=192.168.44.139  # 静态IP
NETMASK=255.255.255.0  # 子网掩码
GATEWAY=192.168.44.2	# NAT设置的网关
DNS1=114.114.114.114	# DNS设置
HWADDR=00:0c:29:6c:73:ee  # 根据ip addr查找的MAC地址进行设置
NM_CONTROLLED="no"

[root@localhost network-scripts]# cd ..
[root@localhost sysconfig]# vim network
DNS1=114.114.114.114	# DNS设置
```

## 出现的问题

### 服务无法启动

```shell
# 启动网络出现了问题
[root@localhost network-scripts]# systemctl start network
Job for network.service failed because the control process exited with error code. See "systemctl status network.service" and "journalctl -xe" for details.
# 解决
[root@localhost network-scripts]# systemctl stop NetworkManager
[root@localhost network-scripts]# systemctl disable NetworkManager
```

### xshell连接不上虚拟机

```shell
# 确认虚拟机的防火墙关闭
[root@localhost ~]# systemctl status firewalld.service 
● firewalld.service - firewalld - dynamic firewall daemon
   Loaded: loaded (/usr/lib/systemd/system/firewalld.service; disabled; vendor preset: enabled)
   Active: inactive (dead)
     Docs: man:firewalld(1)
# 主机与虚拟机互相ping,发现虚拟机可以ping通主机,但是主机无法ping通虚拟机.
```

**将虚拟机的IP等配置到VMnet8,解决问题**

