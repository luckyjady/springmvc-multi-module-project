package com.cnblogs.kmpp.redis.Impl;

import com.cnblogs.kmpp.domain.Member;
import com.cnblogs.kmpp.redis.MemberDao;
import com.cnblogs.kmpp.redis.RedisGeneratorDao;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Repository(value = "memberDao")
public class MemberDaoImpl extends RedisGeneratorDao<String, Member> implements MemberDao {
    /**
     * 添加对象
     */
    public boolean add(final Member member) {
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] key = serializer.serialize(member.getId());
                byte[] name = serializer.serialize(member.getNickname());
                return connection.setNX(key, name);
            }
        });
    }

    /**
     * 添加集合
     */
    public boolean add(final List<Member> list) {
        Assert.notEmpty(list);
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                RedisSerializer<String> serializer = getRedisSerializer();
                for (Member member : list) {
                    byte[] key = serializer.serialize(member.getId());
                    byte[] name = serializer.serialize(member.getNickname());
                    connection.setNX(key, name);
                }
                return true;
            }
        }, false, true);
    }

    /**
     * 删除对象 ,依赖key
     */
    public void delete(String key) {
        List<String> list = new ArrayList<String>();
        list.add(key);
        delete(list);
    }

    /**
     * 删除集合 ,依赖key集合
     */
    public void delete(List<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 修改对象
     */
    public boolean update(final Member member) {
        String key = member.getId();
        if (get(key) == null) {
            throw new NullPointerException("数据行不存在, key = " + key);
        }
        return redisTemplate.execute(new RedisCallback<Boolean>() {
            public Boolean doInRedis(RedisConnection connection)
                    throws DataAccessException {
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] key1 = serializer.serialize(member.getId());
                byte[] name = serializer.serialize(member.getNickname());
                connection.set(key1, name);
                return true;
            }
        });
    }

    /**
     * 根据key获取对象
     */
    public Member get(final String keyId) {
        return redisTemplate.execute(new RedisCallback<Member>() {
            public Member doInRedis(RedisConnection connection)
                    throws DataAccessException {
                RedisSerializer<String> serializer = getRedisSerializer();
                byte[] key = serializer.serialize(keyId);
                byte[] value = connection.get(key);
                if (value == null) {
                    return null;
                }
                String nickname = serializer.deserialize(value);
                return new Member(keyId, nickname);
            }
        });
    }
}
