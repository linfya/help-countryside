package com.eng.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eng.util.*;
import com.eng.domain.dto.PageDTO;
import com.eng.domain.dto.UserDTO;
import com.eng.domain.dto.UserInfoDTO;
import com.eng.domain.po.User;
import com.eng.domain.po.UserInfo;
import com.eng.domain.vo.FollowHumanVO;
import com.eng.domain.vo.MyInfoVO;
import com.eng.domain.vo.UserInfoVO;
import com.eng.mapper.UserInfoMapper;
import com.eng.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.*;

import static com.eng.util.RedisConstants.*;

@Slf4j
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    /** 系统通知长轮询的最长等待时间（毫秒） */
    private static final long NOTICE_WAIT_MILLIS = 10_000L;

    @Lazy
    @Autowired
    private UserServiceImpl userService;


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private TencentCosUtil tencentCosUtil;

    @Value("${plant.api.app-code}")
    private String plantApiAppCode;

    private String streamName = "stream.liked";




    /*
     * 查看个人详情
     * */
    @Override
    public Result userMainInfo(Long id) {
        UserInfo user = getById(id);
        return Result.ok(user);
    }

    /**
     * 系统通知：用 Redis Stream 做长轮询，最多阻塞 {@value #NOTICE_WAIT_MILLIS} 毫秒，
     * 超时返回空结果，避免请求线程被无限占用。
     */
    @Override
    public Result systemInform() {
        long deadline = System.currentTimeMillis() + NOTICE_WAIT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                    Consumer.from("g1", "c1"),
                    StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
                    StreamOffset.create(streamName, ReadOffset.lastConsumed())
            );
            if (list != null && !list.isEmpty()) {
                return Result.ok(list.get(0));
            }
        }
        return Result.ok();
    }

    @Override
    public Result updateUserIcon(MultipartFile file, String token) {
        Long userId = UserLocal.getUser().getUserId();
        int length = file.getOriginalFilename().length();
        String suffix = file.getOriginalFilename()
                .substring(file.getOriginalFilename().lastIndexOf(".") + 1,length);
        if (!"jpg,png".toUpperCase().contains(suffix.toUpperCase())){
            return Result.fail("仅支持jpg,png格式");
        }
        String upload = tencentCosUtil.upload(file,"UserIcon/" + userId + "/");
        userService.lambdaUpdate()
                .eq(User::getUserId,userId)
                .set(User::getIcon,upload)
                .update();
        stringRedisTemplate.opsForHash().put(LOGIN_USER_KEY + token,"icon",upload);
        return Result.ok();
    }

    @Override
    public Result getInfoMain() {
        Long userId = UserLocal.getUser().getUserId();
        UserInfo userInfo = query().eq("user_id", userId).one();
        MyInfoVO myInfoVO = BeanUtil.copyProperties(userInfo, MyInfoVO.class);
        User user = userService.getById(userId);
        myInfoVO.setUserName(user.getUserName());
        myInfoVO.setIcon(user.getIcon());
        return Result.ok(myInfoVO);
    }



    /*
    * 修改详情
    * */
    @Override
    public Result userMainInfoChange(UserInfoDTO userInfoDTO, String token) {
        UserDTO user = UserLocal.getUser();
        Long userId = user.getUserId();
        lambdaUpdate()
                .set(UserInfo::getGender,userInfoDTO.getGender())
                .set(UserInfo::getIntroduce,userInfoDTO.getIntroduce())
                .set(UserInfo::getBirthday,userInfoDTO.getBirthday())
                .eq(UserInfo::getUserId, userId)
                .update();
        userService.lambdaUpdate()
                .eq(User::getUserId,userId)
                .set(User::getUserName,userInfoDTO.getUserName())
                .update();
        stringRedisTemplate.opsForHash().put(LOGIN_USER_KEY + token,"userName",userInfoDTO.getUserName());
        return Result.ok("修改成功");
    }


    /*
    * 我的详情
    * */
    @Override
    public Result userMain() {
        UserDTO user = UserLocal.getUser();
        if (user == null){
            return Result.fail("没请求头捏");
        }
        UserInfo userInfo = query().eq("user_id", user.getUserId()).one();
        UserInfoVO userInfoVO = BeanUtil.copyProperties(userInfo, UserInfoVO.class);
        //UserInfoVO userInfoVO = new UserInfoVO("111", userInfo.getCity(), userInfo.getWorks(), userInfo.getFans(), userInfo.getFollowHuman());
        userInfoVO.setUserName(user.getUserName());
        userInfoVO.setIcon(user.getIcon());
        return Result.ok(userInfoVO);
    }

    /*
    * 关注
    * */
    @Override
    public Result followHuman(Long id) {
        Long userId = UserLocal.getUser().getUserId();
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(HUMAN_LIKED_KEY + id, userId.toString());
        if (BooleanUtil.isFalse(isMember)){
            boolean liked = update().setSql("fans = fans + 1").eq("user_id", id).update();
            boolean myLiked = update().setSql("follow_human = follow_human + 1").eq("user_id", userId).update();
            if (liked && myLiked){
                stringRedisTemplate.opsForSet().add(HUMAN_LIKED_KEY + id, userId.toString());
                stringRedisTemplate.opsForList().leftPush(USER_LIKED_HUMAN + userId.toString(),id.toString());
            }
            return Result.ok("关注成功");
        }else {
            boolean liked = update().setSql("fans = fans - 1").eq("user_id", id).update();
            boolean myLiked = update().setSql("follow_human = follow_human - 1").eq("user_id", userId).update();
            if (liked && myLiked){
                stringRedisTemplate.opsForSet().remove(HUMAN_LIKED_KEY + id, userId.toString());
                stringRedisTemplate.opsForList().remove(USER_LIKED_HUMAN + userId.toString(),0,id.toString());
            }
            return Result.fail("消除关注成功");

        }
    }


    /*
    * 我关注的用户
    * */
    @Override
    public Result myLikeHuman() {
        UserDTO user = UserLocal.getUser();
        Long userId = user.getUserId();
        List<String> range = stringRedisTemplate.opsForList().range(USER_LIKED_HUMAN + userId, 0, -1);
        Long size = stringRedisTemplate.opsForList().size(USER_LIKED_HUMAN + userId);
        if (range.size() == 0){
            return Result.ok("关注列表为空");
        }
        ArrayList<FollowHumanVO> followHuman = new ArrayList<>();
        for (String HumanId :
                range) {
            setHuman(followHuman,HumanId,userId,HUMAN_LIKED_KEY);
        }
        PageDTO<FollowHumanVO> followHumanVOPageDTO = new PageDTO<>(size, null, followHuman);
        return Result.ok(followHumanVOPageDTO);
    }


    /*
    * 我的粉丝
    * */
    @Override
    public Result myFans() {
        Long userId = UserLocal.getUser().getUserId();
        Set<String> members = stringRedisTemplate.opsForSet().members(HUMAN_LIKED_KEY + userId);
        Long size = stringRedisTemplate.opsForSet().size(HUMAN_LIKED_KEY + userId);
        if (members.size() == 0){
            return Result.ok("粉丝列表为空");
        }
        ArrayList<FollowHumanVO> followHuman = new ArrayList<>();
        for (String FansId :
                members) {
            setHuman(followHuman,FansId,userId,HUMAN_LIKED_KEY);
        }
        PageDTO<FollowHumanVO> followHumanVOPageDTO = new PageDTO<>(size, null, followHuman);
        return Result.ok(followHumanVOPageDTO);
    }



    /**
     * 扫描植物：先把图片传到 COS，再调用第三方识别接口。
     */
    @Override
    public Result identifyPlant(MultipartFile image) {
        String upload = tencentCosUtil.upload(image, "plant/");
        String host = "https://jmzwhhsb.market.alicloudapi.com";
        String path = "/zwsb/plant/recognition";
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE " + plantApiAppCode);
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<>();
        Map<String, String> bodys = new HashMap<>();
        bodys.put("base64", null);
        bodys.put("url", upload);
        bodys.put("baikeNum", "10000");
        try {
            HttpResponse response = HttpUtils.doPost(host, path, "POST", headers, querys, bodys);
            return Result.ok(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            log.error("植物识别接口调用失败，图片地址：{}", upload, e);
            return Result.fail("调用失败");
        }
    }

    public void setHuman(ArrayList<FollowHumanVO> list,String id,Long userId,String key){
        FollowHumanVO followHumanVO = new FollowHumanVO();
        long l = Long.parseLong(id);
        User human = userService.query().eq("user_id", l).one();
        UserInfo humanInfo = query().eq("user_id", l).one();
        followHumanVO.setId(l);
        followHumanVO.setIntroduce(humanInfo.getIntroduce());
        followHumanVO.setUserName(human.getUserName());
        followHumanVO.setIcon(human.getIcon());
        Boolean member = stringRedisTemplate.opsForSet().isMember(key + id, userId.toString());
        followHumanVO.setMyLiked(BooleanUtil.isTrue(member));
        list.add(followHumanVO);
    }


}
