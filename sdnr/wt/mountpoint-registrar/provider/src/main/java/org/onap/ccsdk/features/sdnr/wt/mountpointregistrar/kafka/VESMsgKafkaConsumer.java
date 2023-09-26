package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.errors.InvalidGroupIdException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class that provides a KafkaConsumer to communicate with a kafka cluster
 */
public class VESMsgKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(VESMsgKafkaConsumer.class);
    final KafkaConsumer<String, String> consumer;
    private final int pollTimeout;
    private String topicName;
    private static final String DESERIALIZER_CLASS = "org.apache.kafka.common.serialization.StringDeserializer";

    /**
     *
     * @param consumerProperties
     * @param configuration The config provided to the client
     */
    public VESMsgKafkaConsumer(Properties strimziKafkaProperties, Properties consumerProperties) {
        Properties props = new Properties();
        props.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, strimziKafkaProperties.getProperty("bootstrapServers"));
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, strimziKafkaProperties.getProperty("securityProtocol"));
        props.put(SaslConfigs.SASL_MECHANISM, strimziKafkaProperties.getProperty("saslMechanism"));
        props.put(SaslConfigs.SASL_JAAS_CONFIG, strimziKafkaProperties.getProperty("saslJaasConfig"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                consumerProperties.getProperty("consumerGroup") + "-" + consumerProperties.getProperty("topic"));
        props.put(ConsumerConfig.CLIENT_ID_CONFIG,
                consumerProperties.getProperty("topic") + "-" + consumerProperties.getProperty("consumerID"));
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, DESERIALIZER_CLASS);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DESERIALIZER_CLASS);
        consumer = new KafkaConsumer<>(props);
        pollTimeout = Integer.parseInt(consumerProperties.getProperty("timeout"));
    }

    /**
     *
     * @param topic The kafka topic to subscribe to
     */
    public void subscribe(String topic) {
        try {
            consumer.subscribe(Collections.singleton(topic));
            this.topicName = topic;
        } catch (InvalidGroupIdException e) {
            log.error("Invalid Group {}", e.getMessage());
        }
    }

    /**
     *
     * @return The list of records returned from the poll
     */
    public List<String> poll() {
        List<String> msgs = new ArrayList<>();
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(pollTimeout));
        for (ConsumerRecord<String, String> rec : records) {
            msgs.add(rec.value());
        }
        return msgs;
    }

    public String getTopicName() {
        return topicName;
    }
}
