#### 主要特性
-------------
- 采用server/client模式实现多台linux服务器之间配置文件实时同步
- 调用rsync来实现可靠文件传输
- 依赖zookeeper实现文件更新即时通知
- 依赖java环境，以java进程方式启动，不依赖web容器
- 所有同步记录有日志可查，历史文件有备份，可以方便找回


#### 环境要求
-------------
- jdk 6及以上
- rsync，其中主服务器的 rsync 以 deamon 形式启动
- zookeeper服务器


#### 编译方式
-------------
 - 取最新代码，然后在根目录执行： 
 >mvn clean && mvn dependency:copy-dependencies && mvn install
 - 或者直接执行根目录下面的 build.bat 或者 build.sh
 - 编译完成之后在 target 目录下面会生成 conf-sync.zip
 
#### 部署方式
-------------
 将conf-sync.zip上传到服务器，并解压到conf-sync，例如: /usr/local/conf-sync
##### server 模式:
###### /etc/rsyncd.conf 配置:

```
[module1]
comment = module1 config file
path = /data/app/module1/
hosts allow = 10.20.164.0/24
hosts deny = *
read only = yes
 
[module2]
comment = module2 config file
path = /data/app/module2/
hosts allow = 10.20.164.0/24
hosts deny = *
read only = yes
```

	其中, module1 和 module2 表示要监控并同步的节点，conf-sync server 启动后会根据这个配置文件来定位每个节点的文件根目录，并监控其中所有文件及目录变化，一旦有更新会通知zookeeper。
		
	conf-sync client 启动后会从zookeeper获得server的IP，以及所有节点及节点的根目录，并根据启动参数来选择本地要同步哪几个节点的文件到本地。
		
	由于conf-sync最终是通过client发起rsync命令从server同步文件的，所以需要 server 先以 deamon 形式启动  rsync 服务。
确认 rsync 服务启动成功后，进入 /usr/local/conf-sync 目录,执行下面命令以server模式启动conf-sync服务：
 > sudo sh server.sh  -z 10.25.65.80:2181,10.25.65.81:2181 -c /etc/rsyncd.conf -p module1,module2
 
##### client 模式:
 		必须先安装 rsync（安装即可，无需启动后台服务）
 进入 /usr/local/conf-sync 目录,执行下面命令以client模式启动conf-sync服务：
 > sudo sh client.sh  -z 10.25.65.80:2181,10.25.65.81:2181  -p module2
 
##### 停止服务:
 进入 /usr/local/conf-sync 目录,执行:
 > sudo sh stop.sh
 
#### 参数说明
-------------
```
-z 10.25.65.80:2181,10.25.65.81:2181
//zookeeper服务器的地址

-c /etc/rsyncd.conf
//server模式必填， 以 deamon 形式启动的rsync服务的配置文件

-p module1,module2
//在server 模式下，表示要监控的rsync节点（不一定要监控rsync的所有节点），
//在client 模式下，表示要同步的的rsync节点（不一定要同步server端的所有节点），
```


### 相关问题
-------------
在使用中如有问题可以邮件联系
> ezhangliang@qq.com
