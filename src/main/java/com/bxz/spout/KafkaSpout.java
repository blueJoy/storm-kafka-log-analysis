package com.bxz.spout;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * kafka消费
 *      老版本的消费方式：0.9x之前的
 *
 * @auther baixiangzhu
 * @create 2017/10/26
 **/
public class KafkaSpout extends BaseRichSpout {

    private static Logger log = LoggerFactory.getLogger(KafkaSpout.class);
   
    SpoutOutputCollector collector;
    ConsumerConnector consumer;
    String zkList;
    String topic;
    String groupId;
    int numThreads;
    
    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        
        collector = spoutOutputCollector;
        zkList = map.get("zkList").toString();
        topic = map.get("toptic").toString();
        groupId = map.get("groupId").toString();
        numThreads = Integer.parseInt(map.get("numThreads").toString());
        initKafka();
    }

    private void initKafka() {

        Properties props = new Properties();
        props.put("zookeeper.connect", zkList);
        props.put("group.id", groupId);
        props.put("zookeeper.session.timeout.ms", "4000");
        props.put("zookeeper.connection.timeout.ms", "10000");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("rebalance.max.retries", "10");
        props.put("rebalance.backoff.ms", "2000");

        ConsumerConfig consumerConfig = new ConsumerConfig(props);

        consumer = Consumer.createJavaConsumerConnector(consumerConfig);
    }

    @Override
    public void nextTuple() {

        Utils.sleep(100L);

        List<KafkaStream<byte[], byte[]>> streams = getStreamFromKafka();

        for (KafkaStream stream :streams){

            ConsumerIterator<byte[], byte[]> it = stream.iterator();

            while (it.hasNext()){
                String msg = new String(it.next().message());
                log.info("consumer kafka message = [{}]",msg);

                collector.emit(new Values(msg));
            }
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

        outputFieldsDeclarer.declare(new Fields("msg"));
    }


    public List<KafkaStream<byte[], byte[]>> getStreamFromKafka() {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(numThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer
                .createMessageStreams(topicCountMap);

        return consumerMap.get(topic);
    }
}
