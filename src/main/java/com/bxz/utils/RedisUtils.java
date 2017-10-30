package com.bxz.utils;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashSet;
import java.util.Set;

/**
 * @auther baixiangzhu
 * @create 2017/10/23
 **/
public class RedisUtils {

/*    private static final String HOST ="192.168.16.134";
    private static final Integer PORT = 6379;*/


    private static JedisCluster jedis;

    private static Set<HostAndPort> hosts = new HashSet<>();

    private static final Integer PORT = 7014;


    static {

        hosts.add(new HostAndPort("meixin-red-one.dev.cloud.db",PORT));
        hosts.add(new HostAndPort("meixin-red-two.dev.cloud.db",PORT));
        hosts.add(new HostAndPort("meixin-red-three.dev.cloud.db",PORT));

        jedis = new JedisCluster(hosts);
    }

    public static String set(String key,String value){

        String result = null;

        try{
           result =  jedis.set(key, value);

        }catch (Exception e){
            e.printStackTrace();
        }

        return result;

    }

    public static String get(String key){

        String result = null;

        try{
            result =  jedis.get(key);

        }catch (Exception e){
            e.printStackTrace();
        }

        return result;

    }

    public static Long sadd(String key,String ... values){

        Long sadd = null;

        try{

            sadd = jedis.sadd(key, values);

        }catch (Exception e){

        }

        return sadd;

    }

    public static void incr(String key){

        try{
            jedis.incr(key);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
