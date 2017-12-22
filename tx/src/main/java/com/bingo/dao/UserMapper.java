package com.bingo.dao;

import com.bingo.domain.*;
import com.bingo.vo.AddInfo;
import com.bingo.vo.FriendList;
import com.bingo.vo.GroupList;
import com.bingo.vo.GroupMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper {
    /**
     * @description 退出群
     * @param groupMember
     */
    @Delete("delete from t_group_members where gid=#{gid} and uid=#{uid}")
    int leaveOutGroup(GroupMember groupMember );

    /**
     * @description 添加群成员
     */
    @Insert("insert into t_group_members(gid,uid) values(#{gid},#{uid})")
    int addGroupMember(GroupMember groupMember);

    /**
     * @description 删除好友
     * @param friendId 好友Id
     * @param uId 个人Id
     * @return Int
     */
    @Delete("delete from t_friend_group_friends where fgid in (select id from t_friend_group where uid in (#{friendId}, #{uId})) and uid in(#{friendId}, #{uId})")
    int removeFriend(@Param("friendId")Integer friendId, @Param("uId")Integer uId);

    /**
     * @description 更新用户头像
     * @param userId
     * @param avatar
     * @return
     */
    @Update("update t_user set avatar=#{avatar} where id=#{userId}")
    int updateAvatar(@Param("userId")Integer userId, @Param("avatar")String avatar);

    /**
     * @description 移动好友分组
     * @param groupId 新的分组id
     * @param uId 被移动的好友id
     * @param mId 我的id
     * @return
     */
    @Update("update t_friend_group_friends set fgid = #{groupId} where id =(select t.id from ((select id from t_friend_group_friends where fgid in (select id from t_friend_group where uid = #{mId}) and uid = #{uId}) t))")
    int changeGroup(@Param("groupId")Integer groupId, @Param("uId")Integer uId, @Param("mId")Integer mId);

    /**
     * @description 添加好友操作
     */
    @Insert("insert into t_friend_group_friends(fgid,uid) values(#{mgid},#{tid}),(#{tgid},#{mid})")
    int addFriend(AddFriends addFriends);

    /**
     * @description 统计未处理的消息
     * @param uid
     */
    @Select("<script> select count(*) from t_add_message where to_uid=#{uid} <if test='agree!=null'> and agree=#{agree} </if> </script>")
    Integer countUnHandMessage(@Param("uid")Integer uid, @Param("agree")Integer agree);

    /**
     * @description 查询添加好友、群组信息
     * @param uid
     * @return List[AddInfo]
     */
    @Select("select * from t_add_message where to_uid = #{uid} order by time desc")
    @Results({@Result(column="from_uid",property="from"),
            @Result(column="to_uid",property="uid"),
            @Result(column="agree",property="read"),
            @Result(column="group_id",property="from_group")
    })
    List<AddInfo> findAddInfo(@Param("uid")Integer uid);

    /**
     * @description 更新好友、群组信息请求
     * @param addMessage
     * @return
     */
    @Update("update t_add_message set agree = #{agree} where id = #{id}")
    int updateAddMessage(AddMessage addMessage);

    /**
     * @description 添加好友、群组信息请求
     * @param addMessage
     * @return
     */
    @Insert("insert into t_add_message(from_uid,to_uid,group_id,remark,agree,type,time) values (#{fromUid},#{toUid},#{groupId},#{remark},#{agree},#{Type},#{time}) ON DUPLICATE KEY UPDATE remark=#{remark},time=#{time},agree=#{agree};")
    int saveAddMessage(AddMessage addMessage);

    /**
     * @description 根据群名模糊统计
     * @param groupName
     * @return
     */
    @Select("<script> select count(*) from t_group where 1 = 1 <if test='groupName != null'> and group_name like '%${groupName}%'</if></script>")
    int countGroup(@Param("groupName")String  groupName);

    /**
     * @description 根据群名模糊查询群
     * @param groupName
     * @return
     */
    @Select("<script> select id,group_name,avatar,create_id from t_group where 1=1 <if test='groupName != null'> and group_name like '%${groupName}%'</if></script>")
    List<GroupList> findGroup(@Param("groupName")String groupName);

    /**
     * @description 根据群id查询群信息
     * @param gid
     * @return
     */
    @Select("select id,group_name,avatar,create_id from t_group where id = #{gid}")
    GroupList findGroupById(@Param("gid")Integer gid);

    /**
     * @description 根据用户名和性别统计用户
     * @param username
     * @param sex
     */
    @Select("<script> select count(*) from t_user where 1 = 1 <if test='username != null'> and username like '%${username}%'</if><if test='sex != null'> and sex=#{sex}</if></script>")
    int countUser(@Param("username") String username , @Param("sex")Integer sex);

    /**
     * @description 根据用户名和性别查询用户
     * @param username
     * @param sex
     */
    @Select("<script> select id,username,status,sign,avatar,email from t_user where 1=1 <if test='username != null'> and username like '%${username}%'</if><if test='sex != null'> and sex=#{sex}</if></script>")
    List<User> findUsers(@Param("username") String username, @Param("sex")Integer sex);

    /**
     * @description 统计查询消息
     * @param uid 消息所属用户
     * @param mid 来自哪个用户
     * @param Type 消息类型，可能来自friend或者group
     */
    @Select("<script> select count(*) from t_message where type = #{Type} and " +
            "<choose><when test='uid!=null and mid !=null'>(toid = #{uid} and mid = #{mid}) or (toid = #{mid} and mid = #{uid}) </when><when test='mid != null'> mid = #{mid} </when></choose> order by timestamp </script>")
    int countHistoryMessage(@Param("uid")Integer uid, @Param("mid")Integer mid, @Param("Type")String Type );

    /**
     * @description 查询消息
     * @param uid 消息所属用户
     * @param mid 来自哪个用户
     * @param Type 消息类型，可能来自friend或者group
     */
    @Results({@Result(property="id",column="mid")})
    @Select("<script> select toid,fromid,mid,content,type,to_char(timestamp,'YYYY-MM-DD HH:mm:ss') timestamp,status from t_message where type = #{Type} and " +
            "<choose><when test='uid!=null and mid !=null'>(toid = #{uid} and mid = #{mid}) or (toid = #{mid} and mid = #{uid}) </when><when test='mid != null'> mid = #{mid} </when></choose> order by timestamp </script>")
    List<Receive> findHistoryMessage(@Param("uid")Integer uid, @Param("mid")Integer mid, @Param("Type")String Type);

    /**
     * @description 查询消息
     * @param uid
     * @param status 历史消息还是离线消息 0代表离线 1表示已读
     */
    @Results({@Result(property="id",column="mid")})
    @Select("select toid,fromid,mid,content,type,to_char(timestamp,'YYYY-MM-DD HH:mm:ss') timestamp,status from t_message where toid = #{uid} and status = #{status}")
    List<Receive> findOffLineMessage(@Param("uid")Integer uid, @Param("status")Integer status);

    /**
     * @description 保存用户聊天记录
     * @param receive 聊天记录信息
     * @return Int
     */
    @Insert("insert into t_message(mid,toid,fromid,content,type,timestamp,status) values(#{id},#{toid},#{fromid},#{content},#{type},#{timestamp},#{status})")
    int saveMessage(Receive receive);

    /**
     * @description 更新签名
     */
    @Update("update t_user set sign = #{sign} where id = #{uid}")
    int updateSign(@Param("sign")String sign, @Param("uid")Integer uid);

    /**
     * @description 激活用户账号
     * @param activeCode
     * @return List[User]
     */
    @Update("update t_user set status = 'offline' where active = #{activeCode}")
    int activeUser(@Param("activeCode")String activeCode);

    /**
     * @description 根据群组ID查询群里用户的信息
     * @param gid
     * @return List[User]
     */
    @Select("select id,username,status,sign,avatar,email from t_user where id in(select uid from t_group_members where gid = #{gid})")
    List<User> findUserByGroupId(int gid);

    /**
     * @description 根据ID查询用户信息
     * @param id
     * @return User
     */
    @Select("select id,username,status,sign,avatar,email,sex,create_date from t_user where id = #{id}")
    User findUserById(int id);

    /**
     * @description 根据ID查询用户群组列表,不管是自己创建的还是别人创建的
     * @param uid 用户ID
     * @return List[Group]
     */
    @Results({@Result(property="createId",column="create_id")})
    @Select("select id,group_name,avatar,create_id from t_group where id in(select distinct gid from t_group_members where uid = #{uid})")
    List<GroupList> findGroupsById(int uid);

    /**
     * @description 根据ID查询用户好友分组列表
     * @param uid 用户ID
     * @return List[FriendList]
     */
    @Select("select id, group_name from t_friend_group where uid = #{uid}")
    List<FriendList> findFriendGroupsById(int uid);

    /**
     * @description 根据好友列表ID查询用户信息
     * @param fgid
     * @return List[User]
     */
    @Select("select id,username,avatar,sign,status,email,sex from t_user where id in(select uid from t_friend_group_friends where fgid = #{fgid})")
    List<User> findUsersByFriendGroupIds(int fgid);

    /**
     * @description 保存用户信息
     * @param user
     * @return Int
     */
    @Insert("insert into t_user(username,password,email,create_date,active) values(#{username},#{password},#{email},#{createDate},#{active})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int saveUser(User user);

    /**
     * @description
     * @param email
     * @return User
     */
    @Select("select id,username,email,avatar,sex,sign,password,status,active from t_user where email = #{email}")
    User matchUser(String email);

    /**
     * @description 创建好友分组列表
     */
    @Insert("insert into t_friend_group(group_name,uid) values(#{groupName},#{uid})")
    int createFriendGroup(FriendGroup friendGroup);
}
