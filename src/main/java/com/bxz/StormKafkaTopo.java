package com.bxz;

import com.bxz.bolt.LogAnalysisBolt;
import com.bxz.spout.KafkaSpout;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.topology.TopologyBuilder;

/**
 * strom-kafka
 *
 * @auther baixiangzhu
 * @create 2017/10/21
 **/
public class StormKafkaTopo {

    public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("kafka-spout",new KafkaSpout(),3);
        builder.setBolt("log-split",new LogAnalysisBolt(),3).shuffleGrouping("kafka-spout");

        //拓扑名称
        String name = "log-statistics";

        Config conf = new Config();
        conf.setDebug(true);
        conf.put("zkList", "10.112.178.18:2181,10.112.178.19:2181,10.112.178.16:2181");
        conf.put("groupId", "group-1");
        conf.put("toptic", "log_data_statistics");
        conf.put("numThreads",1);

        if(args != null && args.length > 0){

            //集群模式
            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(name, conf, builder.createTopology());

        }else{

            //本地模式
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology(name,conf,builder.createTopology());

            Thread.sleep(1000000L);

            cluster.shutdown();

        }
    }

}
