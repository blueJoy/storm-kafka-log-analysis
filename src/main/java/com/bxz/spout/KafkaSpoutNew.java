package com.bxz.spout;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

/**
 * kafka client 新的消费方式.代码简便
 *      kafka-client 0.10.x 之后的
 *      ps:配置已写死，暂未启用
 *
 * @auther baixiangzhu
 * @create 2017/10/30
 **/
public class KafkaSpoutNew extends BaseRichSpout{

    private static Logger log = LoggerFactory.getLogger(KafkaSpoutNew.class);

    KafkaConsumer consumer;
    SpoutOutputCollector collector;

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {

        collector = spoutOutputCollector;

        consumer =  new KafkaConsumer<>(getConfProp());

        //订阅消费topic
        consumer.subscribe(Arrays.asList("log_data_statistics"));

    }

    private Properties getConfProp() {

        Properties props = new Properties();
        props.put("bootstrap.servers", ",10.112.178.16:9092,10.112.178.18:9092,10.112.178.19:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        return props;
    }

    @Override
    public void nextTuple() {

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records){

                System.out.printf("offset = %d, key = %s, value = %s\n", record.offset(), record.key(), record.value());

                //发送值
                collector.emit(new Values(record.value()));

            }
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

        outputFieldsDeclarer.declare(new Fields("msg"));

    }
}
