package com.eng.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.eng.util.Result;
import com.eng.util.TencentCosUtil;
import com.eng.util.UserLocal;
import com.eng.domain.dto.ItemDTO;
import com.eng.domain.dto.OrderInfoDTO;
import com.eng.domain.dto.PageDTO;
import com.eng.domain.po.*;
import com.eng.domain.vo.ItemVO;
import com.eng.mapper.ItemMapper;
import com.eng.service.ItemCommentsService;
import com.eng.service.ItemService;
import com.eng.service.UserInfoService;
import com.eng.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.eng.domain.PageQuery.DEFAULT_PAGE_NUM;

@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {


    @Autowired
    private ItemCommentsService itemCommentsService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private TencentCosUtil tencentCosUtil;

    @Override
    public Result queryItem(Integer curran) {
        Page<Item> item = query()
                .orderByDesc("update_time")
                .eq("status",1)
                .page(new Page<>(curran, DEFAULT_PAGE_NUM));
        List<Item> records = item.getRecords();
        PageDTO<Item> itemPageDTO = new PageDTO<>(item.getTotal(), item.getPages(), records);
        return Result.ok(itemPageDTO);
    }

    @Override
    public Result queryItemById(Long id) {
        Item byId = getById(id);
        if (byId == null){
            return Result.fail("商品不存在");
        }
        //LambdaQueryChainWrapper<ItemComments> last = itemCommentsService.lambdaQuery().eq(ItemComments::getItemId, id).last("limit 1");
        List<ItemComments> itemId = itemCommentsService.query().eq("item_id", id).last("limit 3").list();
        if (!itemId.isEmpty()){
            for (ItemComments i :
                    itemId) {
                User user = userService.getById(i.getUserId());
                i.setUserName(user.getUserName());
                i.setIcon(user.getIcon());
            }
            UserInfo userId = userInfoService.query().eq("user_id", byId.getCreater()).one();
            byId.setCity(userId.getCity());
            byId.setFirstComment(itemId);
            return Result.ok(byId);
        }
        return Result.ok(byId);

    }

    @Override
    public Result uploadItem() {
        Long userId = UserLocal.getUser().getUserId();
        Item one = query().eq("creater", userId).eq("status", 0).one();
        if (one == null){
            Item item = new Item(null, "", 0, 0, "", null,null,null, null, null, 0, userId,"item");
            save(item);
            return Result.ok();
        }
        ItemVO itemVO = BeanUtil.copyProperties(one, ItemVO.class);
        return Result.ok(itemVO);
    }

    @Override
    public Result updateItem(ItemDTO itemDTO) {
        Long userId = UserLocal.getUser().getUserId();
        boolean update = lambdaUpdate()
                .set(Item::getName, itemDTO.getName())
                .set(Item::getPrice, itemDTO.getPrice())
                .set(Item::getType, itemDTO.getType())
                .set(Item::getStock, itemDTO.getStock())
                .eq(Item::getStatus, 0)
                .eq(Item::getCreater, userId)
                .update();
        if (!update){
            return Result.fail("保存失败");
        }
        return Result.ok("保存成功");
    }



    @Override
    public Result addItem(ItemDTO itemDTO) {
        Long userId = UserLocal.getUser().getUserId();
        Item one = lambdaQuery()
                .eq(Item::getCreater, userId)
                .eq(Item::getStatus, 0)
                .one();
        if (one.getImage() == null){
            return Result.fail("图片不能为空");
        }
        String firstImages = firstImages(one);
        lambdaUpdate()
                .set(Item::getName,itemDTO.getName())
                .set(Item::getPrice, itemDTO.getPrice())
                .set(Item::getType,itemDTO.getType())
                .set(Item::getFirstImage,firstImages)
                .set(Item::getStock,itemDTO.getStock())
                .set(Item::getCreateTime,DateUtil.now())
                .set(Item::getUpdateTime,DateUtil.now())
                .set(Item::getStatus,1)
                .eq(Item::getStatus,0)
                .eq(Item::getCreater,userId)
                .update();
        boolean update = userInfoService.update().setSql("items = items + 1").eq("user_id", userId).update();
        if (!update){
            return Result.fail("上传失败");
        }
        return Result.ok("上传成功");

    }


    @Override
    public Result uploadDescription(MultipartFile file) {
        ArrayList<String> images = new ArrayList<>();
        Long userId = UserLocal.getUser().getUserId();
        Item one = lambdaQuery().eq(Item::getCreater, userId).eq(Item::getStatus, 0).one();
        if (one.getDescription() != null){
            List<String> list = JSONUtil.parseArray(one.getDescription()).toList(String.class);
            images.addAll(list);
        }
        String upload = tencentCosUtil.upload(file,"DescriptionFile/" + userId + "/");
        images.add(upload);
        String jsonStr = JSONUtil.toJsonStr(images);
        lambdaUpdate()
                .set(Item::getDescription,jsonStr)
                .eq(Item::getCreater,userId)
                .eq(Item::getStatus,0)
                .update();
        return Result.ok(upload);
    }



    @Override
    public Result uploadFile(MultipartFile file) {
        ArrayList<String> images = new ArrayList<>();
        Long userId = UserLocal.getUser().getUserId();
        Item one = lambdaQuery().eq(Item::getCreater, userId).eq(Item::getStatus, 0).one();
        if (one.getImage() != null){
            List<String> list = JSONUtil.parseArray(one.getImage()).toList(String.class);
            images.addAll(list);
        }
        String upload = tencentCosUtil.upload(file,"ItemFile/" + userId + "/");
        images.add(upload);
        String jsonStr = JSONUtil.toJsonStr(images);
        lambdaUpdate()
                .set(Item::getImage,jsonStr)
                .eq(Item::getCreater,userId)
                .eq(Item::getStatus,0)
                .update();
        return Result.ok(upload);
    }



    @Override
    public Result bacthItem(List<OrderInfoDTO> batchItemDTOS) {
        for (OrderInfoDTO b :
             batchItemDTOS) {
            try {
                baseMapper.deleteStock(b.getId(),b.getNum());
            } catch (Exception e) {
                log.error("更新库存异常", e);
            }
        }
        return Result.ok();
    }


    @Override
    public Result deleteImage(String url) {
        Long userId = UserLocal.getUser().getUserId();
        Item item = lambdaQuery()
                .eq(Item::getCreater, userId)
                .eq(Item::getStatus, 0)
                .one();
        List<String> list = JSONUtil.parseArray(item.getImage()).toList(String.class);
        boolean remove = list.remove(url);
        if (!remove){
            return Result.fail("失败");
        }
        String jsonStr = JSONUtil.toJsonStr(list);
        lambdaUpdate()
                .set(Item::getImage, jsonStr)
                .eq(Item::getCreater, userId)
                .eq(Item::getStatus, 0)
                .update();
        return Result.ok();
    }

    @Override
    public Result deleteDescription(String url) {
        Long userId = UserLocal.getUser().getUserId();
        Item item = lambdaQuery()
                .eq(Item::getCreater, userId)
                .eq(Item::getStatus, 0)
                .one();
        List<String> list = JSONUtil.parseArray(item.getDescription()).toList(String.class);
        boolean remove = list.remove(url);
        if (!remove){
            return Result.fail("失败");
        }
        String jsonStr = JSONUtil.toJsonStr(list);
        lambdaUpdate()
                .set(Item::getDescription, jsonStr)
                .eq(Item::getCreater, userId)
                .eq(Item::getStatus, 0)
                .update();
        return Result.ok();
    }

    @Override
    public Result getMyItem() {
        Long userId = UserLocal.getUser().getUserId();
        List<Item> items = query()
                .orderByDesc("create_time")
                .eq("creater", userId)
                .eq("status", 1)
                .list();
        // 获取当前页数据
        return Result.ok(items);
    }

    @Override
    public Result updateMyItem(Long itemId, ItemDTO itemDTO) {
        Item one = lambdaQuery()
                .eq(Item::getId, itemId)
                .one();
        if (one.getImage() == null){
            return Result.fail("图片不能为空");
        }
        String firstImages = firstImages(one);
        boolean update = lambdaUpdate()
                .set(Item::getName, itemDTO.getName())
                .set(Item::getPrice, itemDTO.getPrice())
                .set(Item::getType, itemDTO.getType())
                .set(Item::getFirstImage, firstImages)
                .set(Item::getStock, itemDTO.getStock())
                .set(Item::getUpdateTime, DateUtil.now())
                .eq(Item::getId, itemId)
                .update();
        if (!update){
            return Result.fail("修改失败");
        }
        return Result.ok("修改成功");
    }

    @Override
    public Result uploadMyItemFile(MultipartFile file, Long itemId) {
        ArrayList<String> images = new ArrayList<>();
        Long userId = UserLocal.getUser().getUserId();
        Item one = lambdaQuery().eq(Item::getId,itemId).one();
        if (one.getImage() != null){
            List<String> list = JSONUtil.parseArray(one.getImage()).toList(String.class);
            images.addAll(list);
        }
        String upload = tencentCosUtil.upload(file,"ItemFile/" + userId + "/");
        images.add(upload);
        String jsonStr = JSONUtil.toJsonStr(images);
        lambdaUpdate()
                .set(Item::getImage,jsonStr)
                .eq(Item::getId,itemId)
                .update();
        return Result.ok(upload);
    }

    @Override
    public Result uploadMyItemDescription(MultipartFile file, Long itemId) {
        ArrayList<String> images = new ArrayList<>();
        Long userId = UserLocal.getUser().getUserId();
        Item one = lambdaQuery().eq(Item::getId,itemId).one();
        if (one.getDescription() != null){
            List<String> list = JSONUtil.parseArray(one.getDescription()).toList(String.class);
            images.addAll(list);
        }
        String upload = tencentCosUtil.upload(file,"DescriptionFile/" + userId + "/");
        images.add(upload);
        String jsonStr = JSONUtil.toJsonStr(images);
        lambdaUpdate()
                .set(Item::getDescription,jsonStr)
                .eq(Item::getId,itemId)
                .update();
        return Result.ok(upload);
    }

    @Override
    public Result deleteMyItemImage(String url, Long itemId) {
        Item item = lambdaQuery()
                .eq(Item::getId,itemId)
                .one();
        List<String> list = JSONUtil.parseArray(item.getImage()).toList(String.class);
        boolean remove = list.remove(url);
        if (!remove){
            return Result.fail("失败");
        }
        String jsonStr = JSONUtil.toJsonStr(list);
        lambdaUpdate()
                .set(Item::getImage, jsonStr)
                .eq(Item::getId,itemId)
                .update();
        return Result.ok();
    }

    @Override
    public Result deleteMyItemDescription(String url, Long itemId) {
        Item item = lambdaQuery()
                .eq(Item::getId,itemId)
                .one();
        List<String> list = JSONUtil.parseArray(item.getDescription()).toList(String.class);
        boolean remove = list.remove(url);
        if (!remove){
            return Result.fail("失败");
        }
        String jsonStr = JSONUtil.toJsonStr(list);
        lambdaUpdate()
                .set(Item::getDescription, jsonStr)
                .eq(Item::getId,itemId)
                .update();
        return Result.ok();
    }

    public void addSold(Long ItemId){
        baseMapper.addSold(ItemId);
    }


    private String firstImages(Item item){
        List<String> list = JSONUtil.parseArray(item.getImage()).toList(String.class);
        String s = list.get(0);
        return s;
    }


}
