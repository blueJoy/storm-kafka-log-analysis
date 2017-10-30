这是个简单的日志收集分析的用例：

   ####准备:
       
       jdk  1.8 
       zookeeper  
       flume  
       kafka 
       storm 
       redis 
       
   ####流程
   
    LOG --> Flume --> Kafka --> Strom --> redis
    
    流程：使用flume的agent监听应用的log文件，发送到Kafka中。Strom的Spout去
    Kafka中拉取消息，在Bolt中清洗数据，执行逻辑，把结果存入redis中。
    
   ####环境安装
   1. Zookeeper 
        - 官网下载，解压 【集群安装看官方文档，填写必要配置】
        - bin/ZkServer.sh  启动
        - bin/zkCli.sh   进入客户端 ls/get等等等命令 
   2. Kafka
        - 官网下载，解压 【集群安装看官方文档，填写必要配置】
        - 启动：bin/kafka-server-start.sh config/server.properties
        - 创建topic：bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
        - 查看topic：bin/kafka-topics.sh --list --zookeeper localhost:2181
        - 发送消息：bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
        - 消费消息：bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
        - 查看消费位置： bin/kafka-consumer-offset-checker --zookeeper 10.112.178.16:2181 --group group-1  --topic test
   3. Storm
        - 官网下载，解压 【集群安装看官方文档，填写必要配置】
        - bin/storm nimbus
        - bin/storm supervisor
        - bin/storm ui  【UI默认端口8080】
        - 发布拓扑：bin/storm jar testApp/bi-storm-log-analysis-1.0-SNAPSHOT.jar com.gome.bi.strom.log.analysis.StormKafkaTopo name
   4. Flume
      - 在需要监听log文件的服务器，下载并解压
      - 启动命令 bin/flume-ng agent --conf conf --conf-file kafka.conf --name agent -Dflume.root.logger=INFO,console|LOGFILE
        
        详细配置：
        ```text
            agent.sources = s1
            agent.channels = c1
            agent.sinks = k1
            
            #shell 监听文件
            agent.sources.s1.type=exec                                                                                                          
            agent.sources.s1.command=tail -f /gomeo2o/logs/bs-service-cashback/bs-service-cashback.log                                                                            
            agent.sources.s1.channels=c1
            
            
            agent.channels.c1.type=memory
            agent.channels.c1.capacity=10000
            agent.channels.c1.transactionCapacity=100
            
            #设置Kafka接收器                                                                                                                    
            agent.sinks.k1.type= org.apache.flume.sink.kafka.KafkaSink
            #设置Kafka的broker地址和端口号                                                                                                      
            agent.sinks.k1.brokerList=10.112.178.16:9092
            #设置Kafka的Topic                                                                                                                   
            agent.sinks.k1.topic=log_data_statistics
            #设置序列化方式                                                                                                                     
            agent.sinks.k1.serializer.class=kafka.serializer.StringEncoder
            
            agent.sinks.k1.channel=c1

        ```
        
   ####常见问题：
   需要向集群中所有storm的lib目录下添加拓扑业务依赖jar包：
    比如：Kafka，kafka-client,zookeeper,zkclient,guava,scala,curator-framework,curator-client,curator-recipes,redis,fastjson等
        