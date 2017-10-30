package com.bxz.bolt;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bxz.enums.BusinessType;
import com.bxz.enums.StatisticType;
import com.bxz.utils.RedisUtils;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志筛选
 *
 * @auther baixiangzhu
 * @create 2017/10/26
 **/
public class LogAnalysisBolt extends BaseRichBolt{

    private static Logger log = LoggerFactory.getLogger(LogAnalysisBolt.class);

    Map<String,List<String>> relation = new HashMap<>();

    Pattern pattern = Pattern.compile("\\[(.*?)\\]");

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private static final String DATA_REGEX = "=";
    private static final String REDIS_SEPERATOR = ":";

    OutputCollector collector;

    @Override
    public void prepare(Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        collector = outputCollector;
        initRelation();

    }

    /**
     * 初始化对应关系
     */
    private void initRelation() {
        relation.put(BusinessType.SHOP_INDEX.name(), Arrays.asList(StatisticType.PV.name(),StatisticType.UV.name()));
        relation.put(BusinessType.MY_CASHBACK.name(), Arrays.asList(StatisticType.PV.name(),StatisticType.UV.name()));
        relation.put(BusinessType.EVALUATE_LIST.name(), Arrays.asList(StatisticType.PV.name(),StatisticType.UV.name()));
        relation.put(BusinessType.ADDITIONAL_EVALUATE_PAGE.name(), Arrays.asList(StatisticType.PV.name(),StatisticType.UV.name()));
        relation.put(BusinessType.EVALUATE_PAGE.name(), Arrays.asList(StatisticType.PV.name(),StatisticType.UV.name()));
        relation.put(BusinessType.REJECTION_ORDER.name(), Arrays.asList(StatisticType.COUNT.name()));
        relation.put(BusinessType.CANCEL_ORDER.name(), Arrays.asList(StatisticType.COUNT.name()));
        relation.put(BusinessType.TIMEOUT_CANCEL_ORDER.name(), Arrays.asList(StatisticType.COUNT.name()));
        relation.put(BusinessType.REFUNDED_ORDER.name(), Arrays.asList(StatisticType.COUNT.name()));
        relation.put(BusinessType.TOTAL_ORDER.name(), Arrays.asList(StatisticType.COUNT.name()));

    }

    @Override
    public void execute(Tuple tuple) {

        /*
          消费日志的格式：
            2017-10-30 03:23:59.843  INFO [bs-service-cashback,77883ba88c035c9e,77883ba88c035c9e,true] 1 --- [nio-9057-exec-1] c.g.o.b.s.c.logging.aspects.LogAspect    :
             [MY_CASHBACK],data={"pageSize":10,"userId":"17025795549","pageNum":1,"status":"EXPECT"}

             思路：正则匹配[]中的类型，如果跟枚举匹配，则把结果存入redis  【枚举类型可以放入配置中心，动态变更】
                   pv: MY_CASHBACK:PV:2017-10-30    redis incr
                   uv: MY_CASHBACK:PV:2017-10-30    redis set (用户去重)
                   count: MY_CASHBACK:COUNT:2017-10-30  redis incr
         */
        String msg = tuple.getString(0);

        log.info("get msg = [{}] from spout",msg);

        Matcher matcher = this.pattern.matcher(msg);

        String type = null;

        while (matcher.find()){
            type = matcher.group(1).trim();
            if (type != null){

                log.info("handle msg type = [{}]",type);

                if(BusinessType.isContains(type)){

                    handleLgo(type,msg);

                }

            }
        }

        collector.ack(tuple);
    }

    /**
     *   处理日志
     */
    private void handleLgo(String type,String msg) {

        List<String> statisticTypes = relation.get(type);

        for (String stype : statisticTypes){
            log.info("handle log stype=[{}]",stype);

            String redisKey = getRedisKey(type,stype);

            if(StatisticType.COUNT.name().equals(stype)
                    || StatisticType.PV.name().equals(stype)){

                RedisUtils.incr(redisKey);

            }else if( StatisticType.UV.name().equals(stype)){

                String userId = getUserIdFromMsg(msg);
                if(userId != null && userId.length() > 0){
                    RedisUtils.sadd(redisKey,userId);
                }
            }else{
                log.warn("not exist type.type=[{}],stype=[{}]",type,stype);
            }
        }

    }

    /**
     * 获取用户ID
     * @param msg
     * @return
     */
    private String getUserIdFromMsg(String msg) {

        String[] split = msg.split(DATA_REGEX);

        if(split.length > 1){

            String data = split[1];

            JSONObject jsonObject = JSON.parseObject(data);

            String userId = jsonObject.getString("userId");

            return userId;
        }

        return null;
    }

    /**
     * 拼接redis Key
     *
     * @param type
     * @param stype
     * @return
     */
    private String getRedisKey(String type,String stype) {

        String dataFormat = sdf.format(new Date());

        String key = type + REDIS_SEPERATOR + stype + REDIS_SEPERATOR + dataFormat;

        return key;

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {

    }
}
