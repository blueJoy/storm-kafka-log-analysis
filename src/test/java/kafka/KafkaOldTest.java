package kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @auther baixiangzhu
 * @create 2017/10/26
 **/
public class KafkaOldTest {

    private final ConsumerConnector consumer;
    private ExecutorService executor;

    private final String topic;

    public KafkaOldTest(String a_zookeeper, String a_groupId, String a_topic) {
        consumer = Consumer.createJavaConsumerConnector(createConsumerConfig(a_zookeeper,a_groupId));
        this.topic = a_topic;
    }

    private static ConsumerConfig createConsumerConfig(String a_zookeeper,
                                                       String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "largest");
        props.put("consumer.timeout.ms", "100000");

        return new ConsumerConfig(props);
    }

    public void run(int numThreads) {
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(numThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer
                .createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        // now launch all the threads
        executor = Executors.newFixedThreadPool(numThreads);

        // now create an object to consume the messages
        //
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new ConsumerMsgTask(stream, threadNumber));
            threadNumber++;
        }
    }


    public static void main(String[] arg) {

       /* Properties props = new Properties();
        props.put("bootstrap.servers", "10.112.178.16:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe("log_data_statistics");

        while (true) {
            Map<String, ConsumerRecords<String, String>> records = consumer.poll(100);
            for (Map.Entry<String, ConsumerRecords<String, String>> record : records.entrySet()) {
                System.out.printf("key = %s %s\n", record.getKey());
                ConsumerRecords<String, String> value = record.getValue();
                System.out.printf("consumer topic = %s",value.topic());
            }
        }
*/

        String[] args = { "10.112.178.18:2181,10.112.178.19:2181,10.112.178.16:2181", "group-3", "log_data_statistics", "12" };
        String zooKeeper = args[0];
        String groupId = args[1];
        String topic = args[2];
        int threads = Integer.parseInt(args[3]);

        KafkaOldTest demo = new KafkaOldTest(zooKeeper, groupId, topic);

        demo.run(threads);

        try {
            Thread.sleep(10000);
        } catch (InterruptedException ie) {

        }


    }

}

class ConsumerMsgTask implements Runnable {
    private KafkaStream m_stream;
    private int m_threadNumber;

    public ConsumerMsgTask(KafkaStream stream, int threadNumber) {
        m_threadNumber = threadNumber;
        m_stream = stream;
    }

    public void run() {
        ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
        while (it.hasNext())
            System.out.println("Thread " + m_threadNumber + ": "
                    + new String(it.next().message()));
        System.out.println("Shutting down Thread: " + m_threadNumber);
    }
}
